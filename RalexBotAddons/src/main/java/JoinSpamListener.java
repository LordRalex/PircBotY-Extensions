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

import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.JoinEvent;
import com.lordralex.ralexbot.api.events.PartEvent;
import com.lordralex.ralexbot.api.events.QuitEvent;

/**
 * @version 1.0
 * @author Laptop
 */
public class JoinSpamListener extends Listener {

    protected int time = 7000;
    protected int number = 3;

    @Override
    public void setup() {
    }

    @Override
    public void runEvent(JoinEvent event) {
    }

    @Override
    public void runEvent(PartEvent event) {
    }

    @Override
    public void runEvent(QuitEvent event) {
    }
}
