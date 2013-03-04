package com.lordralex.ralexbot.threads;

import com.lordralex.ralexbot.RalexBot;

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
                RalexBot.getLogger().config("Time expired, killing parent " + parentThread.getName());
                parentThread.interrupt();
            } catch (InterruptedException e) {
            }
        }
    }
}
