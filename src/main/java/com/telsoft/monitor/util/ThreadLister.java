package com.telsoft.monitor.util;

public class ThreadLister
{
    private static void printThreadInfo(Thread t, String indent)
    {
        if (t == null)
            return;
        System.out.println(indent + "Thread: " + t.getName() + "  Priority: " + t.getPriority() +
                           (t.isDaemon() ? " Daemon" : "") + (t.isAlive() ? "" : " Not Alive"));
        StackTraceElement[] trace =  t.getStackTrace();
        if (trace != null)
            for (int i = 0;i<trace.length;i++)
            {
                System.out.println(indent +indent + trace[i].toString());
            }
    }

    /** Display info about a thread group */
    private static void printGroupInfo(ThreadGroup g, String indent)
    {
        if (g == null)
            return;
        int numThreads = g.activeCount();
        int numGroups = g.activeGroupCount();
        Thread[] threads = new Thread[numThreads];
        ThreadGroup[] groups = new ThreadGroup[numGroups];

        g.enumerate(threads, false);
        g.enumerate(groups, false);

        System.out.println(indent + "Thread Group: " + g.getName() + "  Max Priority: " + g.getMaxPriority() +
                           (g.isDaemon() ? " Daemon" : ""));

        for (int i = 0; i < numThreads; i++)
            printThreadInfo(threads[i], indent + "    ");
        for (int i = 0; i < numGroups; i++)
            printGroupInfo(groups[i], indent + "    ");
    }

    /** Find the root thread group and list it recursively */
    public static void listAllThreads()
    {
        ThreadGroup currentThreadGroup;
        ThreadGroup rootThreadGroup;
        ThreadGroup parent;

        // Get the current thread group
        currentThreadGroup = Thread.currentThread().getThreadGroup();

        // Now go find the root thread group
        rootThreadGroup = currentThreadGroup;
        parent = rootThreadGroup.getParent();
        while (parent != null)
        {
            rootThreadGroup = parent;
            parent = parent.getParent();
        }

        printGroupInfo(rootThreadGroup, "");
    }

    public static void main(String[] args)
    {
        ThreadLister.listAllThreads();
    }
}
