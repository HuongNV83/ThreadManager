package com.telsoft.monitor.ddtp.editor;

import java.util.*;

import java.awt.*;
import javax.swing.event.*;

import com.telsoft.monitor.ddtp.packet.*;
import smartlib.swing.*;
import smartlib.util.*;
import smartlib.transport.*;

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
public class DialogThreadManager extends JXDialog implements ControlButtonListener
{
	////////////////////////////////////////////////////////
	private Vector vtTableData = new Vector();
	private SocketTransmitter channel;
	private String mstrThreadID;
	////////////////////////////////////////////////////////

	public DialogThreadManager(Component parent,SocketTransmitter channel,Vector vtTableData) throws Exception
	{
		super(parent,true);
		this.channel = channel;
		this.vtTableData = vtTableData;
		jbInit();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		buildForm();
		((PanelControlButton)getFormData().getLayout("ControlButton")).setNormalState();
		onChangeAction(ACTION_NONE,ACTION_NONE);
		if(vtTableData.size() > 0)
		{
			((VectorTable)getFormData().getControl("ThreadList")).setData(vtTableData);
			((VectorTable)getFormData().getControl("ThreadList")).setRowSelectionInterval(0,0);
		}
		showDetailValue();
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		((VectorTable)getFormData().getControl("ThreadList")).getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent evt)
			{
				try
				{
					showDetailValue();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void showDetailValue() throws Exception
	{
		////////////////////////////////////////////////////////
		int iSelectedRow = ((VectorTable)getFormData().getControl("ThreadList")).getSelectedRow();
		if(iSelectedRow >= 0)
		{
			Vector vtRow = (Vector)((VectorTable)getFormData().getControl("ThreadList")).getRow(iSelectedRow);
			mstrThreadID = vtRow.elementAt(3).toString();
			getFormData().setFieldValue("ThreadID",vtRow.elementAt(3).toString());
			getFormData().setFieldValue("ThreadName",vtRow.elementAt(0).toString());
			getFormData().setFieldValue("ThreadClass",vtRow.elementAt(1).toString());
			getFormData().setFieldValue("StartupType",vtRow.elementAt(2).toString());
		}
		else
		{
			mstrThreadID = "";
		}
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @param strThreadName String
	 * @param strClassName String
	 * @param strStartupType String
	 */
	////////////////////////////////////////////////////////
	private void updateTable(String strThreadName,String strClassName,String strStartupType)
	{
		int iSelectedRow = ((VectorTable)getFormData().getControl("ThreadList")).getSelectedRow();
		Vector vtRow = (Vector)((VectorTable)getFormData().getControl("ThreadList")).getRow(iSelectedRow);
		vtRow.setElementAt(strThreadName,0);
		vtRow.setElementAt(strClassName,1);
		vtRow.setElementAt(strStartupType,2);
	}

	private void addTable(String strThreadName,String strClassName,String strStartupType,String strThreadId)
	{
		Vector vtRow = new Vector();
		vtRow.add(strThreadName);
		vtRow.add(strClassName);
		vtRow.add(strStartupType);
		vtRow.add(strThreadId);
		VectorTable tbl = ((VectorTable)getFormData().getControl("ThreadList"));
		tbl.addRow(vtRow);
		tbl.changeSelectedRow(tbl.getRowCount() - 1);
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @param iOldAction int
	 * @param iNewAction int
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean validateInput(int iOldAction,int iNewAction)
	{
		if((iOldAction == ACTION_MODIFY || iOldAction == ACTION_ADD_COPY)
		   && iNewAction == ACTION_SAVE)
		{
			return helper.validateInput();
		}
		return true;
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @param iOldAction int
	 * @param iNewAction int
	 */
	////////////////////////////////////////////////////////
	public void onChangeAction(int iOldAction,int iNewAction)
	{
		if(iNewAction == ACTION_NONE)
		{
			getFormData().setFieldEnabled(false);
			((VectorTable)getFormData().getControl("ThreadList")).setEnabled(true);
			getFormData().getControl("ThreadList").requestFocus();
		}
		else if(iNewAction == ACTION_MODIFY || iNewAction == ACTION_ADD_COPY)
		{
			getFormData().setFieldEnabled(true);
			((VectorTable)getFormData().getControl("ThreadList")).setEnabled(false);
			getFormData().getControl("ThreadName").requestFocus();
		}
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean add()
	{
		try
		{
			Packet request = channel.getProcessor().getServerMonitor().createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",getFormData().getFieldString("ThreadID"));
			request.setString("ThreadName",getFormData().getFieldString("ThreadName"));
			request.setString("ThreadClass",getFormData().getFieldString("ThreadClass"));
			request.setString("ThreadStartupType",getFormData().getFieldString("StartupType"));
			request.setString("SourceThreadID",mstrThreadID);

			channel.sendRequest("ThreadProcessor","manageThreadsCopy",request);

			addTable(getFormData().getFieldString("ThreadName"),getFormData().getFieldString("ThreadClass"),
					 getFormData().getFieldString("StartupType"),getFormData().getFieldString("ThreadID"));
			getFormData().clearBackup();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean modify()
	{
		try
		{
			Packet request = channel.getProcessor().getServerMonitor().createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",getFormData().getFieldString("ThreadID"));
			request.setString("ThreadName",getFormData().getFieldString("ThreadName"));
			request.setString("ThreadClass",getFormData().getFieldString("ThreadClass"));
			request.setString("ThreadStartupType",getFormData().getFieldString("StartupType"));
			channel.sendRequest("ThreadProcessor","manageThreadsStore",request);
			updateTable(getFormData().getFieldString("ThreadName"),getFormData().getFieldString("ThreadClass"),
						getFormData().getFieldString("StartupType"));
			getFormData().clearBackup();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean remove()
	{
		return true;
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean search()
	{
		return true;
	}

	////////////////////////////////////////////////////////

	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getPermission()
	{
		return "SIUD";
	}
}
