package com.telsoft.monitor.register;

import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;

import com.telsoft.monitor.ddtp.*;
import com.telsoft.monitor.manager.*;
import com.telsoft.monitor.manager.tree.*;

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
public final class Register
{
	public static class MonitorInfo
	{
		public Class monitorClass;
		public String strType;
	}

	public static class MonitorEditorInfo
	{
		public Class monitorClass;
		public Class editorClass;
		public MonitorEditor editorCache = null;
	}

	private static Vector mvtMonitorInfor = new Vector();
	private static Vector mvtMonitorEditorInfor = new Vector();
	private static JComponent ctnEditor = null;

	static
	{
		registerMonitorType(DDTPServerMonitor.class,"Service Monitor");
		registerMonitorEditor(DDTPServerMonitor.class,com.telsoft.monitor.ddtp.editor.DDTPEditor.class);
		registerMonitorEditor(DDTPThreadMonitor.class,com.telsoft.monitor.ddtp.editor.ThreadEditor.class);

		registerMonitorEditor(FolderObject.class, FolderEditor.class);
	}

	/**
	 *
	 * @param monitorClass Class
	 * @param strType String
	 */
	public static void registerMonitorType(Class monitorClass,String strType)
	{
		MonitorInfo moInfor = new MonitorInfo();
		moInfor.monitorClass = monitorClass;
		moInfor.strType = strType;
		mvtMonitorInfor.add(moInfor);
	}

	/**
	 *
	 * @param monitorClass Class
	 * @param monEditor Class
	 */
	public static void registerMonitorEditor(Class monitorClass,Class monEditor)
	{
		MonitorEditorInfo moInfor = new MonitorEditorInfo();
		moInfor.monitorClass = monitorClass;
		moInfor.editorClass = monEditor;
		mvtMonitorEditorInfor.add(moInfor);
	}

	/**
	 *
	 * @param obj MonitorObject
	 * @return String
	 */
	public static String getTypeForObject(MonitorObject obj)
	{
		return getTypeForClass(obj.getClass());
	}

	/**
	 *
	 * @param obj MonitorObject
	 * @return String
	 */
	public static String getTypeForClass(Class obj)
	{
		for(int i = 0;i < mvtMonitorInfor.size();i++)
		{
			MonitorInfo moInfor = (MonitorInfo)mvtMonitorInfor.elementAt(i);
			if(moInfor.monitorClass.equals(obj))
			{
				return moInfor.strType;
			}
		}
		return "(unknown type)";
	}

	/**
	 *
	 * @param ctn Container
	 */
	public static void registerBoard(JComponent ctn)
	{
		ctnEditor = ctn;
	}

	/**
	 *
	 * @return Vector
	 */
	public static Vector getMonitorTypes()
	{
		return mvtMonitorInfor;
	}

	/**
	 *
	 * @param obj MonitorObject
	 * @return MonitorEditor
	 * @throws Exception
	 */
	public static MonitorEditor getMonitorEditorForObject(MonitorObject obj) throws Exception
	{
		return getMonitorEditorForClass(obj.getClass(),obj.getModel());
	}

	/**
	 *
	 * @param clz Class
	 * @param model ManagerModel
	 * @return MonitorEditor
	 * @throws Exception
	 */
	public static MonitorEditor getMonitorEditorForClass(Class clz,ManagerModel model) throws Exception
	{
		for(int i = 0;i < mvtMonitorEditorInfor.size();i++)
		{
			MonitorEditorInfo moInfor = (MonitorEditorInfo)mvtMonitorEditorInfor.elementAt(i);
			if(moInfor.monitorClass.equals(clz))
			{
				if(moInfor.editorCache == null)
				{
					Constructor cnt = moInfor.editorClass.getConstructor(new Class[]
						{ManagerModel.class});
					moInfor.editorCache = (MonitorEditor)cnt.newInstance(new Object[]
						{model});
				}
				return moInfor.editorCache;
			}
		}
		throw new Exception("There is no editor is registered for class " + clz.getName());
	}

	/**
	 *
	 * @return Container
	 * @throws Exception
	 */
	public static JComponent getBoard() throws Exception
	{
		if(ctnEditor == null)
		{
			throw new Exception("There is no board is registered");
		}
		return ctnEditor;
	}
}
