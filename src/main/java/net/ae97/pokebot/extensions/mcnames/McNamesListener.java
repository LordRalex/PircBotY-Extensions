package net.ae97.pokebot.extensions.mcnames;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ae97.pircboty.ChatFormat;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.Listener;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by urielsalis on 1/26/2017
 */
public class McNamesListener implements Listener, CommandExecutor {

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getCommand().equals("ns")) {
            if (event.getArgs().length == 0) {
                event.getUser().send().notice("Usage: ns <name> [--extended]");
                return;
            }
            boolean extended = false;
            if (event.getArgs().length > 1 && event.getArgs()[1].equals("--extended")) extended = true;
            String result = getNS(event.getArgs()[0], extended);
            String[] splitted = result.split("\n");
            for(String s: splitted) {
                event.respond(s);
            }
            return;
        }
    }

    private String getNS(String s, boolean extended) {
        if (s == null || s.isEmpty()) return "Invalid username";
        long unixTimestamp = System.currentTimeMillis() / 1000l;


        String result = findInfo(s, extended, false, 0);
        if (result == null || result.isEmpty()) {
            result = findInfo(s, extended, true, unixTimestamp - 2505600); //2500 seems to always work, get previous name
            if (result == null || result.isEmpty()) {
                result = findInfo(s, extended, true, 0); //get original name
                if (result == null || result.isEmpty())
                    return ChatFormat.RED + "Username doesn't exists" + ChatFormat.NORMAL;
            }
        }
        return result;
    }

    private String findInfo(String s, boolean extended, boolean previousName, long timestamp) {
        try {
            String str = "https://api.mojang.com/users/profiles/minecraft/" + s;
            if (previousName) str = str + "?at=" + timestamp;
            URL url = new URL(str);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
            String id = rootobj.get("id").getAsString();
            String currentName = rootobj.get("name").getAsString();
            boolean migrated = true;
            boolean paid = true;
            if (rootobj.has("legacy")) migrated = false;
            if (rootobj.has("demo")) paid = false;
            String[] names = getNames(id, extended);
            StringBuilder output = new StringBuilder();
            if (previousName) output.append(ChatFormat.RED + "Name was changed to: " + ChatFormat.NORMAL);
            output.append(ChatFormat.BOLD + currentName + ChatFormat.NORMAL + ": " + ChatFormat.BLUE + "UUID: " + ChatFormat.NORMAL + id + " ");
            output.append(paid ? ChatFormat.GREEN + "PAID " + ChatFormat.NORMAL : ChatFormat.RED + "DEMO " + ChatFormat.NORMAL);
            output.append(migrated ? ChatFormat.YELLOW + "MIGRATED " + ChatFormat.NORMAL : ChatFormat.RED + "LEGACY " + ChatFormat.NORMAL);
            if (names.length > 0) {
                String namesStr = StringUtils.join(names, ", ");
                output.append("\n" + ChatFormat.DARK_GRAY + "Previous names: " + ChatFormat.NORMAL + namesStr);
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String[] getNames(String id, boolean extended) {
        try {
            URL url = new URL("https://api.mojang.com/user/profiles/" + id + "/names");
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            JsonArray rootobj = root.getAsJsonArray(); //May be an array, may be an object.
            if (rootobj.size() == 1) return new String[]{};
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            if (extended) {
                String[] names = new String[rootobj.size() - 1];
                for (int i = 0; i < rootobj.size() - 1; i++) {
                    if (rootobj.get(i).getAsJsonObject().has("changedToAt")) {
                        names[i] = rootobj.get(i).getAsJsonObject().get("name").getAsString() + " changed " + format.format(new Date(new Timestamp(rootobj.get(i).getAsJsonObject().get("changedToAt").getAsLong()).getTime()));
                    } else {
                        names[i] = rootobj.get(i).getAsJsonObject().get("name").getAsString();
                    }
                }
                return names;
            } else {
                int limit = rootobj.size() - 3;
                if (limit < 0) limit = 0;
                String[] names = new String[rootobj.size() - limit];
                int counter = 0;
                for (int i = rootobj.size() - 1; i >= limit; i--) {
                    if (rootobj.get(i).getAsJsonObject().has("changedToAt")) {
                        names[counter] = rootobj.get(i).getAsJsonObject().get("name").getAsString() + " changed " + format.format(new Date(new Timestamp(rootobj.get(i).getAsJsonObject().get("changedToAt").getAsLong()).getTime()));
                    } else {
                        names[counter] = rootobj.get(i).getAsJsonObject().get("name").getAsString();
                    }
                    counter++;
                }
                return names;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{};
    }


    @Override
    public String[] getAliases() {
        return new String[]{"ns"};
    }
}
