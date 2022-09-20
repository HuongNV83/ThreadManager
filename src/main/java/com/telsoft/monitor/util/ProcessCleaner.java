package com.telsoft.monitor.util;

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
public class ProcessCleaner implements Runnable
{
    private Process mprcMain;

    public ProcessCleaner(Process prc)
    {
        mprcMain = prc;
        new Thread(this).start();
    }

    public void run()
    {
        try
        {
            mprcMain.waitFor();
            Thread.sleep(500);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                mprcMain.getInputStream().close();
                mprcMain.getOutputStream().close();
                mprcMain.getErrorStream().close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
