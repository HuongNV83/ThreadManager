package com.telsoft.monitor.manager.tree;

import java.util.*;

import javax.swing.tree.*;

import com.telsoft.monitor.manager.*;

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
public class MonitorObjectList extends Vector<MonitorObject> implements TreeObject<MonitorObject>
{
	private boolean mbGroup = false;
	private final Vector<String> mvtGroupKeys = new Vector<String>();
	private DefaultMutableTreeNode treeNode;

	public MonitorObjectList()
	{
	}

	public boolean hasChildObject()
	{
		return getChildCount() > 0;
	}

	public int getChildCount()
	{
		return size();
	}

	public MonitorObject getChild(int index)
	{
		return get(index);
	}

	public MonitorObject getParent()
	{
		return null;
	}

	public void setParent(MonitorObject parent)
	{
	}

	public Map getAttributes()
	{
		return null;
	}

	public void setAttribute(String strKey,Object strValue)
	{
	}

	public MonitorObject getAttribute(String strKey)
	{
		return null;
	}

	public int indexOfChild(MonitorObject child)
	{
		return indexOf(child);
	}

	public int getIndex()
	{
		return 0;
	}

	/**
	 *
	 * @return boolean
	 */
	public boolean isGrouped()
	{
		return mbGroup;
	}

	/**
	 *
	 * @param bGroup boolean
	 * @param mpKeys Map
	 */
	public void setGroup(Vector<String> mpKeys)
	{
		mvtGroupKeys.clear();
		if (mpKeys != null)
			mvtGroupKeys.addAll(mpKeys);
		mbGroup = mvtGroupKeys.size() > 0;
	}

	/**
	 *
	 * @return Map
	 */
	public Vector<String> getGroupKeys()
	{
		return mvtGroupKeys;
	}

	public void setContainerNode(DefaultMutableTreeNode node)
	{
		treeNode = node;
	}

	public DefaultMutableTreeNode getContainerNode()
	{
		return treeNode;
	}
}
