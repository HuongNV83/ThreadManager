package com.telsoft.monitor.manager;

import java.awt.*;
import javax.swing.*;

import smartlib.dictionary.*;
import com.telsoft.monitor.register.*;

public class DialogProperties extends JDialog
{
	private MonitorObject mo;
	private JLabel lblNoProperties = new JLabel(DefaultDictionary.getString("noproperties"));

	public DialogProperties(MonitorObject mo,Frame owner,String title,boolean modal) throws Exception
	{
		super(owner,title,modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.mo = mo;
		jbInit();
		setSize(350,250);

	}

	private void jbInit() throws Exception
	{
		MonitorEditor me = Register.getMonitorEditorForObject(mo);
		if(me != null)
		{
			JPanel pnl = me.getPropertiesPanel(mo);
			if(pnl != null)
			{
				setLayout(new GridLayout());
				add(pnl);
			}
			else
			{
				setLayout(new GridLayout());
				add(lblNoProperties);
			}
		}
	}
}
