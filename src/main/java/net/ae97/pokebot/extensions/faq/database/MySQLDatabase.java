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
package net.ae97.pokebot.extensions.faq.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.ae97.pircboty.ChatFormat;
import net.ae97.pokebot.PokeBot;

/**
 * @author Lord_Ralex
 */
public class MySQLDatabase extends Database {

    private final String host, user, pass, database;
    private final int port;
    private final Pattern BOLD = Pattern.compile("\\[\\/?b\\]/ig");
    private final Pattern UNDERLINE = Pattern.compile("\\[\\/?u\\]/ig");

    public MySQLDatabase(String n, Map<String, String> params) throws SQLException {
        super(n, params);
        host = params.get("host");
        port = Integer.parseInt(params.get("port"));
        user = params.get("user");
        pass = params.get("pass");
        database = params.get("database");
    }

    @Override
    public void load() {
    }

    @Override
    public String[] getEntry(String key) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass)) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT content FROM factoids "
                    + "INNER JOIN games ON games.id = factoids.game "
                    + "WHERE name = ? AND games.idname IN (?, 'global') "
                    + "ORDER BY games.id DESC "
                    + "LIMIT 1")) {
                statement.setString(1, key);
                statement.setString(2, getName().toLowerCase());
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    return formatToIrc(result.getString("content").split(";;"));
                }
            }
        } catch (SQLException ex) {
            PokeBot.getLogger().log(Level.SEVERE, "Error on SQL call", ex);
        }
        return null;
    }

    private String[] formatToIrc(String[] lines) {
        for(int i=0; i < lines.length; i++) {
            lines[i] = BOLD.matcher(lines[i]).replaceAll(ChatFormat.BOLD.getCode());
            lines[i] = UNDERLINE.matcher(lines[i]).replaceAll(ChatFormat.UNDERLINE.getCode());
        }
        return lines;
    }
}
