package com.telsoft.monitor.ddtp.packet.smart;

import java.io.*;

import smartlib.transport.message.*;
import smartlib.transport.*;

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
public class SmartPacket extends HessianObject implements Packet
{
	public SmartPacket()
	{
		super();
	}

	/**
	 * Create ddtp from inputstream
	 * @param is inputstream contain ddtp data
	 * @throws IOException
	 */
	public SmartPacket(InputStream is) throws IOException
	{
		super(is);
	}

	public void clear()
	{
		getData().clear();
	}

	public void store(OutputStream output,boolean bCompress) throws IOException
	{
		store(output);
	}
}
