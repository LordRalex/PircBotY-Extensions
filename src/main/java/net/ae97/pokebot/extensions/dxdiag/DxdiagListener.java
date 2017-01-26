package net.ae97.pokebot.extensions.dxdiag;

import com.google.gson.Gson;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.Listener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.logging.Level;

/**
 * Created by urielsalis on 1/26/2017
 */
public class DxdiagListener implements Listener, CommandExecutor {
    private final DxdiagParser core;
    private String apiKey;

    public DxdiagListener(DxdiagParser system) {
        core = system;
        core.getConfig().getString("arkAPIKey");
    }

    @Override
    public void runEvent(CommandEvent event) {
        if(event.getCommand().equals("dx")) {
            if (event.getArgs().length == 0) {
                event.respond("Usage: dx <link>");
                return;
            }
            event.respond(parseDxdiag(event.getArgs()[0]));
        }

    }

    private String findCPU(String tmp, String minified, boolean is64) {
        //ark.intel.com
        String[] strs = tmp.split("\\s+");
        String cpu = null;
        for(String str: strs) {
            if(Character.isLetter(str.charAt(0)) && Character.isDigit(str.charAt(1))) {
                cpu = str;
                break;
            }
        }
        try {
            if(cpu != null) {
                InputStreamReader reader = new InputStreamReader(new URL("http://odata.intel.com/API/v1_0/Products/Processors()?api_key="+apiKey+"&$select=ProductId,CodeNameEPMId,GraphicsModel&$filter=substringof(%27"+cpu+"%27,ProductName)&$format=json").openStream());
                Ark ark = new Gson().fromJson(reader, Ark.class);
                boolean showMessage = true;
                for(Ark.CPU cpu2: ark.d) {
                    if(cpu2.GraphicsModel != null) {
                        //search in database
                        String message = findDriver(cpu2.GraphicsModel, minified, is64);
                        if(showMessage)
                            return "Ark: " + message;
                        showMessage = false;
                        break;
                    }
                }
                if(showMessage)
                    return "Cant find "+cpu+" in ark";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Cant find "+cpu+" in ark";
    }

    private String parseDxdiag(String s) {
        String minified = "";
        boolean is64 = false;
        StringBuffer result = new StringBuffer();
        for (String str : s.trim().split(" ")) {
            if (str.contains("paste.ubuntu.com")) {
                try {
                    Document document = Jsoup.parse(new URL(str), 10000);
                    Element code = document.select(".code").first();
                    String value = code.select(".paste").first().select("pre").first().text();
                    String[] lines2 = value.split("\n");
                    boolean showedCpu = false;
                    for (String line2 : lines2) {
                        if (line2.contains("Operating System")) {
                            if (line2.contains("64")) is64 = true;
                            String[] split = line2.trim().split(" ");
                            minified = split[3];
                        } else if (line2.contains("Card name")) {
                            String card = line2.trim().split(":")[1];
                            result.append("\n" + findDriver(card, minified, is64));
                        } else if (!showedCpu && line2.contains("Processor: ") && !line2.contains("Video")) {
                            result.append("\n" + findCPU(line2.trim().split(":")[1].trim(), minified, is64));
                            showedCpu = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return result.substring(1);
    }

    public String findDriver(String name, String os, boolean is64) {
        if (!name.contains("Standard VGA") && !name.contains("Microsoft")) {
            name = name.replace("NVIDIA ", "").replace("(R)", "").replace("AMD ", "").replace("®", "").toLowerCase().trim();
            if(name.equals("intel hd graphics")) return "Do Manual search https://www-ssl.intel.com/content/www/us/en/support/graphics-drivers/000005526.html & https://www-ssl.intel.com/content/www/us/en/support/graphics-drivers/000005538.html";
            try (Connection connection = openConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT link FROM Dxdiag where os = ? AND arch = ? AND Name like ?")) {
                    statement.setString(1, os);
                    statement.setString(2, is64 ? "64" : "32");
                    statement.setString(3, "%" + name + "%");
                    ResultSet set = statement.executeQuery();
                    while (set.next()) {
                        return set.getString("link");
                    }
                    return "Not found";
                }
            } catch (SQLException e) {
                core.getLogger().log(Level.SEVERE, "Error inserting hjt", e);
                return "SQL error";
            }
        }
        return "Not found";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"dx"};
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
