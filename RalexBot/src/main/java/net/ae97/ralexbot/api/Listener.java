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
package net.ae97.ralexbot.api;

import net.ae97.ralexbot.api.events.JoinEvent;
import net.ae97.ralexbot.api.events.ActionEvent;
import net.ae97.ralexbot.api.events.PrivateMessageEvent;
import net.ae97.ralexbot.api.events.NickChangeEvent;
import net.ae97.ralexbot.api.events.NoticeEvent;
import net.ae97.ralexbot.api.events.ConnectionEvent;
import net.ae97.ralexbot.api.events.KickEvent;
import net.ae97.ralexbot.api.events.PartEvent;
import net.ae97.ralexbot.api.events.QuitEvent;
import net.ae97.ralexbot.api.events.CommandEvent;
import net.ae97.ralexbot.api.events.MessageEvent;

public abstract class Listener {

    public void onLoad() {
    }

    public void onUnload() {
    }

    public void runEvent(CommandEvent event) {
    }

    public void runEvent(MessageEvent event) {
    }

    public void runEvent(JoinEvent event) {
    }

    public void runEvent(NoticeEvent event) {
    }

    public void runEvent(PartEvent event) {
    }

    public void runEvent(PrivateMessageEvent event) {
    }

    public void runEvent(QuitEvent event) {
    }

    public void runEvent(NickChangeEvent event) {
    }

    public void runEvent(ActionEvent event) {
    }

    public void runEvent(KickEvent event) {
    }

    public void runEvent(ConnectionEvent event) {
    }

    public String[] getAliases() {
        return new String[0];
    }
}
