package com.telsoft.monitor.manager;

import javax.swing.*;

/**
 * <p>Title: Admin Management</p>
 *
 * <p>Description: A part of TELSOFT GATEWAY</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public interface AboutListener
{
    public void initAbout(final JPanel pnlContentAbout);

    /**
     * initLicense
     *
     * @param pnlContentLicense JPanel
     */
    public void initLicense(final JPanel pnlContentLicense);
}
