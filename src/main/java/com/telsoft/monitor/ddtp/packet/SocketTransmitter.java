package com.telsoft.monitor.ddtp.packet;

import java.net.*;
import java.util.*;

import com.telsoft.monitor.ddtp.*;
import smartlib.util.*;
import com.telsoft.monitor.util.AbstractSocket;
import smartlib.transport.*;
import smartlib.transport.client.*;


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
public class SocketTransmitter implements Runnable, Transmitter
{
    public static final int MAX_QUEUE_SIZE = 1024;
    protected Vector mvtRequest = new Vector();
    protected Vector mvtResponse = new Vector();
    protected Thread mthrMain;
    protected String mstrUserName;
    protected String mstrUserID;
    private String mstrPackage = null;
    public Object mobjParent;
    public AbstractSocket msckMain;
    public Date dtStart = null;
    public SocketServer processor = null;
    public static int iWaitTimeOut = 120;
	private DDTPServerMonitor mserverMonitor;

	public SocketTransmitter(AbstractSocket sck,DDTPServerMonitor serverMonitor)
    {
        msckMain = sck;
        mserverMonitor = serverMonitor;
		processor = new SocketServer(this, serverMonitor);
    }

    public String getPackage()
    {
        return mstrPackage;
    }

    public void setPackage(String strPackage)
    {
        mstrPackage = strPackage;
    }

    public void run()
    {
        try
        {
            dtStart = new Date();
            while (isOpen())
            {
                Packet ddtp = mserverMonitor.createPacket(msckMain.getInputStream());
                if (ddtp != null)
                {
                    if (ddtp.getResponseID().length() > 0) // Is response
                    {
                        while (mvtResponse.size() >= MAX_QUEUE_SIZE)
                            mvtResponse.removeElementAt(0);
                        mvtResponse.add(ddtp);
                    } else
                    {
                        while (mvtRequest.size() >= MAX_QUEUE_SIZE)
                            mvtRequest.removeElementAt(0);
                        mvtRequest.add(ddtp);
                    }
                } else
                {
					Thread.sleep(10);
                }
            }
        } catch (SocketException e)
        {
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            close();
        }
    }
    ////////////////////////////////////////////////////////////

    /**
     * Start transmitter thread
     */
    ////////////////////////////////////////////////////////////
    public void start()
    {
        // Start transmitter
        if (mthrMain != null)
            mthrMain.interrupt();
        mthrMain = new Thread(this);
        mthrMain.start();

        // Start processor for transmitter
        processor.start();
    }
    ////////////////////////////////////////////////////////////

    /**
     * gets Packet request from request queue and removes it
     * @param iIndex request index
     * @return request data
     * Author: TrungDD
     */
    ////////////////////////////////////////////////////////////
    public Packet getRequest(int iIndex)
    {
        if (mvtRequest == null)
            return null;
        else if (iIndex < 0 || iIndex >= mvtRequest.size())
            return null;

        Packet ddtpReturn = (Packet)mvtRequest.elementAt(iIndex);
        mvtRequest.removeElementAt(iIndex);
        return ddtpReturn;
    }
    ////////////////////////////////////////////////////////////

    /**
     * gets Packet response from response queue and removes it
     * @param iIndex response index
     * @return response data
     * Author: TrungDD
     */
    ////////////////////////////////////////////////////////////
    public Packet getResponse(int iIndex)
    {
        if (mvtResponse == null)
            return null;

        if (iIndex < 0 || iIndex >= mvtResponse.size())
            return null;

        Packet ddtpReturn = (Packet)mvtResponse.elementAt(iIndex);
        mvtResponse.removeElementAt(iIndex);
        return ddtpReturn;
    }
    ////////////////////////////////////////////////////////////

    /**
     * send and synchronized a packet through a socket
     * @param packet contain request data
     * @throws Exception
     * @author TrungDD
     */
    ////////////////////////////////////////////////////////////
    public synchronized void send(Packet packet) throws Exception
    {
        if (isOpen())
            packet.store(msckMain.getOutputStream());
    }
    ////////////////////////////////////////////////////////////

    /**
     * sends request thought socket
     * @param strClass name of class contain function to invoke
     * @param strFunctionName name of function to invoke
     * @return response data
     * @throws Exception
     * @author TrungDD
     */
    ////////////////////////////////////////////////////////////
    public Packet sendRequest(String strClass, String strFunctionName) throws Exception
    {
        return sendRequest(strClass, strFunctionName, null);
    }
    ////////////////////////////////////////////////////////////

    /**
     * sends request thought socket and wait for response
     * @param request contain request data
     * @param strClass name of class contain function to invoke
     * @param strFunctionName name of function to invoke
     * @return response data
     * @throws Exception
     * @author TrungDD
     */
    ////////////////////////////////////////////////////////////
    public Packet sendRequest(String strClass, String strFunctionName, Packet request) throws Exception
    {
        // Create empty request if passed value is null
        if (request == null)
            request = mserverMonitor.createPacket();

        // Set function name & class name
        request.setFunctionName(strFunctionName);
        if (strClass.indexOf(".") < 0)
            strClass = StringUtil.nvl(mstrPackage, "") + strClass;
        request.setClassName(strClass);

        // Call servlet
        return sendRequest(request);
    }
    ////////////////////////////////////////////////////////////

    /**
     * sends response though socket only, do not wait
     * @param response conatain response data
     * @throws Exception
     * @author TrungDD
     */
    ////////////////////////////////////////////////////////////
    public void sendResponse(Packet response) throws Exception
    {
        send(response);
    }
    ////////////////////////////////////////////////////////////

    /**
     * send request and wait response
     * @param request contain request data
     * @return response data
     * @throws Exception
     * @author TrungDD
     */
    ////////////////////////////////////////////////////////////
    private Packet sendRequest(Packet request) throws Exception
    {
        if (isOpen())
        {
            // Send request
            String strRequestID = request.getRequestID();
            send(request);
            if (strRequestID.length() == 0)
                return null;

            // Wait response
            Packet response = null;
            int iIndex = 0;
            while (isOpen() && (iIndex < iWaitTimeOut))
            {
                response = getResponse(strRequestID);
                if (response != null)
                {
                    // Test error
                    Exception e = response.getException();
                    if (e != null)
					{
						if (e instanceof AppException)
						{
							AppException ae = (AppException) e;
							throw new AppException(e,ae.getContext(),ae.getInfo());
						} else
						throw new Exception(e);
					}
                    return response;
                }
                iIndex++;
                Thread.sleep(1000);
            }

            // Response timeout
            if (isOpen())
            {
                String strDescription = "Quá thời gian time out: " + String.valueOf(iWaitTimeOut) + " giây";
                String strContext = "SocketTransmitter.sendRequest";
                String strInfo = "";
                throw new AppException(strDescription, strContext, strInfo);
            }
        }
        return null;
    }
    ///////////////////////////////////////////////////////

    /**
     * get response in mvtResponse depends on requestID
     * @param strRequestID request id
     * @return response data
     * @author TrungDD
     */
    ///////////////////////////////////////////////////////
    private Packet getResponse(String strRequestID)
    {
        int iThreadIndex = 0;
        int size = mvtResponse.size();
        while (iThreadIndex < size)
        {
            Packet ddtpResponseInfo = (Packet)mvtResponse.elementAt(iThreadIndex);
            if (ddtpResponseInfo.getResponseID().equals(strRequestID))
            {
                mvtResponse.removeElementAt(iThreadIndex);
                return ddtpResponseInfo;
            }
            iThreadIndex++;
        }
        return null;
    }
    ///////////////////////////////////////////////////////

    /**
     * Set user id for transmitter
     * @param strUserID String
     * @author TrungDD
     */
    ///////////////////////////////////////////////////////
    public void setUserID(String strUserID)
    {
        mstrUserID = strUserID;
    }
    ///////////////////////////////////////////////////////

    /**
     * @return user id
     */
    ///////////////////////////////////////////////////////
    public String getUserID()
    {
        return mstrUserID;
    }
    ///////////////////////////////////////////////////////

    /**
     * Set user name for transmitter
     * @param strUserName String
     * @author TrungDD
     */
    ///////////////////////////////////////////////////////
    public void setUserName(String strUserName)
    {
        mstrUserName = strUserName;
    }
    ///////////////////////////////////////////////////////

    /**
     * @return user name
     */
    ///////////////////////////////////////////////////////
    public String getUserName()
    {
        return mstrUserName;
    }
    ///////////////////////////////////////////////////////

    /**
     * Test connection
     * @return true if connected otherwise false
     * @author HiepTH
     */
    ///////////////////////////////////////////////////////
    public boolean isOpen()
    {
        return (msckMain != null && msckMain.isConnected());
    }
    ///////////////////////////////////////////////////////

    /**
     * Close connection
     * @author HiepTH
     */
    ///////////////////////////////////////////////////////
    public void close()
    {
        if (msckMain != null)
        {
            AbstractSocket sck = msckMain;
            msckMain = null;
            try
            {
                sck.close();
            } catch (Exception e)
            {
            }
        }
    }

    public SocketServer getProcessor()
    {
        return processor;
    }

    public void setProcessor(SocketServer sckSrv)
    {
        processor = sckSrv;
    }
}
