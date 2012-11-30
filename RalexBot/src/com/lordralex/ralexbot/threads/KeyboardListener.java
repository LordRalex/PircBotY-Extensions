/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.threads;

import com.lordralex.ralexbot.RalexBot;
import java.util.Scanner;

/**
 *
 * @author Joshua
 */
public final class KeyboardListener extends Thread {

    Scanner kb;
    final RalexBot instance;

    public KeyboardListener(RalexBot a) {
        setName("Keyboard_Listener_Thread");
        kb = new Scanner(System.in);
        instance = a;
    }

    @Override
    public void run() {
        String line;
        boolean run = true;
        while (run) {
            line = kb.nextLine();
            if (line == null || line.trim().equalsIgnoreCase("stop")) {
                run = false;
            }
        }
        synchronized (instance) {
            instance.notify();
        }
        System.out.println("Ending keyboard listener");
    }
}
