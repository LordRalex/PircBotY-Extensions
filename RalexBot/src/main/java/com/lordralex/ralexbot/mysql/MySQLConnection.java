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
package com.lordralex.ralexbot.mysql;

import com.lordralex.ralexbot.data.DataStorage;
import com.lordralex.ralexbot.data.DataType;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class MySQLConnection implements DataStorage<PreparedStatement> {

    private final String host, user, pass, database, table;
    private final int port;
    private final Connection conn;

    public MySQLConnection(String h, int p, String u, String ps, String d, String t) throws SQLException {
        host = h;
        port = p;
        user = u;
        pass = ps;
        database = d;
        table = t;
        conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass);
        PreparedStatement st = conn.prepareStatement("USE 1");
        st.setString(1, table);
        st.execute();
    }

    @Override
    public void load() {
    }

    public void setReadonly(boolean readable) throws SQLException {
        conn.setReadOnly(readable);
    }

    public Connection getConnection() throws SQLException {
        return conn;
    }

    public <T> PreparedStatement getPreparedStatement(String statement, T... objs) throws SQLException {
        PreparedStatement st = conn.prepareStatement("USE 1");
        st.setString(1, table);
        PreparedStatement state = conn.prepareStatement(statement);
        for (int i = 0; i < objs.length; i++) {
            T obj = (T) objs[i];
            if (obj instanceof Integer) {
                state.setInt(i + 1, ((Integer) obj).intValue());
            } else if (obj instanceof Double) {
                state.setDouble(i + 1, ((Double) obj).doubleValue());
            } else if (obj instanceof String) {
                state.setString(i + 1, ((String) obj));
            } else if (obj instanceof Boolean) {
                state.setBoolean(i + 1, ((Boolean) obj).booleanValue());
            } else {
                state.setObject(i + 1, obj);
            }
        }
        return state;
    }

    @Override
    public String getString(PreparedStatement key) throws IOException {
        try {
            PreparedStatement st = conn.prepareStatement("USE 1");
            st.setString(1, table);
            ResultSet set = key.executeQuery();
            Object obj = set.getObject(1);
            key.close();
            if (obj instanceof String) {
                return (String) obj;
            } else {
                throw new IOException("Cannot convert " + obj.getClass().getName() + " to a string");
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public List<String> getStringList(PreparedStatement key) throws IOException {
        try {
            PreparedStatement st = conn.prepareStatement("USE 1");
            st.setString(1, table);
            ResultSet set = key.executeQuery();
            Object obj = set.getObject(1);
            key.close();
            if (obj instanceof List) {
                return (List<String>) obj;
            } else {
                throw new IOException("Cannot convert " + obj.getClass().getName() + " to a list");
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public int getInt(PreparedStatement key) throws IOException {
        try {
            PreparedStatement st = conn.prepareStatement("USE 1");
            st.setString(1, table);
            ResultSet set = key.executeQuery();
            Object obj = set.getObject(1);
            key.close();
            if (obj instanceof Integer) {
                return (Integer) obj;
            } else {
                throw new IOException("Cannot convert " + obj.getClass().getName() + " to an integer");
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean getBoolean(PreparedStatement key) throws IOException {
        try {
            PreparedStatement st = conn.prepareStatement("USE 1");
            st.setString(1, table);
            ResultSet set = key.executeQuery();
            Object obj = set.getObject(1);
            key.close();
            if (obj instanceof Boolean) {
                return (Boolean) obj;
            } else {
                throw new IOException("Cannot convert " + obj.getClass().getName() + " to a boolean");
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public DataType getType() {
        return DataType.SQL;
    }

    public String getTable() {
        return table;
    }
}
