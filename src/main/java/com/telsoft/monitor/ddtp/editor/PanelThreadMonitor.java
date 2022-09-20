package com.telsoft.monitor.ddtp.editor;

import smartlib.dictionary.*;
import com.telsoft.monitor.ddtp.editor.terminal.*;
import smartlib.swing.*;
import smartlib.util.*;

import java.awt.event.*;

import javax.swing.*;
import java.awt.Font;


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
public class PanelThreadMonitor extends JXPanel
{
    private ThreadEditor mthEditor;
    private SwingTerminal txtMonitor;

    /**
     *
     * @param thEditor ThreadEditor
     * @throws Exception
     */
    public PanelThreadMonitor(ThreadEditor thEditor) throws Exception
    {
		super();
        mthEditor = thEditor;
        jbInit();
    }

    /**
     *
     * @return SwingTerminal
     */
    public SwingTerminal createMonitor()
    {
        SwingTerminal st = new SwingTerminal();
        return st;
    }

    /**
     *
     * @throws Exception
     */
    private void jbInit() throws Exception
    {
        buildForm();
        txtMonitor = (SwingTerminal)getFormData().getControl("Monitor");
		Font myFont = new Font("Courier New",Font.PLAIN,12);
		txtMonitor.setFont(myFont);

        ((JLabel)getFormData().getLayout("StatusDesc")).setBorder(Skin.BORDER_LOWRED);
        ((JLabel)getFormData().getLayout("StatusDesc")).setHorizontalAlignment(SwingConstants.CENTER);
        updateStatus();

        ((JButton)getFormData().getControl("Start")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    startThread();
                }
            });

        ((JMenuItem)getFormData().getControl("StopDestroy")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    destroyThread();
                }
            });

        ((JMenuItem)getFormData().getControl("StopNormal")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    stopThread();
                }
            });

        ((JButton)getFormData().getControl("Setting")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setting();
                }
            });

        ((JButton)getFormData().getControl("ViewLog")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    viewLog();
                }
            });

        ((JButton)getFormData().getControl("Process")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    startImmediate();
                }
            });

        ((AbstractButton)getFormData().getControl("ClearAll")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    txtMonitor.setText("");
                }
            });

        ((AbstractButton)getFormData().getControl("ClearSelected")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    clearSelected();
                }
            });

        ((AbstractButton)getFormData().getControl("SelectAll")).addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    txtMonitor.requestFocus();
                    selectAll();
                }
            });
    }

    /**
     *
     */
    public void updateUI()
    {
        super.updateUI();
        if (helper != null && getFormData() != null)
            Skin.applySkin(((JXPopupButton)getFormData().getControl("Stop")).popupMenu);
    }

    /**
     *
     */
    public void updateStatus()
    {
        try
        {
            if (((JButton)getFormData().getControl("Start")) != null)
            {
                Dictionary dic = getDictionary();
                String strThreadName = mthEditor.getCurrentObject().getThreadName();
                if (mthEditor.getCurrentObject().isActive())
                {
                    ((JButton)getFormData().getControl("Start")).setText(dic.getString("Restart"));
                    ((JButton)getFormData().getControl("Start")).setMnemonic(dic.getString("Restart.Mnemonic").charAt(0));
                    getFormData().getControl("Stop").setEnabled(true);
                    ((JLabel)getFormData().getLayout("StatusDesc")).setText(strThreadName + "-" +
                                                                       dic.getString("Status.ThreadStarted"));
                    this.firePropertyChange("Status", "Stopped", "Started");
                } else
                {
                    ((JButton)getFormData().getControl("Start")).setText(dic.getString("Start"));
                    ((JButton)getFormData().getControl("Start")).setMnemonic(dic.getString("Start.Mnemonic").charAt(0));
                    getFormData().getControl("Stop").setEnabled(false);
                    ((JLabel)getFormData().getLayout("StatusDesc")).setText(strThreadName + "-" +
                                                                       dic.getString("Status.ThreadStopped"));
                    this.firePropertyChange("Status", "Started", "Stopped");
                }
            }
        } catch (AppException ex)
        {
        }
    }

    /**
     *
     */
    public void startThread()
    {
        try
        {
            mthEditor.getCurrentObject().startThread();
        } catch (Exception e)
        {
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void stopThread()
    {
        try
        {
            mthEditor.getCurrentObject().stopThread();
        } catch (Exception e)
        {
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void destroyThread()
    {
        try
        {
            mthEditor.getCurrentObject().destroyThread();
        } catch (Exception e)
        {
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    void setting()
    {
        try
        {
            mthEditor.setting(mthEditor.getCurrentObject());
        } catch (Exception e)
        {
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    void schedule()
    {
        try
        {
            mthEditor.schedule(mthEditor.getCurrentObject());
        } catch (Exception e)
        {
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void viewLog()
    {
        try
        {
            mthEditor.viewLog(mthEditor.getCurrentObject());
        } catch (Exception e)
        {
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    void startImmediate()
    {
        try
        {
            mthEditor.getCurrentObject().startImmediate();
        } catch (Exception e)
        {
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    void clearSelected()
    {
    }

    /**
     *
     */
    void selectAll()
    {
    }
}
