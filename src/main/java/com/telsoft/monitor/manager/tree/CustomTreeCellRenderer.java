package com.telsoft.monitor.manager.tree;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.register.*;
import smartlib.util.*;

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
public class CustomTreeCellRenderer extends JLabel implements TreeCellRenderer
{
	private transient Icon icon = null;
	private JTree ownTree = null;

	/**
	 *
	 */
	public CustomTreeCellRenderer()
	{
		setOpaque(true);
	}

	/**
	 *
	 * @param tree JTree
	 * @param value Object
	 * @param selected boolean
	 * @param expanded boolean
	 * @param leaf boolean
	 * @param row int
	 * @param hasFocus boolean
	 * @return Component
	 */
	private static Icon iconRoot =
		new ImageIcon(FileUtil.getResource("resource/com/telsoft/monitor/ddtp/icon/servers.png"));
	private static Icon iconGroup =
		new ImageIcon(FileUtil.getResource("resource/com/telsoft/monitor/ddtp/icon/group.png"));

	public Component getTreeCellRendererComponent(JTree tree,Object value,boolean selected,boolean expanded,
												  boolean leaf,int row,boolean hasFocus)
	{
		ownTree = tree;
		Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
		if(userObject instanceof MonitorObject)
		{
			try
			{
				MonitorEditor me = Register.getMonitorEditorForObject((MonitorObject)userObject);
				if(me.isCustomRenderer())
				{
					Component cmp = me.getRendererComponent(ownTree,(MonitorObject)userObject,selected,expanded,leaf,hasFocus);
					if(cmp != null)
					{
						return cmp;
					}
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		setEnabled(ownTree.isEnabled());
		setComponentOrientation(ownTree.getComponentOrientation());

		if(tree.getModel().getRoot() == value ||
		   userObject instanceof ServerTreeModel.GroupData ||
		   (userObject instanceof MonitorObject && ((MonitorObject)userObject).isActive()))
		{
			setFont(getFont().deriveFont(Font.BOLD));
		}
		else
		{
			setFont(getFont().deriveFont(Font.PLAIN));
		}

		Color background = (selected ? SystemColor.textHighlight : SystemColor.white);
		Color foreground = (selected ? SystemColor.textHighlightText : SystemColor.controlText);
		if(userObject instanceof MonitorObject)
		{
			try
			{
				MonitorEditor me = Register.getMonitorEditorForObject((MonitorObject)userObject);
				foreground = me.onCustomColor((MonitorObject)userObject,1,selected,expanded,leaf,hasFocus);
				background = me.onCustomColor((MonitorObject)userObject,0,selected,expanded,leaf,hasFocus);
				icon = me.onGetCustomIcon((MonitorObject)userObject,selected,expanded,leaf,hasFocus);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			setBackground(background);
			setForeground(foreground);
			setText(value.toString());
		}
		else
		{
			setBackground(background);
			setForeground(foreground);
			setText(value.toString());
			if(value == tree.getModel().getRoot())
			{
				icon = iconRoot;
			}
			else if(userObject instanceof ServerTreeModel.GroupData)
			{
				icon = iconGroup;
			}
			else
			{
				icon = null;
			}
		}
		setIcon(icon);
		return this;
	}
}
