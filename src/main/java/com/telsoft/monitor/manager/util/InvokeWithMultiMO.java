package com.telsoft.monitor.manager.util;

import java.awt.*;

import com.telsoft.monitor.manager.*;

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
public abstract class InvokeWithMultiMO implements Runnable
{
	protected MonitorObject[] mmo;
	protected Component mroot;

	/**
	 *
	 * @param mo MonitorObject
	 */
	public InvokeWithMultiMO(Component root,MonitorObject ...mo)
	{
		mmo = mo;
		mroot = root;
	}

	public final void run()
	{
		StringBuilder buf = new StringBuilder();
		for(MonitorObject mo : mmo)
		{
			runItem(mroot,mo);
		}
	}

	public abstract void runItem(Component root,MonitorObject mo);
}
