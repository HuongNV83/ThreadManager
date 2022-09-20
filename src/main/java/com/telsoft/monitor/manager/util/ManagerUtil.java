package com.telsoft.monitor.manager.util;

import smartlib.dictionary.Dictionary;
import smartlib.dictionary.DictionaryNode;
import com.telsoft.monitor.manager.ManagerModel;
import com.telsoft.monitor.manager.MonitorObject;
import smartlib.util.FileUtil;
import smartlib.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Vector;

public final class ManagerUtil {
    static {
        File fl = new File("config.key");
        if (!fl.exists()) {
            try {
                PasswordLoader.makeKey(fl.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     */
    public static void loadConfig(Vector<MonitorObject> mvtObject, ManagerModel model, String strFileConfig) {
        synchronized (mvtObject) {
            mvtObject.clear();
            Dictionary config = null;
            FileInputStream is = null;
            try {
                is = new FileInputStream(strFileConfig);
                config = new Dictionary(is);
            } catch (IOException ex) {
                ex.printStackTrace();
                config = new Dictionary();
            } finally {
                FileUtil.safeClose(is);
            }
            Vector vtNode = config.getChildList();
            for (int i = 0; i < vtNode.size(); i++) {
                try {
                    DictionaryNode node = (DictionaryNode) vtNode.elementAt(i);
                    String strClass = StringUtil.nvl(node.getString("Class"), "");
                    if (strClass.equals("")) {
                        continue;
                    }
                    if (strClass.equals("com.fss.monitor.ddtp.DDTPServerMonitor")) {
                        strClass = "com.telsoft.monitor.ddtp.DDTPServerMonitor";
                    }
                    if (strClass.equals("com.telsoft.monitor.ddtp.GatewayMonitor")) {
                        strClass = "com.telsoft.monitor.ddtp.DDTPServerMonitor";
                    }
                    Class clz = Class.forName(strClass);
                    Constructor cnt = clz.getConstructor(new Class[]
                            {ManagerModel.class});
                    MonitorObject mObj = (MonitorObject) cnt.newInstance(new Object[]
                            {model});
                    mObj.load(node);
                    mvtObject.add(mObj);
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
        }
    }

    /**
     *
     */
    public static void storeConfig(Vector<MonitorObject> mvtObject, String strFileConfig) {
        synchronized (mvtObject) {
            Dictionary config = new Dictionary();
            for (int i = 0; i < mvtObject.size(); i++) {
                MonitorObject mObj = mvtObject.get(i);
                DictionaryNode node = new DictionaryNode();
                mObj.store(node);
                node.mstrName = "Object" + i;
                node.setString("Class", mObj.getClass().getName());
                config.addChild(node);
            }
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(strFileConfig);
                config.store(os);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                FileUtil.safeClose(os);
            }
        }
    }
}
