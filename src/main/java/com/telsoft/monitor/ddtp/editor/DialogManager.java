package com.telsoft.monitor.ddtp.editor;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import com.telsoft.monitor.manager.*;
import java.util.concurrent.locks.*;

/**
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
public class DialogManager
{
	private Vector mvtWindowList = new Vector();
	private Lock lock = new ReentrantLock();

	class WindowInfo
	{
		Window wnd;
		Class cls;
		MonitorObject monitor;
		String strThreadId;

		public WindowInfo(Window wnd,MonitorObject monitor,String strThreadID)
		{
			this.wnd = wnd;
			this.cls = wnd.getClass();
			this.monitor = monitor;
			this.strThreadId = strThreadID;
		}
	}

	public void remove(Window wnd)
	{
		lock.lock();
		try
		{
			for(int iIndex = mvtWindowList.size() - 1;iIndex >= 0;iIndex--)
			{
				WindowInfo wi = (WindowInfo)mvtWindowList.elementAt(iIndex);
				if(wi.wnd.equals(wnd))
				{
					mvtWindowList.remove(iIndex);
					break;
				}
			}
		} finally
		{
			lock.unlock();
		}
	}

	public boolean addWindow(Window wnd,MonitorObject monitor,String strThreadID)
	{
		lock.lock();
		try
		{
			Window wndInList = getWindow(wnd.getClass(),monitor,strThreadID);
			if(wndInList == null)
			{
				mvtWindowList.add(new WindowInfo(wnd,monitor,strThreadID));
				wnd.addWindowListener(new WindowAdapter()
				{
					public void windowClosed(WindowEvent e)
					{
						remove(e.getWindow());
					}
				});
				return true;
			}
			else
			{
				wnd.dispose();
				return false;
			}
		} finally
		{
			lock.unlock();
		}
	}

	public Window getWindow(Class cls,MonitorObject monitor,String strThreadID)
	{
		lock.lock();
		try
		{
			for(int iIndex = 0;iIndex < mvtWindowList.size();iIndex++)
			{
				WindowInfo wi = (WindowInfo)mvtWindowList.elementAt(iIndex);
				if(wi.monitor.equals(monitor) && wi.strThreadId.equals(strThreadID) && wi.cls.equals(cls))
				{
					return wi.wnd;
				}
			}
			return null;
		} finally
		{
			lock.unlock();
		}
	}

	public void closeAll(MonitorObject monitor)
	{
		lock.lock();
		try
		{
			for(int iIndex = mvtWindowList.size() - 1;iIndex >= 0;iIndex--)
			{
				WindowInfo wi = (WindowInfo)mvtWindowList.elementAt(iIndex);
				if(wi.monitor.equals(monitor))
				{
					if(wi.wnd.isVisible())
					{
						wi.wnd.dispose();
					}
					mvtWindowList.remove(iIndex);
				}
			}
		} finally
		{
			lock.unlock();
		}
	}
}
