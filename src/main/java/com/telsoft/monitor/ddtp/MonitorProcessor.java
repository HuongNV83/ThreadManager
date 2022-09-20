package com.telsoft.monitor.ddtp;

import java.util.*;

import com.telsoft.monitor.ddtp.packet.SocketProcessor;
import smartlib.transport.*;
import smartlib.util.*;

public class MonitorProcessor extends SocketProcessor
{
	private Object mobjRoot = null;
	private DDTPServerMonitor serverMonitor;

	/**
	 *
	 * @param objOwner Object
	 * @param serverMonitor DDTPServerMonitor
	 */
	public MonitorProcessor(Object objOwner,DDTPServerMonitor serverMonitor)
	{
		this.serverMonitor = serverMonitor;
		afterCreateInstance();
		setRootObject(objOwner);
	}

	/**
	 *
	 * @param obj Object
	 */
	private void setRootObject(Object obj)
	{
		mobjRoot = obj;
	}

	/**
	 *
	 * @return Object
	 */
	private Object getRootObject()
	{
		return mobjRoot;
	}

	public void updateProperties()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			Vector vtProps = request.getVector("Properties");
			if(strThreadID.length() > 0)
			{
				DDTPServerMonitor srInfor = ((DDTPServerMonitor)getRootObject());
				srInfor.updateProperties(strThreadID,vtProps);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void logMonitor()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			String strLog = StringUtil.nvl(request.getString("LogResult"),"");
			String strStatus = StringUtil.nvl(request.getString("ThreadStatus"),"");
			boolean bError = StringUtil.nvl(request.getString("ThreadError"),"0").equals("1");
			if(strThreadID.length() > 0)
			{
				DDTPServerMonitor srInfor = ((DDTPServerMonitor)getRootObject());
				srInfor.updateStatus(strThreadID,strStatus,strLog,bError);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void unloadThread()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			if(strThreadID.length() > 0)
			{
				DDTPServerMonitor pnl = ((DDTPServerMonitor)getRootObject());
				pnl.unloadThread(strThreadID);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void loadThread()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			if(strThreadID.length() > 0)
			{
				String strThreadName = StringUtil.nvl(request.getString("ThreadName"),"");
				String strThreadStatus = StringUtil.nvl(request.getString("ThreadStatus"),"");
				Vector vtProps = request.getVector("Properties");
				Map mpExtras = (Map)request.getObject("Extras");
				DDTPServerMonitor client = ((DDTPServerMonitor)getRootObject());
				client.loadThread(strThreadID,strThreadName,strThreadStatus,vtProps,mpExtras);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void renameThread()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			if(strThreadID.length() > 0)
			{
				String strThreadName = StringUtil.nvl(request.getString("ThreadName"),"");
				DDTPServerMonitor client = ((DDTPServerMonitor)getRootObject());
				client.renameThread(strThreadID,strThreadName);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void logAction()
	{
		try
		{
			String strResult = StringUtil.nvl(request.getString("strLog"),"");
			if(strResult.length() > 0)
			{
				DDTPServerMonitor srvInfor = ((DDTPServerMonitor)getRootObject());
//				srvInfor.getLogAction().append(StringListHolder.convertHTML(strResult));
				srvInfor.getLogAction().append(strResult);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void notification()
	{
		try
		{
			Vector vtNotification = request.getVector("notification");
			if(vtNotification != null)
			{
				DDTPServerMonitor srvInfor = ((DDTPServerMonitor)getRootObject());
				srvInfor.doNotify(vtNotification);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void userConnected()
	{
		try
		{
			String strChannel = request.getString("strChannel");
			String strUserName = request.getString("strUserName");
			String strStartDate = request.getString("strStartDate");
			String strHost = request.getString("strHost");
			((DDTPServerMonitor)getRootObject()).addUser(strChannel,strUserName,strStartDate,strHost);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void userDisconnected()
	{
		try
		{
			String strChannel = request.getString("strChannel");
			((DDTPServerMonitor)getRootObject()).removeUser(strChannel);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Packet createPacket()
	{
		return serverMonitor.createPacket();
	}

}
