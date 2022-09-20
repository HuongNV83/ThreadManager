package com.telsoft.monitor.manager;

import java.awt.*;

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
public interface ManagerModel {
    int START_CONNECT_ACTION = 1000;
    int END_CONNECT_ACTION = 1100;

    Component getRootComponent();

    void doEditorAction(MonitorObject mo, int id);

    void showEditor(MonitorObject obj);

    void storeConfig();

    void loadConfig();

    void disconnectAll();

    void notifyItemChanged(MonitorObject objSender, MonitorObject objTarget, boolean shouldRebuild);

    void notifyItemChanged(MonitorObject objSender, MonitorObject objTarget);

    void notifyItemAdded(MonitorObject objSender, MonitorObject objTarget);

    void notifyItemRemoved(MonitorObject objSender, MonitorObject objTarget);

    void notifyListChanged(MonitorObject objSender);

    void addToManager(MonitorObject mo);

    void removeFromManager(MonitorObject mo);

    void action(int id, Object objSelected) throws Exception;

    MonitorManager getMonitorManager();

    MonitorObject[] getMonitorObjects();

    void showMessage(String str);
}
