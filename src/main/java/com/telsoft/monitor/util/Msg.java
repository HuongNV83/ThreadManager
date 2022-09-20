package com.telsoft.monitor.util;

import smartlib.swing.*;
import smartlib.util.*;
import java.awt.*;

/**
 */
public final class Msg
{
    /**
     * @param strQuestion
     * @return
     */
    public static boolean confirm(String strQuestion)
    {
        return (MessageBox.showConfirmDialog(null,strQuestion,Global.APP_NAME) == MessageBox.OK_OPTION);
    }

	/**
	 * @param strQuestion
	 * @return
	 */
	public static boolean confirm(Component root,String strQuestion)
	{
		return (MessageBox.showConfirmDialog(root,strQuestion,Global.APP_NAME) == MessageBox.OK_OPTION);
	}
}
