package com.telsoft.monitor.ddtp.editor;

import java.beans.*;
import java.util.*;

import java.awt.event.*;
import javax.swing.*;

import com.l2fprod.common.propertysheet.*;
import com.l2fprod.common.util.*;
import smartlib.dictionary.*;
import com.telsoft.monitor.ddtp.*;
import com.telsoft.monitor.ddtp.packet.*;
import smartlib.swing.*;
import smartlib.thread.*;
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
public class DialogParamSetting extends JXDialog
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private Vector mvtParameter = new Vector();
	private String mstrThreadID;
	private SocketTransmitter channel;
	private PropertySheetPanel property;
	protected boolean bChange;

	private ThreadEditor mthEditor;
	private DDTPThreadMonitor ddtpThreadMonitor;

	public DialogParamSetting(ThreadEditor thEditor,DDTPThreadMonitor ddtpThreadMonitor) throws Exception
	{
		super(ddtpThreadMonitor.getModel().getRootComponent(),false);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.ddtpThreadMonitor = ddtpThreadMonitor;
		this.channel = this.ddtpThreadMonitor.getDDTPServer().getChannel();
		mstrThreadID = this.ddtpThreadMonitor.getThreadID();
		mthEditor = thEditor;
		jbInit();
		reload();
	}

	public void reload() throws Exception
	{
		Packet request = ddtpThreadMonitor.getDDTPServer().createPacket();
		request.setRequestID(String.valueOf(System.currentTimeMillis()));
		request.setString("ThreadID",mstrThreadID);
		Packet response = channel.sendRequest("ThreadProcessor","loadSetting",request);
		if(response != null)
		{
			Vector vtSetting = response.getVector("vtSetting");
			translateFromSmart(vtSetting,request);
			Vector vtSettingTP = new Vector(vtSetting.size());
			for(int i = 0;i < vtSetting.size();i++)
			{
				Object objDefine = vtSetting.elementAt(i);
				ThreadParameter tp = null;
				if(objDefine instanceof Vector)
				{
					Vector vtRow = (Vector)objDefine;
					tp = new ThreadParameter();
					tp.setName((String)vtRow.elementAt(0));
					tp.setDefValue(vtRow.elementAt(1));
					tp.setParamType(Integer.parseInt((String)vtRow.elementAt(2)));
					tp.setDescription((String)vtRow.elementAt(4));
					tp.setDefinition(vtRow.elementAt(3));
					tp.setIndex((String)vtRow.elementAt(5));
					if(vtRow.size() >= 7)
					{
						tp.setCategory((String)vtRow.elementAt(6));
					}
					if(vtRow.size() >= 8)
					{
						tp.setMandatory(StringUtil.nvl(vtRow.elementAt(7),"0").equals("1"));
					}
				}
				else
				{
					tp = (ThreadParameter)objDefine;
				}
				vtSettingTP.add(tp);
			}
			mvtParameter = vtSettingTP;
			((JXText)getFormData().getControl("ThreadName")).setText(ddtpThreadMonitor.getThreadName());
			((JXText)getFormData().getControl("ThreadClass")).setText(response.getString("ThreadClassName"));
			((JXCombo)getFormData().getControl("StartupType")).setSelectedValue(response.getString("ThreadStartupType"));
			setTitle(ddtpThreadMonitor.getThreadName());
		}
		else
		{
			mvtParameter = new Vector();
		}
		fillControlValue();
		bChange = false;
	}

	public static void translateFromSmart(Vector vtSetting,Packet pk)
	{
		if(vtSetting == null || vtSetting.size() == 0)
		{
			return;
		}
		for(int i = 0;i < vtSetting.size();i++)
		{
			Object o = vtSetting.get(i);
			if(o instanceof Vector)
			{
				translateFromSmart((Vector)o,pk);
			}
			else
			{
				if(o instanceof smartlib.thread.ThreadParameter)
				{
					smartlib.thread.ThreadParameter smartTP = (smartlib.thread.ThreadParameter)o;
					smartlib.thread.ThreadParameter tp = new smartlib.thread.ThreadParameter();
					tp.setName(smartTP.getName());
					tp.setDefValue(smartTP.getDefValue());
					tp.setParamType(smartTP.getParamType());
					tp.setDescription(smartTP.getDescription());
					tp.setDefinition(smartTP.getDefinition());
					if(smartTP.getDefinition() instanceof Vector)
					{
						translateFromSmart((Vector)smartTP.getDefinition(),pk);
					}
					tp.setIndex(smartTP.getIndex());
					vtSetting.set(i,tp);
				}
			}
		}
	}

	////////////////////////////////////////////////////////
	private void rebuildVisibleNode()
	{

	}

	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		buildForm();
		bChange = false;
		property = (PropertySheetPanel)getFormData().getControl("ParameterList");
		property.setMode(PropertySheet.VIEW_AS_CATEGORIES);
		property.setSortingCategories(true);
		property.setDescriptionVisible(true);
		property.addPropertySheetChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt)
			{
				bChange = true;
				rebuildVisibleNode();
			}
		});
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		((AbstractButton)getFormData().getControl("OK")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onOK();
			}
		});
		////////////////////////////////////////////////////////
		((AbstractButton)getFormData().getControl("Cancel")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onClosing();
			}
		});

		((JButton)getFormData().getControl("Schedule")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				schedule();
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

	void schedule()
	{
		try
		{
			mthEditor.schedule(mthEditor.getCurrentObject());
		}
		catch(Exception e)
		{
			mthEditor.getModel().getMonitorManager().showException(e);
		}
	}

	void applyTo()
	{
		try
		{
			Vector vtData = PropertyUtil.getDataFromProperty(property.getProperties());
			DialogApplyTo at = new DialogApplyTo(mthEditor,ddtpThreadMonitor,vtData);
			WindowManager.centeredWindow(at);
		}
		catch(Exception e)
		{
			mthEditor.getModel().getMonitorManager().showException(e);
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public void fillControlValue() throws Exception
	{
		property.setProperties(PropertyUtil.convertDefinitionToProperty(mvtParameter));
		rebuildVisibleNode();
	}

	public void onOK()
	{
		try
		{
			// Validate input
			if(!helper.validateInput())
			{
				return;
			}

			Vector vtData = new Vector();
			if(bChange)
			{
				vtData = PropertyUtil.getDataFromProperty(property.getProperties());
			}

			// Send command
			Packet request = channel.getProcessor().getServerMonitor().createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			request.setString("ThreadName",getFormData().getFieldString("ThreadName"));
			request.setString("ThreadClass",getFormData().getFieldString("ThreadClass"));
			request.setString("ThreadStartupType",getFormData().getFieldString("StartupType"));
			if(bChange)
			{
				request.setVector("vtSetting",vtData);
			}
			channel.sendRequest("ThreadProcessor","storeSetting",request);
			dispose();
		}
		catch(Exception e)
		{
			if(e instanceof AppException)
			{
				editParameter(((AppException)e).getInfo());
			}
			mthEditor.getModel().getMonitorManager().showException(e);
		}
	}

	public void onClosing()
	{
		if(bChange)
		{
			int iResult =
				MessageBox.showConfirmDialog(this,DefaultDictionary.getString("Confirm.SaveOnExit"),Global.APP_NAME,
											 MessageBox.YES_NO_CANCEL_OPTION);
			if(iResult == MessageBox.YES_OPTION)
			{
				onOK();
			}
			else if(iResult == MessageBox.NO_OPTION)
			{
				dispose();
			}
		}
		else
		{
			dispose();
		}
	}

	private void editParameter(String strParamName)
	{
		if(strParamName == null)
		{
			return;
		}

		for(int i = 0;i < mvtParameter.size();i++)
		{
			Object obj = mvtParameter.elementAt(i);
			if(obj instanceof Vector)
			{
				if(strParamName.startsWith(StringUtil.nvl(((Vector)obj).elementAt(0).toString(),
					"")))
				{
					property.getTable().editCellAt(i,1);
				}
			}
			else if(obj instanceof ThreadParameter)
			{
				if(strParamName.startsWith(((ThreadParameter)obj).getName()))
				{
					property.getTable().editCellAt(i,1);
				}
			}
		}
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
