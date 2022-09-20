package com.telsoft.monitor.manager;

import java.util.*;
import java.util.List;

import java.awt.*;
import javax.swing.*;

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
public abstract class MonitorEditor
{
	protected ManagerModel model = null;

	/**
	 *
	 * @param model ManagerModel
	 */
	public MonitorEditor(ManagerModel model)
	{
		this.model = model;
	}

	/**
	 * connect
	 *
	 * @param root Component
	 * @param obj MonitorObject
	 * @return MonitorObject
	 */
	public MonitorObject connect(Component root,MonitorObject obj,boolean showDialog)
	{
		return null;
	}

	/**
	 *
	 * @param root Component
	 * @param model ManagerModel
	 * @return MonitorObject
	 */
	public MonitorObject connectNew(Component root,ManagerModel model,MonitorObject copyFrom)
	{
		return null;
	}

	/**
	 * getContextMenu
	 *
	 * @param obj MonitorObject
	 * @return JPopupMenu
	 */
	public List<Component> getContextMenu(MonitorObject obj)
	{
		return new Vector<Component>();
	}

	/**
	 * onContextMenu
	 *
	 * @param obj MonitorObject
	 */
	public void onContextMenu(MonitorObject obj)
	{
	}

	/**
	 * onMenuAction
	 *
	 * @param obj MonitorObject
	 * @param id int
	 * @throws Exception
	 */
	public boolean onMenuAction(MonitorObject obj,int id) throws Exception
	{
		return false;
	}

	/**
	 * getContextMenu
	 *
	 * @param obj MonitorObject
	 * @return JPopupMenu
	 */
	public List<Component> getMContextMenu(MonitorObject ...obj)
	{
		return new Vector<Component>();
	}

	/**
	 * onContextMenu
	 *
	 * @param obj MonitorObject
	 */
	public void onMContextMenu(MonitorObject ...obj)
	{
	}

	/**
	 * onMenuAction
	 *
	 * @param obj MonitorObject
	 * @param id int
	 * @throws Exception
	 */
	public boolean onMMenuAction(int id,MonitorObject ...obj) throws Exception
	{
		return false;
	}

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
	public Icon onGetCustomIcon(MonitorObject obj,boolean selected,boolean expanded,boolean leaf,boolean hasFocus)
	{
		return null;
	}

	/**
	 *
	 * @param obj MonitorObject
	 * @param iType int
	 * @param selected boolean
	 * @param expanded boolean
	 * @param leaf boolean
	 * @param row int
	 * @param hasFocus boolean
	 * @return Color
	 */
	public Color onCustomColor(MonitorObject obj,int iType,boolean selected,boolean expanded,boolean leaf,boolean hasFocus)
	{
		switch(iType)
		{
			case 0:
				return(selected ? SystemColor.textHighlight : SystemColor.white);
			case 1:
				return(selected ? SystemColor.textHighlightText : SystemColor.controlText);
		}
		return SystemColor.text;
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
	 * showEditor
	 *
	 * @param obj MonitorObject
	 */
	public void showEditor(MonitorObject obj)
	{
	}

	public boolean isCustomRenderer()
	{
		return false;
	}

	public Component getRendererComponent(Component root,MonitorObject mo,boolean selected,boolean expanded,
										  boolean leaf,boolean hasFocus)
	{
		return null;
	}

	public JPanel getPropertiesPanel(MonitorObject mo)
	{
		return null;
	}
}
