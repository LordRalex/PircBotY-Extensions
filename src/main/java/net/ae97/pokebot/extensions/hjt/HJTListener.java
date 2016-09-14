package net.ae97.pokebot.extensions.hjt;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.Listener;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Created by urielsalis on 13/09/16.
 */
public class HJTListener implements Listener, CommandExecutor {
    private final HJTParser core;
    private final String host, mysqlUser, pass, database;
    private final int port;

    public HJTListener(HJTParser system) {
        core = system;
        host = system.getConfig().getString("host");
        port = system.getConfig().getInt("port");
        mysqlUser = system.getConfig().getString("user");
        pass = system.getConfig().getString("pass");
        database = system.getConfig().getString("database");
    }

    @Override
    public void runEvent(CommandEvent event) {
        switch(event.getCommand()) {
            case "hjt":
                if(event.getArgs().length==0) {
                    event.respond("Usage: hjt [link]");
                    return;
                }
                event.respond(getHJT(event.getArgs()[0]));
                return;
            case "addHJT":
                if(event.getArgs().length==0) {
                    event.respond("Usage: addHJT [name]=[value]");
                    return;
                }
                event.respond(addHJT(arrayToString(event.getArgs())));
                break;
            case "rmHJT":
                if(event.getArgs().length==0) {
                    event.respond("Usage: rmHJT [name]");
                    return;
                }
                event.respond(rmHJT(arrayToString(event.getArgs())));
                break;
        }
    }

    private String rmHJT(String s) {
        try (Connection connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM hjt WHERE name=?")) {
                statement.setString(1, s);
                return s + " removed from database!";
            }
        } catch (SQLException e) {
            core.getLogger().log(Level.SEVERE, "Error inserting hjt", e);
            return "SQL error";
        }
    }

    private String arrayToString(String[] args) {
        StringBuilder builder = new StringBuilder();
        for(String s : args) {
            builder.append(s);
            builder.append(" ");
        }
        String str = builder.toString();
        return str.substring(0, str.length()-1);
    }

    private String getHJT(String s) {
        try {
            URL url = new URL(s);
            Scanner scanner = new Scanner(url.openStream(), "UTF-8");
            String text = scanner.useDelimiter("\\A").next();
            scanner.close();
            StringBuilder builder = new StringBuilder();
            PreparedStatement statement = openConnection().prepareStatement("SELECT name, value FROM `hjt`");
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                String name = set.getString(1);
                String value = set.getString(2);
                if(text.contains(name)) {
                    builder.append(", ").append(value);
                }
            }
            if(builder.toString().isEmpty()) return "Nothing :) (Im in beta. Please add things with .addHJT thingToMatch=thingToShow)";
            return "Found: "+builder.toString().substring(2);
        } catch (IOException e) {
            core.getLogger().log(Level.SEVERE, "Error getting hjt", e);
            return "SQL error";
        } catch (SQLException e) {
            core.getLogger().log(Level.SEVERE, "Error getting hjt", e);
            return "SQL error";
        }
    }

    private String addHJT(String s) {
        if(!s.contains("=")) return "Usage: addHJT [name]=[value]";
        String[] array = s.split("=");
        String name = array[0].trim();
        String value = array[1].trim();
        try (Connection connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO hjt (name, value) VALUES (?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, value);
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
        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, mysqlUser, pass);
    }
}
