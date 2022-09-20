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
public class DialogMParamSetting extends JXDialog
{
	private Vector mvtParameter = new Vector();
	protected Set<String> mvtParameterChanged = new HashSet<String>();

	public class ThreadData
	{
		Vector<ThreadParameter> vtParameter = new Vector<ThreadParameter>();
		Vector vtData = new Vector();
		DDTPThreadMonitor ddtpThread;
		String strStartupType;
		String strClass;
		boolean bChanged = false;
	}

	private PropertySheetPanel property;
	private ThreadData[] threadData;

	public DialogMParamSetting(ThreadEditor thEditor,DDTPThreadMonitor[] ddtps) throws Exception
	{
		super(ddtps[0].getModel().getRootComponent(),true);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		threadData = new ThreadData[ddtps.length];
		for(int i = 0;i < ddtps.length;i++)
		{
			threadData[i] = new ThreadData();
			threadData[i].ddtpThread = ddtps[i];
		}
		jbInit();
		setTitle(String.format("Set parameter for %d thread(s)",ddtps.length));
		reload();
	}

	public void reload() throws Exception
	{
		for(int i = 0;i < threadData.length;i++)
		{
			DDTPServerMonitor ddtpServer = threadData[i].ddtpThread.getDDTPServer();
			Packet request = ddtpServer.createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",threadData[i].ddtpThread.getThreadID());
			Packet response = ddtpServer.getChannel().sendRequest("ThreadProcessor","loadSetting",request);
			if(response != null)
			{
				Vector vtSetting = response.getVector("vtSetting");
				DialogParamSetting.translateFromSmart(vtSetting,request);
				Vector vtSettingTP = new Vector(vtSetting.size());
				for(int ii = 0;ii < vtSetting.size();ii++)
				{
					Object objDefine = vtSetting.elementAt(ii);
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
				threadData[i].vtParameter = vtSettingTP;
				threadData[i].strStartupType = response.getString("ThreadStartupType");
				threadData[i].strClass = response.getString("ThreadClassName");
			}
			else
			{
				throw new Exception("Response is empty");
			}

		}

		// merge data
		Vector[] vtParams = new Vector[threadData.length];
		int i = 0;
		for(ThreadData td : threadData)
		{
			vtParams[i++] = td.vtParameter;
		}
		mvtParameter = mergeParameters(vtParams);
		fillControlValue();
		// set startup type to (unmodified)
		((JXCombo)getFormData().getControl("StartupType")).setSelectedValue("99");
		mvtParameterChanged.clear();
	}

	private Vector<ThreadParameter> mergeParameters(Vector<ThreadParameter>...vtParams) throws CloneNotSupportedException
	{
		Vector<ThreadParameter> vtReturn = new Vector<ThreadParameter>();
		if(vtParams.length == 0)
		{
			return vtReturn;
		}
		for(ThreadParameter tp : vtParams[0])
		{
			vtReturn.add((ThreadParameter)tp.clone());
		}
		for(int i = 1;i < vtParams.length;i++)
		{
			int ii = vtReturn.size() - 1;
			while(ii >= 0)
			{
				ThreadParameter p1 = vtReturn.get(ii);

				boolean bFound = false;
				for(int iii = 0;iii < vtParams[i].size();iii++)
				{
					ThreadParameter p2 = vtParams[i].get(iii);
					if(p1.getName().equals(p2.getName()) && p1.getParamType() == p2.getParamType())
					{
						if(p1.getDefValue().equals(p2.getDefValue()))
						{
							p1.setCategory("Same values");
						}
						else
						{
							p1.setCategory("Various values");
							p1.setDefValue("");
						}
						bFound = true;
						break;
					}
				}

				if(!bFound)
				{
					vtReturn.remove(ii);
					if(ii < vtReturn.size())
					{
						continue;
					}
				}
				ii--;
			}
		}
		return vtReturn;
	}

	////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		buildForm();
		property = (PropertySheetPanel)getFormData().getControl("ParameterList");
		property.setMode(PropertySheet.VIEW_AS_CATEGORIES);
		property.setSorting(true);
		property.setDescriptionVisible(true);
		property.addPropertySheetChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt)
			{
				if(evt.getSource() instanceof Property)
				{
					Property p = (Property)evt.getSource();
					mvtParameterChanged.add(p.getName());
					if(p instanceof DefaultProperty)
					{
						((DefaultProperty)p).setModified(true);
					}
				}
			}
		});

		((AbstractButton)getFormData().getControl("OK")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onOK();
			}
		});

		((AbstractButton)getFormData().getControl("Cancel")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onClosing();
			}
		});
	}

	/**
	 *
	 * @throws Exception
	 */
	public void fillControlValue() throws Exception
	{
		Property[] ap = PropertyUtil.convertDefinitionToProperty(mvtParameter);
		for(Property p : ap)
		{
			if(p instanceof DefaultProperty)
			{
				((DefaultProperty)p).setModified(false);
			}
		}
		property.setProperties(ap);
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
			if(mvtParameterChanged.size() > 0)
			{
				vtData = PropertyUtil.getDataFromProperty(property.getProperties());
			}
			Map mapData = new HashMap();
			for(int i = 0;i < vtData.size();i++)
			{
				Vector vtRow = (Vector)vtData.get(i);
				if(mvtParameterChanged.contains(vtRow.get(0)))
				{
					mapData.put(vtRow.get(0),vtRow.get(1));
				}
			}

			String strStartupType = StringUtil.nvl(((JXCombo)getFormData().getControl("StartupType")).getSelectedValue(),"99");

			// set data to each thread
			for(ThreadData td : threadData)
			{
				td.vtData = new Vector();
				for(int ii = 0;ii < td.vtParameter.size();ii++)
				{
					ThreadParameter tp = td.vtParameter.get(ii);
					Vector vtRow = new Vector();
					vtRow.add(tp.getName());
					if(mvtParameterChanged.contains(tp.getName()))
					{
						td.bChanged = true;
						vtRow.add(mapData.get(tp.getName()));
					}
					else
					{
						vtRow.add(tp.getDefValue());
					}
					td.vtData.add(vtRow);
				}
			}

			// store setting
			for(int i = 0;i < threadData.length;i++)
			{
				DDTPServerMonitor ddtpServer = threadData[i].ddtpThread.getDDTPServer();
				Packet request = ddtpServer.createPacket();
				// Send command
				if(threadData[i].bChanged || (!"99".equals(strStartupType) && !strStartupType.equals(threadData[i].strStartupType)))
				{
					request.setRequestID(String.valueOf(System.currentTimeMillis()));
					request.setString("ThreadID",threadData[i].ddtpThread.getThreadID());
					request.setString("ThreadName",threadData[i].ddtpThread.getThreadName());
					request.setString("ThreadClass",threadData[i].strClass);
					if(!"99".equals(strStartupType))
					{
						request.setString("ThreadStartupType",strStartupType);
					}
					else
					{
						request.setString("ThreadStartupType",threadData[i].strStartupType);
					}

					if(threadData[i].bChanged)
					{
						request.setVector("vtSetting",threadData[i].vtData);
					}
					ddtpServer.getChannel().sendRequest("ThreadProcessor","storeSetting",request);
				}
			}
			dispose();
		}
		catch(Exception e)
		{
			if(e instanceof AppException)
			{
				editParameter(((AppException)e).getInfo());
			}
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}

	public void onClosing()
	{
		if(mvtParameterChanged.size() > 0)
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
