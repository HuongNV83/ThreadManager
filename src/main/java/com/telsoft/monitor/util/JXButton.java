package com.telsoft.monitor.util;

import javax.swing.*;

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
public class JXButton extends JButton
{
    public static final int START_USER_ACTION = 3000;
    private int mId;
    private Object mEx;

    /**
     *
     * @param strCaption String
     * @param id int
     * @param ex Object
     */
    public JXButton(String strCaption, int id, Object ex)
    {
        super(strCaption);
        mId = id;
        mEx = ex;
    }

    /**
     *
     * @return int
     */
    public int id()
    {
        return mId;
    }

    /**
     *
     * @return Object
     */
    public Object ex()
    {
        return mEx;
    }

    /**
     *
     * @param ex Object
     */
    public void setex(Object ex)
    {
        mEx = ex;
    }
}
