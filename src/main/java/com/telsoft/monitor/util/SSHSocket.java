package com.telsoft.monitor.util;

import java.io.*;

import com.trilead.ssh2.*;

/**
 *
 * <p>Title: Client simulator</p>
 *
 * <p>Description: A part of Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class SSHSocket implements AbstractSocket
{
	private Connection conn;
	private LocalStreamForwarder lsf = null;

	/**
	 *
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		try
		{
			if(lsf != null)
			{
				lsf.close();
			}
			if(conn != null)
			{
				conn.close();
			}
		}
		finally
		{
			lsf = null;
			conn = null;
		}
	}

	/**
	 *
	 * @return InputStream
	 * @throws Exception
	 */
	public InputStream getInputStream() throws IOException
	{
		return lsf.getInputStream();
	}

	/**
	 *
	 * @return OutputStream
	 * @throws Exception
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return lsf.getOutputStream();
	}

	/**
	 *
	 * @param strSSHHost String
	 * @param iSSHPort int
	 * @param strSSHUsername String
	 * @param strSSHPassword String
	 * @param strHost String
	 * @param iPort int
	 * @throws Exception
	 */
	public SSHSocket(String strSSHHost,int iSSHPort,String strSSHUsername,final String strSSHPassword,String strHost,int iPort) throws Exception
	{
		conn = new Connection(strSSHHost,iSSHPort);
		conn.connect();
		boolean isAuthenticated = false;

		if (conn.isAuthMethodAvailable(strSSHUsername, "keyboard-interactive")) {
			InteractiveCallback il = new InteractiveCallback() {
				public String[] replyToChallenge(String name, String instruction, int numPrompts,
								 String[] prompt, boolean[] echo) throws Exception {
					String[] result = new String[numPrompts];
					for (int i = 0; i < numPrompts; i++) {
						String s = prompt[i];
						if (s != null)
						{
							s = s.toLowerCase();
						}
						if (s != null && s.contains("password")) {
							result[i] = strSSHPassword;
						} else {
							result[i] = "";
						}

					}
					return result;
				}
			};
			isAuthenticated = conn.authenticateWithKeyboardInteractive(strSSHUsername, il);
		}

		if (!isAuthenticated && conn.isAuthMethodAvailable(strSSHUsername, "password")) {
			isAuthenticated = conn.authenticateWithPassword(strSSHUsername, strSSHPassword);
		}
		if(isAuthenticated == false)
		{
			throw new IOException("Authentication failed.");
		}

		try
		{
			lsf = conn.createLocalStreamForwarder(strHost,iPort);
		}
		catch(IOException ex)
		{
			conn.close();
			conn =null;
			throw ex;
		}
	}

	public boolean isConnected()
	{
		return conn != null && lsf != null;
	}
}
