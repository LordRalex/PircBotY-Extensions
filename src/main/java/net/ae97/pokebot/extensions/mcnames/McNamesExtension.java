package net.ae97.pokebot.extensions.mcnames;

import com.google.gson.*;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.extension.Extension;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class McNamesExtension extends Extension implements Listener, CommandExecutor {

    private static final long MS_IN_A_SECOND = 1000L;
    private static final long SECONDS_IN_A_MONTH = 2505600L; // 29 days (in seconds)
    private static final int MAX_MESSAGE_CHARACTERS = 350;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601 format
    private Gson gson;

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
        if (event.getArgs().length != 1) {
            event.getUser().send().notice("Usage: ns <name>");
            return;
        }
        String result = getNameHistory(event.getArgs()[0]);
        String[] split = result.split("\n");
        for (String s : split) {
            event.respond(s);
        }
    }

    private String getNameHistory(String username) {
        if (username == null || username.isEmpty()) {
            return "Invalid username";
        }

        // the Mojang API uses Unix timestamps without ms (1 second accuracy)
        long unixTimestamp = System.currentTimeMillis() / MS_IN_A_SECOND;
        boolean nameChanged = false;

        try {

            // find the user who currently has this name
            Optional<AccountStatus> status = getAccountStatus(username);
            String result = "";

            if (status.isPresent()) {
                result = getInfo(status.get());
            }

            if (result.isEmpty()) {
                // otherwise, find the user who until recently had this name
                status = getLegacyAccountStatus(username, unixTimestamp - SECONDS_IN_A_MONTH);
                if (status.isPresent()) {
                    result = getInfo(status.get());
                    nameChanged = true;
                }

                if (result.isEmpty()) {
                    // otherwise, find the user who originally had this name
                    status = getLegacyAccountStatus(username, 0);

                    if (status.isPresent()) {
                        result = getInfo(status.get());
                    }

                    if (result.isEmpty()) {
                        return "Username doesn't exist";
                    }
                }
            }

            if (nameChanged) {
                result = "Name was changed to: " + result;
            }
            return result;
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error looking up player " + username, e);
            return "Error looking up player " + username + ", Mojang API might be down";
        }
    }

    /**
     * Get information about a Minecraft username
     *
     * @param accountStatus status of the user account
     * @return \n delimited string containing information about a username
     */
    private String getInfo(AccountStatus accountStatus) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append(accountStatus.getName()).append(" - ").append("UUID: ").append(accountStatus.getId()).append(" - ");

        switch (accountStatus.getPaidStatus()) {
            case PAID:
                output.append("PAID");
                break;
            case DEMO:
                output.append("DEMO");
                break;
            case UNKNOWN:
                output.append("UNKNOWN");
                break;
        }

        output.append(" - ");

        if (accountStatus.isMojang()) {
            output.append("MOJANG");
        } else {
            output.append("LEGACY");
        }

        if (accountStatus.isMojang()) {
            List<NameResponse> names = getNames(accountStatus.getId());
            if (names.size() > 1) {
                StringBuilder nameHistory = new StringBuilder();
                nameHistory.append("\nName history: ").append(names.get(0).getName());

                names = names.subList(1, names.size());

                for (NameResponse name : names) {
                    nameHistory.append(String.format(" → %s (%s)", name.getName(), dateFormat.format(name.getChangedToAt())));
                }

                if (nameHistory.length() > MAX_MESSAGE_CHARACTERS) {
                    output.append(String.format("\nName history too long for display: https://namemc.com/profile/%s", accountStatus.id));
                } else {
                    output.append(nameHistory);
                }
            }
        }
        return output.toString();
    }

    private Optional<AccountStatus> getAccountStatus(String username) throws IOException {
        URL url = new URL("https://api.mojang.com/profiles/minecraft");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type", "application/json");
        String query = "[\"" + username + "\"]";
        request.setDoOutput(true);
        request.setRequestProperty("Content-Length", Integer.toString(query.length()));
        request.getOutputStream().write(query.getBytes(StandardCharsets.UTF_8));
        request.connect();

        JsonParser jp = new JsonParser(); // from gson\
        try (InputStreamReader reader = new InputStreamReader(request.getInputStream())) {
            JsonElement root = jp.parse(reader);
            JsonArray rootArray = root.getAsJsonArray();
            if (rootArray.size() == 0) {
                return Optional.empty();
            }

            JsonObject user = rootArray.get(0).getAsJsonObject();
            PaidStatus paidStatus;
            if (user.has("demo")) {
                paidStatus = PaidStatus.DEMO;
            } else {
                paidStatus = PaidStatus.PAID;
            }
            boolean legacy = user.has("legacy");
            String id = user.get("id").getAsString();
            String name = user.get("name").getAsString();

            return Optional.of(new AccountStatus(!legacy, paidStatus, id, name));
        }
    }

    private Optional<AccountStatus> getLegacyAccountStatus(String username, long timestamp) throws IOException {
        String str = "https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + timestamp;

        URL url = new URL(str);
        HttpURLConnection request = null;
        try {
            request = (HttpURLConnection) url.openConnection();
            request.connect();

            if (request.getResponseCode() == 204) {
                return Optional.empty();
            } else if (request.getResponseCode() == 200) {
                JsonParser jp = new JsonParser();
                try (InputStreamReader reader = new InputStreamReader(request.getInputStream())) {
                    JsonElement root = jp.parse(reader);
                    JsonObject rootobj = root.getAsJsonObject(); // May be an array, may be an object.
                    String id = rootobj.get("id").getAsString();
                    String currentName = rootobj.get("name").getAsString();
                    boolean isLegacy = username.equals(currentName); // If the name was changed its definately migrated, else we dont know
                    return Optional.of(new AccountStatus(isLegacy, PaidStatus.UNKNOWN, id, currentName));
                }
            } else {
                throw new IOException("Unexpected response code " + request.getResponseCode());
            }
        } finally {
            request.disconnect();
        }
    }

    /**
     * Get name history of a user
     *
     * @param id uuid of the user
     * @return Array of past names
     */
    private List<NameResponse> getNames(String id) throws IOException {
        URL url = new URL("https://api.mojang.com/user/profiles/" + id + "/names");
        HttpURLConnection request = null;

        try {
            request = (HttpURLConnection) url.openConnection();
            request.connect();

            if (request.getResponseCode() == 204) {
                return new ArrayList<>(0);
            }
            try (InputStreamReader reader = new InputStreamReader(request.getInputStream())) {
                return Arrays.asList(gson.fromJson(reader, NameResponse[].class));
            }
        } finally {
            request.disconnect();
        }
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
        private boolean mojang;
        private PaidStatus paid;
        private String id;
        private String name;

        public AccountStatus(boolean mojang, PaidStatus paid, String id, String name) {
            this.mojang = mojang;
            this.paid = paid;
            this.id = id;
            this.name = name;
        }

        public boolean isMojang() {
            return mojang;
        }

        public PaidStatus getPaidStatus() {
            return paid;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private enum PaidStatus {
        PAID,
        DEMO,
        UNKNOWN
    }

    @Override
    public String[] getAliases() {
        return new String[]{"ns", "names"};
    }
}
