package com.lordralex.ralexbot.threads;

/**
 *
 * @author Joshua
 */
public class ThreadPingThread extends Thread {

    Thread parentThread;
    int timeToPing;

    public ThreadPingThread(Thread parent, int value) {
        parentThread = parent;
        timeToPing = value;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                wait(timeToPing);
                parentThread.interrupt();
            } catch (InterruptedException e) {
            }
        }
    }
}
