package com.telsoft.monitor.manager;

import java.util.*;

import javax.swing.tree.*;

/**
 *
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
public interface TreeObject<E>
{
	public boolean hasChildObject();
	public int getChildCount();
	public E getChild(int index);
	public E getParent();
	public void setParent(E parent);
	public Map getAttributes();
	public void setAttribute(String strKey, Object strValue);
	public Object getAttribute(String strKey);
	public int indexOfChild(E child);
	public int getIndex();
	public boolean isGrouped();
	public Vector<String> getGroupKeys();
	public void setContainerNode(DefaultMutableTreeNode node);
	public DefaultMutableTreeNode getContainerNode();
	public void setGroup(Vector<String> mpKeys);
}
