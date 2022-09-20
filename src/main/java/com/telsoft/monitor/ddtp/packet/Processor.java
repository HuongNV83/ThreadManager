package com.telsoft.monitor.ddtp.packet;

import java.lang.reflect.*;
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
public class Processor
{
    public static Packet processRequest(Object objCaller, Packet request) throws Exception
    {
        // Get class name & create class instance
        String strClassName = request.getClassName();
        if (strClassName.length() == 0)
            throw new Exception("Class name was not passed");
        Class cls = Class.forName(strClassName);
        Object obj = cls.newInstance();
        if (!(obj instanceof ProcessorStorage))
            throw new Exception("Class '" + strClassName + "' must be a ProcessorStorage");
        ProcessorStorage storage = (ProcessorStorage)obj;
        String strFunctionName = request.getFunctionName();
        if (strFunctionName.length() == 0)
            throw new Exception("Function name was not passed");
        Method method = cls.getMethod(strFunctionName, null);
        if (method == null)
            throw new Exception("Function '" + strClassName + "." + strFunctionName + "' was not declared");
        // Check function
        if (Modifier.isAbstract(method.getModifiers()))
            throw new Exception("Function '" + strClassName + "." + strFunctionName + "' was not implemented");
        if (!Modifier.isPublic(method.getModifiers()))
            throw new Exception("Function '" + strClassName + "." + strFunctionName + "' is not public");

        // Invoke function
        storage.setCaller(objCaller);
        storage.setRequest(request);
        try
        {
            storage.prepareProcess();
            obj = method.invoke(storage, null);
            storage.processCompleted();
        } catch (InvocationTargetException e)
        {
            storage.processFailed();
            if (e.getTargetException() instanceof Exception)
                throw (Exception)e.getTargetException();
            throw new Exception(e.getTargetException());
        }

        // Response
        Packet response = storage.getResponse();
        response.setReturn(obj);
        return response;
    }
}
