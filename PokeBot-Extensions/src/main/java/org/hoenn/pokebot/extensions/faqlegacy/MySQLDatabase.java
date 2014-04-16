/*
 * Copyright (C) 2014 Lord_Ralex
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
package org.hoenn.pokebot.extensions.faqlegacy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lord_Ralex
 */
public class MySQLDatabase extends Database {

    private final Connection connection;

    public MySQLDatabase(String name, String host, String port, String database, String user, String password) throws SQLException {
        super(name);
        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
    }

    @Override
    public String[] getEntry(String key) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT content FROM shfact WHERE name=?")) {
            statement.setString(1, key.toLowerCase());
            ResultSet set = statement.executeQuery();
            if (set.first()) {
                return set.getString(1).split(";;");
            } else {
                return new String[0];
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return new String[0];
        }
    }

}
