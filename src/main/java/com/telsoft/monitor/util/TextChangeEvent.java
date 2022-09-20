package com.telsoft.monitor.util;

import java.util.*;

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
public class TextChangeEvent extends EventObject
{
    private String mstrAppendedText;

    /**
     *
     * @param source Object
     */
    public TextChangeEvent(Object source, String strAppendedText)
    {
        super(source);
        mstrAppendedText = strAppendedText;
    }

    /**
     *
     * @return String
     */
    public String getAppendedText()
    {
        return mstrAppendedText;
    }

}
