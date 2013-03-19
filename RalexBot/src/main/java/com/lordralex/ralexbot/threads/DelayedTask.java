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
package com.lordralex.ralexbot.threads;

public abstract class DelayedTask extends Thread {

    private final int time;

    public DelayedTask(int milli, boolean isMilli) {
        if (isMilli) {
            time = milli;
        } else {
            time = milli * 1000;
        }
    }

    public DelayedTask(int sec) {
        this(sec, false);
    }

    @Override
    public final void run() {
        long start = System.currentTimeMillis();
        synchronized (this) {
            while (start < System.currentTimeMillis()) {
                try {
                    this.wait(time);
                } catch (InterruptedException ex) {
                }
            }
        }
        runTask();
    }

    public abstract void runTask();
}
