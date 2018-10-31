/*
 * Copyright (C) 2016 Joshua
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
package net.ae97.pokebot.extensions.mcping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.stream.Collectors;

import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionLoadFailedException;

/**
 *
 * @author Joshua
 */
public class MCPingExtension extends Extension implements CommandExecutor {

    @Override
    public void load() throws ExtensionLoadFailedException {
        PokeBot.getEventHandler().registerCommandExecutor(this);
    }
    
    @Override
    public void runEvent(CommandEvent ce) {
        if (ce.getArgs().length != 1) {
            ce.respond("Usage: mcping <server ip>[:port]");
            return;
        }
        try {
            HttpURLConnection request = (HttpURLConnection) new URL("https://sessionserver.mojang.com/blockedservers")
                    .openConnection();
            request.connect();
            final List<String> blacklisted = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines()
                    .collect(Collectors.toList());
            final String url = ce.getArgs()[0].split(":")[0];
            boolean isBlacklisted = isBlacklisted(blacklisted, url);
            if(isBlacklisted) {
                ce.respond("Server is blacklisted");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Process pinger = new ProcessBuilder().command("python", "bin/mcping.py", ce.getArgs()[0]).start();
            pinger.waitFor();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(pinger.getInputStream()))) {
                ce.respond(reader.readLine());
            }
        } catch (IOException | InterruptedException ex) {
            ce.respond("Error pinging server: " + ex.getMessage());
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "mcping"
        };
    }

    @Override
    public String getName() {
        return "mcping";
    }

    private static String getHash(String url)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(url.getBytes(StandardCharsets.UTF_8));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private boolean isBlacklisted(List<String> blacklisted, String url) {
        final List<String> parts = Arrays.asList(url.split("\\."));
        if(blacklisted.contains(getHash(url))) {
            return true;
        }
        for (int i = 1; i < parts.size(); i++) {
            final String asterisks = String.join("", Collections.nCopies(i, "*."));
            final String finalUrl = asterisks + String.join(".", parts.subList(i, parts.size()));
            if(blacklisted.contains(getHash(finalUrl))) {
                return true;
            }
        }
        return false;
    }
}
