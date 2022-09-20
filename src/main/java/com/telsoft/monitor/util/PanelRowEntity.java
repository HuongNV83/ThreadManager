package com.telsoft.monitor.util;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

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
public abstract class PanelRowEntity extends JPanel
{
	private TableModel model;
	private int iIndex = -1;

	public PanelRowEntity(TableModel model)
	{
		this.model = model;
	}

	public TableModel getModel()
	{
		return model;
	}

	public boolean goto_(int vIndex)
	{
		if(iIndex != vIndex && vIndex >= 0 && vIndex < getModel().getRowCount())
		{
			iIndex = vIndex;
			fillDetail();
			return true;
		}
		return false;
	}

	public boolean next()
	{
		int iSaveIndex = iIndex;
		if(iIndex < getModel().getRowCount() - 1)
		{
			iIndex++;
			if(iIndex < 0)
			{
				iIndex = 0;
			}
		}
		if(iSaveIndex != iIndex)
		{
			fillDetail();
			return true;
		}
		return false;
	}

	public boolean previous()
	{
		int iSaveIndex = iIndex;
		if(iIndex < 0)
		{
			iIndex = getModel().getRowCount() - 1;
		}
		else if(iIndex > 0)
		{
			iIndex--;
		}
		if(iSaveIndex != iIndex)
		{
			fillDetail();
			return true;
		}
		return false;
	}

	public int getIndex()
	{
		return iIndex;
	}

	public void fillDetail()
	{
		Vector vtVector = new Vector();
		for(int i = 0;i < getModel().getColumnCount();i++)
		{
			vtVector.add(getModel().getValueAt(getIndex(),i));
		}
		doFillDetail(vtVector);
	}

	protected abstract void doFillDetail(Vector vtRow);
}
