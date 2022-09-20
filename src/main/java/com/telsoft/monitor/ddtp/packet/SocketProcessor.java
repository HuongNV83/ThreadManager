package com.telsoft.monitor.ddtp.packet;

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
public abstract class SocketProcessor implements ProcessorStorage
{
    public SocketTransmitter channel = null;
    public Packet request = null;
    public Packet response = null;
    //	public AuthenticateInterface authenticator = null;
    //	public LogInterface log = null;

    public abstract Packet createPacket();

	public void afterCreateInstance()
	{
		response = createPacket();
	}

    protected String getModuleName()
    {
        return null;
    }
    /////////////////////////////////////////////////////////////////
    // Override function
    /////////////////////////////////////////////////////////////////

    public Packet getResponse()
    {
        return response;
    }
    /////////////////////////////////////////////////////////////////

    public void setRequest(Packet request)
    {
        this.request = request;
    }
    /////////////////////////////////////////////////////////////////

    public void setCaller(Object objCaller)
    {
        if (objCaller instanceof SocketTransmitter)
            channel = (SocketTransmitter)objCaller;
    }
    /////////////////////////////////////////////////////////////////

    /**
     *
     * @throws Exception
     */
    /////////////////////////////////////////////////////////////////
    public void prepareProcess() throws Exception
    {
    }
    /////////////////////////////////////////////////////////////////

    /**
     *
     */
    /////////////////////////////////////////////////////////////////
    public void processCompleted()
    {
    }
    /////////////////////////////////////////////////////////////////

    /**
     *
     */
    /////////////////////////////////////////////////////////////////
    public void processFailed()
    {
    }
}
