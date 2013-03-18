package com.lordralex.ralexbot.stream;

import com.lordralex.ralexbot.RalexBot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerOutputStream extends ByteArrayOutputStream {

    private static final String separator = System.getProperty("line.separator");
    private final Level level;
    private final Logger log;

    public LoggerOutputStream(Logger logger, Level level) {
        super();
        log = logger;
        this.level = level;
    }

    @Override
    public void flush() {
        synchronized (this) {
            String record = this.toString();
            super.reset();
            if ((record.length() > 0) && (!record.equals(separator))) {
                log.log(level, record);
            }
        }
    }
}
