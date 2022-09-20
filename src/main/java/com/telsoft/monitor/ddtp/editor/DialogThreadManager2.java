package com.telsoft.monitor.ddtp.editor;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.telsoft.monitor.ddtp.*;
import smartlib.swing.*;
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
public class DialogThreadManager2 extends JXDialog
{
	private Vector vtTableData = new Vector();

	public DialogThreadManager2(Component parent,DDTPServerMonitor obj) throws Exception
	{
		super(parent,true);
		for(int i = 0,n = obj.getChildCount();i < n;i++)
		{
			DDTPThreadMonitor thread = (DDTPThreadMonitor)obj.getChild(i);
			Vector vtRow = new Vector(4);
			vtRow.add(thread.getThreadName());
			if(!thread.isActive())
			{
				vtRow.add("0");
			}
			else if(thread.isError())
			{
				vtRow.add("2");
			}
			else
			{
				vtRow.add("1");
			}
			vtRow.add("false");
			vtRow.add(thread);
			vtTableData.add(vtRow);
		}
		jbInit();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private Action buttonAction = new AbstractAction()
	{
		public void actionPerformed(ActionEvent e)
		{
			final String actionCommand = e.getActionCommand();
			if(actionCommand.equals("CLOSE"))
			{
				dispose();
			}

			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					StringBuilder buf = new StringBuilder();
					for(int i = 0,n = vtTableData.size();i < n;i++)
					{
						Vector vtRow = (Vector)vtTableData.get(i);
						if(vtRow.get(2).equals("true"))
						{
							DDTPThreadMonitor thread = (DDTPThreadMonitor)vtRow.get(3);
							try
							{
								if(actionCommand.equals("STOP"))
								{
									thread.stopThread();
								}
								else if(actionCommand.equals("START") || actionCommand.equals("RESTART"))
								{
									thread.startThread();
								}
							}
							catch(Exception ex)
							{
								buf.append(thread.getThreadName()).append(":").append(ex.getMessage()).append("\r\n");
							}
						}
						if(buf.length() > 0)
						{
							MessageBox.showMessageDialog(DialogThreadManager2.this,buf.toString(),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
						}
					}
				}
			});
		}
	};

	/**
	 *
	 * @throws Exception
	 */
	private void jbInit() throws Exception
	{
		buildForm();
		if(vtTableData.size() > 0)
		{
			VectorTable tblThread = (VectorTable)getFormData().getControl("ThreadList");
			tblThread.setData(vtTableData);
			tblThread.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tblThread.setRowSelectionInterval(0,0);
			tblThread.setEnabled(true);
		}
		((JButton)getFormData().getControl("btnClose")).setAction(buttonAction);
		((JButton)getFormData().getControl("btnClose")).setActionCommand("CLOSE");
		((JButton)getFormData().getControl("btnStop")).setAction(buttonAction);
		((JButton)getFormData().getControl("btnStop")).setActionCommand("STOP");
		((JButton)getFormData().getControl("btnStart")).setAction(buttonAction);
		((JButton)getFormData().getControl("btnStart")).setActionCommand("START");
		((JButton)getFormData().getControl("btnRestart")).setAction(buttonAction);
		((JButton)getFormData().getControl("btnRestart")).setActionCommand("RESTART");
		updateLanguage();
	}
}
