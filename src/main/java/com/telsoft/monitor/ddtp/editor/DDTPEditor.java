package com.telsoft.monitor.ddtp.editor;

import smartlib.dictionary.ErrorDictionary;
import com.telsoft.monitor.ddtp.DDTPServerMonitor;
import com.telsoft.monitor.ddtp.DDTPThreadMonitor;
import com.telsoft.monitor.ddtp.editor.terminal.SwingTerminal;
import com.telsoft.monitor.manager.ManagerModel;
import com.telsoft.monitor.manager.MonitorObject;
import com.telsoft.monitor.manager.ParentMonitorEditor;
import com.telsoft.monitor.manager.util.InvokeWithMonitorObject;
import com.telsoft.monitor.register.Register;
import com.telsoft.monitor.util.JXMenuItem;
import com.telsoft.monitor.util.Msg;
import com.telsoft.monitor.util.TextChangeEvent;
import com.telsoft.monitor.util.TextChangeListener;
import smartlib.swing.JTableContainer;
import smartlib.swing.VectorTable;
import smartlib.swing.WindowManager;
import smartlib.transport.Packet;
import smartlib.util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Vector;
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

public class DDTPEditor extends ParentMonitorEditor implements TextChangeListener {
    /**
     * onGetCustomIcon
     *
     * @param obj MonitorObject
     * @param selected boolean
     * @param expanded boolean
     * @param leaf boolean
     * @param row int
     * @param hasFocus boolean
     * @return Icon
     */
    private static Icon iconApp =
            new ImageIcon(FileUtil.getResource("resource/com/telsoft/monitor/ddtp/icon/app.png"));
    private final DialogManager dialogManager = new DialogManager();
    protected PanelThreadManager pnlManager = null;
    protected GridBagConstraints layoutCons =
            new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0);
    private List<Component> mnuContext = null;
    private JXMenuItem miStopServer = null;
    private JXMenuItem miThreadManager = null;
    private JXMenuItem miThreadManager2 = null;
    private JXMenuItem miChangePass = null;
    private JMenu mnuOrder = null;
    private GridBagLayout layout = new GridBagLayout();
    private PanelThreadMonitor pnlThread = null;
    private DDTPServerMonitor lastServer = null;
    private DDTPThreadMonitor lastThread = null;
    private boolean lastShowServerManager = false;
    private Lock lockAction = new ReentrantLock();
    /*private PanelSystemInfo pnlSysInfo = null;*/
    private JPanel pnlProperties = null;
    private VectorTable tableProperties = new VectorTable();

    public DDTPEditor(ManagerModel model) {
        super(model);
    }

    private static String getProtocolName(int i) {
        switch (i) {
            case DDTPServerMonitor.PROTOCOL_HESSIAN:
                return "Hessian";
            case DDTPServerMonitor.PROTOCOL_VERSION1:
                return "DDTP v1";
            case DDTPServerMonitor.PROTOCOL_VERSION2:
                return "DDTP v2";
            default:
                return "Unknown protocol";
        }
    }

    private static Vector newNameValue(String name, Object value) {
        Vector vt = new Vector();
        vt.add(name);
        vt.add(value);
        return vt;
    }

    /**
     * @param root       Component
     * @param obj        MonitorObject
     * @param showDialog boolean
     * @return MonitorObject
     */
    public MonitorObject connect(Component root, MonitorObject obj, boolean showDialog) {
        lockAction.lock();
        try {
            DDTPServerMonitor mo = (DDTPServerMonitor) obj;
            if (showDialog || mo.getUser().equals("") || mo.getPassword().equals("")) {
                DialogLogin dlg = new DialogLogin(root);
                dlg.getFormData().setFieldValue("SessionName", mo.getSessionName());
                dlg.getFormData().setFieldValue("Host", mo.getIP());
                dlg.getFormData().setFieldValue("Port", String.valueOf(mo.getPort()));
                dlg.getFormData().setFieldValue("UserName", mo.getUser());
                dlg.getFormData().setFieldValue("Password", mo.getPassword());
                dlg.getFormData().setFieldValue("Protocol", String.valueOf(mo.getProtocol()));
                dlg.getFormData().setFieldValue("SavePassword", String.valueOf(mo.isSavePassword()));
                dlg.getFormData().setFieldValue("SystemInfor", String.valueOf(mo.supportSystemInfor()));

                dlg.getFormData().setFieldValue("SSH", String.valueOf(mo.isSSHEnabled()));
                dlg.getFormData().setFieldValue("SSHHost", mo.getSSHHost());
                dlg.getFormData().setFieldValue("SSHPort", String.valueOf(mo.getSSHPort()));
                dlg.getFormData().setFieldValue("SSHUserName", mo.getSSHUser());
                dlg.getFormData().setFieldValue("SSHPassword", mo.getSSHPassword());

                WindowManager.centeredWindow(dlg);
                if (dlg.miReturn == JOptionPane.OK_OPTION) {
                    mo.setSessionName(dlg.getFormData().getFieldString("SessionName"));
                    mo.setIP(dlg.getFormData().getFieldString("Host"));
                    mo.setPort(Integer.parseInt(dlg.getFormData().getFieldString("Port")));
                    mo.setUser(dlg.getFormData().getFieldString("UserName"));
                    mo.setPassword(dlg.getFormData().getFieldString("Password"));
                    mo.setEncryptMethod(dlg.getFormData().getFieldString("EncryptAlgorithm"));
                    mo.setProtocol(Integer.parseInt(dlg.getFormData().getFieldString("Protocol")));
                    mo.setSavePassword(Boolean.parseBoolean(dlg.getFormData().getFieldString("SavePassword")));
                    mo.setSupportSystemInfor(Boolean.parseBoolean(dlg.getFormData().getFieldString("SystemInfor")));

                    mo.setSSHEnabled(Boolean.parseBoolean(dlg.getFormData().getFieldString("SSH")));
                    mo.setSSHHost(dlg.getFormData().getFieldString("SSHHost"));
                    try {
                        mo.setSSHPort(Integer.parseInt(dlg.getFormData().getFieldString("SSHPort")));
                    } catch (Exception ex) {
                        mo.setSSHPort(22);
                    }
                    mo.setSSHUser(dlg.getFormData().getFieldString("SSHUserName"));
                    mo.setSSHPassword(dlg.getFormData().getFieldString("SSHPassword"));

                    return mo;
                }
            } else {
                return mo;
            }
        } catch (Exception ex) {
            model.getMonitorManager().showException(ex);
        } finally {
            lockAction.unlock();
        }
        return null;
    }

    public DDTPServerMonitor createNewDDTPServerMonitor(ManagerModel model) {
        return new DDTPServerMonitor(model);
    }

    /**
     * @param root  Component
     * @param model ManagerModel
     * @return MonitorObject
     */
    public MonitorObject connectNew(Component root, ManagerModel model, MonitorObject copyFrom) {
        lockAction.lock();
        try {
            DialogLogin dlg = new DialogLogin(root);
            if (copyFrom != null && copyFrom instanceof DDTPServerMonitor) {
                DDTPServerMonitor ddCopy = (DDTPServerMonitor) copyFrom;
                dlg.getFormData().setFieldValue("SessionName", "Copy of " + ddCopy.getSessionName());
                dlg.getFormData().setFieldValue("Host", ddCopy.getIP());
                dlg.getFormData().setFieldValue("Port", ddCopy.getPort());
                dlg.getFormData().setFieldValue("UserName", ddCopy.getUser());
                dlg.getFormData().setFieldValue("Password", "");
                dlg.getFormData().setFieldValue("SavePassword", "");
                dlg.getFormData().setFieldValue("SystemInfor", ddCopy.supportSystemInfor());
                dlg.getFormData().setFieldValue("EncryptAlgorithm", ddCopy.getEncryptMethod());
                dlg.getFormData().setFieldValue("Protocol", String.valueOf(ddCopy.getProtocol()));

                dlg.getFormData().setFieldValue("SSH", ddCopy.isSSHEnabled());
                dlg.getFormData().setFieldValue("SSHHost", ddCopy.getSSHHost());
                dlg.getFormData().setFieldValue("SSHPort", ddCopy.getSSHPort());
                dlg.getFormData().setFieldValue("SSHUserName", ddCopy.getSSHUser());
                dlg.getFormData().setFieldValue("SSHPassword", "");
            } else {
                dlg.getFormData().setFieldValue("SessionName", "");
                dlg.getFormData().setFieldValue("Host", "");
                dlg.getFormData().setFieldValue("Port", "");
                dlg.getFormData().setFieldValue("UserName", "");
                dlg.getFormData().setFieldValue("Password", "");
                dlg.getFormData().setFieldValue("SavePassword", "");
                dlg.getFormData().setFieldValue("SystemInfor", "");
                dlg.getFormData().setFieldValue("EncryptAlgorithm", "SHA");
                dlg.getFormData().setFieldValue("Protocol", "3");

                dlg.getFormData().setFieldValue("SSH", "");
                dlg.getFormData().setFieldValue("SSHHost", "");
                dlg.getFormData().setFieldValue("SSHPort", "");
                dlg.getFormData().setFieldValue("SSHUserName", "");
                dlg.getFormData().setFieldValue("SSHPassword", "");
            }
            WindowManager.centeredWindow(dlg);
            if (dlg.miReturn == JOptionPane.OK_OPTION) {
                DDTPServerMonitor mo = createNewDDTPServerMonitor(model);
                mo.setSessionName(dlg.getFormData().getFieldString("SessionName"));
                mo.setIP(dlg.getFormData().getFieldString("Host"));
                mo.setPort(Integer.parseInt(dlg.getFormData().getFieldString("Port")));
                mo.setUser(dlg.getFormData().getFieldString("UserName"));
                mo.setPassword(dlg.getFormData().getFieldString("Password"));
                mo.setEncryptMethod(dlg.getFormData().getFieldString("EncryptAlgorithm"));
                mo.setProtocol(Integer.parseInt(dlg.getFormData().getFieldString("Protocol")));
                mo.setSavePassword(Boolean.parseBoolean(dlg.getFormData().getFieldString("SavePassword")));
                mo.setSupportSystemInfor(Boolean.parseBoolean(dlg.getFormData().getFieldString("SystemInfor")));

                mo.setSSHEnabled(Boolean.parseBoolean(dlg.getFormData().getFieldString("SSH")));
                mo.setSSHHost(dlg.getFormData().getFieldString("SSHHost"));
                try {
                    mo.setSSHPort(Integer.parseInt(dlg.getFormData().getFieldString("SSHPort")));
                } catch (Exception ex) {
                    mo.setSSHPort(22);
                }
                mo.setSSHUser(dlg.getFormData().getFieldString("SSHUserName"));
                mo.setSSHPassword(dlg.getFormData().getFieldString("SSHPassword"));

                return mo;
            }
        } catch (Exception ex) {
            model.getMonitorManager().showException(ex);
        } finally {
            lockAction.unlock();
        }
        return null;
    }

    /**
     * @param obj MonitorObject
     * @return JPopupMenu
     */
    public List<Component> getContextMenu(MonitorObject obj) {
        if (mnuContext == null) {
            mnuContext = super.getContextMenu(obj);

            miStopServer = new JXMenuItem("Stop server", JXMenuItem.START_USER_ACTION + 1, obj);
            miChangePass = new JXMenuItem("Change password", DDTPServerMonitor.CHANGE_PASSWORD_ACTION, obj);
            miThreadManager = new JXMenuItem("Thread Manager", DDTPServerMonitor.MANAGER_THREADS_ACTION, obj);
            miThreadManager2 = new JXMenuItem("Start/stop thread(s)", DDTPServerMonitor.MANAGER_THREADS2_ACTION, obj);

            mnuOrder = new JMenu("Order");
            mnuOrder.add(new JXMenuItem("Name", DDTPServerMonitor.ORDER_BYNAME_ACTION, obj));
            mnuOrder.add(new JXMenuItem("ThreadID", DDTPServerMonitor.ORDER_BYID_ACTION, obj));

            mnuContext.add(miThreadManager);
            mnuContext.add(miThreadManager2);
            mnuContext.add(miChangePass);
            mnuContext.add(mnuOrder);

            mnuContext.add(new JPopupMenu.Separator());

            mnuContext.add(miStopServer);
        }
        return mnuContext;
    }

    /**
     * @param obj MonitorObject
     */
    public void onContextMenu(MonitorObject obj) {
        super.onContextMenu(obj);
        miChangePass.setEnabled(obj.isActive());
        miStopServer.setEnabled(obj.isActive());
        miThreadManager.setEnabled(obj.isActive());
        miThreadManager2.setEnabled(obj.isActive());
        mnuOrder.setEnabled(obj.isActive());
        DDTPServerMonitor dmo = (DDTPServerMonitor) obj;
    }

    /**
     * @param obj MonitorObject
     * @param id  int
     * @throws Exception
     */
    public boolean onMenuAction(MonitorObject obj, int id) throws Exception {
        DDTPServerMonitor ddtpObj = (DDTPServerMonitor) obj;
        switch (id) {
            case DDTPServerMonitor.STOP_SERVER_ACTION: {
                if (Msg.confirm("Stop server?")) {
                    ddtpObj.stopServer();
                }
                break;
            }
            case DDTPServerMonitor.CHANGE_PASSWORD_ACTION: {
                changePassword(ddtpObj);
                break;
            }
            case DDTPServerMonitor.MANAGER_THREADS_ACTION: {
                manageThreads(ddtpObj);
                break;
            }
            case DDTPServerMonitor.MANAGER_THREADS2_ACTION: {
                manageThreads2(ddtpObj);
                break;
            }
            case DDTPServerMonitor.UPDATE_SESSION_ACTION: {
                updateSession(ddtpObj);
                break;
            }
            case DDTPServerMonitor.NOTIFY_ACTION: {
                updateNotify(ddtpObj);
                break;
            }
            case DDTPServerMonitor.ORDER_BYNAME_ACTION: {
                ddtpObj.setOrder("Name");
                ddtpObj.doOrder(ddtpObj.getOrder());
                break;
            }
            case DDTPServerMonitor.ORDER_BYID_ACTION: {
                ddtpObj.setOrder("ThreadID");
                ddtpObj.doOrder(ddtpObj.getOrder());
                break;
            }
            case DDTPServerMonitor.AFTER_DISCONNECT_ACTION: {
                if (pnlManager != null) {
                    updateAction(ddtpObj);
                    updateSession(ddtpObj);
                    updateNotify(ddtpObj);
                    updateExtension(ddtpObj);
                    pnlManager.getFormData().getLayout("Top").removeAll();
                    pnlManager.getFormData().getControl("Board").setEnabled(ddtpObj.isActive());
                    pnlManager.getFormData().getControl("Message").setEnabled(ddtpObj.isActive());
                    pnlManager.getFormData().getControl("Refresh").setEnabled(ddtpObj.isActive());
                    pnlManager.getFormData().getControl("Kick").setEnabled(ddtpObj.isActive());
                    pnlManager.getFormData().getControl("SessionList").setEnabled(ddtpObj.isActive());
                    pnlManager.getFormData().getControl("NotificationList").setEnabled(ddtpObj.isActive());
                    lastThread = null;
                    dialogManager.closeAll(ddtpObj);
                    Register.getBoard().updateUI();
                }
                model.notifyListChanged(ddtpObj);
                break;
            }
            case DDTPServerMonitor.AFTER_LOGIN_ACTION: {
                lastServer = null;
                lastThread = null;
                showEditor(ddtpObj);
                model.notifyListChanged(ddtpObj);
                break;
            }
            case DDTPServerMonitor.RETRY_ACTION: {
                InvokeWithMonitorObject iwmo = new InvokeWithMonitorObject(ddtpObj, getModel().getRootComponent()) {
                    public void run() {
                        String strLastError = null;
                        while (true) {
                            String strMsg = "Connection to <b>" + mmo + "</b> disconnected. <br>Retry to connect?";

                            if (strLastError != null) {
                                strMsg = strLastError + "<br><br>" + strMsg;
                            }
                            strLastError = null;
                            if (Msg.confirm(mroot, strMsg)) {
                                try {
                                    mmo.setActive(true);
                                    if (mmo.isActive()) {
                                        return;
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    strLastError = "<b><font color=red>" + ErrorDictionary.getString(ex) + "</font></b>";
                                }
                            } else {
                                return;
                            }
                        }
                    }
                };

                SwingUtilities.invokeLater(iwmo);
            }
            default: {
                return super.onMenuAction(obj, id);
            }
        }
        return false;
    }

    /**
     * @param obj DDTPServerMonitor
     * @throws Exception
     */
    public void manageThreads(DDTPServerMonitor obj) throws Exception {
        Packet request = obj.createPacket();
        request.setRequestID(String.valueOf(System.currentTimeMillis()));
        Packet response = obj.getChannel().sendRequest("ThreadProcessor", "manageThreadsLoad", request);
        if (response != null) {
            DialogThreadManager dlgManage =
                    new DialogThreadManager(getModel().getRootComponent(), obj.getChannel(), response.getVector("vtTableData"));
            if (getDialogManager().addWindow(dlgManage, obj, "DialogThreadManager")) {
                WindowManager.centeredWindow(dlgManage);
            }
        }
    }

    /**
     * @param obj DDTPServerMonitor
     * @throws Exception
     */
    public void manageThreads2(DDTPServerMonitor obj) throws Exception {
        DialogThreadManager2 dlgManage =
                new DialogThreadManager2(getModel().getRootComponent(), obj);
        if (getDialogManager().addWindow(dlgManage, obj, "DialogThreadManager2")) {
            WindowManager.centeredWindow(dlgManage);
        }
    }

    /**
     * @param obj DDTPServerMonitor
     * @throws Exception
     */
    public void changePassword(DDTPServerMonitor obj) throws Exception {
        DialogChangePassword dlgPass = new DialogChangePassword(getModel().getRootComponent(), obj.getChannel());
        WindowManager.centeredWindow(dlgPass);
    }

    /**
     * showEditor
     *
     * @param obj MonitorObject
     */
    public void showEditor(MonitorObject obj) {
        showEditor(obj, true);
    }

    public void showEditor(MonitorObject obj, boolean showServerManager) {
        lockAction.lock();
        try {
            JComponent ctnBoard = Register.getBoard();
            if (pnlManager == null) {
                pnlManager = new PanelThreadManager();
            }
            pnlManager.setServer((DDTPServerMonitor) obj);
            ctnBoard.removeAll();
            ctnBoard.setLayout(layout);
            ctnBoard.add(pnlManager, layoutCons);
            ctnBoard.updateUI();
            boolean bChanged = lastServer != obj;
            if (bChanged) {
                if (lastServer != null) {
                    lastServer.getLogAction().removeTextChangeListener(this);
                }
                lastServer = (DDTPServerMonitor) obj;
                updateSession(lastServer);
                updateAction(lastServer);
                updateNotify(lastServer);
                updateExtension(lastServer);
                pnlManager.getFormData().getLayout("Top").removeAll();
                if (lastThread != null) {
                    lastThread.getListLog().removeTextChangeListener(this);
                }
                lastThread = null;
                lastServer.getLogAction().removeTextChangeListener(this);
                lastServer.getLogAction().addTextChangeListener(this);

                pnlManager.getFormData().getControl("Board").setEnabled(obj.isActive());
                pnlManager.getFormData().getControl("Message").setEnabled(obj.isActive());
                pnlManager.getFormData().getControl("Refresh").setEnabled(obj.isActive());
                pnlManager.getFormData().getControl("Kick").setEnabled(obj.isActive());
                pnlManager.getFormData().getControl("SessionList").setEnabled(obj.isActive());
                pnlManager.getFormData().getControl("NotificationList").setEnabled(obj.isActive());
            }

            if (lastShowServerManager != showServerManager || bChanged) {
                lastShowServerManager = showServerManager;
                // show monitor panel
                pnlManager.getFormData().getLayout("Top").removeAll();
                pnlManager.getFormData().getLayout("Top").setLayout(layout);

                if (lastShowServerManager) {
                    if (lastThread != null) {
                        lastThread.getListLog().removeTextChangeListener(this);
                    }
                    lastThread = null;
                    showManager((DDTPServerMonitor) obj, true);
                } else {
                    showManager((DDTPServerMonitor) obj, false);
                }
            }

        } catch (Exception ex) {
            model.getMonitorManager().showException(ex);
        } finally {
            lockAction.unlock();
        }
    }

    private void updateExtension(DDTPServerMonitor obj) {
        if (lastServer == obj && pnlManager != null) {
            lockAction.lock();
            try {
                ((JTabbedPane) pnlManager.getFormData().getLayout("Extension")).removeAll();
                if (obj.isActive()) {
                    Object[] o = obj.getExtensionObject();
                    if (o != null) {
                        for (int i = 0; i < o.length; i++) {
                            if (o[i] instanceof JPanel) {
                                ((JTabbedPane) pnlManager.getFormData().getLayout("Extension")).add((JPanel) o[i]);

                            } else if (o[i] instanceof JRootPane) {
                                ((JTabbedPane) pnlManager.getFormData().getLayout("Extension")).add((JRootPane) o[i]);

                            }
                        }
                    }
                }
            } finally {
                lockAction.unlock();
            }
        }
    }

    /**
     * showManager
     *
     * @param obj DDTPServerMonitor
     */
    protected void showManager(DDTPServerMonitor obj, boolean bShow) throws Exception {
        /*if (bShow && obj.supportSystemInfor()) {
            if (pnlSysInfo == null) {
                pnlSysInfo = new PanelSystemInfo();
            }
            pnlManager.getFormData().getLayout("Top").add(pnlSysInfo, layoutCons);
            pnlSysInfo.setServer((DDTPServerMonitor) obj);
        } else {
            if (pnlSysInfo != null) {
                pnlSysInfo.setServer(null);
            }
        }*/
    }

    /**
     * @param thread DDTPThreadMonitor
     */
    public void showThreadEditor(DDTPThreadMonitor thread) {
        lockAction.lock();
        try {
            showEditor(thread.getDDTPServer(), false);

            if (pnlThread == null) {
                pnlThread = new PanelThreadMonitor((ThreadEditor) Register.getMonitorEditorForObject(thread));
            }

            if (lastThread != thread) {
                pnlManager.getFormData().getLayout("Top").removeAll();
                pnlManager.getFormData().getLayout("Top").setLayout(layout);
                pnlManager.getFormData().getLayout("Top").add(pnlThread, layoutCons);
                JPanel pnlExtension = (JPanel) pnlThread.getFormData().getLayout("Extension");
                pnlExtension.removeAll();
                Map mpExtras = thread.getExtras();
                if (mpExtras != null && mpExtras.containsKey("extension_command")) {
                    String[][] strExtension = (String[][]) mpExtras.get("extension_command");
                    if (strExtension != null && strExtension.length > 0) {
                        for (String[] ext : strExtension) {
                            String command = ext[0];
                            String description = ext[1];
                            JButton btn = new JButton(description);
                            btn.addActionListener(new MyActionListener(thread, command));
                            pnlExtension.add(btn);

                        }
                    }
                }

                if (lastThread != null) {
                    lastThread.getListLog().removeTextChangeListener(this);
                }
                lastThread = thread;
                lastThread.getListLog().removeTextChangeListener(this);
                updateThreadLog(thread);
                lastThread.getListLog().addTextChangeListener(this);
                pnlThread.updateStatus();
            }
        } catch (Exception ex) {
            model.getMonitorManager().showException(ex);
        } finally {
            lockAction.unlock();
        }
    }

    /**
     * updateThreadLog
     *
     * @param thread DDTPThreadMonitor
     */
    private void updateThreadLog(final DDTPThreadMonitor thread) {
        if (lastThread == thread) {
            lockAction.lock();
            try {
                final SwingTerminal txt = (SwingTerminal) pnlThread.getFormData().getControl("Monitor");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        txt.clear();
                        List<Object> ls = thread.getListLog().getList();
                        txt.append(ls);
                        txt.scrollToLast();
                    }
                });
            } finally {
                lockAction.unlock();
            }
        }
    }

    /**
     * updateAction
     *
     * @param obj DDTPServerMonitor
     */
    private void updateAction(DDTPServerMonitor obj) {
        if (lastServer == obj && pnlManager != null) {
            lockAction.lock();
            try {
                ((SwingTerminal) pnlManager.getFormData().getControl("Board")).clear();

                List<Object> ls = obj.getLogAction().getList();
                SwingTerminal board = (SwingTerminal) pnlManager.getFormData().getControl("Board");
                board.append(ls);
                board.scrollToLast();
            } finally {
                lockAction.unlock();
            }
        }
    }

    /**
     * @param obj DDTPServerMonitor
     */
    private void updateNotify(DDTPServerMonitor obj) {
        if (lastServer == obj && pnlManager != null && obj != null) {
            lockAction.lock();
            try {
                pnlManager.updateNotify(obj.getNotifyList());
            } finally {
                lockAction.unlock();
            }
        }
    }

    /**
     * updateSession
     *
     * @param obj DDTPServerMonitor
     */
    private void updateSession(DDTPServerMonitor obj) {
        if (lastServer == obj && pnlManager != null) {
            lockAction.lock();
            try {
                ((VectorTable) pnlManager.getFormData().getControl("SessionList")).setData(obj.getSessions());
            } finally {
                lockAction.unlock();
            }
        }
    }

    /**
     * @param e TextChangeEvent
     */
    public void textChanged(TextChangeEvent e) {
        if (pnlManager == null) {
            return;
        }
        Object obj = e.getSource();
        lockAction.lock();
        try {
            if (obj instanceof DDTPServerMonitor && obj == lastServer) {
                pnlManager.showResult((SwingTerminal) pnlManager.getFormData().getControl("Board"), e.getAppendedText());
            } else if (obj instanceof DDTPThreadMonitor && obj == lastThread) {
                if (pnlThread == null) {
                    return;
                }
                pnlManager.showResult((SwingTerminal) pnlThread.getFormData().getControl("Monitor"), e.getAppendedText());
                pnlThread.updateStatus();
            }
        } finally {
            lockAction.unlock();
        }
    }

    public Icon onGetCustomIcon(MonitorObject obj, boolean selected, boolean expanded, boolean leaf,
                                boolean hasFocus) {
        return iconApp;
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }

    public JPanel getPropertiesPanel(MonitorObject mo) {
        DDTPServerMonitor ddtpMo = (DDTPServerMonitor) mo;
        if (pnlProperties == null) {
            pnlProperties = new JTableContainer(tableProperties);
            tableProperties.addColumn("Name", 0, false);
            tableProperties.addColumn("Value", 1, false);
        }
        Vector vtData = new Vector();
        vtData.add(newNameValue("Title", ddtpMo.toString()));
        vtData.add(newNameValue("Host", ddtpMo.getIP()));
        vtData.add(newNameValue("Port", ddtpMo.getPort()));
        vtData.add(newNameValue("Protocol", getProtocolName(ddtpMo.getProtocol())));
        vtData.add(newNameValue("PE method", ddtpMo.getEncryptMethod()));
        if (ddtpMo.isActive()) {
            vtData.add(newNameValue("Username", ddtpMo.getUser()));
            vtData.add(newNameValue("Application", ddtpMo.getAppName()));
            vtData.add(newNameValue("Version", ddtpMo.getAppVersion()));
        }
        tableProperties.setData(vtData);
        return pnlProperties;
    }

    class MyActionListener implements ActionListener {
        private String command;
        private DDTPThreadMonitor thread;

        public MyActionListener(DDTPThreadMonitor thread, String command) {
            this.command = command;
            this.thread = thread;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                thread.invokeExtension(command);
            } catch (Exception ex) {
                model.getMonitorManager().showException(ex);
            }
        }

    }
}
