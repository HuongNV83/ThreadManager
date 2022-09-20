package com.telsoft.monitor.ddtp.editor.terminal;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.Lock;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.Timer;

import smartlib.util.*;

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
public class SwingTerminal extends JList {
    protected RubberBandListCellRenderer renderer;
    protected AlphaComposite alcomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);
    protected Color PCOLOR;

    protected Color selectedBackground;
    protected Color selectedForeground;
    protected Color normalBackground;
    protected Color normalForeground;

    class RubberBandListCellRenderer extends TerminalCellRenderer implements MouseListener, MouseMotionListener {
        private int iRowHeight = 16;

        private final Point srcPoint = new Point();
        public Path2D polygon = null;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String s = (String) value;
//			if(s.contains("[NOTIFICATION]"))
//			{
//				setBackground(Color.GREEN);
//			}

            String[] ls = StringUtil.toStringArray(s, "\n");

            FontMetrics metrics = getFontMetrics(getFont());
            iRowHeight = metrics.getHeight();
            int height = iRowHeight * ls.length;
            if (height < 16) {
                height = 16;
            }
            Rectangle rect = list.getVisibleRect();
            int width = metrics.stringWidth(s) + 20;
            setPreferredSize(new Dimension(width, height));

            if (isSelected) {
                setForeground(selectedForeground);
                setBackground(selectedBackground);
            } else {
                setForeground(normalForeground);
                setBackground(normalBackground);
            }
            return this;
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            JList list = (JList) e.getSource();
            list.setFocusable(true);
            if (polygon == null) {
                srcPoint.setLocation(e.getPoint());
            }
            Point destPoint = e.getPoint();
            polygon = new Path2D.Double();
            polygon.moveTo(srcPoint.x, srcPoint.y);
            polygon.lineTo(destPoint.x, srcPoint.y);
            polygon.lineTo(destPoint.x, destPoint.y);
            polygon.lineTo(srcPoint.x, destPoint.y);
            polygon.closePath();
            list.setSelectedIndices(getIntersectsIcons(list, polygon));
            list.repaint();
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            JList list = (JList) e.getSource();
            list.setFocusable(true);
            polygon = null;
            list.repaint();
        }

        public void mousePressed(MouseEvent e) {
            JList list = (JList) e.getSource();
            int index = list.locationToIndex(e.getPoint());
            Rectangle rect = list.getCellBounds(index, index);
            if (!rect.contains(e.getPoint())) {
                list.clearSelection();
                list.getSelectionModel().setAnchorSelectionIndex(-1);
                list.getSelectionModel().setLeadSelectionIndex(-1);
                list.setFocusable(false);
            } else {
                list.setFocusable(true);
            }
        }

        private int[] getIntersectsIcons(JList l, Shape p) {
            ListModel model = l.getModel();
            ArrayList<Integer> list = new ArrayList<Integer>(model.getSize());
            for (int i = 0; i < model.getSize(); i++) {
                Rectangle r = l.getCellBounds(i, i);
                if (p.intersects(r)) {
                    list.add(i);
                }
            }
            int[] il = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                il[i] = list.get(i);
            }
            return il;
        }
    }

    private Lock lock = new ReentrantLock();
    protected Vector vtBuffer = new Vector();
    protected Timer time = new Timer(300, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            synchronized (vtBuffer) {
                if (vtBuffer.size() > 0) {
                    append(vtBuffer);
                    vtBuffer.clear();
                }
            }
        }
    });

    private static String trim(String s) {
        int len = s.length();
        int st = 0;
        int off = 0;
        char[] val = s.toCharArray();

        while ((st < len) && (val[off + st] <= ' ') && (val[off + st] != '\t')) {
            st++;
        }
        while ((st < len) && (val[off + len - 1] <= ' ') && (val[off + st] != '\t')) {
            len--;
        }
        return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;
    }

    private static String clearText(String str) {
        str = str.replaceAll("\\\\n", "\n");
        str = str.replaceAll("\\\\r", "");
        str = str.replaceAll("\\\\t", "\t");
        str = trim(str);
        return str;
    }

    public SwingTerminal() {
        selectedBackground = this.getSelectionBackground();
        selectedForeground = this.getSelectionForeground();
        normalBackground = this.getBackground();
        normalForeground = this.getForeground();
        setModel(new TerminalModel());
        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        time.start();
    }

    public void updateUI() {
        setCellRenderer(null);
        if (renderer != null) {
            removeMouseMotionListener(renderer);
            removeMouseListener(renderer);
        } else {
            renderer = new RubberBandListCellRenderer();
        }
        super.updateUI();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                setCellRenderer(renderer);
                addMouseMotionListener(renderer);
                addMouseListener(renderer);
            }
        });
        Color c = getSelectionBackground();
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        PCOLOR = r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
                : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (renderer.polygon != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(getSelectionBackground());
            g2d.draw(renderer.polygon);
            g2d.setComposite(alcomp);
            g2d.setPaint(PCOLOR);
            g2d.fill(renderer.polygon);
        }
    }

    public void append(String str) {
        vtBuffer.add(str);
    }

    public void append(Collection c) {
        lock.lock();
        try {
            ArrayList l = new ArrayList();
            for (Object o : c) {
                String str = clearText(String.valueOf(o));
                if (str.length() == 0) {
                    continue;
                }
                l.add(str);
            }
            getTerminalModel().addAll(l);
        } finally {
            lock.unlock();
        }
    }

    public TerminalModel getTerminalModel() {
        return (TerminalModel) super.getModel();
    }

    public void setText(String str) {
        lock.lock();
        try {
            str = clearText(str);
            if (str.length() == 0) {
                return;
            }
            vtBuffer.clear();
            getTerminalModel().clear();
            getTerminalModel().addElement(str);
        } finally {
            lock.unlock();
        }
    }

    public void removeItem(int iMax) {
        int i = getModel().getSize() - iMax;
        if (i > 0) {
            getTerminalModel().removeRange(0, i);
        }
    }

    public void scrollToLast() {
        if (getModel().getSize() > 0) {
            ensureIndexIsVisible(getModel().getSize() - 1);
        }
    }

    public void clear() {
        vtBuffer.clear();
        getTerminalModel().clear();
    }

}
