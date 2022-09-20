package com.telsoft.monitor.manager.tree;

import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.manager.util.AppContext;
import com.telsoft.monitor.manager.util.ManagerUtil;
import com.telsoft.monitor.register.Register;
import com.telsoft.monitor.util.JXMenuItem;
import smartlib.swing.MessageBox;
import smartlib.util.Global;
import smartlib.util.StringUtil;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.*;


public class ServerTreeModel extends AbstractTreeModel implements ManagerModel {
    private final MonitorObjectList mvtObject = new MonitorObjectList() {
        public String toString() {
            return "Connections";
        }
    };
    private Component root = null;
    private DefaultMutableTreeNode model = new DefaultMutableTreeNode(mvtObject);
    private boolean mbDestroying = false;
    private MonitorManager mm;

    /**
     * @param root Component
     */
    public ServerTreeModel(Component root, MonitorManager mm, AppContext context) {
        super(null, context);
        this.mm = mm;
        setRoot(model);
        this.root = root;
        mvtObject.setContainerNode(model);
        buildModel(model);
    }

    /**
     * @param items  Collection
     * @param vtKeys Collection
     * @param parent DefaultMutableTreeNode
     * @return Collection
     */
    private Collection<GroupData> orderGroup(Collection<TreeObject> items, Collection<String> vtKeys, DefaultMutableTreeNode parent) {
        Collection<GroupData> vtReturn = new Vector<GroupData>();
        Object objUser = parent.getUserObject();
        GroupData gpParent = null;
        if (objUser instanceof GroupData) {
            gpParent = (GroupData) objUser;
        }

        for (TreeObject item : items) {
            Map<String, Object> mpKV = new LinkedHashMap<String, Object>();

            if (gpParent != null) {
                if (!gpParent.matchValues(item)) {
                    continue;
                }
            }

            boolean bMatch = true;
            for (String strKey : vtKeys) {
                Object objValue = item.getAttribute(strKey);
                if (objValue == null) {
                    bMatch = false;
                    break;
                }
                mpKV.put(strKey, objValue);
            }
            if (!bMatch) {
                continue;
            }

            if (mpKV.size() == vtKeys.size()) {
                GroupData gp = new GroupData(parent);
                gp.mpGroupValues.putAll(mpKV);
                if (!vtReturn.contains(gp)) {
                    vtReturn.add(gp);
                }
            }
        }
        return vtReturn;
    }

    /**
     * @param parent      DefaultMutableTreeNode
     * @param vtGroupKeys Vector
     * @param iLevelFrom  int
     * @param iLevelTo    int
     * @param vtItems     Vector
     */
    private void buildGroupModel(DefaultMutableTreeNode parent, Vector<String> vtGroupKeys, int iLevelFrom, int iLevelTo, Vector<TreeObject> vtItems) {
        Vector<String> vtSubKey = new Vector<String>();
        for (int i = iLevelFrom; i <= iLevelTo; i++) {
            vtSubKey.add(vtGroupKeys.get(i));
        }

        Collection<GroupData> vtGroup = orderGroup(vtItems, vtSubKey, parent);

        for (GroupData gp : vtGroup) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(gp);
            if (vtGroupKeys.size() > iLevelTo + 1) {
                buildGroupModel(node, vtGroupKeys, iLevelFrom, iLevelTo + 1, vtItems);
            }

            for (int j = vtItems.size() - 1; j >= 0; j--) {
                TreeObject item = vtItems.get(j);
                if (gp.matchValues(vtSubKey, item)) {
                    vtItems.remove(j);
                    DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(item);
                    item.setContainerNode(node2);
                    node.add(node2);
                    buildModel(node2);
                }
            }
            if (node.getChildCount() > 0) {
                parent.add(node);
            }
        }

        // Not in group
        if (vtItems.size() > 0) {
            Object userData = parent.getUserObject();
            GroupData gpParent = null;
            if (userData instanceof GroupData) {
                gpParent = (GroupData) userData;
            }

            for (int i = vtItems.size() - 1; i >= 0; i--) {
                TreeObject item = vtItems.get(i);
                if (gpParent == null || gpParent.matchValues(item)) {
                    vtItems.remove(i);
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
                    item.setContainerNode(node);
                    parent.add(node);
                    if (item.getChildCount() > 0) {
                        buildModel(node);
                    }
                }
            }
//			if (nodeOther.getChildCount() > 0)
//				parent.add(nodeOther);
        }
    }

    /**
     * @param parent DefaultMutableTreeNode
     */
    public void buildModel(DefaultMutableTreeNode parent) {
        parent.removeAllChildren();
        Object obj = parent.getUserObject();
        if (obj != mvtObject && !(obj instanceof TreeObject)) {
            return;
        }

        TreeObject<TreeObject> to = (TreeObject) obj;
        if (to.getChildCount() <= 0) {
            return;
        }

        if (to.isGrouped()) {
            Vector<TreeObject> vtOther = new Vector<TreeObject>();

            for (int i = to.getChildCount() - 1; i >= 0; i--) {
                vtOther.add(to.getChild(i));
            }

            Vector<String> vtGroupKeys = to.getGroupKeys();
            buildGroupModel(parent, vtGroupKeys, 0, 0, vtOther);
        } else {
            for (int i = 0; i < to.getChildCount(); i++) {
                TreeObject item = to.getChild(i);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
                item.setContainerNode(node);
                parent.add(node);
                buildModel(node);
            }
        }
    }

    /**
     *
     */
    public void loadConfig() {
        mvtObject.clear();
        nodesChanged(model, null);
        nodeStructureChanged(model);
        ManagerUtil.loadConfig(mvtObject, this, this.getContext().getConfigFile());
        buildModel(model);
        nodeChanged(model);
        nodeStructureChanged(model);
    }

    /**
     *
     */
    public void storeConfig() {
        ManagerUtil.storeConfig(mvtObject, this.getContext().getConfigFile());
    }

    /**
     * @return Component
     */
    public Component getRootComponent() {
        return root;
    }

    /**
     * @param id
     * @param objSelected
     * @throws Exception
     */
    public void action(int id, Object objSelected) throws Exception {
        if (objSelected instanceof MonitorObject) {
            ((MonitorObject) objSelected).doAction(id);
        }
    }

    /**
     * @param obj         Object
     * @param bParentOnly boolean
     * @return Object[]
     */
    public Object[] getPathToRoot(Object obj, boolean bParentOnly) {
        if (obj instanceof TreeNode) {
            return ((DefaultMutableTreeNode) obj).getPath();
        } else if (obj instanceof MonitorObject) {
            MonitorObject mo = (MonitorObject) obj;
            return mo.getContainerNode().getPath();
        } else {
            return null;
        }
    }

    /**
     * disconnectAll
     */
    public void disconnectAll() {
        mbDestroying = true;
        try {
            for (MonitorObject srInfor : mvtObject) {
                try {
                    srInfor.setActive(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            mbDestroying = false;
        }
    }

    /**
     * @param obj MonitorObject
     */
    public void showEditor(MonitorObject obj) {
        try {
            MonitorEditor me = Register.getMonitorEditorForObject(obj);
            me.showEditor(obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * addToTree
     *
     * @param mo MonitorObject
     */
    public void addToManager(MonitorObject mo) {
        mvtObject.add(mo);
        buildModel(model);
        storeConfig();
    }

    /**
     * removeFromTree
     *
     * @param mo MonitorObject
     */
    public void removeFromManager(MonitorObject mo) {
        mvtObject.remove(mo);
        buildModel(model);
        storeConfig();
    }

    /**
     * @param mo MonitorObject
     * @return int
     */
    public int indexOf(MonitorObject mo) {
        return mvtObject.indexOf(mo);
    }

    /**
     * @param mo MonitorObject
     * @param id int
     */
    public void doEditorAction(MonitorObject mo, int id) {
        try {
            if (id >= JXMenuItem.START_USER_ACTION) {
                MonitorEditor me = Register.getMonitorEditorForObject(mo);
                me.onMenuAction(mo, id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.showMessageDialog(root, ex, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
        }
    }

    /**
     * @param objSender MonitorObject
     * @param objTarget MonitorObject
     */
    public void notifyItemChanged(MonitorObject objSender, MonitorObject objTarget) {
        notifyItemChanged(objSender, objTarget, false);
    }

    /**
     * @param objSender MonitorObject
     * @param objTarget MonitorObject
     */
    public void notifyItemAdded(MonitorObject objSender, MonitorObject objTarget) {
        if (mbDestroying) {
            return;
        }
        buildModel(objSender.getContainerNode());
        nodeStructureChanged(objSender.getContainerNode());
    }

    /**
     * @param objSender MonitorObject
     * @param objTarget MonitorObject
     */
    public void notifyItemRemoved(MonitorObject objSender, MonitorObject objTarget) {
        if (mbDestroying) {
            return;
        }
        DefaultMutableTreeNode nodeRemoved = objTarget.getContainerNode();
        removeNodeFromParent(nodeRemoved);
    }

    /**
     * @param objSender MonitorObject
     */
    public void notifyListChanged(MonitorObject objSender) {
        if (mbDestroying) {
            return;
        }
        buildModel(objSender.getContainerNode());
        nodeStructureChanged(objSender.getContainerNode());
    }

    public MonitorObjectList getRootData() {
        return mvtObject;
    }

    public void notifyItemChanged(MonitorObject objSender, MonitorObject objTarget, boolean shouldRebuild) {
        if (mbDestroying) {
            return;
        }
        if (shouldRebuild) {
            buildModel(objSender.getContainerNode());
        }
        nodeChanged(objTarget.getContainerNode());
    }

    public MonitorManager getMonitorManager() {
        return mm;
    }

    public MonitorObject[] getMonitorObjects() {
        MonitorObject[] res = new MonitorObject[mvtObject.size()];
        return mvtObject.toArray(res);
    }

    public void showMessage(String str) {
        MessageBox.showMessageDialog(root, str);
    }

    public static class GroupData {
        final Map<String, Object> mpGroupValues = new LinkedHashMap<String, Object>();
        final Object mRef;

        public GroupData(Object objRef) {
            mRef = objRef;
        }

        public boolean equals(Object obj) {
            if (obj instanceof GroupData) {
                GroupData gp = (GroupData) obj;
                return mpGroupValues.equals(gp.mpGroupValues);
            }
            return false;
        }

        public String toString() {
            Iterator iterValues = mpGroupValues.values().iterator();
            Object objValue = null;
            while (iterValues.hasNext()) {
                objValue = iterValues.next();
            }

            if (objValue == null) {
                return "(null)";
            }

            if (objValue instanceof Class) {
                Class clz = (Class) objValue;
                if (MonitorObject.class.isAssignableFrom(clz)) {
                    objValue = Register.getTypeForClass(clz);
                }
            }
            return objValue.toString();
        }

        /**
         * @param vtKeys Vector
         * @param item   TreeObject
         * @return boolean
         */
        public boolean matchValues(Collection<String> vtKeys, TreeObject item) {
            if (vtKeys.size() != mpGroupValues.size()) {
                return false;
            }

            for (String strKey : vtKeys) {
                if (!mpGroupValues.containsKey(strKey)) {
                    return false;
                }

                String objValueA = StringUtil.nvl(mpGroupValues.get(strKey), "");
                String objValueB = StringUtil.nvl(item.getAttribute(strKey), "");
                if (objValueA.equals("") || objValueB.equals("")) {
                    return false;
                }

                if (!objValueA.equals(objValueB)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * @param item TreeObject
         * @return boolean
         */
        public boolean matchValues(TreeObject item) {
            Collection<String> vtKeys = mpGroupValues.keySet();
            return matchValues(vtKeys, item);
        }
    }
}
