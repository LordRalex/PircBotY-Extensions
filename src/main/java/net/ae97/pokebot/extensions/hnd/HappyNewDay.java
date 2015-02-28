/*
 * Copyright (C) 2015 Joshua
 *
 * This file is a part of pokebot-extensions
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
package net.ae97.pokebot.extensions.hnd;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extension.Extension;

/**
 *
 * @author Joshua
 */
public class HappyNewDay extends Extension {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public String getName() {
        return "Happy_New_Day";
    }

    @Override
    public void load() {
        Date currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC +1:00")).getTime();
        Calendar newDay = Calendar.getInstance(TimeZone.getTimeZone("UTC +1:00"));
        newDay.add(Calendar.DATE, 1);
        newDay.set(Calendar.HOUR, 0);
        newDay.set(Calendar.MINUTE, 0);
        newDay.set(Calendar.SECOND, 0);
        newDay.set(Calendar.MILLISECOND, 0);
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                PokeBot.getChannel("#scrollsguide").send().message("Happy New Day!");
            }
        }, TimeUnit.SECONDS.toSeconds(newDay.getTime().getTime() - currentTime.getTime()), 24 * 60 * 60, TimeUnit.SECONDS
        );

    }

}
