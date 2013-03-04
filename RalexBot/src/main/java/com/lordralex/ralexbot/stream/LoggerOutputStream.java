package com.lordralex.ralexbot.stream;

import com.lordralex.ralexbot.RalexBot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerOutputStream extends ByteArrayOutputStream {

    private static final String separator = System.getProperty("line.separator");
    private final Level level;

    public LoggerOutputStream(Level level) {
        super();
        this.level = level;
    }

    @Override
    public void flush() {
        synchronized (this) {
            String record = this.toString();
            super.reset();
            if ((record.length() > 0) && (!record.equals(separator))) {
                RalexBot.getLogger().logp(level, "", "", record);
            }
        }
    }
}
