package com.telsoft.monitor.manager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import smartlib.swing.*;
import smartlib.util.*;

public class DialogNewFolder extends JDialog
{
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JPanel pnlButton = new JPanel();
	private JButton btnOK = new JButton();
	private JButton btnClose = new JButton();
	private JXText txtFolder = new JXText();

	private int bResultCode = JOptionPane.CANCEL_OPTION;

	public DialogNewFolder(Frame owner,String title,boolean modal)
	{
		super(owner,title,modal);
		try
		{
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			jbInit();
			setSize(300,120);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public DialogNewFolder()
	{
		this(new Frame(),Global.APP_NAME,true);
	}

	private void jbInit() throws Exception
	{
		getContentPane().setLayout(gridBagLayout1);
		btnClose.setText("Cancel");
		btnClose.addActionListener(new DialogNewFolder_btnClose_actionAdapter(this));
		btnOK.setText("OK");

		getContentPane().add(txtFolder,new GridBagConstraints(0,0,1,1,1.0,0.0
			,GridBagConstraints.CENTER,GridBagConstraints.BOTH,
			new Insets(0,0,0,0),0,0));

		getContentPane().add(pnlButton,new GridBagConstraints(0,1,1,1,1.0,0.0
			,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
			new Insets(0,0,0,0),0,0));

		pnlButton.add(btnOK);
		pnlButton.add(btnClose);
		btnOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				bResultCode = JOptionPane.OK_OPTION;
				DialogNewFolder.this.dispose();
			}
		});
	}

	public void btnClose_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	/**
	 * getModalCode
	 *
	 * @return boolean
	 */
	public int getModalCode()
	{
		return bResultCode;
	}

	public String getFolderName()
	{
		return txtFolder.getText();
	}
}

class DialogNewFolder_btnClose_actionAdapter implements ActionListener
{
	private DialogNewFolder adaptee;
	DialogNewFolder_btnClose_actionAdapter(DialogNewFolder adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.btnClose_actionPerformed(e);
	}
}
