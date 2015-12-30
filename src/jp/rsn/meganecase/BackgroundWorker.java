package jp.rsn.meganecase;

import java.util.LinkedList;

public class BackgroundWorker {

    private static BackgroundWorker INSTANCE = null;

    public static final BackgroundWorker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BackgroundWorker();
        }
        return INSTANCE;
    }

    private final ExecuteThread[] threads = new ExecuteThread[Runtime.getRuntime()
            .availableProcessors() + 1];
    private final LinkedList<Runnable> q = new LinkedList<Runnable>();
    private final Object ticket = new Object();
    private boolean active = true;

    public BackgroundWorker() {
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new ExecuteThread("BackgroundWorker", this, i);
            threads[i].setDaemon(true);
            threads[i].setPriority(Thread.NORM_PRIORITY - 1);
            threads[i].start();
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (active) {
                    shutdown();
                }
            }
        });
    }

    public synchronized final void invoke(Runnable task) {
        synchronized (q) {
            q.add(task);
        }
        synchronized (ticket) {
            ticket.notify();
        }
    }

    public final Runnable poll() {
        while (active) {
            synchronized (q) {
                if (q.size() > 0) {
                    Runnable task = q.remove(0);
                    if (task != null) {
                        return task;
                    }
                }
            }
            synchronized (ticket) {
                try {
                    ticket.wait();
                }
                catch (InterruptedException ignore) {
                }
            }
        }
        return null;
    }

    public synchronized final void shutdown() {
        if (active) {
            active = false;
            for (ExecuteThread thread : threads) {
                thread.shutdown();
            }
            synchronized (ticket) {
                ticket.notify();
            }
            INSTANCE = null;
        }
    }

    private static class ExecuteThread extends Thread {

        private final BackgroundWorker q;
        private boolean alive = true;

        public ExecuteThread(String name, BackgroundWorker q, int index) {
            super(name + "[" + index + "]");
            this.q = q;
        }

        public final void shutdown() {
            alive = false;
        }

        @Override
        public void run() {
            while (alive) {
                Runnable task = q.poll();
                if (task != null) {
                    try {
                        task.run();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
