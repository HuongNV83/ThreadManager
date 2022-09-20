package com.telsoft.monitor.ddtp;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.Lock;

import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.util.*;
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
public class DDTPThreadMonitor extends MonitorObject
{
	private String mstrThreadID;
	private String mstrThreadName;
	private int miThreadStatus;
	private StringListHolder txtLogMonitor = new StringListHolder(this);
	private DDTPServerMonitor mParent;
	private boolean mbError;
	private Lock lock = new ReentrantLock();
	private Map mpExtras;

	/**
	 *
	 * @param serverInfor ServerInfor
	 */
	public DDTPThreadMonitor(DDTPServerMonitor serverInfor)
	{
		super(serverInfor.getModel(),false);
		this.setParent(serverInfor);
	}

	public void setParent(MonitorObject parent)
	{
		mParent = (DDTPServerMonitor)parent;
	}

	/**
	 *
	 * @throws Exception
	 */
	public void startThread() throws Exception
	{
		lock.lock();
		try
		{
			Packet request = mParent.createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			mParent.getChannel().sendRequest("ThreadProcessor","startThread",request);
			model.notifyItemChanged(this,this);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public void stopThread() throws Exception
	{
		lock.lock();
		try
		{
			Packet request = mParent.createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			mParent.getChannel().sendRequest("ThreadProcessor","stopThread",request);
			model.notifyItemChanged(this,this);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public void destroyThread() throws Exception
	{
		lock.lock();
		try
		{
			Packet request = mParent.createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			mParent.getChannel().sendRequest("ThreadProcessor","destroyThread",request);
			model.notifyItemChanged(this,this);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public void startImmediate() throws Exception
	{
		lock.lock();
		try
		{

			Packet request = mParent.createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			mParent.getChannel().sendRequest("ThreadProcessor","startImmediate",request);
			model.notifyItemChanged(this,this);
		}
		finally
		{
			lock.unlock();
		}
	}

	public void invokeExtension(String command) throws Exception
	{
		lock.lock();
		try
		{

			Packet request = mParent.createPacket();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			request.setString("Command",command);
			mParent.getChannel().sendRequest("ThreadProcessor","invokeExtension",request);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *
	 * @param strThreadID String
	 */
	public void setThreadID(String strThreadID)
	{
		mstrThreadID = strThreadID;
	}

	/**
	 *
	 * @param threadName String
	 */
	public void setThreadName(String threadName)
	{
		lock.lock();
		try
		{
			if(threadName == null)
			{
				threadName = "";
			}

			if(!threadName.equalsIgnoreCase(mstrThreadName))
			{
				mstrThreadName = threadName;
				model.notifyItemChanged(this,this);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *
	 * @param threadStatus int
	 */
	public void setThreadStatus(int threadStatus)
	{
		lock.lock();
		try
		{
			if(threadStatus != miThreadStatus)
			{
				miThreadStatus = threadStatus;
				model.notifyItemChanged(this,this);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *
	 * @return String
	 */
	public String toString()
	{
		return mstrThreadName;
	}

	/**
	 * getStatus
	 *
	 * @return boolean
	 */
	public int getStatus()
	{
		return miThreadStatus;
	}

	/**
	 * getThreadID
	 *
	 * @return String
	 */
	public String getThreadID()
	{
		return mstrThreadID;
	}

	/**
	 *
	 * @return StringBuffer
	 */
	public StringListHolder getListLog()
	{
		return txtLogMonitor;
	}

	/**
	 *
	 * @return boolean
	 */
	public boolean isActive()
	{
		return getStatus() == ThreadConstant.THREAD_STARTED;
	}

	/**
	 *
	 * @param bActive boolean
	 * @throws Exception
	 */
	public void setActive(boolean bActive) throws Exception
	{
		if(bActive)
		{
			startThread();
		}
		else
		{
			stopThread();
		}
	}

	/**
	 *
	 * @return MonitorObject
	 */
	public MonitorObject getParent()
	{
		return mParent;
	}

	/**
	 * getThreadName
	 *
	 * @return String
	 */
	public String getThreadName()
	{
		return mstrThreadName;
	}

	/**
	 * getDDTPServer
	 *
	 * @return Object
	 */
	public DDTPServerMonitor getDDTPServer()
	{
		return mParent;
	}

	/**
	 *
	 * @param mo MonitorObject
	 * @return boolean
	 */
	public boolean isEqual(MonitorObject mo)
	{
		if(mo == null || !(mo instanceof DDTPThreadMonitor))
		{
			return false;
		}
		DDTPThreadMonitor thrDest = (DDTPThreadMonitor)mo;
		return thrDest.getThreadID().equals(getThreadID());
	}

	/**
	 * setError
	 *
	 * @param bError boolean
	 */
	public void setError(boolean bError)
	{
		lock.lock();
		try
		{
			if(mbError != bError)
			{
				mbError = bError;
				model.notifyItemChanged(this,this);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean isError()
	{
		return mbError;
	}

	public void setExtras(Map mpExtras)
	{
		this.mpExtras = mpExtras;
	}

	public Map getExtras()
	{
		return mpExtras;
	}
}
