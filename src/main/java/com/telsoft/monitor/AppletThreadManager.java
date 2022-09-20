package com.telsoft.monitor;

import javax.swing.*;

import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.manager.tree.*;
import smartlib.swing.*;
import smartlib.util.*;

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
public class AppletThreadManager extends JApplet
{
	public AbstractPanelManager pnlGroup = new PanelManagerTree(this.getRootPane());

	/**
	 *
	 */
	public AppletThreadManager()
	{
		// Init the applet
		try
		{
			jbInit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			System.exit( -1);
		}
	}

	/**
	 *
	 */
	public void start()
	{
	}

	/**
	 *
	 */
	public void stop()
	{
		onExit();
	}

	/**
	 *
	 */
	public void destroy()
	{
		onExit();
	}

	/**
	 *
	 * @throws Exception
	 */
	private void jbInit() throws Exception
	{
		setContentPane(pnlGroup);
	}

	/**
	 *
	 */
	private void onExit()
	{
		if(pnlGroup != null)
		{
			pnlGroup.shutdown();
		}
	}
}
