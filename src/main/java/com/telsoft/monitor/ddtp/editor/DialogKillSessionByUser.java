package com.telsoft.monitor.ddtp.editor;

import smartlib.dictionary.ErrorDictionary;
import com.telsoft.monitor.ddtp.DDTPServerMonitor;
import smartlib.swing.JXDialog;
import smartlib.swing.MessageBox;
import smartlib.swing.VectorTable;
import smartlib.transport.Packet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class DialogKillSessionByUser extends JXDialog {
    public int miReturnValue = JOptionPane.CANCEL_OPTION;
    ////////////////////////////////////////////////////////
    VectorTable tblSession;
    ////////////////////////////////////////////////////////
    private DDTPServerMonitor mServer;
    private Vector vtData;

    public DialogKillSessionByUser(Component parent, DDTPServerMonitor mServer, Vector vtData) throws Exception {
        super(parent, true);
        this.mServer = mServer;
        this.vtData = vtData;
        jbInit();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    ////////////////////////////////////////////////////////

    private void jbInit() throws Exception {
        buildForm();
        tblSession = (VectorTable) getFormData().getControl("Session");
        tblSession.setData(vtData);

        ((JButton) getFormData().getControl("btnKill")).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        ////////////////////////////////////////////////////////
        ((JButton) getFormData().getControl("Cancel")).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClosing();
            }
        });
    }

    public void onOK() {
        int i = tblSession.getSelectedRow();

        Vector vtRow = tblSession.getRow(i);
        final String strUserName = (String) vtRow.get(0);
        final String strGateway = (String) vtRow.get(1);
        if (mServer != null) {
            try {
                Packet request = mServer.createPacket();
                request.setRequestID(String.valueOf(System.currentTimeMillis()));
                request.setString("Username", strUserName);
                request.setString("Gateway", strGateway);
                Packet response = mServer.request("killSessionByUser", request);
                if (response.getException() != null) {
                    throw response.getException();
                } else {
                    MessageBox.showMessageDialog(response.getReturn());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.showMessageDialog(ErrorDictionary.getString(ex));
            }
        }
    }
}
