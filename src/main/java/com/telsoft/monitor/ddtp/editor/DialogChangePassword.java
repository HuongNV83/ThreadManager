package com.telsoft.monitor.ddtp.editor;

import smartlib.dictionary.ErrorDictionary;
import com.telsoft.monitor.ddtp.packet.SocketTransmitter;
import smartlib.swing.JXDialog;
import smartlib.swing.MessageBox;
import smartlib.transport.Packet;
import smartlib.util.AppException;
import smartlib.util.Global;
import smartlib.util.StringUtil;
import smartlib.util.crypto.EncryptionService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
public class DialogChangePassword extends JXDialog {
    public int miReturnValue = JOptionPane.CANCEL_OPTION;
    ////////////////////////////////////////////////////////
    private SocketTransmitter channel;
    ////////////////////////////////////////////////////////

    public DialogChangePassword(Component parent, SocketTransmitter channel) throws Exception {
        super(parent, true);
        this.channel = channel;
        jbInit();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    ////////////////////////////////////////////////////////

    private void jbInit() throws Exception {
        ////////////////////////////////////////////////////////
        buildForm();
        ////////////////////////////////////////////////////////
        Hashtable prt = null;
        try {
            prt = Global.loadHashtable(Global.FILE_CONFIG);
        } catch (Exception e) {
            prt = new Hashtable();
        }
        getFormData().setFieldValue("OldEncryptAlgorithm", StringUtil.nvl(prt.get("Algorithm"), ""));
        getFormData().setFieldValue("NewEncryptAlgorithm",
                StringUtil.nvl(prt.get("Algorithm"), "")); ////////////////////////////////////////////////////////
        // Event map
        ////////////////////////////////////////////////////////
        ((JButton) getFormData().getControl("OK")).addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            if (!helper.validateInput())
                return;
            ////////////////////////////////////////////////////////
            if (!getFormData().getFieldString("ConfirmPassword").equals(getFormData().getFieldString("NewPassword"))) {
                MessageBox.showMessageDialog(this, ErrorDictionary.getString("TELSOFT-00007"), Global.APP_NAME,
                        MessageBox.ERROR_MESSAGE);
                getFormData().getField("ConfirmPassword").requestFocus();
                return;
            }
            ////////////////////////////////////////////////////////
            String strOldPassword = getFormData().getFieldString("OldPassword");
            String strNewPassword = getFormData().getFieldString("NewPassword");
            ////////////////////////////////////////////////////////
            if (strOldPassword.length() > 0 && getFormData().getFieldString("OldEncryptAlgorithm").length() > 0) {
                String oldAlg = getFormData().getFieldString("OldEncryptAlgorithm");
                if (oldAlg.equals("NG")) {
                    strOldPassword = new EncryptionService().encrypt(strOldPassword);
                } else
                    strOldPassword = StringUtil.encrypt(strOldPassword, oldAlg);
            }

            if (strNewPassword.length() > 0 && getFormData().getFieldString("NewEncryptAlgorithm").length() > 0) {
                String newAlg = getFormData().getFieldString("NewEncryptAlgorithm");
                if (newAlg.equals("NG")) {
                    strNewPassword = new EncryptionService().encrypt(strNewPassword);
                } else
                    strNewPassword = StringUtil.encrypt(strNewPassword, newAlg);
            }

            Packet request = channel.getProcessor().getServerMonitor().createPacket();
            request.setRequestID(String.valueOf(System.currentTimeMillis()));
            request.setString("OldPassword", strOldPassword);
            request.setString("NewPassword", strNewPassword);
            request.setString("RealPassword", getFormData().getFieldString("NewPassword"));
            channel.sendRequest("ThreadProcessor", "changePassword", request);
            MessageBox.showMessageDialog(this, getDictionary().getString("SuccessMessage"), Global.APP_NAME,
                    JOptionPane.INFORMATION_MESSAGE);
            miReturnValue = JOptionPane.OK_OPTION;
            ////////////////////////////////////////////////////////
            Hashtable prt = Global.loadHashtable(Global.FILE_CONFIG);
            prt.put("Algorithm", getFormData().getFieldString("NewEncryptAlgorithm"));
            Global.storeHashtable(prt, Global.FILE_CONFIG);
            this.dispose();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            if (e instanceof AppException && ((AppException) e).getContext().equals("validatePassword"))
                getFormData().getField("NewPassword").requestFocus();
            else
                getFormData().getField("OldPassword").requestFocus();
            return;
        }
    }
}
