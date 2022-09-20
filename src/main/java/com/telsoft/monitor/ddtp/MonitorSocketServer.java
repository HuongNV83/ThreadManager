package com.telsoft.monitor.ddtp;

import java.lang.reflect.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.Lock;

import com.telsoft.monitor.ddtp.packet.*;
import smartlib.util.*;
import smartlib.transport.Packet;
import smartlib.transport.ProcessorStorage;

public class MonitorSocketServer extends SocketServer
{
	private Object mobjOwner = null;
	private SocketTransmitter channel = null;
	private Lock lockProcess = new ReentrantLock();

	public MonitorSocketServer(SocketTransmitter channel,Object objOwner,DDTPServerMonitor serverMonitor)
	{
		super(channel,serverMonitor);
		this.channel = channel;
		mobjOwner = objOwner;
	}

	////////////////////////////////////////////////////////

	/**
	 * Call function specified by ddtp request
	 * @param objCaller Object
	 * @param request Packet contain request data
	 * @return response data
	 * @throws Exception
	 * @author Nguyen Cong Khanh
	 */
	////////////////////////////////////////////////////////
	public Packet processRequest(Object objCaller,Packet request) throws Exception
	{
		lockProcess.lock();
		try
		{
			// Get class name & create class instance
			String strClassName = request.getClassName();
			if(strClassName.length() == 0)
			{
				throw new Exception("Class name was not passed");
			}
			Object obj = null;
			Class cls = null;

			// redirect class
			if(strClassName.equals("com.telsoft.monitor.MonitorProcessor") ||
			   strClassName.equals("smartlib.monitor.MonitorProcessor") ||
			   strClassName.equals("com.fss.monitor.MonitorProcessor") ||
			   strClassName.equals("telsoft.monitor.MonitorProcessor"))
			{
				strClassName = MonitorProcessor.class.getName();
			}

			if(strClassName.equals(MonitorProcessor.class.getName()))
			{
				obj = new MonitorProcessor(mobjOwner,serverMonitor);
				cls = MonitorProcessor.class;
			}
			else
			{
				cls = Class.forName(strClassName);
				obj = cls.newInstance();
			}
			// Check class
			if(!(obj instanceof ProcessorStorage))
			{
				throw new Exception("Class '" + strClassName + "' must be a ProcessorStorage");
			}
			ProcessorStorage storage = (ProcessorStorage)obj;

			// Get function name and method
			String strFunctionName = request.getFunctionName();
			if(strFunctionName.length() == 0)
			{
				throw new Exception("Function name was not passed");
			}

			// Get method from class name and function name
			Method method = cls.getMethod(strFunctionName,null);
			if(method == null)
			{
				throw new Exception("Function '" + strClassName + "." + strFunctionName + "' was not declared");
			}

			// Check function
			if(Modifier.isAbstract(method.getModifiers()))
			{
				throw new Exception("Function '" + strClassName + "." + strFunctionName + "' was not implemented");
			}
			if(!Modifier.isPublic(method.getModifiers()))
			{
				throw new Exception("Function '" + strClassName + "." + strFunctionName + "' is not public");
			}

			// Invoke function
			storage.setCaller(objCaller);
			storage.setRequest(request);
			try
			{
				storage.prepareProcess();
				obj = method.invoke(storage,null);
				storage.processCompleted();
			}
			catch(InvocationTargetException e)
			{
				storage.processFailed();
				if(e.getTargetException() instanceof Exception)
				{
					throw(Exception)e.getTargetException();
				}
				throw new Exception(e.getTargetException());
			}

			// Response
			Packet response = storage.getResponse();
			response.setReturn(obj);
			return response;
		}
		finally
		{
			lockProcess.unlock();
		}
	}

	public void run()
	{
		while(isConnected())
		{
			// Get request from queue
			Packet request = channel.getRequest(0);
			Packet response = null;

			try
			{
				// Process request
				if(request == null)
				{
					try
					{
						Thread.sleep(100);
					}
					catch(InterruptedException ex)
					{
					}
					continue;
				}
				response = processRequest(channel,request);
			}
			catch(Throwable e)
			{
				response = serverMonitor.createPacket();
				if(e instanceof AppException)
				{
					response.setException((AppException)e);
				}
				else
				{
					response.setException(new AppException(e.getMessage(),"MonitorSocketServer.run",""));
				}
//				e.printStackTrace();
			}
			finally
			{
				try
				{
					// Return response
					if(request != null)
					{
						String strRequestID = request.getRequestID();
						if(strRequestID.length() > 0 && channel != null)
						{
							response.setResponseID(strRequestID);
							channel.sendResponse(response);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
