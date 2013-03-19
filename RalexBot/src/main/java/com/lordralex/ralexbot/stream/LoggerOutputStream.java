/*
 * Copyright (C) 2013 Lord_Ralex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lordralex.ralexbot.stream;

import java.io.ByteArrayOutputStream;
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
