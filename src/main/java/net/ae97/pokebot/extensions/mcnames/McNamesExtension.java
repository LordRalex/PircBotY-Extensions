package net.ae97.pokebot.extensions.mcnames;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.*;

import net.ae97.pircboty.ChatFormat;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.extension.Extension;

public class McNamesExtension extends Extension implements Listener, CommandExecutor {

    private static final long MS_IN_A_SECOND = 1000L;
    private static final long SECONDS_IN_A_MONTH = 2505600L; // 29 days (in seconds)
    private static final int MAX_MESSAGE_CHARACTERS = 350;

    private Gson gson;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601 format

    @Override
    public String getName() {
        return "McNames";
    }

    @Override
    public void load() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsLong()));
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
            result = findInfo(s, unixTimestamp - SECONDS_IN_A_MONTH);
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
     * 
     * @param username Minecraft username of user
     * @param timestamp Time the user had the name at, or <code>null</code> for legacy/demo and current time
     * @return \n delimited string containing information about a username
     */
    private String findInfo(String username, Long timestamp) {
        try {
            StringBuilder output = new StringBuilder();
            AccountStatus accountResult;
            if(timestamp==null) {
                accountResult = getAccountStatus(username);
            } else {
                accountResult = getLegacyAccountStatus(username, timestamp);
                output.append(ChatFormat.RED + "Name was changed to: " + ChatFormat.NORMAL);
            }

            if(!accountResult.exists) {
                return null;
            }

            output.append(ChatFormat.BOLD + accountResult.getName() + ChatFormat.NORMAL + ": " + ChatFormat.BLUE + "UUID: "
                    + ChatFormat.NORMAL + accountResult.getId() + " ");

            if (accountResult.isPaid())  {
                output.append(ChatFormat.GREEN + "PAID " + ChatFormat.NORMAL);
            } else {
                output.append(ChatFormat.RED + "DEMO " + ChatFormat.NORMAL);
            }

            if (accountResult.isMigrated())  {
                output.append(ChatFormat.YELLOW + "MIGRATED " + ChatFormat.NORMAL);
            } else {
                output.append(ChatFormat.RED + "LEGACY " + ChatFormat.NORMAL);
            }

            if(accountResult.isMigrated()) {
                List<NameResponse> names = getNames(accountResult.getId());
                if (!names.isEmpty()) {
                    StringBuilder nameHistory = new StringBuilder();
                    nameHistory.append("\n" + ChatFormat.DARK_GRAY + "Name history: " + ChatFormat.NORMAL + names.get(0).getName());

                    for(NameResponse name: names) {
                        nameHistory.append(String.format(" → %s (%s)", name.getName(), dateFormat.format(name.getChangedToAt())));
                    }

                    if (nameHistory.length() > MAX_MESSAGE_CHARACTERS) {
                        output.append(String.format("\nName history too long for display: https://namemc.com/profile/%s", accountResult.id));
                    } else {
                        output.append(nameHistory);
                    }
                }
            }
            return output.toString();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error looking up player name", e);
            return null;
        }
    }

    private AccountStatus getAccountStatus(String username) throws IOException {
        URL url = new URL("https://api.mojang.com/profiles/minecraft");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type", "application/json");
        String query = "[\""+username+"\"]";
        request.setRequestProperty("Content-Length", Integer.toString(query.length()));
        request.getOutputStream().write(query.getBytes(StandardCharsets.UTF_8));
        request.connect();

        JsonParser jp = new JsonParser(); // from gson
        JsonElement root = jp.parse(new InputStreamReader(request.getInputStream()));
        JsonArray rootArray = root.getAsJsonArray();
        if(rootArray.size()==0) {
            return new AccountStatus();
        }

        JsonObject user = rootArray.get(0).getAsJsonObject();
        boolean demo = user.has("demo");
        boolean legacy = user.has("legacy");
        String id = user.get("id").getAsString();
        String name = user.get("name").getAsString();

        return new AccountStatus(!demo, !legacy, id, name);
    }

    private AccountStatus getLegacyAccountStatus(String username, long timestamp) throws IOException {
        String str = "https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + timestamp;

        URL url = new URL(str);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader(request.getInputStream()));
        JsonObject rootobj = root.getAsJsonObject(); // May be an array, may be an object.
        String id = rootobj.get("id").getAsString();
        String currentName = rootobj.get("name").getAsString();
        return new AccountStatus(false, false, id, currentName);
    }

    /**
     * Get name history of a user
     * @param id uuid of the user
     * @return Array of past names, or null if something went wrong
     */
    private List<NameResponse> getNames(String id) {
        try {
            URL url = new URL("https://api.mojang.com/user/profiles/" + id + "/names");
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            return Arrays.asList(gson.fromJson(new InputStreamReader(request.getInputStream()), NameResponse[].class));

        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error looking up player uuid", e);
        }
        return Collections.emptyList();
    }

    /**
     * Name response DTO. See <a href="http://wiki.vg/Mojang_API#UUID_-.3E_Name_history"> http://wiki.vg/Mojang_API </a>
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

    private class AccountStatus {
        private boolean migrated;
        private boolean paid;
        private boolean exists;
        private String id;
        private String name;

        public AccountStatus(boolean migrated, boolean paid, String id, String name) {
            this.migrated = migrated;
            this.paid = paid;
            this.id = id;
            this.name = name;
        }

        public AccountStatus() {
            this.exists = false;
        }

        public boolean isMigrated() {
            return migrated;
        }

        public boolean isPaid() {
            return paid;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isExists() {
            return exists;
        }
    }

    @Override
    public String[] getAliases() {
        return new String[] { "ns" };
    }
}
