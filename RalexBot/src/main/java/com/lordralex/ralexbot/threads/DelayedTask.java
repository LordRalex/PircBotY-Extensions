package com.lordralex.ralexbot.threads;

public abstract class DelayedTask extends Thread {

    private final int time;

    public DelayedTask(int milli, boolean isMilli) {
        if (isMilli) {
            time = milli;
        } else {
            time = milli * 1000;
        }
    }

    public DelayedTask(int sec) {
        this(sec, false);
    }

    @Override
    public final void run() {
        long start = System.currentTimeMillis();
        synchronized (this) {
            while (start < System.currentTimeMillis()) {
                try {
                    this.wait(time);
                } catch (InterruptedException ex) {
                }
            }
        }
        runTask();
    }

    public abstract void runTask();
}
