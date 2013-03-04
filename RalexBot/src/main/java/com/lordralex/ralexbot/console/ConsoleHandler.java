package com.lordralex.ralexbot.console;

import com.lordralex.ralexbot.RalexBot;
import java.io.IOException;
import java.util.logging.Level;
import jline.console.ConsoleReader;

public class ConsoleHandler extends java.util.logging.ConsoleHandler {

    private final ConsoleReader reader;

    public ConsoleHandler(ConsoleReader reader) {
        super();
        this.reader = reader;
    }

    @Override
    public synchronized void flush() {
        try {
            reader.print(ConsoleReader.RESET_LINE + "");
            reader.flush();
            super.flush();
            try {
                reader.drawLine();
            } catch (Throwable ex) {
                reader.getCursorBuffer().clear();
            }
        } catch (IOException ex) {
            RalexBot.getLogger().log(Level.SEVERE, null, ex);
        }
    }
}
