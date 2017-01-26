package net.ae97.pokebot.extensions.dxdiag.download;

import net.ae97.pokebot.extensions.dxdiag.DxdiagParser;
import net.ae97.pokebot.extensions.dxdiag.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Created by urielsalis on 1/26/2017
 */
public class DownloadMain {
    public static DxdiagParser core;


    public static void add(Config.GPU gpu, String manufacturer) {
        try (Connection connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Dxdiag where isold = TRUE AND drivername = ?")) {
                statement.setString(1, Util.removeSpecialChars(gpu.name.toLowerCase().trim()));
                statement.execute();
            }
            try (PreparedStatement statement = connection.prepareStatement("UPDATE Dxdiag SET isold=TRUE WHERE drivername = ?")) {
                statement.setString(1, Util.removeSpecialChars(gpu.name.toLowerCase().trim()));
                statement.execute();
            }
        } catch (SQLException e) {
            core.getLogger().log(Level.SEVERE, "Error updating old links", e);
        }
        if (!gpu.downloadLinkWin32Vista.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "Vista", "32", manufacturer, gpu.downloadLinkWin32Vista);
        }
        if (!gpu.downloadLinkWin64Vista.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "Vista", "64", manufacturer, gpu.downloadLinkWin64Vista);
        }

        if (!gpu.downloadLinkWin32XP.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "XP", "32", manufacturer, gpu.downloadLinkWin32XP);
        }
        if (!gpu.downloadLinkWin64XP.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "XP", "64", manufacturer, gpu.downloadLinkWin64XP);
        }

        if (!gpu.downloadLinkWin327.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "7", "32", manufacturer, gpu.downloadLinkWin327);
        }
        if (!gpu.downloadLinkWin647.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "7", "64", manufacturer, gpu.downloadLinkWin647);
        }

        if (!gpu.downloadLinkWin328.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "8", "32", manufacturer, gpu.downloadLinkWin328);
        }
        if (!gpu.downloadLinkWin648.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "8", "64", manufacturer, gpu.downloadLinkWin648);
        }

        if (!gpu.downloadLinkWin3281.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "8.1", "32", manufacturer, gpu.downloadLinkWin3281);
        }
        if (!gpu.downloadLinkWin6481.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "8.1", "64", manufacturer, gpu.downloadLinkWin6481);
        }

        if (!gpu.downloadLinkWin3210.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "10", "32", manufacturer, gpu.downloadLinkWin3210);
        }
        if (!gpu.downloadLinkWin6410.isEmpty()) {
            add(Util.removeSpecialChars(gpu.name.toLowerCase().trim()), "10", "64", manufacturer, gpu.downloadLinkWin6410);
        }

    }

    private static void add(String name, String os, String arch, String manufacturer, String link) {
        try (Connection connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Dxdiag (drivername, os, arch, manufacturer, link, isold) VALUES (?, ?, ?, ?, ?, FALSE)")) {
                statement.setString(1, name);
                statement.setString(2, os);
                statement.setString(3, arch);
                statement.setString(4, manufacturer);
                statement.setString(5, link);

                statement.execute();
            }
        } catch (SQLException e) {
            core.getLogger().log(Level.SEVERE, "Error updating old links", e);

        }
    }

    private static Connection openConnection() throws SQLException {
        String host = core.getConfig().getString("host");
        int port = core.getConfig().getInt("port");
        String mysqlUser = core.getConfig().getString("user");
        String pass = core.getConfig().getString("pass");
        String database = core.getConfig().getString("database");
        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, mysqlUser, pass);
    }
}
