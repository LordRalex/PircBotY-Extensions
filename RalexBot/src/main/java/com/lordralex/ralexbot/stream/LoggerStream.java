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

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class LoggerStream extends PrintStream {

    private final PrintStream parent;
    private final Logger logger;
    private final Level level;
    private final StringBuilder buffer = new StringBuilder();

    public LoggerStream(PrintStream out, Logger log, Level lev) {
        super(out);
        parent = out;
        logger = log;
        level = lev;
    }

    @Override
    public void print(Object x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void print(String x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void print(boolean x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void print(char x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void print(char[] x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void print(double x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void print(float x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void print(int x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void print(long x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        synchronized (parent) {
            parent.print(x);
        }
    }

    @Override
    public void println() {
        writeBuffer();
    }

    @Override
    public void println(Object x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    @Override
    public void println(String x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    @Override
    public void println(boolean x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    @Override
    public void println(char x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    @Override
    public void println(char[] x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    @Override
    public void println(double x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    @Override
    public void println(float x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    @Override
    public void println(int x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    @Override
    public void println(long x) {
        synchronized (buffer) {
            buffer.append(x);
        }
        writeBuffer();
    }

    protected final void writeBuffer() {
        String bufferRemaining;
        synchronized (buffer) {
            bufferRemaining = buffer.substring(0);
            buffer.delete(0, buffer.length());
        }
        logger.log(level, bufferRemaining);
    }
}
