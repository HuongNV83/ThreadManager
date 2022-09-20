package com.telsoft.monitor.ddtp.editor;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import smartlib.dictionary.*;
import com.telsoft.monitor.ddtp.*;
import smartlib.swing.*;
import smartlib.transport.*;

public class DialogKillSession extends JXDialog {
    ////////////////////////////////////////////////////////
    private DDTPServerMonitor mServer;
    public int miReturnValue = JOptionPane.CANCEL_OPTION;
    ////////////////////////////////////////////////////////
    JXCheckList tblSession;
    private Vector vtData;

    public DialogKillSession(Component parent, DDTPServerMonitor mServer, Vector vtData) throws Exception {
        super(parent, true);
        this.mServer = mServer;
        this.vtData = vtData;
        jbInit();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

    ////////////////////////////////////////////////////////

    private void jbInit() throws Exception {
        buildForm();
        tblSession = (JXCheckList) getFormData().getControl("Session");
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
        Vector vtSessionData = tblSession.getData();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < vtSessionData.size(); i++) {
            Vector vtRow = tblSession.getRow(i);
            final String strSessionId = (String) vtRow.get(0);
            final boolean bChecked = "true".equalsIgnoreCase(String.valueOf(vtRow.get(5)));
            if (bChecked && !strSessionId.equals("")) {
                if (mServer != null) {
                    try {
                        Packet request = mServer.createPacket();
                        request.setRequestID(String.valueOf(System.currentTimeMillis()));
                        request.setString("SessionId", strSessionId);
                        Packet response = mServer.request("killSession", request);
                        if (response.getException() != null) {
                            throw response.getException();
                        } else {
                            buf.append(strSessionId).append(":").append(response.getReturn()).append("\r\n");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        buf.append(strSessionId).append(":").append(ErrorDictionary.getString(ex)).append("\r\n");
                    }
                }
            }
        }
        if (buf.length() > 0) {
            MessageBox.showMessageDialog(DialogKillSession.this, buf.toString());
        }

    }
}
