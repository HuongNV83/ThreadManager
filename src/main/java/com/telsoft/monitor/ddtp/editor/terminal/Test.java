package com.telsoft.monitor.ddtp.editor.terminal;

import java.awt.*;
import javax.swing.*;

import smartlib.util.*;

public class Test extends JFrame
{
	static JPanel panel = new JPanel()
	{
		public void paint(Graphics g)
		{
			String input = "this is a test\r\nthis is a test\r\nthis is a test\r\nthis is a test.";

			super.paint(g);
			Graphics2D g2d = (Graphics2D)g;

			String[] ls = StringUtil.toStringArray(input,"\r\n");
			int i = 40;
			for(String s : ls)
			{
				g2d.drawString(s,10,i);
				i += 16;
			}
		}
	};

	public static void main(String args[])
	{
		JFrame frame = new Test();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setSize(200,200);
		panel.setDoubleBuffered(true);
		frame.add(panel);

		frame.setVisible(true);
	}
}
