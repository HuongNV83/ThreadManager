/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.telsoft.monitor.ddtp.editor.terminal;

import java.io.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class TerminalCellRenderer extends JTextArea implements ListCellRenderer,Serializable
{

	/**
	 * An empty <code>Border</code>. This field might not be used. To change the
	 * <code>Border</code> used by this renderer override the
	 * <code>getListCellRendererComponent</code> method and set the border
	 * of the returned component directly.
	 */
	private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1,1,1,1);
	private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1,1,1,1);
	protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

	/**
	 * Constructs a default renderer object for an item
	 * in a list.
	 */
	public TerminalCellRenderer()
	{
		super();
		setOpaque(true);
		setBorder(getNoFocusBorder());
		setName("List.cellRenderer");
	}

	private Border getNoFocusBorder()
	{
		Border border = UIManager.getBorder("List.cellNoFocusBorder");
		if(System.getSecurityManager() != null)
		{
			if(border != null)
			{
				return border;
			}
			return SAFE_NO_FOCUS_BORDER;
		}
		else
		{
			if(border != null &&
			   (noFocusBorder == null ||
				noFocusBorder == DEFAULT_NO_FOCUS_BORDER))
			{
				return border;
			}
			return noFocusBorder;
		}
	}

	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus)
	{
		setComponentOrientation(list.getComponentOrientation());

		Color bg = null;
		Color fg = null;

		JList.DropLocation dropLocation = list.getDropLocation();
		if(dropLocation != null
		   && !dropLocation.isInsert()
		   && dropLocation.getIndex() == index)
		{

			bg = UIManager.getColor("List.dropCellBackground");
			fg = UIManager.getColor("List.dropCellForeground");

			isSelected = true;
		}

		if(isSelected)
		{
			setBackground(bg == null ? list.getSelectionBackground() : bg);
			setForeground(fg == null ? list.getSelectionForeground() : fg);
		}
		else
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setText((value == null) ? "" : value.toString());

		setEnabled(list.isEnabled());
		setFont(list.getFont());

		Border border = null;
		if(cellHasFocus)
		{
			if(isSelected)
			{
				border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
			}
			if(border == null)
			{
				border = UIManager.getBorder("List.focusCellHighlightBorder");
			}
		}
		else
		{
			border = getNoFocusBorder();
		}
		setBorder(border);

		return this;
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 *
	 * @since 1.5
	 * @return <code>true</code> if the background is completely opaque
	 *         and differs from the JList's background;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean isOpaque()
	{
		Color back = getBackground();
		Component p = getParent();
		if(p != null)
		{
			p = p.getParent();
		}
		// p should now be the JList.
		boolean colorMatch = (back != null) && (p != null) &&
							 back.equals(p.getBackground()) &&
							 p.isOpaque();
		return!colorMatch && super.isOpaque();
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void validate()
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 *
	 * @since 1.5
	 */
	@Override
	public void invalidate()
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 *
	 * @since 1.5
	 */
	@Override
	public void repaint()
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void revalidate()
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void repaint(long tm,int x,int y,int width,int height)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void repaint(Rectangle r)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	protected void firePropertyChange(String propertyName,Object oldValue,Object newValue)
	{
		if(propertyName == "document"
		   || ((propertyName == "font" || propertyName == "foreground")
			   && oldValue != newValue))
		{

			super.firePropertyChange(propertyName,oldValue,newValue);
		}
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName,byte oldValue,byte newValue)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName,char oldValue,char newValue)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName,short oldValue,short newValue)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName,int oldValue,int newValue)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName,long oldValue,long newValue)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName,float oldValue,float newValue)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName,double oldValue,double newValue)
	{}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName,boolean oldValue,boolean newValue)
	{}
}
