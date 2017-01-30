package net.ae97.pokebot.extensions.mcnames;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import jline.internal.Nullable;
import net.ae97.pircboty.ChatFormat;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.extension.Extension;

public class McNamesExtension extends Extension implements Listener, CommandExecutor {
    
    private static final long MS_IN_A_SECOND = 1000L;
    
    private Gson gson;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601 format
    
    @Override
    public String getName() {
        return "McNames";
    }

    @Override
    public void load() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return new Date(json.getAsLong() * MS_IN_A_SECOND);
            }
        });

        gson = gsonBuilder.create();
        
        PokeBot.getEventHandler().registerListener(this);
        PokeBot.getEventHandler().registerCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (event.getCommand().equals("ns")) {
            if (event.getArgs().length != 1) {
                event.getUser().send().notice("Usage: ns <name>");
                return;
            }
            String result = getNS(event.getArgs()[0]);
            String[] split = result.split("\n");
            for (String s : split) {
                event.respond(s);
            }
            return;
        }
    }

    private String getNS(String s) {
        if (s == null || s.isEmpty()) {
            return "Invalid username";
        }

        // the Mojang API uses Unix timestamps without ms (1 second accuracy)
        long unixTimestamp = System.currentTimeMillis() / MS_IN_A_SECOND;

        // find the user who currently has this name
        String result = findInfo(s, null);
        if (result == null || result.isEmpty()) {
            // otherwise, find the user who until recently had this name
            result = findInfo(s, unixTimestamp - 2505600L); // 29 days (in seconds)
            if (result == null || result.isEmpty()) {
                // otherwise, find the user who originally had this name
                result = findInfo(s, 0L);
                if (result == null || result.isEmpty()) {
                    return ChatFormat.RED + "Username doesn't exist" + ChatFormat.NORMAL;
                }
            }
        }
        return result;
    }

    /**
     * Get information about a Minecraft username
     * @param username Minecraft username of user
     * @param timestamp Time the user had the name at, or <code>null</code> for the current time
     * @return \n delimited string containing information about a username
     */
    private String findInfo(String username, @Nullable Long timestamp) {
        try {
            String str = "https://api.mojang.com/users/profiles/minecraft/" + username;
            if (timestamp != null) {
                str = str + "?at=" + timestamp.toString();
            }
            URL url = new URL(str);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); // from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); // Convert the input
                                                                                                    // stream to a json
                                                                                                    // element
            JsonObject rootobj = root.getAsJsonObject(); // May be an array, may be an object.
            String id = rootobj.get("id").getAsString();
            String currentName = rootobj.get("name").getAsString();
            boolean migrated = true;
            boolean paid = true;
            if (rootobj.has("legacy")) {
                migrated = false;
            }
            if (rootobj.has("demo")) {
                paid = false;
            }
            NameResponse[] names = getNames(id);
            StringBuilder output = new StringBuilder();
            if (timestamp != null) {
                output.append(ChatFormat.RED + "Name was changed to: " + ChatFormat.NORMAL);
            }
            output.append(ChatFormat.BOLD + currentName + ChatFormat.NORMAL + ": " + ChatFormat.BLUE + "UUID: "
                    + ChatFormat.NORMAL + id + " ");
            output.append(paid ? ChatFormat.GREEN + "PAID " + ChatFormat.NORMAL
                    : ChatFormat.RED + "DEMO " + ChatFormat.NORMAL);
            output.append(migrated ? ChatFormat.YELLOW + "MIGRATED " + ChatFormat.NORMAL
                    : ChatFormat.RED + "LEGACY " + ChatFormat.NORMAL);

            if (names != null && names.length > 1) {
                output.append("\n" + ChatFormat.DARK_GRAY + "Name history: " + ChatFormat.NORMAL + names[0].getName());
                for (int i = 1; i < names.length; i++) {
                    output.append(" → " + dateFormat.format(names[i].getChangedToAt()) + " → " + names[i].getName());
                }
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get name history of a user
     * @param id uuid of the user
     * @return Array of past names, or null if something went wrong
     */
    private NameResponse[] getNames(String id) {
        try {
            URL url = new URL("https://api.mojang.com/user/profiles/" + id + "/names");
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            return gson.fromJson(new InputStreamReader(request.getInputStream()), NameResponse[].class);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Name response DTO.
     * See <a href="http://wiki.vg/Mojang_API#UUID_-.3E_Name_history">
     *   http://wiki.vg/Mojang_API
     * </a>
     */
    private class NameResponse {
        private String name;
        private Date changedToAt;

        public String getName() {
            return name;
        }

        public Date getChangedToAt() {
            return changedToAt;
        }
    }

    @Override
    public String[] getAliases() {
        return new String[] { "ns" };
    }
}
