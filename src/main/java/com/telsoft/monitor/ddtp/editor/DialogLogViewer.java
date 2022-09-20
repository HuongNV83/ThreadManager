package com.telsoft.monitor.ddtp.editor;

import com.telsoft.monitor.ddtp.packet.*;
import smartlib.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import smartlib.transport.*;


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
public class DialogLogViewer extends JXDialog
{
    ////////////////////////////////////////////////////////
    private String mstrTitle = null;
    private String mstrThreadID = null;
    private Vector mvtLogDir = null;
    private SocketTransmitter mChannel = null;
    ////////////////////////////////////////////////////////

    public DialogLogViewer(Component parent, String strThreadID, String strTitle, Vector vtLogDir,
                           SocketTransmitter channel) throws Exception
    {
        super(parent, true);
        this.mstrThreadID = strThreadID;
        this.mstrTitle = strTitle;
        this.mvtLogDir = vtLogDir;
        this.mChannel = channel;
        jbInit();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    ////////////////////////////////////////////////////////

    public void jbInit() throws Exception
    {
        buildForm();
        ////////////////////////////////////////////////////////
        // Create log file tree
        ////////////////////////////////////////////////////////
        JTree treeLog = (JTree)getFormData().getControl("LogFileList");
        DefaultMutableTreeNode topLog = new DefaultMutableTreeNode("Root log");
        createNodes(topLog);
        treeLog.setModel(new DefaultTreeModel(topLog, false));
        treeLog.putClientProperty("JTree.lineStyle", "Angled");
        treeLog.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeLog.addTreeSelectionListener(new TreeSelectionListener()
            {
                public void valueChanged(TreeSelectionEvent e)
                {
                    DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode)((JTree)getFormData().getControl("LogFileList")).getLastSelectedPathComponent();
                    if (node == null || node.isRoot())
                        return;
                    if (node.isLeaf())
                    {
                        String nodeInfo = (String)node.getUserObject();
                        showLog(nodeInfo);
                    }
                }
            });
        ////////////////////////////////////////////////////////
        // Event map
        ////////////////////////////////////////////////////////
        ((JButton)getFormData().getControl("Delete")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    onDelete();
                }
            });
        ////////////////////////////////////////////////////////
        ((JButton)getFormData().getControl("Close")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    dispose();
                }
            });
        ////////////////////////////////////////////////////////
        updateLanguage();
    }
    ////////////////////////////////////////////////////////

    public void createNodes(DefaultMutableTreeNode top)
    {
        DefaultMutableTreeNode logMonth = null;
        DefaultMutableTreeNode logNode = null;

        for (int iMonthIndex = 0; iMonthIndex < mvtLogDir.size(); iMonthIndex++)
        {
            Vector vtLogMonth = (Vector)mvtLogDir.elementAt(iMonthIndex);
            for (int iLogIndex = 0; iLogIndex < vtLogMonth.size(); iLogIndex++)
            {
                String strValue = (String)vtLogMonth.elementAt(iLogIndex);
                if (iLogIndex == 0)
                {
                    logMonth = new DefaultMutableTreeNode(strValue);
                    top.add(logMonth);
                } else
                {
                    logNode = new DefaultMutableTreeNode(strValue);
                    logMonth.add(logNode);
                }
            }
        }
    }
    ////////////////////////////////////////////////////////

    public void showLog(String strFileName)
    {
        JTree treeLog = (JTree)getFormData().getControl("LogFileList");
        JXTextArea txtContent = (JXTextArea)getFormData().getControl("LogContent");
        treeLog.setEnabled(false);
        try
        {
            // Send command
            Packet request = mChannel.getProcessor().getServerMonitor().createPacket();
            request.setRequestID(String.valueOf(System.currentTimeMillis()));
            request.setString("ThreadID", mstrThreadID);
            request.setString("ThreadLogName", strFileName);
            Packet response = mChannel.sendRequest("ThreadProcessor", "loadThreadLogContent", request);
            if (response != null)
            {
                String strContent = response.getString("LogContent");
                txtContent.setText(strContent);
                txtContent.setCaretPosition(0);
            }
        } catch (Exception e)
        {
            txtContent.setText("");
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, "DialogLogViewer", MessageBox.ERROR_MESSAGE);
        } finally
        {
            treeLog.setEnabled(true);
        }
    }
    ////////////////////////////////////////////////////////

    public void updateLanguage() throws Exception
    {
        super.updateLanguage();
        setTitle(getTitle() + " " + mstrTitle);
    }
    ////////////////////////////////////////////////////////

    public void onDelete()
    {
        JTree treeLog = (JTree)getFormData().getControl("LogFileList");
        JXTextArea txtContent = (JXTextArea)getFormData().getControl("LogContent");
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeLog.getLastSelectedPathComponent();
        if (node.isRoot())
            return;
        if (node.isLeaf())
        {
            if (MessageBox.OK_OPTION ==
                MessageBox.showConfirmDialog(this, getDictionary().getString("DeleteOK"), "Delete confirm"))
            {
                treeLog.setEnabled(false);
                DefaultTreeModel model = (DefaultTreeModel)treeLog.getModel();
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
                String strFileName = (String)node.getUserObject();
                try
                {
                    // Send command
                    Packet request = mChannel.getProcessor().getServerMonitor().createPacket();
                    request.setRequestID(String.valueOf(System.currentTimeMillis()));
                    request.setString("ThreadID", mstrThreadID);
                    request.setString("ThreadLogName", strFileName);
                    Packet response = mChannel.sendRequest("ThreadProcessor", "deleteThreadLog", request);
                    String str = response.getString("DeleteResult");
                    txtContent.setText(str);
                    model.removeNodeFromParent(node);

                    if (parentNode.isLeaf())
                        model.removeNodeFromParent(parentNode);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    MessageBox.showMessageDialog(this, e, "DialogLogViewer", MessageBox.ERROR_MESSAGE);
                } finally
                {
                    treeLog.setEnabled(true);
                }
            }
        }
    }
}
