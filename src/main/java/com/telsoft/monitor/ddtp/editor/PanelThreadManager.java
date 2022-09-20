package com.telsoft.monitor.ddtp.editor;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;

import com.telsoft.monitor.ddtp.*;
import com.telsoft.monitor.ddtp.editor.terminal.*;
import com.telsoft.monitor.ddtp.packet.*;
import smartlib.swing.*;
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
public class PanelThreadManager extends JXPanel
{
	public static int MAX_LOG_SIZE = 16384;
	public static int MAX_LOG_SIZE_LINE = 200;
	private DDTPServerMonitor server = null;
	private VectorTable tblNotify;

	public void setServer(DDTPServerMonitor server)
	{
		this.server = server;
	}

	public PanelThreadManager()
	{
		try
		{
			jbInit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}

	/**
	 *
	 * @return SwingTerminal
	 */
	public SwingTerminal createBoard()
	{
		SwingTerminal st = new SwingTerminal();
		return st;
	}

	////////////////////////////////////////////////////////

	/**
	 * Init UI
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		buildForm();
		((SwingTerminal)getFormData().getControl("Board")).clear();
		SwingTerminal ep = (SwingTerminal)getFormData().getControl("Board");
		tblNotify = (VectorTable)getFormData().getControl("NotificationList");
		tblNotify.setData(new Vector());
		////////////////////////////////////////////////////////
		// Event handler
		////////////////////////////////////////////////////////
		((VectorTable)getFormData().getControl("SessionList")).addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() > 1)
				{
					((AbstractButton)getFormData().getControl("Kick")).doClick();
				}
			}
		});
		////////////////////////////////////////////////////////
		((AbstractButton)getFormData().getControl("Refresh")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					Packet request = server.createPacket();
					request.setRequestID(String.valueOf(System.currentTimeMillis()));
					Packet response = server.getChannel().sendRequest("ThreadProcessor","queryUserList",request);
					if(response != null)
					{
						((VectorTable)getFormData().getControl("SessionList")).setData((Vector)response.getReturn());
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					MessageBox.showMessageDialog(PanelThreadManager.this,e,Global.APP_NAME,
												 MessageBox.ERROR_MESSAGE);
				}
			}
		});

		////////////////////////////////////////////////////////
		((AbstractButton)getFormData().getControl("Clear")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					server.clearNotify();
					tblNotify.setData(new Vector());
				}
				catch(Exception e)
				{
					e.printStackTrace();
					MessageBox.showMessageDialog(PanelThreadManager.this,e,Global.APP_NAME,
												 MessageBox.ERROR_MESSAGE);
				}
			}
		});

		////////////////////////////////////////////////////////
		((AbstractButton)getFormData().getControl("Kick")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				int iSelected = ((VectorTable)getFormData().getControl("SessionList")).getSelectedRow();
				if(iSelected < 0)
				{
					return;
				}
				int iResult =
					MessageBox.showConfirmDialog(PanelThreadManager.this,getDictionary().getString("ConfirmKick"),
												 Global.APP_NAME,MessageBox.YES_NO_OPTION);
				if(iResult == MessageBox.NO_OPTION || iResult == MessageBox.CLOSED_OPTION)
				{
					return;
				}
				try
				{
					String strChannel =
						(String)((VectorTable)getFormData().getControl("SessionList")).getRow(iSelected).elementAt(0);
					Packet request = server.createPacket();
					request.setRequestID(String.valueOf(System.currentTimeMillis()));
					request.setString("strChannel",strChannel);
					server.getChannel().sendRequest("ThreadProcessor","kickUser",request);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					MessageBox.showMessageDialog(PanelThreadManager.this,e,Global.APP_NAME,
												 MessageBox.ERROR_MESSAGE);
				}
			}
		});
		////////////////////////////////////////////////////////
		((JXText)getFormData().getControl("Message")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if(((JXText)getFormData().getControl("Message")).getText().length() == 0)
				{
					return;
				}

				try
				{
					server.sendMessage(((JXText)getFormData().getControl("Message")).getText());
					((JXText)getFormData().getControl("Message")).setText("");
				}
				catch(Exception e)
				{
					e.printStackTrace();
					MessageBox.showMessageDialog(PanelThreadManager.this,e,Global.APP_NAME,
												 MessageBox.ERROR_MESSAGE);
				}
			}
		});
		////////////////////////////////////////////////////////
		((AbstractButton)getFormData().getControl("ClearAll")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				((SwingTerminal)getFormData().getControl("Board")).clear();
				server.getLogAction().clear();
			}
		});
		////////////////////////////////////////////////////////
		((AbstractButton)getFormData().getControl("ClearSelected")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				((JEditorPane)getFormData().getControl("Board")).setEditable(true);
				((JEditorPane)getFormData().getControl("Board")).replaceSelection("");
				((JEditorPane)getFormData().getControl("Board")).setEditable(false);
			}
		});
		////////////////////////////////////////////////////////
		((AbstractButton)getFormData().getControl("SelectAll")).addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				((JEditorPane)getFormData().getControl("Board")).requestFocus();
				((JEditorPane)getFormData().getControl("Board")).selectAll();
			}
		});
	}

	/**
	 * Remove all component
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	private synchronized void clearAll()
	{
		try
		{
			// Remove all child component
			getContentPane().setVisible(false);
			getFormData().getLayout("Top").removeAll();
			server.getLogAction().clear();
			((SwingTerminal)getFormData().getControl("Board")).clear();
			((VectorTable)getFormData().getControl("SessionList")).setData(new Vector());
			tblNotify.setData(new Vector());
			Thread.sleep(500);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}

	////////////////////////////////////////////////////////

	/**
	 * Show result of log to monitor
	 * @param txt monitor
	 * @param result log content
	 * @author TrungDD
	 */
	////////////////////////////////////////////////////////
	public static void showResult(final SwingTerminal txt,final String result)
	{
		if(result == null || result.equals(""))
		{
			return;
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					if(txt.getModel().getSize() == 0)
					{
						txt.setText(result);
					}
					else
					{
						txt.removeItem(MAX_LOG_SIZE_LINE);
						txt.append(result);
					}
					txt.scrollToLast();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * updateNotify
	 *
	 * @param vector Vector
	 */
	public void updateNotify(Vector vtNotifyList)
	{
		tblNotify.setData(vtNotifyList);
	}

}
