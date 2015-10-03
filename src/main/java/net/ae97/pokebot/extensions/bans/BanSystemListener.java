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
package net.ae97.pokebot.extensions.bans;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.ae97.pircboty.api.events.JoinEvent;
import net.ae97.pircboty.api.events.SetChannelBanEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;

/**
 * @author Joshua
 */
public class BanSystemListener implements Listener {

    private final BanSystem core;
    private final String host, user, pass, database;
    private final int port;
    private final Map<String, String> channels = new HashMap<>();

    public BanSystemListener(BanSystem system) {
        core = system;
        host = system.getConfig().getString("host");
        port = system.getConfig().getInt("port");
        user = system.getConfig().getString("user");
        pass = system.getConfig().getString("pass");
        database = system.getConfig().getString("database");
        List<String> chans = system.getConfig().getStringList("channels");
        for (String c : chans) {
            String owner = system.getConfig().getString("owners." + c);
            channels.put(c.toLowerCase(), owner);
        }
    }

    @EventExecutor
    public void onJoin(JoinEvent event) {
        if (!channels.containsKey(event.getChannel().getName().toLowerCase())) {
            return;
        }
        String owner = channels.get(event.getChannel().getName().toLowerCase());
        if (owner != null) {
            if (event.getChannel().getOps().contains(PokeBot.getUser(owner))) {
                return;
            }
        }
        if (event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
        } else {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass)) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id,content,kickMessage FROM bans"
                        + " INNER JOIN banchannels ON bans.id = banchannels.banId"
                        + " WHERE channel IN (?, \"all\") AND (expireDate > CURRENT_TIMESTAMP OR expireDate IS NULL) AND ? LIKE content")) {
                    statement.setString(1, event.getChannel().getName());
                    statement.setString(2, event.getUser().getNick() + "!" + event.getUser().getLogin() + "@" + event.getUser().getHostmask());
                    ResultSet set = statement.executeQuery();
                    if (set.first()) {
                        String content = set.getString("content").replace("%", "*");
                        String message = set.getString("kickMessage");
                        event.getChannel().send().ban(content);
                        event.getChannel().send().kick(event.getUser(), message);
                    }
                }
            } catch (SQLException e) {
                core.getLogger().log(Level.SEVERE, "Error on checking for bans", e);
            }
        }
    }

    @EventExecutor
    public void onBan(SetChannelBanEvent event) {
        if (!channels.containsKey(event.getChannel().getName().toLowerCase())) {
            return;
        }
        PokeBot.getScheduler().scheduleTask(new UnbanRunnable(event.getChannel().getName(), event.getHostmask()), core.getConfig().getInt("unban-delay", 3), TimeUnit.HOURS);
    }

}
