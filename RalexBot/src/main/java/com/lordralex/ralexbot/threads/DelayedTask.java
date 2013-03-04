package com.lordralex.ralexbot.threads;

public abstract class DelayedTask extends Thread {

    String message;
    int time;

    public DelayedTask(int sec) {
        time = sec;
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
