package com.telsoft.monitor.ddtp.editor;

import smartlib.dictionary.*;
import smartlib.dictionary.Dictionary;
import com.telsoft.monitor.ddtp.packet.*;
import com.telsoft.monitor.util.*;
import smartlib.swing.*;
import smartlib.util.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
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
public class DialogSchedule extends JXDialog implements ControlButtonListener
{
    ////////////////////////////////////////////////////////
    // Member variable
    ////////////////////////////////////////////////////////
    private Dictionary mdicSchedule;
    private String mstrThreadID = null;
    private SocketTransmitter channel = null;
    ////////////////////////////////////////////////////////

    public DialogSchedule(Component parent, String strThreadID, SocketTransmitter channel) throws Exception
    {
        super(parent, true);
        mstrThreadID = strThreadID;
        this.channel = channel;
        jbInit();
        search();
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    ////////////////////////////////////////////////////////

    private void jbInit() throws Exception
    {
        ////////////////////////////////////////////////////////
        buildForm();
        ((PanelControlButton)getFormData().getLayout("ControlButton")).setNormalState();
        onChangeAction(ACTION_NONE, ACTION_NONE);
        ////////////////////////////////////////////////////////
        VectorTable tbl = (VectorTable)getFormData().getField("MonthDay");
        for (int iIndex = 0; iIndex < 31; iIndex++)
        {
            Vector vtRow = new Vector();
            vtRow.add(String.valueOf(iIndex + 1));
            vtRow.add(String.valueOf(iIndex + 1));
            tbl.addRow(vtRow);
        }
        ////////////////////////////////////////////////////////
        getFormData().getControl("ScheduleList").addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() > 1)
                    {
                        ((PanelControlButton)getFormData().getLayout("ControlButton")).btnModify.doClick();
                    }
                }
            });
        ////////////////////////////////////////////////////////
        ((VectorTable)getFormData().getField("WeekDay")).getTableHeader().addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent evt)
                {
                    if (onHeaderClick(((VectorTable)getFormData().getField("WeekDay")), evt.getX()))
                    {
                        evt.consume();
                    }
                }
            });
        ////////////////////////////////////////////////////////
        ((VectorTable)getFormData().getField("MonthDay")).getTableHeader().addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent evt)
                {
                    if (onHeaderClick(((VectorTable)getFormData().getField("MonthDay")), evt.getX()))
                    {
                        evt.consume();
                    }
                }
            });
        ////////////////////////////////////////////////////////
        ((VectorTable)getFormData().getField("YearMonth")).getTableHeader().addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent evt)
                {
                    if (onHeaderClick(((VectorTable)getFormData().getField("YearMonth")), evt.getX()))
                    {
                        evt.consume();
                    }
                }
            });
        ////////////////////////////////////////////////////////
        ((JXCombo)getFormData().getField("ScheduleType")).addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == e.SELECTED)
                    {
                        onChangeScheduleType();
                    }
                }
            });
        ////////////////////////////////////////////////////////
        ((VectorTable)getFormData().getControl("ScheduleList")).getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    fillDetailValue();
                }
            });
    }

    ////////////////////////////////////////////////////////

    public boolean onHeaderClick(VectorTable tbl, int iPosition)
    {
        if (!tbl.isEnabled())
        {
            return false;
        }
        if (tbl.getRowCount() <= 0)
        {
            return false;
        }
        int iColumnIndex = tbl.getColumnModel().getColumnIndexAtX(iPosition);
        if (tbl.getColumn(iColumnIndex).getModelIndex() == 2)
        {
            String strValue = "FALSE";
            if (tbl.getRow(0).elementAt(2).equals(strValue))
            {
                strValue = "TRUE";
            }
            tbl.setAllValueEx(2, strValue);
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////

    public void updateLanguage() throws Exception
    {
        super.updateLanguage();
        onChangeScheduleType();
    }

    ////////////////////////////////////////////////////////

    private void onChangeScheduleType()
    {
        int iSelected = ((JXCombo)getFormData().getField("ScheduleType")).getSelectedIndex();
        if (iSelected == 0) // Daily
        {
            ((JLabel)getFormData().getLayout("AdditionValueDesc")).setText(getDictionary().getString("Day"));
        } else if (iSelected == 1) // Weekly
        {
            ((JLabel)getFormData().getLayout("AdditionValueDesc")).setText(getDictionary().getString("Week"));
        } else if (iSelected == 2) // Monthly
        {
            ((JLabel)getFormData().getLayout("AdditionValueDesc")).setText(getDictionary().getString("Month"));
        } else if (iSelected == 3) // Yearly
        {
            ((JLabel)getFormData().getLayout("AdditionValueDesc")).setText(getDictionary().getString("Year"));
        }
        getFormData().getLayout("Input").updateUI();
    }

    ////////////////////////////////////////////////////////

    private void fillDetailValue()
    {
        try
        {
            ////////////////////////////////////////////////////////
            VectorTable tblSchedule = ((VectorTable)getFormData().getControl("ScheduleList"));
            int iSelected = tblSchedule.getSelectedRow();
            if (iSelected < 0)
            {
                clearDetailValue();
                return;
            }
            Vector vtRow = tblSchedule.getRow(iSelected);
            Dictionary dic = (Dictionary)vtRow.elementAt(4);
            ////////////////////////////////////////////////////////
            String strScheduleType = dic.getString("ScheduleType");
            if (strScheduleType == null || strScheduleType.length() == 0)
            {
                strScheduleType = "0";
            }
            int iScheduleType = Integer.parseInt(strScheduleType);
            String strAdditionValue = dic.getString("AdditionValue");
            if (strAdditionValue == null || strAdditionValue.length() == 0)
            {
                strAdditionValue = "1";
            }
            int iAdditionValue = Integer.parseInt(strAdditionValue);
            String strWeekDay = StringUtil.nvl(dic.getString("WeekDay"), "");
            String strMonthDay = StringUtil.nvl(dic.getString("MonthDay"), "");
            String strYearMonth = StringUtil.nvl(dic.getString("YearMonth"), "");
            ////////////////////////////////////////////////////////
            getFormData().setFieldValue("ScheduleType", String.valueOf(iScheduleType));
            onChangeScheduleType();
            getFormData().setFieldValue("ExecutionTime", dic.getString("ExecutionTime"));
            getFormData().setFieldValue("AdditionValue", String.valueOf(iAdditionValue));
            getFormData().setFieldValue("StartTime", dic.getString("StartTime"));
            getFormData().setFieldValue("EndTime", dic.getString("EndTime"));
            getFormData().setFieldValue("ExpectedDate", dic.getString("ExpectedDate"));
            VectorTable tblWeekDay = ((VectorTable)getFormData().getField("WeekDay"));
            VectorTable tblMonthDay = ((VectorTable)getFormData().getField("MonthDay"));
            VectorTable tblYearMonth = ((VectorTable)getFormData().getField("YearMonth"));
            ////////////////////////////////////////////////////////
            if (strWeekDay.length() == 0)
            {
                tblWeekDay.setAllValueEx(2, "TRUE");
            } else
            {
                for (int iIndex = 0; iIndex < tblWeekDay.getRowCount(); iIndex++)
                {
                    if (strWeekDay.indexOf("," + tblWeekDay.getRow(iIndex).elementAt(0) + ",") >= 0)
                    {
                        tblWeekDay.getRow(iIndex).setElementAt("TRUE", 2);
                    } else
                    {
                        tblWeekDay.getRow(iIndex).setElementAt("FALSE", 2);
                    }
                }
            }
            ////////////////////////////////////////////////////////
            if (strMonthDay.length() == 0)
            {
                tblMonthDay.setAllValueEx(2, "TRUE");
            } else
            {
                for (int iIndex = 0; iIndex < tblMonthDay.getRowCount(); iIndex++)
                {
                    if (strMonthDay.indexOf("," + tblMonthDay.getRow(iIndex).elementAt(0) + ",") >= 0)
                    {
                        tblMonthDay.getRow(iIndex).setElementAt("TRUE", 2);
                    } else
                    {
                        tblMonthDay.getRow(iIndex).setElementAt("FALSE", 2);
                    }
                }
            }
            ////////////////////////////////////////////////////////
            if (strYearMonth.length() == 0)
            {
                tblYearMonth.setAllValueEx(2, "TRUE");
            } else
            {
                for (int iIndex = 0; iIndex < tblYearMonth.getRowCount(); iIndex++)
                {
                    if (strYearMonth.indexOf("," + tblYearMonth.getRow(iIndex).elementAt(0) + ",") >= 0)
                    {
                        tblYearMonth.getRow(iIndex).setElementAt("TRUE", 2);
                    } else
                    {
                        tblYearMonth.getRow(iIndex).setElementAt("FALSE", 2);
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
        }
    }

    ////////////////////////////////////////////////////////

    private void clearDetailValue() throws Exception
    {
        getFormData().setFieldValue("ScheduleType", "0");
        onChangeScheduleType();
        getFormData().setFieldValue("ExecutionTime", "");
        getFormData().setFieldValue("AdditionValue", "");
        getFormData().setFieldValue("StartTime", "");
        getFormData().setFieldValue("EndTime", "");
        getFormData().setFieldValue("ExpectedDate", "");
        ((VectorTable)getFormData().getField("WeekDay")).setAllValueEx(2, "FALSE");
        ((VectorTable)getFormData().getField("MonthDay")).setAllValueEx(2, "FALSE");
        ((VectorTable)getFormData().getField("YearMonth")).setAllValueEx(2, "FALSE");
    }

    ////////////////////////////////////////////////////////

    private void fillDefaultValue() throws Exception
    {
        getFormData().setFieldValue("ScheduleType", "0");
        onChangeScheduleType();
        getFormData().setFieldValue("ExecutionTime", "");
        getFormData().setFieldValue("AdditionValue", "");
        getFormData().setFieldValue("StartTime", "");
        getFormData().setFieldValue("EndTime", "");
        getFormData().setFieldValue("ExpectedDate", "");
        ((VectorTable)getFormData().getField("WeekDay")).setAllValueEx(2, "TRUE");
        ((VectorTable)getFormData().getField("MonthDay")).setAllValueEx(2, "TRUE");
        ((VectorTable)getFormData().getField("YearMonth")).setAllValueEx(2, "TRUE");
    }

    ////////////////////////////////////////////////////////
    // Implementation
    ////////////////////////////////////////////////////////

    public boolean validateInput(int iOldAction, int iNewAction)
    {
        if (iOldAction == ACTION_NONE && (iNewAction == ACTION_MODIFY || iNewAction == ACTION_REMOVE))
        {
            if (((VectorTable)getFormData().getControl("ScheduleList")).getSelectedRow() < 0)
            {
                return false;
            }
        }
        if ((iOldAction == ACTION_ADD || iOldAction == ACTION_ADD_COPY || iOldAction == ACTION_MODIFY) &&
            iNewAction == ACTION_SAVE)
        {
            if (!helper.validateInput())
            {
                return false;
            }
            VectorTable tblWeekDay = ((VectorTable)getFormData().getField("WeekDay"));
            VectorTable tblMonthDay = ((VectorTable)getFormData().getField("MonthDay"));
            VectorTable tblYearMonth = ((VectorTable)getFormData().getField("YearMonth"));
            Dictionary dic = getDictionary();
            int iIndex = 0;
            while (iIndex < tblWeekDay.getRowCount() &&
                   !Boolean.valueOf((String)tblWeekDay.getRow(iIndex).elementAt(2)).booleanValue())
            {
                iIndex++;
            }
            if (iIndex >= tblWeekDay.getRowCount())
            {
                MessageBox.showMessageDialog(this, dic.getString("WeekDayMessage"), Global.APP_NAME,
                                             MessageBox.ERROR_MESSAGE);
                tblWeekDay.requestFocus();
                return false;
            }
            iIndex = 0;
            while (iIndex < tblYearMonth.getRowCount() &&
                   !Boolean.valueOf((String)tblYearMonth.getRow(iIndex).elementAt(2)).booleanValue())
            {
                iIndex++;
            }
            if (iIndex >= tblYearMonth.getRowCount())
            {
                MessageBox.showMessageDialog(this, dic.getString("YearMonthMessage"), Global.APP_NAME,
                                             MessageBox.ERROR_MESSAGE);
                tblYearMonth.requestFocus();
                return false;
            }
            iIndex = 0;
            while (iIndex < tblMonthDay.getRowCount() &&
                   !Boolean.valueOf((String)tblMonthDay.getRow(iIndex).elementAt(2)).booleanValue())
            {
                iIndex++;
            }
            if (iIndex >= tblMonthDay.getRowCount())
            {
                MessageBox.showMessageDialog(this, dic.getString("MonthDayMessage"), Global.APP_NAME,
                                             MessageBox.ERROR_MESSAGE);
                tblMonthDay.requestFocus();
                return false;
            }

            try
            {
                // Calculate execution time
                mdicSchedule = buildScheduleScript();
                Date dtNextExpectedDate = ScheduleUtil.calculateNextDate(mdicSchedule, false, 0);
                if (dtNextExpectedDate == null)
                {
                    MessageBox.showMessageDialog(this, dic.getString("NeverRunMessage"), Global.APP_NAME,
                                                 MessageBox.ERROR_MESSAGE);
                    tblYearMonth.requestFocus();
                    return false;
                }
                int iReturn =
                    MessageBox.showConfirmDialog(this, dic.getString("NextDateMessage", StringUtil.format(dtNextExpectedDate,
                                                                                                          "EEEE dd MMMM, yyyy")),
                                                 Global.APP_NAME, JOptionPane.YES_NO_OPTION);
                if (iReturn == MessageBox.NO_OPTION || iReturn == MessageBox.CLOSED_OPTION)
                {
                    getFormData().setFieldValue("ExpectedDate", Global.FORMAT_DATE().format(dtNextExpectedDate));
                    ((JXDatePlus)getFormData().getField("ExpectedDate")).getEditor().getEditorComponent().requestFocus();
                    ((JTextField)((JXDatePlus)getFormData().getField("ExpectedDate")).getEditor().getEditorComponent()).select(0,
                                                                                                                          ((JXDatePlus)getFormData().getField("ExpectedDate")).getText().length());
                    return false;
                }
                DictionaryNode nd = mdicSchedule.getChild("ExpectedDate");
                nd.mstrValue = Global.FORMAT_DATE().format(dtNextExpectedDate);
            } catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    ////////////////////////////////////////////////////////

    public void onChangeAction(int iOldAction, int iNewAction)
    {
        try
        {
            VectorTable tblSchedule = ((VectorTable)getFormData().getControl("ScheduleList"));
            if (iNewAction == ACTION_NONE)
            {
                // Set control state
                getFormData().setFieldEnabled(false);
                tblSchedule.setEnabled(true);

                // Default focus
                tblSchedule.requestFocus();

                // Fill detail value
                fillDetailValue();
            } else if (iNewAction == ACTION_ADD || iNewAction == ACTION_ADD_COPY || iNewAction == ACTION_MODIFY ||
                       iNewAction == ACTION_SEARCH)
            {
                // Set control state
                getFormData().setFieldEnabled(true);
                tblSchedule.setEnabled(false);

                if (iNewAction == ACTION_ADD)
                {
                    fillDefaultValue();
                } else if (iNewAction == ACTION_SEARCH)
                {
                    clearDetailValue();
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
        }
    }

    ////////////////////////////////////////////////////////

    public Dictionary buildScheduleScript() throws Exception
    {
        ////////////////////////////////////////////////////////
        Dictionary dic = new Dictionary();
        dic.mndRoot.setChildValue("ScheduleType", getFormData().getFieldString("ScheduleType"));
        dic.mndRoot.setChildValue("ExecutionTime", getFormData().getFieldString("ExecutionTime"));
        dic.mndRoot.setChildValue("AdditionValue", getFormData().getFieldString("AdditionValue"));
        dic.mndRoot.setChildValue("StartTime", getFormData().getFieldString("StartTime"));
        dic.mndRoot.setChildValue("EndTime", getFormData().getFieldString("EndTime"));
        dic.mndRoot.setChildValue("ExpectedDate", getFormData().getFieldString("ExpectedDate"));
        ////////////////////////////////////////////////////////
        VectorTable tblWeekDay = ((VectorTable)getFormData().getField("WeekDay"));
        VectorTable tblMonthDay = ((VectorTable)getFormData().getField("MonthDay"));
        VectorTable tblYearMonth = ((VectorTable)getFormData().getField("YearMonth"));
        String str = "";
        boolean bAll = true;
        for (int iIndex = 0; iIndex < tblWeekDay.getRowCount(); iIndex++)
        {
            if (Boolean.valueOf((String)tblWeekDay.getRow(iIndex).elementAt(2)).booleanValue())
            {
                str += tblWeekDay.getRow(iIndex).elementAt(0) + ",";
            } else
            {
                bAll = false;
            }
        }
        if (bAll)
        {
            str = "";
        }
        if (str.length() > 0)
        {
            str = "," + str;
        }
        dic.mndRoot.setChildValue("WeekDay", str);
        ////////////////////////////////////////////////////////
        str = "";
        bAll = true;
        for (int iIndex = 0; iIndex < tblMonthDay.getRowCount(); iIndex++)
        {
            if (Boolean.valueOf((String)tblMonthDay.getRow(iIndex).elementAt(2)).booleanValue())
            {
                str += tblMonthDay.getRow(iIndex).elementAt(0) + ",";
            } else
            {
                bAll = false;
            }
        }
        if (bAll)
        {
            str = "";
        }
        if (str.length() > 0)
        {
            str = "," + str;
        }
        dic.mndRoot.setChildValue("MonthDay", str);
        ////////////////////////////////////////////////////////
        str = "";
        bAll = true;
        for (int iIndex = 0; iIndex < tblYearMonth.getRowCount(); iIndex++)
        {
            if (Boolean.valueOf((String)tblYearMonth.getRow(iIndex).elementAt(2)).booleanValue())
            {
                str += tblYearMonth.getRow(iIndex).elementAt(0) + ",";
            } else
            {
                bAll = false;
            }
        }
        if (bAll)
        {
            str = "";
        }
        if (str.length() > 0)
        {
            str = "," + str;
        }
        dic.mndRoot.setChildValue("YearMonth", str);
        ////////////////////////////////////////////////////////
        return dic;
    }

    ////////////////////////////////////////////////////////

    public boolean add()
    {
        VectorTable tblSchedule = ((VectorTable)getFormData().getControl("ScheduleList"));
        try
        {
            // Make script
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            mdicSchedule.store(os);

            // Call add
            Packet request = channel.getProcessor().getServerMonitor().createPacket();
            request.setRequestID(String.valueOf(System.currentTimeMillis()));
            request.setString("ThreadID", mstrThreadID);
            request.setString("Schedule", new String(os.toByteArray()));
            Packet response = channel.sendRequest("ThreadProcessor", "addSchedule", request);
            String strScheduleID = response.getString("ScheduleID");

            // Update UI
            Vector vtRow = new Vector();
            vtRow.add(strScheduleID);
            vtRow.add(ScheduleUtil.getScheduleDescription(mdicSchedule));
            vtRow.add(Global.FORMAT_DATE().format(ScheduleUtil.getExpectedDate(mdicSchedule)));
            vtRow.add(String.valueOf(ScheduleUtil.getExecutionCount(mdicSchedule)));
            vtRow.add(mdicSchedule);
            tblSchedule.addRow(vtRow);
        } catch (Exception e)
        {
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            ((JXDatePlus)getFormData().getField("ExpectedDate")).requestFocus();
            return false;
        }
        return true;
    }

    ////////////////////////////////////////////////////////

    public boolean modify()
    {
        VectorTable tblSchedule = ((VectorTable)getFormData().getControl("ScheduleList"));
        try
        {
            int iSelected = tblSchedule.getSelectedRow();
            Vector vtRow = tblSchedule.getRow(iSelected);
            String strScheduleID = (String)vtRow.elementAt(0);

            // Make script
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            mdicSchedule.store(os);

            // Call update
            Packet request = channel.getProcessor().getServerMonitor().createPacket();
            request.setRequestID(String.valueOf(System.currentTimeMillis()));
            request.setString("ThreadID", mstrThreadID);
            request.setString("ScheduleID", strScheduleID);
            request.setString("Schedule", new String(os.toByteArray()));
            channel.sendRequest("ThreadProcessor", "updateSchedule", request);

            // Update UI
            vtRow.setElementAt(ScheduleUtil.getScheduleDescription(mdicSchedule), 1);
            vtRow.setElementAt(Global.FORMAT_DATE().format(ScheduleUtil.getExpectedDate(mdicSchedule)), 2);
            vtRow.setElementAt(String.valueOf(ScheduleUtil.getExecutionCount(mdicSchedule)), 3);
            vtRow.setElementAt(mdicSchedule, 4);
            tblSchedule.setRow(iSelected, vtRow);
            if (tblSchedule.getRowCount() > 0)
            {
                if (iSelected < 0 || iSelected >= tblSchedule.getRowCount())
                {
                    iSelected = tblSchedule.getRowCount() - 1;
                }
                tblSchedule.changeSelectedRow(iSelected);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            ((JXDatePlus)getFormData().getField("ExpectedDate")).requestFocus();
            return false;
        }
        return true;
    }

    ////////////////////////////////////////////////////////

    public boolean remove()
    {
        VectorTable tblSchedule = ((VectorTable)getFormData().getControl("ScheduleList"));
        try
        {
            int iSelected = tblSchedule.getSelectedRow();
            Vector vtRow = tblSchedule.getRow(iSelected);
            String strScheduleID = (String)vtRow.elementAt(0);

            // Call delete
            Packet request = channel.getProcessor().getServerMonitor().createPacket();
            request.setRequestID(String.valueOf(System.currentTimeMillis()));
            request.setString("ThreadID", mstrThreadID);
            request.setString("ScheduleID", strScheduleID);
            channel.sendRequest("ThreadProcessor", "deleteSchedule", request);

            // Update UI
            tblSchedule.deleteRow(iSelected);
            if (tblSchedule.getRowCount() > 0)
            {
                if (iSelected < 0 || iSelected >= tblSchedule.getRowCount())
                {
                    iSelected = tblSchedule.getRowCount() - 1;
                }
                tblSchedule.changeSelectedRow(iSelected);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    ////////////////////////////////////////////////////////

    public boolean search()
    {
        VectorTable tblSchedule = ((VectorTable)getFormData().getControl("ScheduleList"));
        try
        {
            // Call search
            Packet request = channel.getProcessor().getServerMonitor().createPacket();
            request.setRequestID(String.valueOf(System.currentTimeMillis()));
            request.setString("ThreadID", mstrThreadID);
            Packet response = channel.sendRequest("ThreadProcessor", "querySchedule", request);
            String strSchedule = (String)response.getReturn();
            ByteArrayInputStream is = new ByteArrayInputStream(strSchedule.getBytes());
            Dictionary dic = new Dictionary(is);
            Vector vtSchedule = ScheduleUtil.scriptToSchedule(dic);

            // Update UI
            Vector vtData = new Vector();
            for (int iIndex = 0; iIndex < vtSchedule.size(); iIndex++)
            {
                dic = (Dictionary)vtSchedule.elementAt(iIndex);
                Vector vtRow = new Vector();
                vtRow.add(dic.getString("ScheduleID"));
                vtRow.add(ScheduleUtil.getScheduleDescription(dic));
                vtRow.add(Global.FORMAT_DATE().format(ScheduleUtil.getExpectedDate(dic)));
                vtRow.add(String.valueOf(ScheduleUtil.getExecutionCount(dic)));
                vtRow.add(dic);
                vtData.add(vtRow);
            }
            tblSchedule.setData(vtData);
            if (tblSchedule.getRowCount() > 0)
            {
                tblSchedule.changeSelectedRow(0);
                fillDetailValue();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            MessageBox.showMessageDialog(this, e, Global.APP_NAME, MessageBox.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    ////////////////////////////////////////////////////////

    public boolean print()
    {
        return true;
    }

    ////////////////////////////////////////////////////////

    public void onClosing()
    {
        if (((PanelControlButton)getFormData().getLayout("ControlButton")).exit())
        {
            super.onClosing();
        }
    }

    ////////////////////////////////////////////////////////

    public String getPermission()
    {
        return "SIUD";
    }
}
