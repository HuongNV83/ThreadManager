package com.telsoft.monitor.manager.tree;

import java.util.List;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.util.*;
import com.telsoft.monitor.util.JXMenuItem;
import smartlib.swing.*;
import smartlib.util.*;

public class FolderEditor extends MonitorEditor {
    private List<Component> mnuContext = null;
    private JXMenuItem miConnectToAll = null;
    private JXMenuItem miDisconnectToAll = null;
    private JXMenuItem miRemove = null;

    private static Icon iconGroup =
            new ImageIcon(FileUtil.getResource("resource/com/telsoft/monitor/ddtp/icon/group.png"));

    public FolderEditor(ManagerModel model) {
        super(model);
    }

    public Icon onGetCustomIcon(MonitorObject obj, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return iconGroup;
    }

    public List<Component> getContextMenu(MonitorObject obj) {
        if (mnuContext == null) {
            mnuContext = super.getContextMenu(obj);
            miConnectToAll = new JXMenuItem("Connect to all...", JXMenuItem.START_USER_ACTION + 1, obj);
            miDisconnectToAll = new JXMenuItem("Disconnect from all...", JXMenuItem.START_USER_ACTION + 2, obj);
            miRemove = new JXMenuItem("Remove folder", JXMenuItem.START_USER_ACTION + 3, obj);
            mnuContext.add(miConnectToAll);
            mnuContext.add(miDisconnectToAll);
            mnuContext.add(new JPopupMenu.Separator());
            mnuContext.add(miRemove);
        }
        return mnuContext;
    }

    public boolean onMenuAction(MonitorObject obj, int id) throws Exception {
        FolderObject fo = (FolderObject) obj;
        switch (id) {
            case JXMenuItem.START_USER_ACTION + 1: {
                int n = fo.getChildCount();
                for (int i = 0; i < n; i++) {
                    final MonitorObject mo = fo.getChild(i);
                    if (!mo.isActive()) {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    mo.setActive(true);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    String strMessage = "Connecting to " + mo.toString() + "...";
                                    MessageBox.showMessageDialog(model.getRootComponent(), ex, strMessage, MessageBox.ERROR_MESSAGE);
                                }
                            }
                        }).start();
                    }
                }
                break;
            }
            case JXMenuItem.START_USER_ACTION + 2: {
                if (Msg.confirm("Disconnect from all?")) {
                    int n = fo.getChildCount();
                    for (int i = 0; i < n; i++) {
                        final MonitorObject mo = fo.getChild(i);
                        if (mo.isActive()) {
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        mo.setActive(false);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        String strMessage = "Disconnecting to " + mo.toString() + "...";
                                        MessageBox.showMessageDialog(model.getRootComponent(), ex, strMessage, MessageBox.ERROR_MESSAGE);
                                    }
                                }
                            }).start();
                        }
                    }
                }
                break;
            }
            case JXMenuItem.START_USER_ACTION + 3: {
                if (Msg.confirm("Remove folder with all connections inside?")) {
                    int n = fo.getChildCount();
                    for (int i = 0; i < n; i++) {
                        MonitorObject mo = fo.getChild(i);
                        if (mo.isActive()) {
                            mo.setActive(false);
                        }
                    }
                    fo.getList().clear();
                    ((ServerTreeModel) model).removeFromManager(fo);
                    ((ServerTreeModel) model).buildModel((DefaultMutableTreeNode) ((ServerTreeModel) model).getRoot());
                    model.storeConfig();
                    return true;
                }
            }
        }
        return false;
    }

    public void onContextMenu(MonitorObject obj) {
        FolderObject fo = (FolderObject) obj;
        int n = fo.getChildCount();
        boolean bConnect = false;
        boolean bDisconnect = false;

        for (int i = 0; i < n; i++) {
            bConnect = !fo.getChild(i).isActive() || bConnect;
            bDisconnect = fo.getChild(i).isActive() || bDisconnect;
        }
        miConnectToAll.setEnabled(bConnect);
        miDisconnectToAll.setEnabled(bDisconnect);
    }

}
