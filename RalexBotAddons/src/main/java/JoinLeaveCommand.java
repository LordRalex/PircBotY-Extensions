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

import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.events.CommandEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
import com.lordralex.ralexbot.api.users.BotUser;
import com.lordralex.ralexbot.api.users.User;
import com.lordralex.ralexbot.settings.Settings;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JoinLeaveCommand extends Listener {

    private Map<String, String> channelList = new ConcurrentHashMap<>();
    private int MAX_CHANNELS;
    private Settings settings;

    @Override
    public void setup() {
        settings = new Settings(new File("settings", "config.yml"));
        MAX_CHANNELS = settings.getInt("max-channels");
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        String command = event.getCommand().toLowerCase().trim();
        String[] args = event.getArgs();
        Channel channel = event.getChannel();
        User sender = event.getSender();
        if (command.equalsIgnoreCase("join")) {
            if (args.length != 1) {
                return;
            }
            channel = Channel.getChannel(args[0]);
            if (channelList.containsKey(channel.getName())) {
                return;
            }
            if (channelList.size() >= MAX_CHANNELS) {
                return;
            }
            channelList.put(channel.getName(), sender.getNick());
            BotUser.getBotUser().joinChannel(channel.getName());
        } else if (command.equalsIgnoreCase("leave")) {
            if (args.length == 1) {
                channel = Channel.getChannel(args[0]);
            }
            String getJoin = channelList.get(channel.getName());
            if (sender.hasOP(channel.getName()) || sender.getNick().equalsIgnoreCase(getJoin)) {
                channelList.remove(channel.getName());
                BotUser.getBotUser().leaveChannel(channel.getName());
            } else {
                sender.sendMessage("You did not have him join this channel");
            }
        }
    }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        String oldNick = event.getOldNick();
        String newNick = event.getNewNick();
        if (channelList.containsValue(oldNick)) {
            String[] keys = channelList.keySet().toArray(new String[0]);
            for (String channel : keys) {
                String old = channelList.get(channel);
                if (old != null && old.equalsIgnoreCase(oldNick)) {
                    channelList.remove(channel);
                    channelList.put(channel, newNick);
                }
            }
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "join",
            "leave"
        };
    }
}
