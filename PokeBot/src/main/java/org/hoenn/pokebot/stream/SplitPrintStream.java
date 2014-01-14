/*
 * Copyright (C) 2014 Lord_Ralex
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
package org.hoenn.pokebot.stream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Lord_Ralex
 */
public class SplitPrintStream extends PrintStream {

    private final PrintStream first, second;

    public SplitPrintStream(PrintStream f) throws FileNotFoundException {
        super(f);
        first = f;
        second = new PrintStream(new FileOutputStream("logs.log", true));
    }

    @Override
    public void write(byte[] b) throws IOException {
        first.write(b);
        second.write(b);
    }

    @Override
    public void write(int b) {
        first.write(b);
        second.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        first.write(buf, off, len);
        second.write(buf, off, len);
    }

    @Override
    public void close() {
        first.close();
        second.close();
    }

    @Override
    public void flush() {
        first.flush();
        second.flush();
    }
}
