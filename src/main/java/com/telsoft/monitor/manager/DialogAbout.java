package com.telsoft.monitor.manager;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import smartlib.util.*;

public class DialogAbout extends JDialog
{
	private JPanel pnlAbout = new JPanel();
	private JTabbedPane jTabbedPane1 = new JTabbedPane();
	private JScrollPane pnProperties = new JScrollPane();
	private JScrollPane pnEnvironment = new JScrollPane();
	private JTable tblProperties = new JTable();
	private JTable tblEnvironment = new JTable();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JPanel pnlButton = new JPanel();
	private JButton btnClose = new JButton();

	DefaultTableModel tmProperties = new DefaultTableModel()
	{
		public boolean isCellEditable(int row,int column)
		{
			return false;
		}
	};
	DefaultTableModel tmEnvironment = new DefaultTableModel()
	{
		public boolean isCellEditable(int row,int column)
		{
			return false;
		}

	};
	private AboutListener aboutListener;

	public DialogAbout(AboutListener aboutListener,Frame owner,String title,boolean modal)
	{
		super(owner,title,modal);
		this.aboutListener = aboutListener;
		try
		{
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			jbInit();
			setSize(520,400);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public DialogAbout(AboutListener aboutListener)
	{
		this(aboutListener,new Frame(),Global.APP_NAME,true);
	}

	/**
	 *
	 * @param strTitle String
	 * @param pane JTabbedPane
	 * @return JPanel
	 */
	private static JPanel createContent(String strTitle,JTabbedPane pane)
	{
		Border border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148,145,140));
		JPanel pnlContainer = new JPanel(new GridBagLayout());
		JLabel lbTitle = new JLabel();
		lbTitle.setFont(new Font("Tahoma",Font.ITALIC,18));
		lbTitle.setText(strTitle);
		JPanel pnlContent = new JPanel(new GridBagLayout());
		pnlContent.setBorder(border1);
		pnlContainer.add(lbTitle,new GridBagConstraints(0,0,1,1,1.0,0.0
			,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
			new Insets(5,15,5,5),0,0));
		pnlContainer.add(pnlContent,
						 new GridBagConstraints(0,1,1,1,1.0,1.0
												,GridBagConstraints.CENTER,
												GridBagConstraints.BOTH,
												new Insets(0,15,2,2),0,0));
		pane.add(strTitle,pnlContainer);
		return pnlContent;
	}

	private void jbInit() throws Exception
	{
		pnlAbout.setLayout(gridBagLayout1);
		btnClose.setText("Close");
		btnClose.addActionListener(new DialogAbout_btnClose_actionAdapter(this));
		pnProperties.setBorder(BorderFactory.createEmptyBorder());
		pnEnvironment.setBorder(BorderFactory.createEmptyBorder());
		getContentPane().add(pnlAbout);
		pnProperties.getViewport().add(tblProperties);
		pnEnvironment.getViewport().add(tblEnvironment);

		JPanel pnlContentAbout = createContent("About",jTabbedPane1);
		JPanel pnlContent = createContent("System properties",jTabbedPane1);
		pnlContent.add(pnProperties,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		pnlContent = createContent("Environment Variables",jTabbedPane1);
		pnlContent.add(pnEnvironment,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
		JPanel pnlContentLicense = createContent("License",jTabbedPane1);

		pnlAbout.add(pnlButton,new GridBagConstraints(0,1,1,1,1.0,0.0
			,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
			new Insets(0,0,0,0),0,0));
		pnlButton.add(btnClose);
		pnlAbout.add(jTabbedPane1,new GridBagConstraints(0,0,1,1,1.0,1.0
			,GridBagConstraints.CENTER,GridBagConstraints.BOTH,
			new Insets(0,0,0,0),0,0));
		tmProperties.addColumn("Name");
		tmProperties.addColumn("Value");
		Enumeration keys = System.getProperties().keys();
		while(keys.hasMoreElements())
		{
			Object key = keys.nextElement();
			tmProperties.addRow(new Object[]
								{key,
								System.getProperty(key.toString())});
		}

		tmEnvironment.addColumn("Name");
		tmEnvironment.addColumn("Value");
		Iterator evkeys = System.getenv().keySet().iterator();
		while(evkeys.hasNext())
		{
			Object key = evkeys.next();
			tmEnvironment.addRow(new Object[]
								 {key,System.getenv(key.toString())});
		}

		tblProperties.setModel(tmProperties);
		tblProperties.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblEnvironment.setModel(tmEnvironment);
		tblEnvironment.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if(aboutListener != null)
		{
			aboutListener.initAbout(pnlContentAbout);
			aboutListener.initLicense(pnlContentLicense);
		}
	}

	public void btnClose_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}
}

class DialogAbout_btnClose_actionAdapter implements ActionListener
{
	private DialogAbout adaptee;
	DialogAbout_btnClose_actionAdapter(DialogAbout adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.btnClose_actionPerformed(e);
	}
}
