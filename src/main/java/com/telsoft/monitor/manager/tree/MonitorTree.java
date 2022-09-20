package com.telsoft.monitor.manager.tree;

import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.manager.util.AppContext;
import com.telsoft.monitor.manager.util.InvokeWithMonitorObject;
import com.telsoft.monitor.register.Register;
import com.telsoft.monitor.util.JXMenuItem;
import smartlib.swing.MessageBox;
import smartlib.swing.WindowManager;
import smartlib.util.Global;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MonitorTree extends JTree implements TreeSelectionListener, MonitorManager {
    protected ServerTreeModel model = null;
    protected Component cmpRoot = null;
    protected ActionListener actListen = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            try {
                int id = ((JXMenuItem) e.getSource()).id();
                if (id >= ManagerModel.START_CONNECT_ACTION && id <= ManagerModel.END_CONNECT_ACTION) {
                    Class clz = (Class) ((JXMenuItem) e.getSource()).ex();
                    MonitorEditor editor = Register.getMonitorEditorForClass(clz, model);
                    MonitorObject mo = editor.connectNew(cmpRoot, model, null);
                    if (mo != null) {
                        int i = model.indexOf(mo);
                        if (i == -1) {
                            model.addToManager(mo);
                        } else {
                            throw new Exception("Object " + mo.toString() + " existed, please reconfigure.");
//							mo = (MonitorObject)model.getChild(model.getRoot(),i);
                        }
                        MonitorTree.this.updateUI();
                        new Thread(new InvokeWithMonitorObject(mo, MonitorTree.this.cmpRoot) {
                            public void run() {
                                try {
                                    mmo.setActive(true);
                                } catch (Exception ex) {
                                    showException(ex);
                                }
                            }
                        }).start();
                    }
                }
            } catch (Exception ex) {
                showException(ex);
            }
        }
    };
    private CustomTreeCellRenderer cellRender = null;
    private JPopupMenu mnuConnectTo = new JPopupMenu();
    private JPopupMenu popupMenu = null;
    KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            if (popupMenu != null && e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
                popupMenu.show(e.getComponent(), 0, 0);
            }
        }

    };
    MouseListener mouseListener = new MouseAdapter() {
        private void showIfPopupTrigger(MouseEvent mouseEvent) {
            if (popupMenu != null) {
                if (popupMenu.isPopupTrigger(mouseEvent)) {
                    popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                }
            }
        }

        public void mousePressed(MouseEvent mouseEvent) {
            if (mouseEvent.getButton() == mouseEvent.BUTTON3) {
                MonitorTree mt = MonitorTree.this;
                TreePath tp = mt.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
                if (tp != null) {
                    if (!mt.isPathSelected(tp)) {
                        mt.setSelectionPath(tp);
                    }
                    showIfPopupTrigger(mouseEvent);
                }
            } else if (mouseEvent.getButton() == mouseEvent.BUTTON1 && mouseEvent.getClickCount() >= 2) {
                MonitorTree mt = MonitorTree.this;
                TreePath tp = mt.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
                if (tp != null && tp.getLastPathComponent() instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();
                    if (userObject != null && userObject instanceof MonitorObject) {
                        MonitorObject mo = (MonitorObject) userObject;
                        if (!mo.isActive()) {
                            try {
                                MonitorEditor me = Register.getMonitorEditorForObject(mo);
                                me.onMenuAction(mo, 0);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        public void mouseReleased(MouseEvent mouseEvent) {
            mousePressed(mouseEvent);
        }
    };
    private JXMenuItem miNewFolder = new JXMenuItem("New folder", 5, null);
    private MonitorObject currentMO = null;
    private MonitorObject[] currentMMO = null;
    private PopupMenuListener ppListen = new PopupMenuListener() {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            Object obj = e.getSource();
            if (obj instanceof JPopupMenu) {
                JPopupMenu pp = (JPopupMenu) obj;
                if (currentMO != null) {
                    try {
                        MonitorEditor me = Register.getMonitorEditorForObject(currentMO);
                        if (currentMMO != null) {
                            me.onMContextMenu(currentMMO);
                        } else {
                            me.onContextMenu(currentMO);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    };
    private ActionListener actNewFolder = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            try {
                TreeObject to = (TreeObject) ((JXMenuItem) e.getSource()).ex();

                DialogNewFolder dlg = new DialogNewFolder(JOptionPane.getFrameForComponent(cmpRoot), "New folder...", true);
                WindowManager.centeredWindow(dlg);
                if (dlg.getModalCode() == JOptionPane.OK_OPTION) {
                    String strFolder = dlg.getFolderName();
                    FolderObject fo = new FolderObject(model, false);
                    fo.setName(strFolder);
                    int i = model.indexOf(fo);
                    if (i == -1) {
                        model.addToManager(fo);
                    } else {
                        fo = (FolderObject) model.getChild(model.getRoot(), i);
                    }
                    MonitorTree.this.updateUI();
                }
            } catch (Exception ex) {
                showException(ex);
            }
        }
    };
    private ActionListener actDefO = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            try {
                Object o = ((JXMenuItem) e.getSource()).ex();
                if (o instanceof MonitorObject[]) {
                    MonitorObject[] mo = (MonitorObject[]) o;
                    int id = ((JXMenuItem) e.getSource()).id();
                    if (id >= JXMenuItem.START_USER_ACTION) {
                        MonitorEditor me = Register.getMonitorEditorForObject(mo[0]);
                        if (me.onMMenuAction(id, mo)) {
                            MonitorTree.this.updateUI();
                        }
                    }
                } else {
                    MonitorObject mo = (MonitorObject) o;
                    int id = ((JXMenuItem) e.getSource()).id();
                    if (id >= JXMenuItem.START_USER_ACTION) {
                        MonitorEditor me = Register.getMonitorEditorForObject(mo);
                        if (me.onMenuAction(mo, id)) {
                            MonitorTree.this.updateUI();
                        }
                    }
                }
            } catch (Exception ex) {
                showException(ex);
            }
        }
    };

    /**
     * @param root Component
     */
    public MonitorTree(Component root, AppContext context) {
        super();
        this.cmpRoot = root;
        setEditable(false);
        setDoubleBuffered(true);

        model = new ServerTreeModel(root, this, context);
        setModel(model);
        miNewFolder.setex(model.getRootData());
        miNewFolder.addActionListener(actNewFolder);

        cellRender = new CustomTreeCellRenderer();
        setCellRenderer(cellRender);

        addTreeSelectionListener(this);
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        if (cmpRoot instanceof AbstractPanelManager) {
            ((AbstractPanelManager) cmpRoot).getConnectMenu().removeAll();
        }

        Vector vt = Register.getMonitorTypes();
        for (int i = 0; i < vt.size(); i++) {
            Register.MonitorInfo moInfor = (Register.MonitorInfo) vt.elementAt(i);

            JXMenuItem mi =
                    new JXMenuItem("Connect to a " + moInfor.strType.toUpperCase() + " server ...", ManagerModel.START_CONNECT_ACTION +
                            i, moInfor.monitorClass);
            mi.addActionListener(actListen);
            mnuConnectTo.add(mi);

            Icon icon = null;
            try {
                MonitorEditor me = Register.getMonitorEditorForClass(moInfor.monitorClass, model);
                icon = me.onGetCustomIcon(null, false, false, false, false);
            } catch (Exception ex) {
            }

            if (icon != null) {
                mi.setIcon(icon);
            }

            if (cmpRoot instanceof AbstractPanelManager) {
                mi =
                        new JXMenuItem("Connect to a " + moInfor.strType + " server ...", ManagerModel.START_CONNECT_ACTION + i,
                                moInfor.monitorClass);
                mi.addActionListener(actListen);
                ((AbstractPanelManager) cmpRoot).getConnectMenu().add(mi);
                if (icon != null) {
                    mi.setIcon(icon);
                }
            }
        }
        mnuConnectTo.addSeparator();
        mnuConnectTo.add(miNewFolder);
        addMouseListener(mouseListener);
        addKeyListener(keyListener);
    }

    public void showException(Exception ex) {
        ex.printStackTrace();
        MessageBox.showMessageDialog(MonitorTree.this.cmpRoot, ex, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
    }

    private void applyMenuListener(JMenu mnu, Object obj, ActionListener actdef) {
        Component[] cmps = mnu.getMenuComponents();
        for (int i = 0; i < cmps.length; i++) {
            if (cmps[i] != null && cmps[i] instanceof JXMenuItem) {
                JXMenuItem mi = (JXMenuItem) cmps[i];
                if (mi.id() >= JXMenuItem.START_USER_ACTION) {
                    mi.removeActionListener(actDefO);
                    mi.addActionListener(actDefO);
                    mi.setex(obj);
                }
            } else if (cmps[i] != null && cmps[i] instanceof JMenu) {
                applyMenuListener((JMenu) cmps[i], obj, actDefO);
            }
        }
    }

    public void treeSelectionChanged(DefaultMutableTreeNode[] mobj) {
        if (mobj.length == 1) {
            DefaultMutableTreeNode obj = mobj[0];
            Object userData = obj.getUserObject();
            popupMenu = null;
            if (userData != null && userData instanceof MonitorObject) {
                MonitorObject mObj = (MonitorObject) userData;
                JPopupMenu mnuItem = new JPopupMenu();
                try {
                    MonitorEditor me = Register.getMonitorEditorForObject(mObj);
                    List<Component> mnuME = me.getContextMenu(mObj);
                    if (mnuME != null) {
                        for (Component cmp : mnuME) {
                            if (cmp != null && cmp instanceof JXMenuItem) {
                                mnuItem.add((JXMenuItem) cmp);
                            } else if (cmp != null && cmp instanceof JPopupMenu.Separator) {
                                mnuItem.add((JPopupMenu.Separator) cmp);
                            } else if (cmp != null && cmp instanceof JMenu) {
                                mnuItem.add((JMenu) cmp);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Component[] cmps = mnuItem.getComponents();
                if (cmps.length <= 0) {
                    currentMMO = null;
                    currentMO = null;
                    popupMenu = null;
                    return;
                }

                for (int i = 0; i < cmps.length; i++) {
                    if (cmps[i] != null && cmps[i] instanceof JXMenuItem) {
                        JXMenuItem mi = (JXMenuItem) cmps[i];
                        if (mi.id() >= JXMenuItem.START_USER_ACTION) {
                            mi.removeActionListener(actDefO);
                            mi.addActionListener(actDefO);
                            mi.setex(mObj);
                        }
                    } else if (cmps[i] != null && cmps[i] instanceof JMenu) {
                        applyMenuListener((JMenu) cmps[i], mObj, actDefO);
                    }
                }
                mnuItem.removePopupMenuListener(ppListen);
                mnuItem.addPopupMenuListener(ppListen);
                popupMenu = mnuItem;

                currentMO = mObj;

                if (getSelectionPath() != null) {
                    Object obj2 = getSelectionPath().getLastPathComponent();
                    if (obj2 != null && obj2 instanceof DefaultMutableTreeNode) {
                        final Object usObject = ((DefaultMutableTreeNode) obj2).getUserObject();
                        if (usObject != null && usObject instanceof MonitorObject) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    model.showEditor((MonitorObject) usObject);
                                }
                            });
                        }
                    }
                }
            } else if (userData != null && userData instanceof ServerTreeModel.GroupData) {
                Object ref = ((ServerTreeModel.GroupData) userData).mRef;
                if (ref != null && ref instanceof DefaultMutableTreeNode) {
                    treeSelectionChanged(new DefaultMutableTreeNode[]
                            {(DefaultMutableTreeNode) ref});
                }
            } else if (obj == model.getRoot()) {
                popupMenu = mnuConnectTo;
            }
        } else {
            popupMenu = null;
            currentMMO = null;
            currentMO = null;

            List<MonitorObject> lsMo = new ArrayList<MonitorObject>();
            Class lastClass = null;
            for (DefaultMutableTreeNode obj : mobj) {
                Object userData = obj.getUserObject();
                if (userData != null && userData instanceof MonitorObject) {
                    if (lastClass == null) {
                        lastClass = userData.getClass();
                    } else {
                        if (!lastClass.isAssignableFrom(userData.getClass()) && !userData.getClass().isAssignableFrom(lastClass)) {
                            return;
                        }
                    }
                    lsMo.add((MonitorObject) userData);
                } else {
                    return;
                }
            }
            if (lsMo.size() <= 0) {
                return;
            }

            JPopupMenu mnuItem = new JPopupMenu();
            try {
                MonitorEditor me = Register.getMonitorEditorForObject(lsMo.get(0));
                currentMMO = new MonitorObject[lsMo.size()];
                lsMo.toArray(currentMMO);

                List<Component> mnuME = me.getMContextMenu(currentMMO);
                if (mnuME != null) {
                    for (Component cmp : mnuME) {
                        if (cmp != null && cmp instanceof JXMenuItem) {
                            mnuItem.add((JXMenuItem) cmp);
                        } else if (cmp != null && cmp instanceof JPopupMenu.Separator) {
                            mnuItem.add((JPopupMenu.Separator) cmp);
                        } else if (cmp != null && cmp instanceof JMenu) {
                            mnuItem.add((JMenu) cmp);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Component[] cmps = mnuItem.getComponents();
            if (cmps.length <= 0) {
                currentMMO = null;
                return;
            }
            for (int i = 0; i < cmps.length; i++) {
                if (cmps[i] != null && cmps[i] instanceof JXMenuItem) {
                    JXMenuItem mi = (JXMenuItem) cmps[i];
                    if (mi.id() >= JXMenuItem.START_USER_ACTION) {
                        mi.removeActionListener(actDefO);
                        mi.addActionListener(actDefO);
                        mi.setex(currentMMO);
                    }
                } else if (cmps[i] != null && cmps[i] instanceof JMenu) {
                    applyMenuListener((JMenu) cmps[i], currentMMO, actDefO);
                }
            }
            mnuItem.removePopupMenuListener(ppListen);
            mnuItem.addPopupMenuListener(ppListen);
            popupMenu = mnuItem;
            currentMO = currentMMO[0];
        }
    }

    /**
     * @param e TreeSelectionEvent
     */
    public void valueChanged(TreeSelectionEvent e) {
        Object obj = e.getSource();
        if (obj instanceof MonitorTree && ((MonitorTree) e.getSource()).getSelectionCount() > 0) {
            DefaultMutableTreeNode[] tn = new DefaultMutableTreeNode[((MonitorTree) e.getSource()).getSelectionCount()];
            TreePath[] tps = ((MonitorTree) e.getSource()).getSelectionPaths();
            for (int i = 0; i < tps.length; i++) {
                TreePath tp = tps[i];
                tn[i] = (DefaultMutableTreeNode) tp.getLastPathComponent();
            }
            treeSelectionChanged(tn);
        }
    }

    /**
     * @return ServerTreeModel
     */
    public ManagerModel getManagerModel() {
        return model;
    }

    /**
     * @return TreeModelListener
     */
    protected TreeModelListener createTreeModelListener() {
        return new NewTreeModelHandler();
    }

    class NewTreeModelHandler extends TreeModelHandler {
        public void treeStructureChanged(TreeModelEvent e) {
            Object[] childs = e.getChildren();
            if (childs != null) {
                TreePath[] chilPaths = new TreePath[childs.length];
                boolean[] saveExpand = new boolean[childs.length];
                TreePath parent = e.getTreePath();
                for (int i = 0; i < childs.length; i++) {
                    chilPaths[i] = parent.pathByAddingChild(childs[i]);
                    saveExpand[i] = MonitorTree.this.isExpanded(chilPaths[i]);
                }
                super.treeStructureChanged(e);
                for (int i = 0; i < childs.length; i++) {
                    if (saveExpand[i]) {
                        MonitorTree.this.expandPath(chilPaths[i]);
                    } else {
                        MonitorTree.this.collapsePath(chilPaths[i]);
                    }
                }
            } else {
                super.treeStructureChanged(e);
            }
        }
    }
}
