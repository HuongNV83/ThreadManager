package com.telsoft.monitor.util;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.Lock;

import javax.swing.event.*;

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
public class StringListHolder
{
	public final static int MAX_LOG_SIZE = 200;
	private ArrayList buf = new ArrayList();
	private EventListenerList listenerList = new EventListenerList();
	private Object mOwner = null;
	private Lock lockAccess = new ReentrantLock();

	/**
	 *
	 * @param owner Object
	 */
	public StringListHolder(Object owner)
	{
		mOwner = owner;
	}

	/**
	 *
	 * @param str String
	 */
	public void append(String str)
	{
		lockAccess.lock();
		try
		{
			while(buf.size() > MAX_LOG_SIZE)
			{
				buf.remove(0);
			}
			buf.add(str);
		}
		finally
		{
			lockAccess.unlock();
		}
		fireStateChanged(str);
	}

	/**
	 *
	 */
	public void clear()
	{
		lockAccess.lock();
		try
		{
			buf.clear();
		}
		finally
		{
			lockAccess.unlock();
		}
		fireStateChanged(null);
	}

	/**
	 *
	 * @return String
	 */
	public List getList()
	{
		lockAccess.lock();
		try
		{
			return Collections.unmodifiableList(buf);

		}
		finally
		{
			lockAccess.unlock();
		}
	}

	/**
	 * Adds a <code>ChangeListener</code> to the button.
	 * @param l the listener to be added
	 */
	public void addTextChangeListener(TextChangeListener l)
	{
		listenerList.add(TextChangeListener.class,l);
	}

	/**
	 * Removes a ChangeListener from the button.
	 * @param l the listener to be removed
	 */
	public void removeTextChangeListener(TextChangeListener l)
	{
		listenerList.remove(TextChangeListener.class,l);
	}

	/**
	 *
	 * @param strAppendText String
	 */
	protected void fireStateChanged(String strAppendText)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for(int i = listeners.length - 2;i >= 0;i -= 2)
		{
			if(listeners[i] == TextChangeListener.class)
			{
				TextChangeEvent changeEvent = new TextChangeEvent(mOwner,strAppendText);
				((TextChangeListener)listeners[i + 1]).textChanged(changeEvent);
			}
		}
	}

//	/**
//	 *
//	 * @param txt JTextArea
//	 * @param result String
//	 */
//	public static void appendText(JTextArea txt,String result)
//	{
//		try
//		{
//			if(txt.getText().length() == 0)
//			{
//				txt.setText(result);
//				txt.setSelectionStart(txt.getText().length());
//				txt.setSelectionEnd(txt.getText().length() - 1);
//			}
//			else
//			{
//				if(txt.getText().length() > MAX_LOG_SIZE)
//				{
//					txt.getDocument().remove(0,txt.getText().length() - MAX_LOG_SIZE);
//				}
//				txt.setSelectionStart(txt.getText().length());
//				txt.getDocument().insertString(txt.getText().length(),result,null);
//				txt.setSelectionEnd(txt.getText().length());
//			}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//	}

//	/**
//	 * convertHTML
//	 *
//	 * @param strResult String
//	 * @return String
//	 */
//	public static String convertHTML(String strResult)
//	{
//		if(strResult == null)
//		{
//			return null;
//		}
//		String[] strArr = strResult.split(" ",3);
//		if(strArr.length != 3)
//		{
//			return strResult;
//		}
//		StringBuffer buf = new StringBuffer();
//		buf.append(strArr[0]).append(" ").append(strArr[1]).append(" ");
//		int iPos = strArr[2].indexOf("]");
//		if(strArr[2].startsWith("[") && iPos > 0)
//		{
//			String strCode = strArr[2].substring(1,iPos);
//			boolean appended = false;
//			if(strCode.equals("CONNECTED") || strCode.equals("STARTED"))
//			{
//				appended = true;
//				buf.append("<font color='#0033CC'><b>");
//			}
//			else if(strCode.equals("DISCONNECTED") || strCode.equals("STOPPED"))
//			{
//				appended = true;
//				buf.append("<font color='#CC3300'><b>");
//			}
//
//			String strValue = strArr[2].substring(iPos + 1);
//			buf.append(strValue);
//			if(appended)
//			{
//				buf.append("</b></font>");
//			}
//		}
//		else
//		{
//			buf.append(strArr[2]);
//		}
//		return buf.toString();
//	}
}
