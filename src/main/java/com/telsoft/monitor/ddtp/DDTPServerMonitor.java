package com.telsoft.monitor.ddtp;

import smartlib.dictionary.DictionaryNode;
import com.telsoft.monitor.ddtp.packet.SocketTransmitter;
import com.telsoft.monitor.ddtp.packet.smart.SmartPacket;
import com.telsoft.monitor.manager.ManagerModel;
import com.telsoft.monitor.manager.MonitorObject;
import com.telsoft.monitor.manager.util.PasswordLoader;
import com.telsoft.monitor.util.*;
import smartlib.swing.BuildHelper;
import smartlib.transport.Packet;
import smartlib.transport.monitor.ddtp.packet.hessian.HessianPacket;
import smartlib.transport.monitor.ddtp.packet.v10.DDTPv10;
import smartlib.transport.monitor.ddtp.packet.v20.DDTPv20;
import smartlib.util.StringUtil;
import smartlib.util.crypto.EncryptionService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Title: Thread Monitor</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class DDTPServerMonitor extends MonitorObject {
    public static final int STOP_SERVER_ACTION = JXMenuItem.START_USER_ACTION + 1;
    public static final int CHANGE_PASSWORD_ACTION = JXMenuItem.START_USER_ACTION + 2;
    public static final int MANAGER_THREADS_ACTION = JXMenuItem.START_USER_ACTION + 3;
    public static final int UPDATE_SESSION_ACTION = JXMenuItem.START_USER_ACTION + 4;
    public static final int AFTER_DISCONNECT_ACTION = JXMenuItem.START_USER_ACTION + 5;
    public static final int AFTER_LOGIN_ACTION = JXMenuItem.START_USER_ACTION + 6;
    public static final int ORDER_BYNAME_ACTION = JXMenuItem.START_USER_ACTION + 7;
    public static final int ORDER_BYID_ACTION = JXMenuItem.START_USER_ACTION + 8;
    public static final int NOTIFY_ACTION = JXMenuItem.START_USER_ACTION + 9;
    public static final int UPDATE_STATE_ACTION = JXMenuItem.START_USER_ACTION + 10;
    public static final int RETRY_ACTION = JXMenuItem.START_USER_ACTION + 11;
    public static final int MANAGER_THREADS2_ACTION = JXMenuItem.START_USER_ACTION + 12;
    public static final int PROTOCOL_VERSION1 = 1;
    public static final int PROTOCOL_VERSION2 = 2;
    public static final int PROTOCOL_HESSIAN = 3;
    public static final int PROTOCOL_SMART = 4;
    protected SocketTransmitter channel = null;
    private String strIP;
    private int iPort;
    private String strSessionName;
    private String strUser;
    private String strPassword = "";
    private boolean bSavePassword = false;
    private String strEncryptMethod = "";
    private int iProtocolVersion = PROTOCOL_HESSIAN;
    private String strOrder = "";
    private boolean mbSystemInfor = false;
    private Vector mvtThread = new Vector();
    private Vector mvtSession = new Vector();
    private Vector mvtNotifications = new Vector();
    private String mstrThreadAppName = null;
    private String mstrThreadAppVersion = null;
    private String mstrAppName = null;
    private String mstrAppVersion = null;
    private String mstrExtensionClass = null;
    private String mstrExtensionForm = null;
    private Object[] moExtensionObject = null;
    private String mstrExtensionBean;

    private StringListHolder txtAction = new StringListHolder(this);
    private boolean bConnectionFailured = true;

    private String[] mstrStartCommand;
    private String[] mstrStopCommand;
    private int miShellPort;
    private String mstrShellUsername;
    private String mstrShellPassword;
    private boolean bShellSavePassword = false;

    // for SSH Tunnel
    private boolean bSSHEnabled;
    private String strSSHHost;
    private int iSSHPort;
    private String strSSHUsername;
    private String strSSHPassword;

    private Lock lockLogin = new ReentrantLock();
    private Lock lockAction = new ReentrantLock();
    private Comparator compareName = new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((DDTPThreadMonitor) o1).getThreadName().compareTo(((DDTPThreadMonitor) o2).getThreadName());
        }
    };
    private Comparator compareThreadID = new Comparator() {
        public int compare(Object o1, Object o2) {
            String s1 = ((DDTPThreadMonitor) o1).getThreadID();
            String s2 = ((DDTPThreadMonitor) o2).getThreadID();
            return s1.compareTo(s2);
        }
    };

    /**
     * @param model ManagerModel
     */
    public DDTPServerMonitor(ManagerModel model) {
        super(model, true);
    }

    public static boolean isEqualMap(Map<Object, Object> m1, Map<Object, Object> m2) {
        Map<Object, Object> mTemp = new HashMap<Object, Object>();
        mTemp.putAll(m2);
        mTemp.putAll(m1);

        for (Map.Entry<Object, Object> e : mTemp.entrySet()) {
            Object v1 = e.getValue();
            Object v2 = m2.get(e.getKey());

            if ((v1 == null || "".equals(v1)) && (v2 == null || "".equals(v2))) {
                return false;
            }

            if (!e.getValue().equals(v2)) {
                return false;
            }

        }
        return true;
    }

    public static Map vectorToMap(Vector vtProps) {
        Map<String, Object> mMap = new LinkedHashMap<String, Object>();
        for (int i = 0; i < vtProps.size(); i++) {
            Vector vtRow = (Vector) vtProps.get(i);
            mMap.put(vtRow.get(0).toString(), vtRow.get(1));
        }
        return mMap;
    }

    /**
     * @return String
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (isActive()) {
            buf.append(strUser).append("-");
        }
        buf.append(strSessionName);
        return buf.toString();
    }

    /**
     * @param strChannel String
     */
    public void removeUser(String strChannel) {
        lockAction.lock();
        try {
            Vector vtUser = getSession(strChannel);
            if (vtUser != null) {
                mvtSession.remove(vtUser);
                if (model != null) {
                    model.doEditorAction(this, UPDATE_SESSION_ACTION);
                }
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @return Vector
     */
    public Vector getSessions() {
        return mvtSession;
    }

    /**
     * @param strChannel String
     * @return Vector
     */
    public Vector getSession(String strChannel) {
        for (int i = 0; i < mvtSession.size(); i++) {
            Vector vtItem = (Vector) mvtSession.elementAt(i);
            if (vtItem.elementAt(0).equals(strChannel)) {
                return vtItem;
            }
        }
        return null;
    }

    /**
     * @param strChannel     String
     * @param strUserName    String
     * @param strConnectDate String
     * @param strHost        String
     */
    public void addUser(String strChannel, String strUserName, String strConnectDate, String strHost) {
        lockAction.lock();
        try {
            removeUser(strChannel);
            Vector vtItem = new Vector(4);
            vtItem.add(strChannel);
            vtItem.add(strUserName);
            vtItem.add(strConnectDate);
            vtItem.add(strHost);
            mvtSession.add(vtItem);
            if (model != null) {
                model.doEditorAction(this, UPDATE_SESSION_ACTION);
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @param strThreadID   String
     * @param strThreadName String
     */
    public void renameThread(String strThreadID, String strThreadName) {
        lockAction.lock();
        try {
            DDTPThreadMonitor thInfor = getThread(strThreadID);
            if (thInfor != null && !thInfor.getThreadName().equalsIgnoreCase(strThreadName)) {
                thInfor.setThreadName(strThreadName);
                if (strOrder.equalsIgnoreCase("Name")) {
                    doOrder(strOrder);
                }
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @param threadID     String
     * @param threadName   String
     * @param threadStatus String
     * @param vtProps      Vector
     */
    public void loadThread(String threadID, String threadName, String threadStatus, Vector vtProps, Map mpExtras) {
        lockAction.lock();
        try {
            DDTPThreadMonitor thInfor = getThread(threadID);
            if (thInfor == null) {
                thInfor = new DDTPThreadMonitor(this);
                thInfor.setThreadID(threadID);
                thInfor.setThreadName(threadName);
                thInfor.setThreadStatus(Integer.parseInt(threadStatus));
                Map<String, Object> mMap = vectorToMap(vtProps);
                thInfor.getAttributes().putAll(mMap);
                thInfor.setExtras(mpExtras);
                mvtThread.add(thInfor);
                model.notifyItemAdded(this, thInfor);
                doOrder(strOrder);
            } else {
                thInfor.setThreadName(threadName);
                thInfor.setThreadStatus(Integer.parseInt(threadStatus));
                model.notifyItemChanged(this, thInfor);
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @param strThreadID String
     */
    public void unloadThread(String strThreadID) {
        lockAction.lock();
        try {
            DDTPThreadMonitor thInfor = getThread(strThreadID);
            if (thInfor != null) {
                if (mvtThread.remove(thInfor)) {
                    model.notifyItemRemoved(this, thInfor);
                }
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @param strThreadID String
     * @return ThreadInfor
     */
    public DDTPThreadMonitor getThread(String strThreadID) {
        for (int i = 0; i < mvtThread.size(); i++) {
            DDTPThreadMonitor thInfor = (DDTPThreadMonitor) mvtThread.elementAt(i);
            if (thInfor.getThreadID().equals(strThreadID)) {
                return thInfor;
            }
        }
        return null;
    }

    /**
     * @param strThreadID String
     * @param status      String
     * @param strLog      String
     * @param bError      boolean
     */
    public void updateStatus(String strThreadID, String status, String strLog, boolean bError) {
        lockAction.lock();
        try {
            int iStatus = Integer.parseInt(status);
            DDTPThreadMonitor thInfor = getThread(strThreadID);
            if (thInfor != null) {
                thInfor.setThreadStatus(iStatus);
                thInfor.setError(bError);
                if (strLog.length() > 0) {
                    thInfor.getListLog().append(strLog);
                }
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     *
     */
    public void clear() {
        lockAction.lock();
        try {
            mvtThread = new Vector();
            mvtSession = new Vector();
            mvtNotifications = new Vector();
            txtAction.clear();
            model.notifyListChanged(this);
            model.doEditorAction(this, AFTER_DISCONNECT_ACTION);
            if (bConnectionFailured) {
                model.doEditorAction(this, RETRY_ACTION);
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     *
     */
    public void disconnect() {
        lockAction.lock();
        try {
            if (channel != null) {
                channel.close();
                channel = null;
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @return String
     */
    public String getThreadAppName() {
        return StringUtil.nvl(mstrThreadAppName, "Unknown platform");
    }

    /**
     * @return String
     */
    public String getThreadAppVersion() {
        return StringUtil.nvl(mstrThreadAppVersion, "Unknown version");
    }

    /**
     * @return String
     */
    public String getAppName() {
        return mstrAppName;
    }

    /**
     * setAppName
     *
     * @param string String
     */
    public void setAppName(String string) {
        mstrAppName = string;
    }

    /**
     * @return String
     */
    public String getAppVersion() {
        return StringUtil.nvl(mstrAppVersion, "Unknown version");
    }

    public final SocketTransmitter createSocketTrasmitter(AbstractSocket sck) {
        return new SocketTransmitter(sck, this) {
            public void close() {
                if (msckMain != null) {
                    channel = null;
                    super.close();
                    clear();
                }
            }
        };
    }

    public Packet createPacket() {
        switch (iProtocolVersion) {
            case PROTOCOL_VERSION1:
                return new DDTPv10();
            case PROTOCOL_VERSION2:
                return new DDTPv20();
            case PROTOCOL_HESSIAN:
                return new HessianPacket();
            case PROTOCOL_SMART:
                return new SmartPacket();
        }
        throw new RuntimeException("Unknown protocol version " + iProtocolVersion);
    }

    public Packet createPacket(InputStream in) throws IOException {
        switch (iProtocolVersion) {
            case PROTOCOL_VERSION1:
                return new DDTPv10(in);
            case PROTOCOL_VERSION2:
                return new DDTPv20(in);
            case PROTOCOL_HESSIAN:
                return new HessianPacket(in);
            case PROTOCOL_SMART:
                return new SmartPacket(in);
        }
        throw new RuntimeException("Unknown protocol version " + iProtocolVersion);
    }

    private void buildExtensionAction(Map<String, JComponent> mp) {
        for (Map.Entry<String, JComponent> e : mp.entrySet()) {
            if (e.getValue() instanceof AbstractButton) {
                AbstractButton btn = (AbstractButton) e.getValue();
                btn.addActionListener(new ExtensionActionListener(this, e.getKey()));
            }
        }
    }

    private void buildExtensionAction(BuildHelper builder) {
        Map<String, JComponent> mp = builder.getFormData().mmapControlList;
        buildExtensionAction(mp);
        mp = builder.getFormData().mmapFieldList;
        buildExtensionAction(mp);
    }

    /**
     * @throws Exception
     */
    public void login() throws Exception {
        if (lockLogin.tryLock()) {
            try {
                bConnectionFailured = false;
                if (isActive()) {
                    disconnect();
                }

                // Request to connect
                AbstractSocket sck = null;
                if (bSSHEnabled) {
                    sck = new SSHSocket(strSSHHost, iSSHPort, strSSHUsername, strSSHPassword, strIP, iPort);
                } else {
                    sck = new PlainSocket(strIP, iPort);
                }
                // Start up a channel thread that reads messages from the server
                channel = createSocketTrasmitter(sck);
                channel.setProcessor(new MonitorSocketServer(channel, this, this));
                channel.setUserName(strUser);
                channel.setPackage("com.telsoft.thread.");
                channel.start();

                // Request to Server
                Packet request = createPacket();
                request.setRequestID(String.valueOf(System.currentTimeMillis()));
                request.setString("UserName", strUser);
                String strPass = strPassword;
                if (!strPass.equals("") && !strEncryptMethod.equals("")) {
                    if (strEncryptMethod.equals("NG")) {
                        strPass = new EncryptionService().encrypt(strPassword);
                    } else
                        strPass = StringUtil.encrypt(strPassword, strEncryptMethod);
                }
                request.setString("Password", strPass);

                // Response from Server
                String requestClass = request.getClass().getName();
                if (requestClass.startsWith("com.telsoft.monitor.ddtp.packet.smart.")) {
                    channel.setPackage("smartlib.thread.");
                }
                Packet response = channel.sendRequest("ThreadProcessor", "login", request);
                if (response != null) {
                    String strVersion = StringUtil.nvl(response.getString("strChannel"), "");
                    if (strVersion.startsWith("com.fss.")) {
                        channel.setPackage("com.fss.thread.");
                    } else if (strVersion.startsWith("smartlib.")) {
                        channel.setPackage("smartlib.thread.");
                    }
                    mstrThreadAppName = response.getString("strThreadAppName");
                    mstrThreadAppVersion = response.getString("strThreadAppVersion");
                    mstrAppName = response.getString("strAppName");
                    mstrAppVersion = response.getString("strAppVersion");

                    mstrExtensionForm = response.getString("ExtensionForm");
                    mstrExtensionClass = response.getString("ExtensionClass");

                    if (mstrExtensionClass != null && mstrExtensionClass.length() > 0) {
                        ExtensionLoader loader = (ExtensionLoader) Class.forName(mstrExtensionClass).newInstance();
                        moExtensionObject = loader.init(this);
                    } else if (mstrExtensionForm != null && mstrExtensionForm.length() > 0) {
                        mstrExtensionBean = response.getString("ExtensionBean");
                        JRootPane pnl = new JRootPane();
                        BuildHelper build = new BuildHelper(pnl);
                        /*build.buildForm(mstrExtensionForm);*/
                        build.buildForm();
                        moExtensionObject = new JRootPane[]
                                {pnl};
                        buildExtensionAction(build);

                    }

                    if (response.getString("PasswordExpired") != null) {
                        model.doEditorAction(this, CHANGE_PASSWORD_ACTION);
                    }
                    String strLog = StringUtil.nvl(response.getString("strLog"), "");

                    getLogAction().clear();

                    String[] ls = StringUtil.toStringArray(strLog, "\n");
                    for (String o : ls) {
                        getLogAction().append(o);
                    }

                    Vector vtThread = response.getVector("vtThread");
                    updateThreadList(vtThread);
                    model.doEditorAction(this, AFTER_LOGIN_ACTION);
                    bConnectionFailured = true;
                }
            } catch (Exception e) {
                // Disconnect from server
                disconnect();
                throw e;
            } finally {
                lockLogin.unlock();
            }
        } else {
            throw new Exception("Logging");
        }
    }

    /**
     * updateThreadList
     *
     * @param vtThread Vector
     */
    private void updateThreadList(Vector vtThread) {
        lockAction.lock();
        try {
            synchronized (mvtThread) {
                mvtThread.clear();
                for (int i = 0; i < vtThread.size(); i++) {
                    Vector vtRow = (Vector) vtThread.elementAt(i);
                    DDTPThreadMonitor threadInfor = new DDTPThreadMonitor(this);
                    threadInfor.setThreadID((String) vtRow.elementAt(0));
                    threadInfor.setThreadName((String) vtRow.elementAt(1));
                    threadInfor.setThreadStatus(Integer.parseInt((String) vtRow.elementAt(2)));

                    String s = StringUtil.nvl((String) vtRow.elementAt(3), "");
                    if (s != null) {
                        String[] ls = s.split("\r\n");
                        for (String si : ls) {
                            if (si.trim().length() > 0) {
                                threadInfor.getListLog().append(si);
                            }
                        }
                    }
                    if (vtRow.size() >= 5) {
                        threadInfor.getAttributes().putAll(vectorToMap((Vector) vtRow.get(4)));
                    }

                    if (vtRow.size() >= 6) {
                        Map mpExtras = (Map) vtRow.get(5);
                        threadInfor.setExtras(mpExtras);
                    }
                    mvtThread.add(threadInfor);
                }
                doOrder(strOrder);
                model.notifyListChanged(this);
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @throws Exception
     */
    public void stopServer() throws Exception {
        lockAction.lock();
        try {
            try {
                bConnectionFailured = false;
                Packet request = createPacket();
                request.setRequestID(String.valueOf(System.currentTimeMillis()));
                channel.sendRequest("ThreadProcessor", "closeServer", request);
            } catch (Exception ex) {
                bConnectionFailured = true;
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @param strMessage String
     * @throws Exception
     */
    public void sendMessage(String strMessage) throws Exception {
        Packet request = createPacket();
        request.setString("strMessage", strMessage);
        request("sendMessage", request);
    }

    /**
     * @param strFunction String
     * @param request     Packet
     * @return Packet
     * @throws Exception
     */
    public Packet request(String strFunction, Packet request) throws Exception {
        return channel.sendRequest("ThreadProcessor", strFunction, request);
    }

    /**
     * @return StringBuffer
     */
    public StringListHolder getLogAction() {
        return txtAction;
    }

    /**
     * @return boolean
     */
    public boolean isActive() {
        return (channel != null && channel.isOpen());
    }

    /**
     * @param bActive boolean
     * @throws Exception
     */
    public void setActive(boolean bActive) throws Exception {
        if (bActive) {
            login();
        } else {
            bConnectionFailured = false;
            disconnect();
        }
    }

    /**
     * @return boolean
     */
    public boolean hasChildObject() {
        return isActive();
    }

    /**
     * @return int
     */
    public int getChildCount() {
        return mvtThread.size();
    }

    /**
     * @param index int
     * @return MonitorObject
     */
    public MonitorObject getChild(int index) {
        return (MonitorObject) mvtThread.get(index);
    }

    /**
     * @param child MonitorObject
     * @return int
     */
    public int indexOfChild(MonitorObject child) {
        return mvtThread.indexOf(child);
    }

    /**
     * @param node DictionaryNode
     */
    public void store(DictionaryNode node) {
        lockAction.lock();
        try {
            super.store(node);
            node.setString("Session", strSessionName);
            node.setString("Host", strIP);
            node.setString("Port", String.valueOf(iPort));
            node.setString("User", strUser);
            node.setString("Order", StringUtil.nvl(strOrder, ""));
            node.setString("EncryptMethod", strEncryptMethod);
            node.setString("Protocol", String.valueOf(iProtocolVersion));
            node.setString("SystemInfor", mbSystemInfor ? "1" : "0");

            node.setString("SSHHost", strSSHHost);
            node.setString("SSHPort", String.valueOf(iSSHPort));
            node.setString("SSHUser", strSSHUsername);
            node.setString("SSH", bSSHEnabled ? "1" : "0");

            node.setString("SavePassword", bSavePassword ? "1" : "0");
            if (bSavePassword) {
                try {
                    node.setString("Password", PasswordLoader.getEncryptedString(strPassword, "config.key"));
                } catch (Exception ex) {
                }

                try {
                    node.setString("SSHPassword", PasswordLoader.getEncryptedString(strSSHPassword, "config.key"));
                } catch (Exception ex) {
                }
            }

            node.setString("ShellStartCommand", StringUtil.join(mstrStartCommand, "\r\n"));
            node.setString("ShellStopCommand", StringUtil.join(mstrStopCommand, "\r\n"));
            node.setString("ShellPort", String.valueOf(miShellPort));
            node.setString("ShellUsername", mstrShellUsername);
            node.setString("ShellSavePassword", bShellSavePassword ? "1" : "0");
            if (bShellSavePassword) {
                try {
                    node.setString("ShellPassword", PasswordLoader.getEncryptedString(mstrShellPassword, "config.key"));
                } catch (Exception ex) {
                }
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @param node DictionaryNode
     */
    public void load(DictionaryNode node) {
        lockAction.lock();
        try {
            super.load(node);
            strSessionName = node.getString("Session");
            iProtocolVersion = Integer.parseInt(node.getString("Protocol"));
            strOrder = node.getString("Order");
            strEncryptMethod = node.getString("EncryptMethod");
            strIP = node.getString("Host");
            iPort = Integer.parseInt(node.getString("Port"));
            strUser = node.getString("User");
            mbSystemInfor = StringUtil.nvl(node.getString("SystemInfor"), "0").equals("1");

            strSSHHost = node.getString("SSHHost");
            try {
                iSSHPort = Integer.parseInt(node.getString("SSHPort"));
            } catch (Exception ex) {
                iSSHPort = 22;
            }
            strSSHUsername = node.getString("SSHUser");
            bSSHEnabled = StringUtil.nvl(node.getString("SSH"), "0").equals("1");

            bSavePassword = StringUtil.nvl(node.getString("SavePassword"), "0").equals("1");
            if (bSavePassword) {
                try {
                    strPassword = PasswordLoader.getDecryptedString(node.getString("Password"), "config.key");
                } catch (Exception ex) {
                }

                try {
                    strSSHPassword = PasswordLoader.getDecryptedString(node.getString("SSHPassword"), "config.key");
                } catch (Exception ex) {
                }
            }
            mstrStartCommand = StringUtil.toStringArray(node.getString("ShellStartCommand"), "\r\n");
            mstrStopCommand = StringUtil.toStringArray(node.getString("ShellStopCommand"), "\r\n");
            try {
                miShellPort = Integer.parseInt(node.getString("ShellPort"));
            } catch (Exception ex) {
                miShellPort = 22;
            }
            mstrShellUsername = node.getString("ShellUsername");
            bShellSavePassword = StringUtil.nvl(node.getString("ShellSavePassword"), "0").equals("1");
            if (bShellSavePassword) {
                try {
                    mstrShellPassword = PasswordLoader.getDecryptedString(node.getString("ShellPassword"), "config.key");
                } catch (Exception ex) {
                }
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @param mo MonitorObject
     * @return boolean
     */
    public boolean isEqual(MonitorObject mo) {
        if (mo instanceof DDTPServerMonitor) {
            DDTPServerMonitor a = (DDTPServerMonitor) mo;
            if (a.strSessionName.equalsIgnoreCase(strSessionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param strOrderType String
     */
    public void doOrder(String strOrderType) {
        lockAction.lock();
        try {
            if (strOrderType.equalsIgnoreCase("Name")) {
                doSort(compareName);
                model.notifyListChanged(this);
            } else if (strOrderType.equalsIgnoreCase("ThreadID")) {
                doSort(compareThreadID);
                model.notifyListChanged(this);
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * @param comp Comparator
     */
    protected void doSort(Comparator comp) {
        if (mvtThread.size() < 2) {
            return;
        }
        for (int i = 0; i < mvtThread.size() - 1; i++) {
            for (int j = i + 1; j < mvtThread.size(); j++) {
                Object A = mvtThread.elementAt(i);
                Object B = mvtThread.elementAt(j);
                if (comp.compare(A, B) > 0) {
                    mvtThread.setElementAt(B, i);
                    mvtThread.setElementAt(A, j);
                }
            }
        }
    }

    public SocketTransmitter getChannel() {
        return channel;
    }

    public String getUser() {
        return strUser;
    }

    public void setUser(String value) {
        strUser = value;
    }

    public String getPassword() {
        return strPassword;
    }

    public void setPassword(String value) {
        strPassword = value;
    }

    public String getIP() {
        return strIP;
    }

    public void setIP(String value) {
        strIP = value;
    }

    public int getPort() {
        return iPort;
    }

    public void setPort(int value) {
        iPort = value;
    }

    public String getSSHUser() {
        return strSSHUsername;
    }

    public void setSSHUser(String value) {
        strSSHUsername = value;
    }

    public String getSSHPassword() {
        return strSSHPassword;
    }

    public void setSSHPassword(String value) {
        strSSHPassword = value;
    }

    public String getShellUser() {
        return mstrShellUsername;
    }

    public String getShellPassword() {
        return mstrShellPassword;
    }

    public String getSSHHost() {
        return strSSHHost;
    }

    public void setSSHHost(String value) {
        strSSHHost = value;
    }

    public int getSSHPort() {
        return iSSHPort;
    }

    public void setSSHPort(int value) {
        iSSHPort = value;
    }

    public int getShellPort() {
        return miShellPort;
    }

    public boolean isSSHEnabled() {
        return bSSHEnabled;
    }

    public void setSSHEnabled(boolean enabled) {
        bSSHEnabled = enabled;
    }

    public String getSessionName() {
        return strSessionName;
    }

    public void setSessionName(String value) {
        strSessionName = value;
    }

    public String getOrder() {
        return strOrder;
    }

    public void setOrder(String value) {
        strOrder = value;
    }

    public String getEncryptMethod() {
        return strEncryptMethod;
    }

    public void setEncryptMethod(String value) {
        strEncryptMethod = value;
    }

    public int getProtocol() {
        return iProtocolVersion;
    }

    public void setProtocol(int iVersion) {
        iProtocolVersion = iVersion;
    }

    public boolean isSavePassword() {
        return bSavePassword;
    }

    public void setSavePassword(boolean bSave) {
        bSavePassword = bSave;
    }

    /**
     * doNotify
     *
     * @param vtNotification Vector
     */
    public void doNotify(Vector vtNotification) {
        lockAction.lock();
        try {
            mvtNotifications = vtNotification;
            if (model != null) {
                model.doEditorAction(this, NOTIFY_ACTION);
            }
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * getNotifyList
     *
     * @return Vector
     */
    public Vector getNotifyList() {
        return mvtNotifications;
    }

    /**
     * clearNotify
     */
    public void clearNotify() {
        mvtNotifications.clear();
    }

    /**
     * updateProperties
     *
     * @param strThreadID String
     * @param vtProps     Vector
     */
    public void updateProperties(String strThreadID, Vector vtProps) {
        lockAction.lock();
        try {
            DDTPThreadMonitor thInfor = getThread(strThreadID);
            if (thInfor != null) {
                Map<Object, Object> mMap = vectorToMap(vtProps);
                if (!isEqualMap(thInfor.getAttributes(), mMap)) {
                    thInfor.getAttributes().putAll(mMap);
                    if (isGrouped()) {
                        model.notifyListChanged(this);
                    } else {
                        model.notifyItemChanged(this, thInfor, true);
                    }
                }
            }
        } finally {
            lockAction.unlock();
        }
    }

    public boolean supportSystemInfor() {
        return mbSystemInfor;
    }

    public void setSupportSystemInfor(boolean b) {
        mbSystemInfor = b;
    }

    public String[] getStartCommand() {
        return mstrStartCommand;
    }

    public String[] getStopCommand() {
        return mstrStopCommand;
    }

    public Object[] getExtensionObject() {
        return moExtensionObject;
    }

    public static class ExtensionActionListener implements ActionListener {
        private String strActionCode;
        private DDTPServerMonitor server;

        public ExtensionActionListener(DDTPServerMonitor server, String strActionCode) {
            this.server = server;
            this.strActionCode = strActionCode;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (strActionCode != null && strActionCode.length() > 0) {
                    Packet request = server.createPacket();
                    request.setRequestID(String.valueOf(System.currentTimeMillis()));
                    request.setString("ACTION", strActionCode);
                    Packet response = server.channel.sendRequest(server.mstrExtensionBean, "extension", request);
                    if (response.getReturn() != null) {
                        server.model.showMessage(String.valueOf(response.getReturn()));
                    }
                }
            } catch (Exception ex) {
                server.model.showMessage(ex.getLocalizedMessage());
            }
        }
    }
}
