package net.ae97.pokebot.extensions.dxdiag;

import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extension.Extension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by urielsalis on 1/26/2017
 */
public class DxdiagParser extends Extension {
    @Override
    public String getName() {
        return "DxdiagParser";
    }

    @Override
    public void load() {
        DxdiagListener listener = new DxdiagListener(this);
        PokeBot.getEventHandler().registerListener(listener);
        PokeBot.getEventHandler().registerCommandExecutor(listener);
    }

    public Connection openConnection() throws SQLException {
        String host = getConfig().getString("host");
        int port = getConfig().getInt("port");
        String mysqlUser = getConfig().getString("user");
        String pass = getConfig().getString("pass");
        String database = getConfig().getString("database");
        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, mysqlUser, pass);
    }
}
