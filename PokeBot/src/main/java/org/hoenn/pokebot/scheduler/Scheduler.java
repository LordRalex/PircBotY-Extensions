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
package org.hoenn.pokebot.scheduler;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Lord_Ralex
 */
public class Scheduler {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final Map<Integer, ScheduledFuture<?>> index = new ConcurrentHashMap<>();
    private volatile Integer id = 0;

    public Scheduler() {
    }

    public int scheduleTask(Runnable task, int delay, TimeUnit unit) {
        ScheduledFuture<?> future = executorService.schedule(task, delay, unit);
        int currentID;
        synchronized (id) {
            id++;
            currentID = id.intValue();
        }
        index.put(currentID, future);
        return currentID;
    }

    public int scheduleTask(Callable task, int delay, TimeUnit unit) {
        ScheduledFuture<?> future = executorService.schedule(task, delay, unit);
        int currentID;
        synchronized (id) {
            id++;
            currentID = id.intValue();
        }
        index.put(currentID, future);
        return currentID;
    }

    public boolean isRunning(int id) {
        ScheduledFuture<?> future = index.get(id);
        return future == null ? true : future.getDelay(TimeUnit.NANOSECONDS) <= 0;
    }

    public boolean isDone(int id) {
        ScheduledFuture<?> future = index.get(id);
        return future == null ? true : future.isDone();
    }

    public boolean isCancelled(int id) {
        ScheduledFuture<?> future = index.get(id);
        return future == null ? true : future.isCancelled();
    }

    public void cancelTask(int id) {
        ScheduledFuture<?> future = index.remove(id);
        if (future != null) {
            future.cancel(true);
        }
    }
}
