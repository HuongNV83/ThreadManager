package com.telsoft.monitor.util;

import java.io.*;
import java.net.*;

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
public class PlainSocket implements AbstractSocket
{
	private Socket mSocket;

	/**
	 *
	 * @param strHost String
	 * @param iPort int
	 * @throws Exception
	 */
	public PlainSocket(String strHost,int iPort) throws Exception
	{
		mSocket = new Socket(strHost,iPort);
		mSocket.setSoLinger(true,0);

	}

	/**
	 *
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		try
		{
			if(mSocket != null)
			{
				mSocket.close();
			}
		}
		finally
		{
			mSocket = null;
		}
	}

	/**
	 * getInputStream
	 *
	 * @return InputStream
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException
	{
		return mSocket.getInputStream();
	}

	/**
	 *
	 * @return OutputStream
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return mSocket.getOutputStream();
	}

	/**
	 * isConnected
	 *
	 * @return boolean
	 */
	public boolean isConnected()
	{
		return mSocket.isConnected() && !mSocket.isClosed();
	}
}
