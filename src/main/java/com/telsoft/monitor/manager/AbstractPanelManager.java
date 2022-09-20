package com.telsoft.monitor.manager;

import com.borland.plaf.borland.BorlandLookAndFeel;
import smartlib.dictionary.DefaultDictionary;
import com.telsoft.monitor.OutputViewer;
import com.telsoft.monitor.manager.util.AppContext;
import com.telsoft.monitor.register.Register;
import smartlib.dictionary.ErrorDictionary;
import smartlib.swing.MessageBox;
import smartlib.swing.Skin;
import smartlib.swing.WindowManager;
import smartlib.util.FileUtil;
import smartlib.util.Global;

import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicSeparatorUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AbstractPanelManager extends JPanel implements AboutListener, AppContext {
    private static Icon iconTELSOFT = new ImageIcon(FileUtil.getResource("resource/com/telsoft/monitor/telsoft.png"));

    static {
        Global.APP_NAME = "Service Manager";
        Global.APP_VERSION = "1.0.0";
        try {
            ErrorDictionary.appendDictionary("resource/com/telsoft/dictionary/Dictionary.txt", true);
            ErrorDictionary.setCurrentLanguage("EN");
            DefaultDictionary.setCurrentLanguage("EN");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JRootPane mJRootPane = null;
    private JPanel pnlMain = new JPanel();
    private JMenu mnuConnection;
    private String configFile = "servers.txt";
    /**
     * initAbout
     *
     * @param pnlContentAbout JPanel
     */
    private JTextPane lblAbout = null;

    public AbstractPanelManager(JRootPane rootPane) {
        try {
            /*UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());*/
            UIManager.setLookAndFeel(new BorlandLookAndFeel() {
                public void provideErrorFeedback(Component component) {
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        FontUIResource fui = new FontUIResource(Skin.FONT_COMMON);
        setUIFont(fui);
        DefaultDictionary.setCurrentLanguage("EN");
        mJRootPane = rootPane;
        mnuConnection = createPopupMenu("Connect");
        try {
            jbInit();
            if (mJRootPane != null) {
                mJRootPane.setJMenuBar(createMenuBar());
            }
            Register.registerBoard(pnlMain);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setUIFont(FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    /**
     * Returns the <code>JRootPane</code> ancestor for this component.
     *
     * @return the <code>JRootPane</code> that contains this component,
     * or <code>null</code> if no <code>JRootPane</code> is found
     */
    public JRootPane getRootPane() {
        return mJRootPane;
    }

    private JMenuItem createMenu(String strText, ActionListener al) {
        JMenuItem mi = new JMenuItem(strText);
        mi.addActionListener(al);
        return mi;
    }

    private JMenu createPopupMenu(String strText) {
        JMenu mi = new JMenu(strText);
        return mi;
    }

    public JMenu getConnectMenu() {
        return mnuConnection;
    }

    /**
     * @return PopupMenu
     */
    protected JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu mnuSystem = new JMenu("System");
        mb.add(mnuSystem);

        JMenuItem mnuConfig = createMenu("Open...", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser open = new JFileChooser(getConfigFile());
                int option = open.showSaveDialog(AbstractPanelManager.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    reload(open.getSelectedFile().getAbsolutePath());
                }
            }
        });
        mnuSystem.add(mnuConfig);
        mnuSystem.add(mnuConnection);
        mnuSystem.add(createMenu("Exit", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                shutdown();
                System.exit(0);
            }
        }));
        JMenu mnuHelp = new JMenu("Help");
        mb.add(mnuHelp);
        mnuHelp.add(createMenu("Console", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    OutputViewer.init();
                    WindowManager.centeredWindow(OutputViewer.getFrame());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.showMessageDialog(JOptionPane.getFrameForComponent(AbstractPanelManager.this),
                            ex, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
                }
            }
        }));

        mnuHelp.add(createMenu("About", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    DialogAbout frm = new DialogAbout(AbstractPanelManager.this, JOptionPane.getFrameForComponent(AbstractPanelManager.this),
                            Global.APP_NAME, true);
                    WindowManager.centeredWindow(frm);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.showMessageDialog(JOptionPane.getFrameForComponent(AbstractPanelManager.this),
                            ex, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
                }
            }
        }));

        JSeparator sep = new JSeparator();
        sep.setUI(new SpaceSeparatorUI());
        mb.add(sep);

        JMenuItem miTelsoft = new JMenuItem(iconTELSOFT);
        miTelsoft.setUI((ButtonUI) UIManager.getUI(new JButton()));
        mb.add(miTelsoft);
        return mb;
    }

    public void reload(String newFile) {
        shutdown();
        this.setConfigFile(newFile);
        initServers();

    }

    public JPanel getPanelBoard() {
        return pnlMain;
    }

    public abstract void initServers();

    public abstract void shutdown();

    protected abstract void jbInit() throws Exception;

    public void initAbout(final JPanel pnlContentAbout) {
        if (lblAbout == null) {
            String strAbout = "<b>" + Global.APP_NAME + "</b><br>Version 1.0";
            lblAbout = new JTextPane();
            lblAbout.setContentType("text/html");
            lblAbout.setText(strAbout);
            lblAbout.setEditable(false);
        }
        pnlContentAbout.add(lblAbout,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    /**
     * initLicense
     *
     * @param pnlContentLicense JPanel
     */
    public void initLicense(final JPanel pnlContentLicense) {
    }

    static class SpaceSeparatorUI extends BasicSeparatorUI {
        public void paint(Graphics g, JComponent c) {
        }
    }
}
