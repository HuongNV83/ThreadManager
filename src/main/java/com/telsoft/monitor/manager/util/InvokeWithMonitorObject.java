package com.telsoft.monitor.manager.util;

import com.telsoft.monitor.manager.*;

import java.awt.*;


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
public abstract class InvokeWithMonitorObject implements Runnable
{
    protected MonitorObject mmo;
    protected Component mroot;

    /**
     *
     * @param mo MonitorObject
     */
    public InvokeWithMonitorObject(MonitorObject mo, Component root)
    {
        mmo = mo;
        mroot = root;
    }
}
