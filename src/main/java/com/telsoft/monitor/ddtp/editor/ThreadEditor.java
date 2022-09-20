package com.telsoft.monitor.ddtp.editor;

import java.util.*;
import java.util.List;

import java.awt.*;
import javax.swing.*;

import com.telsoft.monitor.ddtp.*;
import com.telsoft.monitor.ddtp.packet.*;
import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.manager.util.*;
import com.telsoft.monitor.register.*;
import com.telsoft.monitor.util.*;
import smartlib.util.*;
import smartlib.swing.WindowManager;
import smartlib.swing.MessageBox;
import java.util.concurrent.locks.*;
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
public class ThreadEditor extends MonitorEditor
{
	private List<Component> mnuContext = null;
	private List<Component> mnuMContext = null;
	private JXMenuItem miStart = null;
	private JXMenuItem miStop = null;
	private JXMenuItem miDestroy = null;
	private JXMenuItem miSetting = null;
	private JXMenuItem miSchedule = null;
	private JXMenuItem miExecute = null;
	private JXMenuItem miViewLog = null;

	private JXMenuItem miMStart = null;
	private JXMenuItem miMStop = null;
	private JXMenuItem miMDestroy = null;
	private JXMenuItem miMSetting = null;
	private JXMenuItem miMExecute = null;

	private DDTPThreadMonitor mCurrentObject;
	private Lock lock = new ReentrantLock();

	public ThreadEditor(ManagerModel model)
	{
		super(model);
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
	private static Icon iconRun =
		new ImageIcon(FileUtil.getResource("resource/com/telsoft/monitor/ddtp/icon/play.png"));
	private static Icon iconRunError =
		new ImageIcon(FileUtil.getResource("resource/com/telsoft/monitor/ddtp/icon/playerror.png"));
	private static Icon iconStop =
		new ImageIcon(FileUtil.getResource("resource/com/telsoft/monitor/ddtp/icon/stop.png"));

	public Icon onGetCustomIcon(MonitorObject obj,boolean selected,boolean expanded,boolean leaf,
								boolean hasFocus)
	{
		if(obj != null)
		{
			if(obj.isActive())
			{
				if(obj instanceof DDTPThreadMonitor)
				{
					DDTPThreadMonitor ddtpThread = (DDTPThreadMonitor)obj;
					if(ddtpThread.isError())
					{
						return iconRunError;
					}
				}
				return iconRun;
			}
			else
			{
				return iconStop;
			}
		}
		return null;
	}

	public Color onCustomColor(MonitorObject obj,int iType,boolean selected,boolean expanded,boolean leaf,
							   boolean hasFocus)
	{
		if(iType == 1)
		{
			return(selected ? SystemColor.textHighlightText :
				   (obj.isActive() ? new Color(0,92,0) : new Color(128,0,0)));
		}
		return super.onCustomColor(obj,iType,selected,expanded,leaf,hasFocus);
	}

	/**
	 *
	 * @param obj DDTPThreadMonitor
	 * @throws Exception
	 */
	void setting(DDTPThreadMonitor obj) throws Exception
	{
		DialogParamSetting dlgSetting = new DialogParamSetting(this,obj);

		if(getDialogManager(obj).addWindow(dlgSetting,obj.getDDTPServer(),obj.getThreadID()))
		{
			WindowManager.centeredWindow(dlgSetting);
		}
	}

	/**
	 *
	 * @param obj DDTPThreadMonitor
	 * @throws Exception
	 */
	void mSetting(MonitorObject[] obj) throws Exception
	{
		DDTPThreadMonitor[] dobj = new DDTPThreadMonitor[obj.length];
		for(int i = 0;i < obj.length;i++)
		{
			dobj[i] = (DDTPThreadMonitor)obj[i];
		}

		DialogMParamSetting dlgSetting = new DialogMParamSetting(this,dobj);
		WindowManager.centeredWindow(dlgSetting);
	}

	/**
	 *
	 * @param obj DDTPThreadMonitor
	 * @throws Exception
	 */
	void schedule(DDTPThreadMonitor obj) throws Exception
	{
		DialogSchedule dlg =
			new DialogSchedule(obj.getModel().getRootComponent(),obj.getThreadID(),obj.getDDTPServer().getChannel());
		if(getDialogManager(obj).addWindow(dlg,obj.getDDTPServer(),obj.getThreadID()))
		{
			WindowManager.centeredWindow(dlg);
		}
	}

	/**
	 *
	 * @param obj DDTPThreadMonitor
	 * @throws Exception
	 */
	public void viewLog(DDTPThreadMonitor obj) throws Exception
	{
		Packet request = obj.getDDTPServer().createPacket();
		request.setRequestID(String.valueOf(System.currentTimeMillis()));
		request.setString("ThreadID",obj.getThreadID());
		Packet response = obj.getDDTPServer().getChannel().sendRequest("ThreadProcessor","loadThreadLog",request);
		if(response != null)
		{
			Vector vtDirLog = response.getVector("vtDirLog");
			DialogLogViewer dlgLogViewer =
				new DialogLogViewer(obj.getDDTPServer().getModel().getRootComponent(),obj.getThreadID(),
									obj.getThreadName(),vtDirLog,obj.getDDTPServer().getChannel());
			if(getDialogManager(obj).addWindow(dlgLogViewer,obj.getDDTPServer(),obj.getThreadID()))
			{
				WindowManager.centeredWindow(dlgLogViewer);
			}
		}
	}

	/**
	 * getContextMenu
	 *
	 * @param obj MonitorObject
	 * @return JPopupMenu
	 */
	public List<Component> getContextMenu(MonitorObject obj)
	{
		if(mnuContext == null)
		{
			mnuContext = super.getContextMenu(obj);
			miStart = new JXMenuItem("Start thread",JXMenuItem.START_USER_ACTION + 1,obj);
			miStop = new JXMenuItem("Stop thread",JXMenuItem.START_USER_ACTION + 2,obj);
			miDestroy = new JXMenuItem("Destroy thread",JXMenuItem.START_USER_ACTION + 3,obj);
			miSetting = new JXMenuItem("Setting",JXMenuItem.START_USER_ACTION + 4,obj);
			miSchedule = new JXMenuItem("Schedule",JXMenuItem.START_USER_ACTION + 5,obj);
			miExecute = new JXMenuItem("Execute immediate",JXMenuItem.START_USER_ACTION + 6,obj);
			miViewLog = new JXMenuItem("View log",JXMenuItem.START_USER_ACTION + 7,obj);
			mnuContext.add(miStart);
			mnuContext.add(miStop);
			mnuContext.add(miDestroy);
			mnuContext.add(new JPopupMenu.Separator());
			mnuContext.add(miExecute);
			mnuContext.add(new JPopupMenu.Separator());
			mnuContext.add(miSetting);
			mnuContext.add(miSchedule);
			mnuContext.add(miViewLog);
		}
		return mnuContext;
	}

	/**
	 * onContextMenu
	 *
	 * @param obj MonitorObject
	 */
	public void onContextMenu(MonitorObject obj)
	{
		if(obj.isActive())
		{
			miStart.setText("Restart");
		}
		else
		{
			miStart.setText("Start");
		}

		miStop.setEnabled(obj.isActive());
		miDestroy.setEnabled(obj.isActive());
		miExecute.setEnabled(true);
		miSchedule.setEnabled(true);
		miSetting.setEnabled(true);
		miViewLog.setEnabled(true);
	}

	/**
	 *
	 * @param obj MonitorObject
	 * @param id int
	 * @throws Exception
	 */
	public boolean onMenuAction(MonitorObject obj,int id) throws Exception
	{
		DDTPThreadMonitor threadObj = (DDTPThreadMonitor)obj;
		switch(id)
		{
			case JXMenuItem.START_USER_ACTION + 1:
			{
				threadObj.startThread();
				break;
			}
			case JXMenuItem.START_USER_ACTION + 2:
			{
				threadObj.stopThread();
				break;
			}
			case JXMenuItem.START_USER_ACTION + 3:
			{
				if(Msg.confirm("Destroy Thread?"))
				{
					threadObj.destroyThread();
				}
				break;
			}
			case JXMenuItem.START_USER_ACTION + 4:
			{
				setting(threadObj);
				break;
			}
			case JXMenuItem.START_USER_ACTION + 5:
			{
				schedule(threadObj);
				break;
			}
			case JXMenuItem.START_USER_ACTION + 6:
			{
				if(Msg.confirm("Immediate execute?"))
				{
					threadObj.startImmediate();
				}
				break;
			}
			case JXMenuItem.START_USER_ACTION + 7:
			{
				viewLog(threadObj);
				break;
			}
		}
		return false;
	}

	/**
	 * showEditor
	 *
	 * @param obj MonitorObject
	 */
	public void showEditor(MonitorObject obj)
	{
		try
		{
			DDTPEditor de = (DDTPEditor)Register.getMonitorEditorForObject(obj.getParent());
			mCurrentObject = (DDTPThreadMonitor)obj;
			de.showThreadEditor(mCurrentObject);
		}
		catch(Exception ex)
		{
			mCurrentObject = null;
			ex.printStackTrace();
			MessageBox.showMessageDialog(model.getRootComponent(),ex,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}

	/**
	 *
	 * @return DDTPThreadMonitor
	 * @throws AppException
	 */
	public DDTPThreadMonitor getCurrentObject() throws AppException
	{
		if(mCurrentObject == null)
		{
			throw new AppException("Current thread is null");
		}
		return mCurrentObject;
	}

	public DialogManager getDialogManager(MonitorObject obj) throws Exception
	{
		DDTPEditor de = (DDTPEditor)Register.getMonitorEditorForObject(obj.getParent());
		return de.getDialogManager();
	}

	public boolean onMMenuAction(int id,MonitorObject ...obj) throws Exception
	{
		switch(id)
		{
			case JXMenuItem.START_USER_ACTION + 1:
			{
				new Thread(new InvokeWithMultiMO(model.getRootComponent(),obj)
				{
					public void runItem(Component root,MonitorObject mo)
					{
						try
						{
							((DDTPThreadMonitor)mo).startThread();
						}
						catch(Exception ex)
						{
							model.getMonitorManager().showException(ex);
						}
					}
				}).start();
				break;
			}
			case JXMenuItem.START_USER_ACTION + 2:
			{
				new Thread(new InvokeWithMultiMO(model.getRootComponent(),obj)
				{
					public void runItem(Component root,MonitorObject mo)
					{
						try
						{
							((DDTPThreadMonitor)mo).stopThread();
						}
						catch(Exception ex)
						{
							model.getMonitorManager().showException(ex);
						}
					}
				}).start();
				break;
			}
			case JXMenuItem.START_USER_ACTION + 3:
			{
				if(Msg.confirm("Destroy Thread?"))
				{
					new Thread(new InvokeWithMultiMO(model.getRootComponent(),obj)
					{
						public void runItem(Component root,MonitorObject mo)
						{
							try
							{
								((DDTPThreadMonitor)mo).destroyThread();
							}
							catch(Exception ex)
							{
								model.getMonitorManager().showException(ex);
							}
						}
					}).start();
				}
				break;
			}
			case JXMenuItem.START_USER_ACTION + 4:
			{
				mSetting(obj);
				break;
			}
			case JXMenuItem.START_USER_ACTION + 6:
			{
				if(Msg.confirm("Immediate execute?"))
				{
					new Thread(new InvokeWithMultiMO(model.getRootComponent(),obj)
					{
						public void runItem(Component root,MonitorObject mo)
						{
							try
							{
								((DDTPThreadMonitor)mo).startImmediate();
							}
							catch(Exception ex)
							{
								model.getMonitorManager().showException(ex);
							}
						}
					}).start();
				}
				break;
			}
		}
		return false;
	}

	public List<Component> getMContextMenu(MonitorObject ...obj)
	{
		Object parent = obj[0].getParent();
		for(int i = 1;i < obj.length;i++)
		{
			if(obj[i].getParent() != parent)
			{
				return null;
			}
		}

		if(mnuMContext == null)
		{
			mnuMContext = super.getMContextMenu(obj);
			miMStart = new JXMenuItem("Start/restart threads",JXMenuItem.START_USER_ACTION + 1,obj);
			miMStop = new JXMenuItem("Stop threads",JXMenuItem.START_USER_ACTION + 2,obj);
			miMDestroy = new JXMenuItem("Destroy threads",JXMenuItem.START_USER_ACTION + 3,obj);
			miMSetting = new JXMenuItem("Setting",JXMenuItem.START_USER_ACTION + 4,obj);
			miMExecute = new JXMenuItem("Execute immediate",JXMenuItem.START_USER_ACTION + 6,obj);
			mnuMContext.add(miMStart);
			mnuMContext.add(miMStop);
			mnuMContext.add(miMDestroy);
			mnuMContext.add(new JPopupMenu.Separator());
			mnuMContext.add(miMExecute);
			mnuMContext.add(new JPopupMenu.Separator());
			mnuMContext.add(miMSetting);
		}
		return mnuMContext;
	}
}
