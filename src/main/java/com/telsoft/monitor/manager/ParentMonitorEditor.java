package com.telsoft.monitor.manager;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.telsoft.monitor.manager.tree.*;
import com.telsoft.monitor.manager.util.*;
import com.telsoft.monitor.register.*;
import com.telsoft.monitor.util.*;
import com.telsoft.monitor.util.JXMenuItem;
import smartlib.swing.*;

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
public class ParentMonitorEditor extends MonitorEditor
{
	private static final int USER_ACTION = JXMenuItem.START_USER_ACTION + 9000;
	private JXMenuItem miDefRemove = new JXMenuItem("Remove",USER_ACTION + 1,null);
	private JXMenuItem miDefConnect = new JXMenuItem("Connect",USER_ACTION + 2,null);
	private JXMenuItem miDefDisconnect = new JXMenuItem("Disconnect",USER_ACTION + 3,null);
	private JXMenuItem miGroup = new JXMenuItem("Group childs by...",USER_ACTION + 4,null);
	private JMenu mnuMoveToFolder = new JMenu("Move to folder...");
	private JXMenuItem mnuRemoveFromFolder = new JXMenuItem("Remove from folder...",USER_ACTION + 6,null);
	private JXMenuItem miProperties = new JXMenuItem("Properties",USER_ACTION + 7,null);
	private JXMenuItem miCopy = new JXMenuItem("Duplicate",USER_ACTION + 8,null);

	private List<Component> mnuContext = null;
	private ActionListener actMoveToFolder = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				MonitorObject to = (MonitorObject)((JXMenuItem)e.getSource()).ex();
				FolderObject folder = (FolderObject)((JXMenuItem)e.getSource()).ex2();
				if(to.getParent() == null)
				{
					model.removeFromManager(to);
				}
				else
				{
					FolderObject fo2 = (FolderObject)to.getParent();
					fo2.getList().remove(to);
				}
				folder.getList().add(to);
				to.setParent(folder);
				((ServerTreeModel)model).buildModel((DefaultMutableTreeNode)((ServerTreeModel)model).getRoot());
				model.storeConfig();
				model.getMonitorManager().updateUI();
			}
			catch(Exception ex)
			{
				model.getMonitorManager().showException(ex);
			}
		}
	};

	public ParentMonitorEditor(ManagerModel model)
	{
		super(model);
	}

	public List<Component> getContextMenu(MonitorObject obj)
	{
		if(mnuContext == null)
		{
			mnuContext = super.getContextMenu(obj);
			mnuContext.add(miDefConnect);
			mnuContext.add(miDefDisconnect);
			mnuContext.add(new JPopupMenu.Separator());
			mnuContext.add(miDefRemove);
			mnuContext.add(new JPopupMenu.Separator());
			mnuContext.add(miGroup);
			mnuContext.add(mnuMoveToFolder);
			mnuContext.add(mnuRemoveFromFolder);
			mnuContext.add(miCopy);
			mnuContext.add(new JPopupMenu.Separator());
			mnuContext.add(miProperties);
		}
		return mnuContext;
	}

	public void onContextMenu(MonitorObject mo)
	{
		miDefConnect.setEnabled(!mo.isActive());
		miDefDisconnect.setEnabled(mo.isActive());
		mnuRemoveFromFolder.setEnabled(mo.getParent() instanceof FolderObject);
		mnuMoveToFolder.removeAll();
		MonitorObjectList mol = ((ServerTreeModel)model).getRootData();
		for(int i = 0;i < mol.size();i++)
		{
			TreeObject toChild = (TreeObject)mol.get(i);
			if(toChild instanceof FolderObject && mo.getParent() != toChild)
			{
				JXMenuItem mi = new JXMenuItem(toChild.toString(),0,mo);
				mnuMoveToFolder.add(mi);
				mi.setex2(toChild);
				mi.addActionListener(actMoveToFolder);
			}
		}
	}

	public boolean onMenuAction(MonitorObject mo,int id) throws Exception
	{
		switch(id)
		{
			case 0: //default action
			{
				doConnect(mo,true);
				break;
			}
			case USER_ACTION + 1:
			{
				if(Msg.confirm("Remove connection?"))
				{
					mo.setActive(false);
					if(mo.getParent() == null)
					{
						model.removeFromManager(mo);
					}
					else
					{
						FolderObject fo = (FolderObject)mo.getParent();
						fo.getList().remove(mo);
					}
					((ServerTreeModel)model).buildModel((DefaultMutableTreeNode)((ServerTreeModel)model).getRoot());
					model.storeConfig();
					return true;
				}
			}
			case USER_ACTION + 2:
			{
				doConnect(mo,true);
				break;
			}
			case USER_ACTION + 3:
			{
				if(Msg.confirm("Disconnect?"))
				{
					mo.setActive(false);
				}
				return true;
			}
			case USER_ACTION + 4:
			{
				try
				{
					TreeObject to = mo;
					DialogGroup dlgGroup = new DialogGroup(JOptionPane.getFrameForComponent(model.getRootComponent()),"Group childs by...",true);
					dlgGroup.buildModel(to);
					WindowManager.centeredWindow(dlgGroup);
					if(dlgGroup.getModalCode() == JOptionPane.OK_OPTION)
					{
						Vector vtKeys = dlgGroup.getGroupKeys();
						to.setGroup(vtKeys);
						((ServerTreeModel)model).buildModel(to.getContainerNode());
						((ServerTreeModel)model).nodeStructureChanged(to.getContainerNode());
						return true;
					}
				}
				catch(Exception ex)
				{
					model.getMonitorManager().showException(ex);
				}
				break;
			}
			case USER_ACTION + 7:
			{
				try
				{
					DialogProperties dlgProperties = new DialogProperties(mo,JOptionPane.getFrameForComponent(model.getRootComponent()),"Properties",true);
					WindowManager.centeredWindow(dlgProperties);
				}
				catch(Exception ex)
				{
					model.getMonitorManager().showException(ex);
				}
				break;
			}
			case USER_ACTION + 6:
			{
				try
				{
					if(mo.getParent() != null)
					{
						FolderObject fo2 = (FolderObject)mo.getParent();
						fo2.getList().remove(mo);
						mo.setParent(null);
						model.addToManager(mo);
						((ServerTreeModel)model).buildModel((DefaultMutableTreeNode)((ServerTreeModel)model).getRoot());
						model.storeConfig();
						return true;
					}
				}
				catch(Exception ex)
				{
					model.getMonitorManager().showException(ex);
				}
				break;
			}
			case USER_ACTION + 8:
			{
				try
				{
					MonitorEditor editor = Register.getMonitorEditorForObject(mo);
					MonitorObject moDuplicated = editor.connectNew(model.getRootComponent(),model,mo);
					if(moDuplicated != null)
					{
						Object parent = mo.getParent();
						FolderObject folder = null;
						if(parent instanceof FolderObject)
						{
							folder = (FolderObject)parent;
						}
						int i = ((ServerTreeModel)model).indexOf(moDuplicated);
						if(i == -1)
						{
							if(folder == null)
							{
								model.addToManager(moDuplicated);
							}
							else
							{
								folder.getList().add(moDuplicated);
								moDuplicated.setParent(folder);
							}
						}
						else
						{
							moDuplicated = (MonitorObject)((ServerTreeModel)model).getChild(((ServerTreeModel)model).getRoot(),i);
						}

						((ServerTreeModel)model).buildModel((DefaultMutableTreeNode)((ServerTreeModel)model).getRoot());
						model.storeConfig();
						model.getMonitorManager().updateUI();

						new Thread(new InvokeWithMonitorObject(moDuplicated,model.getRootComponent())
						{
							public void run()
							{
								try
								{
									mmo.setActive(true);
								}
								catch(Exception ex)
								{
									model.getMonitorManager().showException(ex);
								}
							}
						}).start();
						return true;
					}
				}
				catch(Exception ex)
				{
					model.getMonitorManager().showException(ex);
				}
				break;
			}
		}
		return false;
	}

	protected void doConnect(MonitorObject mo,boolean showDialog)
	{
		try
		{
			MonitorEditor editor = Register.getMonitorEditorForObject(mo);
			MonitorObject mo1 = editor.connect(model.getRootComponent(),mo,showDialog);
			if(mo1 != null)
			{
				model.storeConfig();
				model.getMonitorManager().updateUI();
				new Thread(new InvokeWithMonitorObject(mo1,model.getRootComponent())
				{
					public void run()
					{
						try
						{
							mmo.setActive(true);
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
							String strMessage = "Connecting to " + mmo.toString() + "...";
							MessageBox.showMessageDialog(mroot,ex,strMessage,MessageBox.ERROR_MESSAGE);
						}
					}
				}).start();
			}
		}
		catch(Exception ex)
		{
			model.getMonitorManager().showException(ex);
		}
	}
}
