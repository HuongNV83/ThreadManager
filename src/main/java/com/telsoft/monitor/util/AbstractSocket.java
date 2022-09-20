package com.telsoft.monitor.util;

import java.io.*;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public interface AbstractSocket
{
	public boolean isConnected();

	/**
	 *
	 * @throws IOException
	 */
	public void close() throws IOException;

	/**
	 *
	 * @return OutputStream
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException;

	public InputStream getInputStream() throws IOException;
}
