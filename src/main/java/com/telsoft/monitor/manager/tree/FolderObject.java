package com.telsoft.monitor.manager.tree;

import java.lang.reflect.*;
import java.util.*;

import smartlib.dictionary.*;
import com.telsoft.monitor.manager.*;
import smartlib.util.*;

public class FolderObject extends MonitorObject
{
	private MonitorObjectList list = new MonitorObjectList();
	private String mstrName;

	public FolderObject(ManagerModel model,boolean groupType)
	{
		super(model,groupType);
	}

	public FolderObject(ManagerModel model)
	{
		super(model,false);
	}

	public boolean hasChildObject()
	{
		return list.size() > 0;
	}

	public int getChildCount()
	{
		return list.size();
	}

	/**
	 * getChild
	 *
	 * @param index int
	 * @return Object
	 */
	public MonitorObject getChild(int index)
	{
		return list.getChild(index);
	}

	/**
	 * indexOfChild
	 *
	 * @param child Object
	 * @return int
	 */
	public int indexOfChild(MonitorObject child)
	{
		return list.indexOfChild(child);
	}

	/**
	 * isActive
	 *
	 * @return boolean
	 */
	public boolean isActive()
	{
		return true;
	}

	/**
	 * setActive
	 *
	 * @param bActive boolean
	 * @throws Exception
	 */
	public void setActive(boolean bActive) throws Exception
	{
	}

	public MonitorObjectList getList()
	{
		return list;
	}

	public void load(DictionaryNode parentNode)
	{
		mstrName = parentNode.getString("Name");
		Vector vtNode = parentNode.getChildList();
		for(int i = 0;i < vtNode.size();i++)
		{
			try
			{
				DictionaryNode node = (DictionaryNode)vtNode.elementAt(i);
				String strClass = StringUtil.nvl(node.getString("Class"),"");
				if(strClass.equals(""))
				{
					continue;
				}
				if(strClass.equals("com.fss.monitor.ddtp.DDTPServerMonitor"))
				{
					strClass = "com.telsoft.monitor.ddtp.DDTPServerMonitor";
				}
				if(strClass.equals("com.telsoft.monitor.ddtp.GatewayMonitor"))
				{
					strClass = "com.telsoft.monitor.ddtp.DDTPServerMonitor";
				}
				Class clz = Class.forName(strClass);
				Constructor cnt = clz.getConstructor(new Class[]
					{ManagerModel.class});
				MonitorObject mObj = (MonitorObject)cnt.newInstance(new Object[]
					{model});
				mObj.setParent(this);
				mObj.load(node);
				list.add(mObj);
			}
			catch(Exception ex1)
			{
				ex1.printStackTrace();
			}
		}
	}

	/**
	 *
	 * @param parentNode DictionaryNode
	 */
	public void store(DictionaryNode parentNode)
	{
		parentNode.setString("Name",mstrName);
		for(int i = 0;i < list.size();i++)
		{
			MonitorObject mObj = list.get(i);
			DictionaryNode node = new DictionaryNode();
			mObj.store(node);
			node.mstrName = "Object" + i;
			node.setString("Class",mObj.getClass().getName());
			parentNode.addChild(node);
		}
	}

	public String getName()
	{
		if(mstrName == null)
		{
			return "";
		}
		else
		{
			return mstrName;
		}
	}

	public void setName(String strName)
	{
		mstrName = strName;
	}

	public boolean isEqual(MonitorObject mo)
	{
		if(mo instanceof FolderObject)
		{
			FolderObject a = (FolderObject)mo;
			if(a.getName().equalsIgnoreCase(mstrName))
			{
				return true;
			}
		}
		return false;
	}

	public String toString()
	{
		return getName();
	}

}
