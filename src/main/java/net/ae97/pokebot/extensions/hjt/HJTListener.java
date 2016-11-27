package net.ae97.pokebot.extensions.hjt;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.Listener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by urielsalis on 13/09/16.
 */
public class HJTListener implements Listener, CommandExecutor {

    private final HJTParser core;

    public HJTListener(HJTParser system) {
        core = system;
    }

    @Override
    public void runEvent(CommandEvent event) {
        switch (event.getCommand()) {
            case "hjt":
                if (event.getArgs().length == 0) {
                    event.respond("Usage: hjt [link]");
                    return;
                }
                event.respond(getHJT(event.getArgs()[0]));
                return;
            case "addHJT":
                if (event.getArgs().length == 0) {
                    event.respond("Usage: addHJT [name]=[value]");
                    return;
                }
                event.respond(addHJT(StringUtils.join(event.getArgs(), " ")));
                break;
            case "rmHJT":
                if (event.getArgs().length == 0) {
                    event.respond("Usage: rmHJT [name]");
                    return;
                }
                event.respond(rmHJT(StringUtils.join(event.getArgs(), " ")));
                break;
        }
    }

    private String rmHJT(String s) {
        try (Connection connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM hjt WHERE name = ?")) {
                statement.setString(1, s);
                statement.execute();
                return s + " removed from database!";
            }
        } catch (SQLException e) {
            core.getLogger().log(Level.SEVERE, "Error inserting hjt", e);
            return "SQL error";
        }
    }

    private String getHJT(String s) {
        String text;
        try (Scanner scanner = new Scanner(new URL(s).openStream())) {
            text = scanner.useDelimiter("\\A").next();
        } catch (IOException ex) {
            core.getLogger().log(Level.SEVERE, "Error getting hjt", ex);
            return "Error reading from URL (" + s + "): " + ex.getMessage();
        }
        StringBuilder builder = new StringBuilder();
        try (ResultSet set = openConnection().prepareStatement("SELECT name, value FROM hjt").executeQuery()) {
            while (set.next()) {
                String name = set.getString(1);
                String value = set.getString(2);
                if (text.contains(name)) {
                    if (builder.length() == 0) {
                        builder.append(value);
                    } else {
                        builder.append(", ").append(value);
                    }
                }
            }
        } catch (SQLException ex) {
            core.getLogger().log(Level.SEVERE, "Error getting hjt", ex);
            return "SQL error: " + ex.getMessage();
        }
        if (builder.length() == 0) {
            return "Nothing matching in database";
        } else {
            return "Found: " + builder.toString();
        }
    }

    private String addHJT(String s) {
        if (!s.contains("=")) {
            return "Usage: addHJT [name]=[value]";
        }
        String[] array = s.split("=");
        String name = array[0].trim();
        String value = array[1].trim();
        try (Connection connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO hjt (name, value) VALUES (?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, value);
                statement.execute();
                return name + "=" + value + " added to database!";
            }
        } catch (SQLException e) {
            core.getLogger().log(Level.SEVERE, "Error inserting hjt", e);
            return "SQL error";
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"hjt", "addHJT", "rmHJT"};
    }

    private Connection openConnection() throws SQLException {
        String host = core.getConfig().getString("host");
        int port = core.getConfig().getInt("port");
        String mysqlUser = core.getConfig().getString("user");
        String pass = core.getConfig().getString("pass");
        String database = core.getConfig().getString("database");
        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, mysqlUser, pass);
    }
}
