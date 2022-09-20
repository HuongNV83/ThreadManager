package com.telsoft.monitor.manager.tree;

import com.telsoft.monitor.manager.AbstractPanelManager;
import smartlib.swing.PopupSearchData;
import smartlib.swing.SearchTreeHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelManagerTree extends AbstractPanelManager {
    private MonitorTree trServer;
    private PopupSearchData pop = new PopupSearchData(new SearchTreeHelper());

    public PanelManagerTree(JRootPane rootPane) {
        super(rootPane);
    }

    /**
     * @throws Exception
     */
    protected void jbInit() throws Exception {
        JScrollPane pnlTreeServer = new JScrollPane();
        trServer = new MonitorTree(this, this);
        JSplitPane pnlSplit = new JSplitPane();
        JPanel pnlLeft = new JPanel(new GridBagLayout());

        pnlLeft.add(pnlTreeServer,
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.setLayout(new GridBagLayout());
        this.add(pnlSplit,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        pnlSplit.setLeftComponent(pnlLeft);
        pnlSplit.setRightComponent(getPanelBoard());
        pnlSplit.setDividerLocation(300);
        pnlSplit.setContinuousLayout(true);
        pnlSplit.setOneTouchExpandable(true);
        pnlTreeServer.setViewportView(trServer);
        pnlTreeServer.setBorder(BorderFactory.createCompoundBorder());
        trServer.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!pop.isSearching()) {
                    pop.startSearch(trServer, e);
                }
            }

        });
        initServers();
    }

    /**
     *
     */
    public void initServers() {
        trServer.getManagerModel().loadConfig();
        trServer.expandPath(trServer.getPathForRow(0));
        trServer.repaint();
    }

    /**
     *
     */
    public void shutdown() {
        try {
            trServer.getManagerModel().disconnectAll();
            trServer.getManagerModel().storeConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
