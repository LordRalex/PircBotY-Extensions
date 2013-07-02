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
package com.lordralex.ralexbot.handlers;

import com.lordralex.ralexbot.RalexBot;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class FileLogHandler extends Handler {

    private final BufferedWriter output;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd hh:mm:ss");
    private static final String LINE_SEP = System.getProperty("line.separator");

    public FileLogHandler(String fileName) throws IOException {
        File file = new File(fileName);
        file.createNewFile();
        output = new BufferedWriter(new FileWriter(file, true));
    }

    @Override
    public void publish(LogRecord record) {
        try {
            Date date = new Date(record.getMillis());
            String time = dateFormat.format(date);
            StringBuilder message = new StringBuilder();
            message.append(time);
            message.append("[");
            message.append(record.getLevel().getName());
            message.append("]");
            message.append(" ");
            message.append(record.getMessage());
            message.append(LINE_SEP);
            if (record.getThrown() != null) {
                Throwable thrown = record.getThrown();
                message.append(thrown.getMessage());
                message.append(LINE_SEP);
                StackTraceElement[] elems = thrown.getStackTrace();
                for (int i = 0; i < elems.length; i++) {
                    message.append(elems[i].toString());
                    message.append(LINE_SEP);
                }

            }
            output.write(message.toString());
            flush();
        } catch (IOException ex) {
            Logger.getLogger(FileLogHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void flush() {
        try {
            output.flush();
        } catch (IOException e) {
        }
    }

    @Override
    public void close() throws SecurityException {
        try {
            output.close();
        } catch (IOException ex) {
            RalexBot.getLogger().log(Level.SEVERE, "An error occured on closing the OutputStream", ex);
        }
    }
}
