package com.telsoft.monitor;

import java.awt.event.*;
import javax.swing.*;

import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.manager.tree.*;
import smartlib.swing.*;
import smartlib.util.*;
import org.apache.log4j.*;

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

public class FrameThreadManager extends JFrame
{
	public AbstractPanelManager pnlGroup = new PanelManagerTree(this.getRootPane());

	/**
	 *
	 */
	public FrameThreadManager()
	{
		BasicConfigurator.configure();
		try
		{
			jbInit();
			this.setTitle(Global.APP_NAME);
			this.setSize(800,600);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			System.exit( -1);
		}
	}

	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		setContentPane(pnlGroup);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				onClosing();
			}
		});
	}
	//////////////////////////////////////////////////////////
	public void onClosing()
	{
		try
		{
			pnlGroup.shutdown();
			dispose();
			System.exit(0);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

//	////////////////////////////////////////////////////////
//	// Main entry of client
//	////////////////////////////////////////////////////////
//	static class MyEventQueue extends EventQueue
//	{
//		protected void dispatchEvent(AWTEvent event)
//		{
//			if(event instanceof InputEvent)
//			{
//				System.out.println(event);
//			}
//			super.dispatchEvent(event);
//		}
//	}

	public static void main(String[] args) throws Exception
	{
//		Toolkit.getDefaultToolkit().getSystemEventQueue().push(
//			new MyEventQueue());
//		ClassLoader clsLoader = FrameThreadManager.class.getClassLoader();
//		if (clsLoader instanceof URLClassLoader)
//		{
//			URLClassLoader urlLoader = (URLClassLoader) clsLoader;
//			URL[] urls = urlLoader.getURLs();
//			for (URL url : urls)
//			{
//				System.out.println(url.getPath());
//			}
//		}
//
//
//
		FrameThreadManager frame = new FrameThreadManager();
		WindowManager.centeredWindow(frame);
	}
}
