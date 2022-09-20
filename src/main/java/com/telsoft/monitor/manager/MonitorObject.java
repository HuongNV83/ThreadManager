package com.telsoft.monitor.manager;

import java.util.*;

import javax.swing.tree.*;

import smartlib.dictionary.*;
import smartlib.util.*;

public abstract class MonitorObject implements TreeObject<MonitorObject>
{
	protected ManagerModel model = null;
	private final Map<String,Object> mmMap = new LinkedHashMap<String,Object>();
	private boolean mbGroup = false;
	private final Vector<String> mbGroupKeys = new Vector<String>();
	private DefaultMutableTreeNode treeNode;
	private MonitorObject defParent;

	/**
	 *
	 * @param model ManagerModel
	 * @param groupType boolean
	 */
	public MonitorObject(ManagerModel model,boolean groupType)
	{
		this.model = model;
		if(groupType)
		{
			setAttribute(MonitorObject.class.getName(),this.getClass());
		}
	}

	public abstract boolean isActive();

	public abstract void setActive(boolean bActive) throws Exception;

	/**
	 *
	 * @param node DictionaryNode
	 */
	public void store(DictionaryNode node)
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0;i < getGroupKeys().size();i++)
		{
			buf.append(getGroupKeys().get(i)).append(",");
		}
		if(buf.length() > 0)
		{
			buf.deleteCharAt(buf.length() - 1);
		}
		node.setString("$GroupKeys",buf.toString());
	}

	/**
	 *
	 * @param node DictionaryNode
	 */
	public void load(DictionaryNode node)
	{
		String strGroupKeys = node.getString("$GroupKeys");
		Vector vtGroupKey = StringUtil.toStringVector(strGroupKeys,",");
		setGroup(vtGroupKey);
	}

	/**
	 *
	 * @return boolean
	 */
	public boolean hasChildObject()
	{
		return false;
	}

	/**
	 *
	 * @return int
	 */
	public int getChildCount()
	{
		return 0;
	}

	/**
	 *
	 * @param index int
	 * @return MonitorObject
	 */
	public MonitorObject getChild(int index)
	{
		return null;
	}

	/**
	 *
	 * @return MonitorObject
	 */
	public MonitorObject getParent()
	{
		return defParent;
	}

	/**
	 *
	 * @param parent MonitorObject
	 */
	public void setParent(MonitorObject parent)
	{
		defParent = parent;
	}

	/**
	 *
	 * @return Map
	 */
	public Map getAttributes()
	{
		return mmMap;
	}

	/**
	 *
	 * @param strKey String
	 * @param strValue Object
	 */
	public void setAttribute(String strKey,Object strValue)
	{
		mmMap.put(strKey,strValue);
	}

	public void setAttributes(Map map)
	{
		mmMap.clear();
		mmMap.putAll(map);
	}

	/**
	 *
	 * @param strKey String
	 * @return Object
	 */
	public Object getAttribute(String strKey)
	{
		return mmMap.get(strKey);
	}

	/**
	 *
	 * @param child MonitorObject
	 * @return int
	 */
	public int indexOfChild(MonitorObject child)
	{
		return -1;
	}

	/**
	 *
	 * @param iAction int
	 */
	public void doAction(int iAction)
	{
	}

	/**
	 *
	 * @param mo MonitorObject
	 * @return boolean
	 */
	public boolean isEqual(MonitorObject mo)
	{
		return false;
	}

	/**
	 *
	 * @param obj Object
	 * @return boolean
	 */
	public boolean equals(Object obj)
	{
		if(obj instanceof MonitorObject)
		{
			return isEqual((MonitorObject)obj);
		}
		else
		{
			return false;
		}
	}

	/**
	 * getModel
	 *
	 * @return Object
	 */
	public ManagerModel getModel()
	{
		return model;
	}

	/**
	 *
	 * @return int
	 */
	public int getIndex()
	{
		MonitorObject obj = getParent();
		if(obj == null)
		{
			return -1;
		}
		else
		{
			return obj.indexOfChild(this);
		}
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
	 * @param mpKeys Vector
	 */
	public void setGroup(Vector<String> mpKeys)
	{
		mbGroupKeys.clear();
		if(mpKeys != null)
		{
			mbGroupKeys.addAll(mpKeys);
		}
		mbGroup = mbGroupKeys.size() > 0;
	}

	/**
	 *
	 * @return Map
	 */
	public Vector getGroupKeys()
	{
		return mbGroupKeys;
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
