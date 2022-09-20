package com.telsoft.monitor.manager;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import smartlib.swing.*;
import smartlib.util.*;

public class DialogGroup extends JDialog
{
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JPanel pnlSelection = new JPanel();
	private JPanel pnlButton = new JPanel();
	private JButton btnOK = new JButton();
	private JButton btnClose = new JButton();

	private Vector vtResultKey = new Vector();
	private int bResultCode = JOptionPane.CANCEL_OPTION;

	DefaultTableModel tmSelected = new DefaultTableModel()
	{
		public boolean isCellEditable(int row,int column)
		{
			return false;
		}
	};

	DefaultTableModel tmAvailable = new DefaultTableModel()
	{
		public boolean isCellEditable(int row,int column)
		{
			return false;
		}

	};

	private JTable tblAvailable = new JTable();
	private JTable tblSelected = new JTable();
	private JTableContainer pnAvailable = new JTableContainer(tblAvailable);
	private JTableContainer pnSelected = new JTableContainer(tblSelected);

	private MouseAdapter mouseListener = new MouseAdapter()
	{
		public void mouseClicked(MouseEvent e)
		{
			Object obj = e.getSource();
			if (obj == tblAvailable)
			{
				int iIndex = tblAvailable.getSelectedRow();
				if (iIndex != -1)
				{
					Object strKey = tblAvailable.getValueAt(iIndex,0);
					tmAvailable.removeRow(iIndex);
					tmSelected.addRow(new Object[]{strKey});
				}
			} else if (obj == tblSelected)
			{
				int iIndex = tblSelected.getSelectedRow();
				if (iIndex != -1)
				{
					Object strKey = tblSelected.getValueAt(iIndex,0);
					tmSelected.removeRow(iIndex);
					Vector vtRow = new Vector();
					vtRow.add(strKey);
					tmAvailable.addRow(vtRow);
				}
			}
		}
	};

	public DialogGroup(Frame owner,String title,boolean modal)
	{
		super(owner,title,modal);
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

	public DialogGroup()
	{
		this(new Frame(),Global.APP_NAME,true);
	}

	private void jbInit() throws Exception
	{
		getContentPane().setLayout(gridBagLayout1);
		pnlSelection.setLayout(gridBagLayout1);
		btnClose.setText("Cancel");
		btnClose.addActionListener(new DialogGroup_btnClose_actionAdapter(this));
		btnOK.setText("OK");

		pnAvailable.setBorder(
			  BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),"Available fields"));

		pnSelected.setBorder(
			  BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),"Selected fields"));

		getContentPane().add(pnlSelection,new GridBagConstraints(0,0,1,1,1.0,1.0
			,GridBagConstraints.CENTER,GridBagConstraints.BOTH,
			new Insets(0,0,0,0),0,0));

		getContentPane().add(pnlButton,new GridBagConstraints(0,1,1,1,1.0,0.0
			,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
			new Insets(0,0,0,0),0,0));

		pnlSelection.add(pnAvailable,new GridBagConstraints(0,0,1,1,1.0,0.5
			,GridBagConstraints.CENTER,GridBagConstraints.BOTH,
			new Insets(0,0,0,0),0,0)
			);
		pnlSelection.add(pnSelected,new GridBagConstraints(1,0,1,1,1.0,0.5
			,GridBagConstraints.CENTER,GridBagConstraints.BOTH,
			new Insets(0,0,0,0),0,0)
			);

		pnlButton.add(btnOK);
		pnlButton.add(btnClose);
		btnOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				vtResultKey.clear();
				Vector vtSelected = tmSelected.getDataVector();
				for (int i = 0;i < vtSelected.size();i++)
				{
					Vector vtRow = (Vector) vtSelected.get(i);
					vtResultKey.add(vtRow.get(0));
				}
				bResultCode = JOptionPane.OK_OPTION;
				DialogGroup.this.dispose();
			}
		});

		tmAvailable.addColumn("Field");
		tmSelected.addColumn("Field");

		tblAvailable.setModel(tmAvailable);
		tblAvailable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblAvailable.addMouseListener(mouseListener);
		tblSelected.setModel(tmSelected);
		tblSelected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblSelected.addMouseListener(mouseListener);
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

	/**
	 * getGroupKeys
	 *
	 * @return Vector
	 */
	public Vector getGroupKeys()
	{
		return vtResultKey;
	}

	/**
	 * isGrouped
	 *
	 * @return boolean
	 */
	public boolean isGrouped()
	{
		return vtResultKey.size() > 0;
	}

	/**
	 * buildModel
	 *
	 * @param to TreeObject
	 */
	public void buildModel(TreeObject to)
	{
		Vector vtKeys = new Vector();
		for (int i = 0;i<to.getChildCount();i++)
		{
			TreeObject toChild = (TreeObject) to.getChild(i);
			Iterator iter = toChild.getAttributes().keySet().iterator();
			while (iter.hasNext())
			{
				Object key = iter.next();
				if (!vtKeys.contains(key))
				{
					vtKeys.add(key);
				}
			}
		}
		Vector vtLstGroup = new Vector();
	    if (to.isGrouped())
			vtLstGroup = to.getGroupKeys();

		Vector vtTemp = new Vector();
		for (Object key:vtKeys)
		{
			if (!vtLstGroup.contains(key))
				tmAvailable.addRow(new Object[] {key});
			else
				vtTemp.add(key);
		}

	    for (Object key:vtLstGroup)
		{
			if (vtTemp.contains(key))
				tmSelected.addRow(new Object[] {key});
		}
	}
}

class DialogGroup_btnClose_actionAdapter implements ActionListener
{
	private DialogGroup adaptee;
	DialogGroup_btnClose_actionAdapter(DialogGroup adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.btnClose_actionPerformed(e);
	}
}
