package com.lordralex.ralexbot.threads;

import com.lordralex.ralexbot.RalexBot;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.PrivateMessageEvent;

/**
 *
 * @author Joshua
 */
public class KeepAliveThread extends Thread {

    int time = 120;
    RalexBot instance;
    PircBotX driver;

    public KeepAliveThread(RalexBot a, PircBotX b, int value) {
        instance = a;
        driver = b;
        time = value;
    }

    @Override
    public void run() {
        boolean stop = false;
        while (driver.isConnected() && !stop) {
            synchronized (this) {
                try {
                    wait(time * 1000);
                } catch (InterruptedException ex) {
                    stop = true;
                }
            }
            driver.sendRawLine("PING");
            try {
                ThreadPingThread pinger = new ThreadPingThread(this, 30 * 1000);
                boolean wasEvent = false;
                while (!wasEvent) {
                    Event evt = driver.waitFor(Event.class);
                    if (evt instanceof PrivateMessageEvent) {
                    } else {
                        //instance.getEventHandler().fireEvent(evt);
                    }
                }
                pinger.interrupt();
            } catch (InterruptedException ex) {
                Logger.getLogger(RalexBot.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!driver.isConnected()) {
                instance.interrupt();
            }
        }
    }
}
