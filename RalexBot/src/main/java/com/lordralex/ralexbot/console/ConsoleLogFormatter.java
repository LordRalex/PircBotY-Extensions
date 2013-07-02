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
package com.lordralex.ralexbot.console;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConsoleLogFormatter extends Formatter {

    private final SimpleDateFormat date;

    public ConsoleLogFormatter() {
        SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss");
        this.date = dt;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        Throwable ex = record.getThrown();
        builder.append(date.format(record.getMillis()));
        builder.append(" [");
        builder.append(record.getLevel().getLocalizedName().toUpperCase());
        builder.append("] ");
        builder.append(record.getMessage());
        builder.append('\n');
        if (record.getThrown() != null) {
            Throwable thrown = record.getThrown();
            for (int i = 0; i < thrown.getStackTrace().length; i++) {
                builder.append(thrown.getStackTrace()[i].toString());
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
