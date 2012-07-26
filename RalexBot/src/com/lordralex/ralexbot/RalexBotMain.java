package com.lordralex.ralexbot;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jibble.pircbot.IrcException;

/**
 * @version 1.2
 * @author Lord_Ralex
 * @since 1.0
 */
public class RalexBotMain {

    private static RalexBot bot;
    private static boolean stop = false;
    private static boolean isAlone = true;
    private static boolean crashed = false;

    /**
     * This starts the bot. This is the preferred way as of 1.2 and replaces the
     * old way of using the constructor to start the bot.
     *
     * @since 1.2
     */
    public void startBot(Scanner keyboard, String[] args) {
        CommandListener thread = new CommandListener(keyboard);
        try {
            if (args.length >= 1) {
                isAlone = Boolean.parseBoolean(args[0]);
            }
            if (args.length >= 2) {
                bot = new RalexBot(Boolean.parseBoolean(args[1]));
            } else {
                bot = new RalexBot(false);
            }
            try {
                thread.start();
                while (bot.isConnected() && !stop) {
                }
                if (thread.isAlive()) {
                    thread.interrupt();
                }
                keyboard.close();
                crashed = false;
            } catch (Throwable e) {
                Logger.getLogger(RalexBotMain.class.getName()).log(Level.SEVERE, null, e);
                //bot.getCmdExec().executeCommand("stop", null, null);
                crashed = true;
            } finally {
                if (thread.isAlive()) {
                    thread.interrupt();
                }
                keyboard.close();
                bot.forceStop();
                if (isAlone) {
                    System.exit(0);
                }
            }
        } catch (IrcException | IOException ex) {
            Logger.getLogger(RalexBotMain.class.getName()).log(Level.SEVERE, "Error caught:", ex);
            bot.forceStop();
            crashed = true;
        } catch (Exception ex) {
            Logger.getLogger(RalexBotMain.class.getName()).log(Level.SEVERE, "Unhandled exception occured:", ex);
            bot.forceStop();
            crashed = true;
        } finally {
            if (thread.isAlive()) {
                thread.interrupt();
            }
            keyboard.close();
            if (isAlone) {
                System.exit(0);
            }
        }
    }

    /**
     * The main constructor to create the entire bot. This is the preferred way
     * to start the bot.
     *
     * @param keyboard The keyboard to be used for the console listener
     * @param args Any arguments to pass
     * @deprecated Replacement is to use startBot(Scanner keyboard, String[]
     * args)
     * @since 1.0
     */
    public RalexBotMain(Scanner keyboard, String[] args) {
    }

    public RalexBotMain() {
    }

    /**
     * @param args the command line arguments
     *
     * @since 1.0
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(RalexBotMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        new RalexBotMain().startBot(new Scanner(System.in), args);
    }

    /**
     * This will tell the driver to stop the bot, however this will not
     * forcefully kill it.
     *
     * @since 1.0
     */
    public static void stop() {
        stop = true;
        print("Kill received");
    }

    /**
     * This will inform if the bot crashed for some exception to which it could
     * not recover from.
     *
     * @return true if the bot crashed, otherwise false
     * @since 1.0
     */
    public boolean isCrashed() {
        return crashed;
    }

    /**
     * Gets the bot the main driver is running as.
     *
     * @return The bot being used
     * @since 1.0
     */
    public static RalexBot getBot() {
        return bot;
    }

    private static class CommandListener extends Thread {

        Scanner keyboard;

        public CommandListener(Scanner kb) {
            keyboard = kb;
        }

        @Override
        public void run() {
            String message = null;
            while (bot.isConnected() && !stop) {
                System.out.print("-> ");
                if (keyboard.hasNext() && !stop && bot.isConnected()) {
                    message = keyboard.nextLine();
                }
                if (message != null) {
                    //bot.getCmdExec().executeCommand(message, null, null);
                }
            }
            keyboard.close();
        }
    }

    /**
     * Sends a message to the console. This should be used whenever something
     * should be printed to the console to allow the "->" to carry over when
     * needed
     *
     * @param message The message to send to the console
     * @since 1.1
     */
    public static void print(String message) {
        System.out.println(message);
        System.out.print("-> ");
    }
}
