package com.telsoft.monitor.ddtp.packet;

import java.io.*;

import com.telsoft.monitor.ddtp.*;
import smartlib.util.*;
import smartlib.transport.*;


public class SocketServer implements Runnable
{
    private Thread mthrMain;
    private SocketTransmitter channel = null;
	protected DDTPServerMonitor serverMonitor;

    ///////////////////////////////////////////////////////////

	/**
	 *
	 * @param channel SocketTransmitter
	 * @param serverMonitor DDTPServerMonitor
	 */
	public SocketServer(SocketTransmitter channel,DDTPServerMonitor serverMonitor)
    {
        this.serverMonitor = serverMonitor;
        this.channel = channel;
    }
    ///////////////////////////////////////////////////////////

    /**
     * Start processor thread
     */
    ///////////////////////////////////////////////////////////
    public void start()
    {
        if (mthrMain != null)
            mthrMain.interrupt();
        mthrMain = new Thread(this);
        mthrMain.start();
    }
    ///////////////////////////////////////////////////////////

    /**
     * always listen from request queue and process request
     * @author
     * - Thai Hoang Hiep
     * - Dang Dinh Trung
     */
    ///////////////////////////////////////////////////////////
    public void run()
    {
        while (isConnected())
        {
            // Get request from queue
            Packet request = channel.getRequest(0);
            Packet response = null;

            try
            {
                // Process request
                if (request == null)
                    continue;
                response = Processor.processRequest(channel, request);
            } catch (Throwable e)
            {
                response = serverMonitor.createPacket();
                if (e instanceof AppException)
                    response.setException((AppException)e);
                else
                    response.setException(new AppException(e.getMessage(), "SocketServer.run", ""));
                e.printStackTrace();
            } finally
            {
                try
                {
                    // Return response
                    if (request != null)
                    {
                        String strRequestID = request.getRequestID();
                        if (strRequestID.length() > 0 && channel != null)
                        {
                            response.setResponseID(strRequestID);
                            channel.sendResponse(response);
                        }
                    }
                } catch (IOException e)
                {
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (request == null)
                {
                    try
                    {
                        Thread.sleep(1000);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    ///////////////////////////////////////////////////////////

    /**
     * Check connection
     * @return true if connected, otherwise false
     * @author HiepTH
     */
    ///////////////////////////////////////////////////////////
    public boolean isConnected()
    {
        return (channel != null && channel.isOpen());
    }

	public DDTPServerMonitor getServerMonitor()
	{
		return serverMonitor;
	}
}
