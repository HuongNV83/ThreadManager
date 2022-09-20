package com.telsoft.monitor.ddtp.editor;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;

import com.telsoft.monitor.ddtp.*;
import com.telsoft.monitor.ddtp.packet.*;
import com.telsoft.monitor.manager.*;
import smartlib.swing.*;
import smartlib.thread.*;
import smartlib.util.*;
import com.telsoft.monitor.manager.tree.*;
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
public class DialogApplyTo extends JXDialog
{
	private ThreadEditor mthEditor;
	private DDTPThreadMonitor ddtpThreadMonitor;
	private JXCheckList tblThread;
	private JXCheckList tblField;
	private Vector mvtParameter;

	public DialogApplyTo(ThreadEditor thEditor,DDTPThreadMonitor ddtpThreadMonitor,Vector vtParameter) throws Exception
	{
		super(ddtpThreadMonitor.getModel().getRootComponent(),true);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.ddtpThreadMonitor = ddtpThreadMonitor;
		mvtParameter = vtParameter;
		mthEditor = thEditor;
		jbInit();
		reload();
	}

	public void reload() throws Exception
	{
		Vector vtThread = new Vector();
		// Get all activated ddtp connections
		MonitorObject[] mos = mthEditor.getModel().getMonitorObjects();

		Vector<MonitorObject> vtMo = new Vector<MonitorObject>();
		for(MonitorObject mo : mos)
		{
			if(mo instanceof DDTPServerMonitor)
			{
				vtMo.add(mo);
				continue;
			}

			if(mo instanceof FolderObject)
			{
				FolderObject fo = (FolderObject)mo;
				for(int i = 0;i < fo.getChildCount();i++)
				{
					MonitorObject mo1 = fo.getChild(i);
					if(mo1 instanceof DDTPServerMonitor)
					{
						vtMo.add(mo1);
					}
				}
			}
		}

		for(MonitorObject mo : vtMo)
		{
			if(!(mo instanceof DDTPServerMonitor))
			{
				continue;
			}
			DDTPServerMonitor ddtpServer = (DDTPServerMonitor)mo;
			if(!ddtpServer.isActive())
			{
				continue;
			}
			for(int i = 0,n = ddtpServer.getChildCount();i < n;i++)
			{
				DDTPThreadMonitor thread = (DDTPThreadMonitor)ddtpServer.getChild(i);
				if(thread == ddtpThreadMonitor)
				{
					continue;
				}
				Vector vtRow = new Vector(4);
				vtRow.add(ddtpServer);
				vtRow.add(thread);
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
				vtThread.add(vtRow);
			}
		}
		Vector vtThreadData = new Vector();
		for(Vector vt : (Vector<Vector>)mvtParameter)
		{
			Vector vtRow = new Vector();
			vtRow.add(vt.get(0));
			vtRow.add(vt.get(1));
			vtRow.add("false");
			vtThreadData.add(vtRow);
		}
		tblField.setData(vtThreadData);
		tblThread.setData(vtThread);
	}

	////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		buildForm();
		////////////////////////////////////////////////////////
		tblField = (JXCheckList)getFormData().getControl("FieldSelected");
		tblThread = (JXCheckList)getFormData().getControl("ThreadSelected");
		((AbstractButton)getFormData().getControl("Close")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onClosing();
			}
		});

		((JButton)getFormData().getControl("Apply")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				applyTo();
			}
		});
	}

	void applyTo()
	{
		try
		{
			Vector<Vector> vtData = tblField.getData();
			Map<Object,Object> mapParameters = new HashMap<Object,Object>();
			for(Vector vtRow : vtData)
			{
				if("true".equals(vtRow.get(2)))
				{
					mapParameters.put(vtRow.get(0),vtRow.get(1));
				}
			}

			Vector<Vector> vtThread = tblThread.getData();
			// store setting
			for(Vector vtRow : vtThread)
			{
				DDTPServerMonitor ddtpServer = (DDTPServerMonitor)vtRow.get(0);
				DDTPThreadMonitor ddtpThread = (DDTPThreadMonitor)vtRow.get(1);
				if(!vtRow.get(3).equals("true"))
				{
					continue;
				}
				Packet request = ddtpServer.createPacket();

				// Load current setting of thread
				request.setRequestID(String.valueOf(System.currentTimeMillis()));
				request.setString("ThreadID",ddtpThread.getThreadID());
				Packet response = ddtpServer.getChannel().sendRequest("ThreadProcessor","loadSetting",request);
				Vector vtSetting = response.getVector("vtSetting");
				Vector vtNewSetting = new Vector(vtSetting.size());
				for(int ii = 0;ii < vtSetting.size();ii++)
				{
					Object objDefine = vtSetting.elementAt(ii);
					Vector vtRowSetting = new Vector();
					if(objDefine instanceof Vector)
					{
						Vector vtR = (Vector)objDefine;
						vtRowSetting.add(vtR.elementAt(0));
						vtRowSetting.add(vtR.elementAt(1));
					}
					else
					{
						ThreadParameter tp = (ThreadParameter)objDefine;
						vtRowSetting.add(tp.getName());
						vtRowSetting.add(tp.getDefValue());
						tp = (ThreadParameter)objDefine;
					}
					if(mapParameters.containsKey(vtRowSetting.get(0)))
					{
						vtRowSetting.set(1,mapParameters.get(vtRowSetting.get(0)));
					}
					vtNewSetting.add(vtRowSetting);
				}
				// Synchronize parameters

				// Send command
				request = ddtpServer.createPacket();
				request.setRequestID(String.valueOf(System.currentTimeMillis()));
				request.setString("ThreadID",ddtpThread.getThreadID());
				request.setString("ThreadName",ddtpThread.getThreadName());
				request.setString("ThreadClass",response.getString("ThreadClassName"));
				request.setString("ThreadStartupType",response.getString("ThreadStartupType"));
				request.setVector("vtSetting",vtNewSetting);
				ddtpServer.getChannel().sendRequest("ThreadProcessor","storeSetting",request);
			}
			dispose();
		}
		catch(Exception e)
		{
			mthEditor.getModel().getMonitorManager().showException(e);
		}
	}

	public void onClosing()
	{
		dispose();
	}

	public void onOpened()
	{
		WindowManager.refresh();
		try
		{
			afterOpen();
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			this.dispose();
		}
	}
}
