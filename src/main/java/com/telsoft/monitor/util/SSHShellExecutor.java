package com.telsoft.monitor.util;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import smartlib.swing.*;
import com.trilead.ssh2.*;

public class SSHShellExecutor extends JXDialog
{
	private String strSSHPassword;
	private String strSSHUsername;
	private int iSSHPort;
	private String strSSHHost;

	private JXTextArea txtLog = new JXTextArea();
	private JScrollPane pane = new JScrollPane(txtLog);
	private JButton btnClose = new JButton("Close");

	public void log(String strLog)
	{
		txtLog.append(strLog);
		txtLog.append("\r\n");
		txtLog.setCaretPosition(txtLog.getDocument().getLength());
	}

	public void send(Session ss,String s,boolean bAutoFlush) throws IOException
	{
		byte[] buf = s.getBytes();
		send(ss,buf,bAutoFlush);
	}

	public void send(Session ss,String s) throws IOException
	{
		send(ss,s,true);
	}

	public void send(Session ss,byte[] buf,boolean bAutoFlush) throws IOException
	{
		ss.getStdin().write(buf);
		if(bAutoFlush)
		{
			ss.getStdin().flush();
		}
	}

	public int available(Session ss) throws IOException
	{
		return ss.getStdout().available();
	}

	/**
	 * Returns a String from the telnet connection. Blocks
	 * until one is available. No guarantees that the string is in
	 * any way complete.
	 * NOTE: uses Java 1.0.2 style String-bytes conversion.
	 * @return String
	 * @throws IOException
	 */
	public String receive(Session ss) throws IOException
	{
		byte[] bt = new byte[available(ss)];
		int iByteRead = ss.getStdout().read(bt);
		return new String(bt,0,iByteRead);
	}

	public String receiveUntil(Session ss,String token,long timeout) throws IOException
	{
		StringBuffer buf = new StringBuffer();
		long deadline = 0;
		if(timeout >= 0)
		{
			deadline = System.currentTimeMillis() + timeout;
		}
		do
		{
			if(timeout >= 0)
			{
				while(ss.getStdout().available() <= 0)
				{
					if(System.currentTimeMillis() > deadline)
					{
						throw new IOException("Wait '" + token + "' for " + timeout + " miliseconds was timeout\r\n" + buf.toString());
					}
					try
					{
						Thread.sleep(100L);
					}
					catch(InterruptedException ignored)
					{
					}
				}
			}
			buf.append(receive(ss));
		}
		while(buf.indexOf(token) == -1);
		return buf.toString();
	}

	public void execute(final long iTimeout,final String ...strCommand)
	{
		btnClose.setEnabled(false);
		new Thread()
		{
			public void run()
			{
				Connection sshConnection;

				while(!SSHShellExecutor.this.isVisible())
				{
					try
					{
						Thread.currentThread().sleep(100);
					}
					catch(InterruptedException ex1)
					{
						log(ex1.toString());
					}
				}
				try
				{
					log("Trying to connect to " + strSSHHost + ":" + iSSHPort + "...");
					sshConnection = new Connection(strSSHHost,iSSHPort);
					sshConnection.connect();
					log("Connected");
					log("Authenticating");
					boolean isAuthenticated = sshConnection.authenticateWithPassword(strSSHUsername,strSSHPassword);
					if(isAuthenticated == false)
					{
						throw new IOException("Authentication failed.");
					}
					log("Authenticated\r\n");

					try
					{
						Session sshSession = sshConnection.openSession();
						try
						{
							log("Starting shell...");
							sshSession.requestDumbPTY();
							sshSession.startShell();
							String str = receiveUntil(sshSession,"#",iTimeout);
							log(str);
							for(int i = 0;i < strCommand.length;i++)
							{
								send(sshSession,strCommand[i] + "\n");
								/*sshSession.waitUntilDataAvailable(iTimeout);*/
								sshSession.waitForCondition(ChannelCondition.EXIT_STATUS, iTimeout);
								str = receiveUntil(sshSession,"#",iTimeout);
								log(str);
							}
						}
						finally
						{
							if(sshSession != null)
							{
								sshSession.close();
							}
						}
					}
					finally
					{
						if(sshConnection != null)
						{
							sshConnection.close();
						}
						log("Disconnected");
					}
				}
				catch(IOException ex)
				{
					log(ex.toString());
				}
				btnClose.setEnabled(true);
			}
		}.start();
		setVisible(true);
	}

	public SSHShellExecutor(Component parent,boolean bModal,String strSSHHost,int iSSHPort,String strSSHUsername,String strSSHPassword)
	{
		super(parent,bModal);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.strSSHHost = strSSHHost;
		this.iSSHPort = iSSHPort;
		this.strSSHUsername = strSSHUsername;
		this.strSSHPassword = strSSHPassword;

		setLayout(new GridBagLayout());
		getContentPane().add(pane,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),2,2));
		getContentPane().add(btnClose,
							 new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),2,2));
		setSize(500,350);
		txtLog.setBackground(Color.blue);
		txtLog.setForeground(Color.white);
		txtLog.setEditable(false);
		btnClose.addActionListener(new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
	}

	public static void main(String[] args) throws IOException
	{
		SSHShellExecutor exec = new SSHShellExecutor(null,true,"10.50.9.59",22,"root","r00t123");
		exec.execute(30000,"cd /app","cd cps","cd core_lsn","ls -l");
	}
}
