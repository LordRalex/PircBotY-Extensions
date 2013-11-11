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
package org.hoenn.pokebot.threads;

/**
 *
 * @author Joshua
 */
public class ThreadPingThread extends Thread {

    private final Thread parentThread;
    private final int timeToPing;

    public ThreadPingThread(Thread parent, int value) {
        parentThread = parent;
        timeToPing = value;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                wait(timeToPing);
                parentThread.interrupt();
            } catch (InterruptedException e) {
            }
        }
    }
}
