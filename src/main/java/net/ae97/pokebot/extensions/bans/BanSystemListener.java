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
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.ae97.pircboty.Channel;
import net.ae97.pircboty.User;
import net.ae97.pircboty.api.events.JoinEvent;
import net.ae97.pircboty.api.events.SetChannelBanEvent;
import net.ae97.pircboty.api.events.UserAuthEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;

/**
 * @author Joshua
 */
public class BanSystemListener implements Listener {

    private final BanSystem core;
    private final String host, mysqlUser, pass, database;
    private final int port;

    public BanSystemListener(BanSystem system) {
        core = system;
        host = system.getConfig().getString("host");
        port = system.getConfig().getInt("port");
        mysqlUser = system.getConfig().getString("user");
        pass = system.getConfig().getString("pass");
        database = system.getConfig().getString("database");
    }

    @EventExecutor
    public void onJoin(JoinEvent event) {

    }

    @EventExecutor
    public void onAuth(UserAuthEvent event) {
        Set<Channel> channels = event.getUser().getChannels();
        for (Channel chan : channels) {
            processEvent(event.getUser(), chan);
        }
    }

    @EventExecutor
    public void onBan(SetChannelBanEvent event) {
        if (!getChannels().contains(event.getChannel().getName().toLowerCase())) {
            return;
        }
        PokeBot.getScheduler().scheduleTask(new UnbanRunnable(event.getChannel().getName(), event.getHostmask()), core.getConfig().getInt("unban-delay", 3), TimeUnit.HOURS);
    }

    private void processEvent(User user, Channel channel) {
        if (!getChannels().contains(channel.getName().toLowerCase())) {
            return;
        }
        if (user.getBot().getUserBot().getNick().equals(user.getNick())) {
            return;
        }
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, mysqlUser, pass)) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id,content,kickMessage FROM bans"
                    + " INNER JOIN banchannels ON bans.id = banchannels.banId"
                    + " WHERE channel IN (?, \"all\") AND (expireDate > CURRENT_TIMESTAMP OR expireDate IS NULL) AND (? LIKE content OR content=?)")) {
                statement.setString(1, channel.getName());
                statement.setString(2, user.getNick() + "!" + user.getLogin() + "@" + user.getHostmask());
                statement.setString(3, "$a:" + user.getLogin());
                ResultSet set = statement.executeQuery();
                if (set.first()) {
                    String content = set.getString("content").replace("%", "*");
                    String message = set.getString("kickMessage");
                    channel.send().ban(content);
                    channel.send().kick(user, message + " (#" + set.getInt("id") + ")");
                }
            }
        } catch (SQLException e) {
            core.getLogger().log(Level.SEVERE, "Error on checking for bans", e);
        }
    }

    private List<String> getChannels() {
        return core.getConfig().getStringList("channels");
    }

}
