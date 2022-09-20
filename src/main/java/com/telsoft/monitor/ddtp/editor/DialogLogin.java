package com.telsoft.monitor.ddtp.editor;

import smartlib.swing.FormAssistantUtil;
import smartlib.swing.JXDialog;
import smartlib.swing.JXText;
import smartlib.swing.MessageBox;
import smartlib.util.Global;
import smartlib.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Hashtable;

/**
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
public class DialogLogin extends JXDialog {
    ////////////////////////////////////////////////////////
    public int miReturn = JOptionPane.CANCEL_OPTION;

    ////////////////////////////////////////////////////////

    public DialogLogin(Component parent) throws Exception {
        super(parent, true);
        jbInit();
        pack();
        setSize(FormAssistantUtil.getIntegerValue(getConfig().mndRoot, "Width", getWidth()),
                FormAssistantUtil.getIntegerValue(getConfig().mndRoot, "Height", getHeight()));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    ////////////////////////////////////////////////////////

    public void afterOpen() {
        if (((JXText) getFormData().getField("UserName")).getText().length() > 0) {
            getFormData().getField("Password").requestFocusInWindow();
        } else {
            getFormData().getField("UserName").requestFocusInWindow();
        }
    }

    ////////////////////////////////////////////////////////

    private void jbInit() throws Exception {
        ////////////////////////////////////////////////////////
        buildForm();
        Hashtable prt = null;
        try {
            prt = Global.loadHashtable(Global.FILE_CONFIG);
        } catch (Exception e) {
            prt = new Hashtable();
        }
        getFormData().setFieldValue("SessionName", StringUtil.nvl(prt.get("SessionName"), ""));
        getFormData().setFieldValue("UserName", StringUtil.nvl(prt.get("UserName"), ""));
        getFormData().setFieldValue("Host", StringUtil.nvl(prt.get("Host"), ""));
        getFormData().setFieldValue("Port", StringUtil.nvl(prt.get("Port"), ""));

        getFormData().setFieldValue("SSHUserName", StringUtil.nvl(prt.get("SSHUserName"), ""));
        getFormData().setFieldValue("SSHHost", StringUtil.nvl(prt.get("SSHHost"), ""));
        getFormData().setFieldValue("SSHPort", StringUtil.nvl(prt.get("SSHPort"), ""));

        getFormData().setFieldValue("EncryptAlgorithm", StringUtil.nvl(prt.get("Algorithm"), ""));
        getFormData().setFieldValue("Protocol", StringUtil.nvl(prt.get("Protocol"), ""));
        ////////////////////////////////////////////////////////
        // Event map
        ////////////////////////////////////////////////////////
        ((JButton) getFormData().getControl("OK")).addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onOK();
            }
        });
        ////////////////////////////////////////////////////////
        ((JButton) getFormData().getControl("Cancel")).addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClosing();
            }
        });
    }

    ////////////////////////////////////////////////////////

    public void onOK() {
        try {
            ////////////////////////////////////////////////////////
            // Validate input
            ////////////////////////////////////////////////////////
            if (!helper.validateInput()) {
                return;
            }
            ////////////////////////////////////////////////////////
            // Store config
            ////////////////////////////////////////////////////////
            Hashtable prt = null;
            try {
                prt = Global.loadHashtable(Global.FILE_CONFIG);
            } catch (Exception e) {
                prt = new Hashtable();
            }
            prt.put("SessionName", getFormData().getFieldString("SessionName"));
            prt.put("UserName", getFormData().getFieldString("UserName"));
            prt.put("Host", getFormData().getFieldString("Host"));
            prt.put("Port", getFormData().getFieldString("Port"));
            prt.put("SSHUserName", getFormData().getFieldString("SSHUserName"));
            prt.put("SSHHost", getFormData().getFieldString("SSHHost"));
            prt.put("SSHPort", getFormData().getFieldString("SSHPort"));

            prt.put("Algorithm", getFormData().getFieldString("EncryptAlgorithm"));
            prt.put("Protocol", getFormData().getFieldString("Protocol"));
            try {
                Global.storeHashtable(prt, Global.FILE_CONFIG);
            } catch (Exception e) {
            }
            miReturn = JOptionPane.OK_OPTION;
            this.dispose();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            ((JXText) getFormData().mmapFieldList.get(("UserName"))).requestFocus();
            return;
        }
    }
}
