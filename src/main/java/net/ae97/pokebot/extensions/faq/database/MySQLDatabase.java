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
import net.ae97.pokebot.PokeBot;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lord_Ralex
 */
public class MySQLDatabase extends Database {

    private final Connection connection;
    private final String GET, SEARCH;

    public MySQLDatabase(String n, Map<String, String> params) throws SQLException {
        super(n, params);
        String host = params.get("host");
        int port = Integer.parseInt(params.get("port"));
        String user = params.get("user");
        String pass = params.get("pass");
        String database = params.get("database");
        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass);
        GET = params.get("get");
        SEARCH = params.get("search");
    }

    @Override
    public void load() {
    }

    @Override
    public String[] getEntry(String[] key) {
        String[] marks = new String[key.length];
        for (int i = 0; i < marks.length; i++) {
            marks[i] = "?";
        }
        synchronized (GET) {
            String stmt = GET.replace("{categories}", StringUtils.join(marks, ", "));
            try (PreparedStatement statement = connection.prepareStatement(stmt)) {
                statement.setString(1, getName());
                for (int i = 0; i < marks.length; i++) {
                    statement.setString(i + 2, key[i]);
                }
                statement.setInt(marks.length + 2, marks.length);
                statement.setInt(marks.length + 3, marks.length);
                stmt = statement.toString().split(":", 2)[1];
                try (ResultSet set = statement.executeQuery()) {
                    if (set.first()) {
                        String result = set.getObject("content", String.class);
                        if (result == null || result.isEmpty()) {
                            return null;
                        }
                        return result.split(";;");
                    } else {
                        String searchStm = SEARCH.replace("{categories}", StringUtils.join(marks, ", "));
                        try (PreparedStatement searchStatement = connection.prepareStatement(searchStm)) {
                            searchStatement.setString(1, getName());
                            for (int i = 0; i < marks.length; i++) {
                                searchStatement.setString(i + 2, key[i]);
                            }
                            System.out.println("Statement: " + searchStatement.toString().split(":", 2)[1]);
                            try (ResultSet searchSet = searchStatement.executeQuery()) {
                                if (searchSet.first()) {
                                    String result = searchSet.getObject("content", String.class);
                                    if (result == null || result.isEmpty()) {
                                        return null;
                                    }
                                    return result.split(";;");
                                }
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                PokeBot.getLogger().log(Level.SEVERE, "Error on SQL call: " + stmt, ex);
            }
        }
        return null;
    }
}
