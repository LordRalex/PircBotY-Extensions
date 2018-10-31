package net.ae97.pokebot.extensions.mcping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.stream.Collectors;

public class BlacklistChecker {
    private static List<String> blacklisted = Collections.emptyList();

    public static void setBlacklist(List<String> blacklisted) {
        BlacklistChecker.blacklisted = blacklisted.stream().map(BlacklistChecker::getHash).collect(Collectors.toList());
    }

    public static void refreshBlacklist() throws IOException {
        HttpURLConnection request = (HttpURLConnection) new URL("https://sessionserver.mojang.com/blockedservers")
                .openConnection();
        request.connect();
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            blacklisted = bufferedReader.lines().map(String::toLowerCase).collect(Collectors.toList());
        }
    }

    public static boolean isHostBlacklisted(String host) {
        final String hostWithoutPort = host.split(":")[0];
        return isIPBlacklisted(hostWithoutPort) || isUrlBlacklisted(hostWithoutPort);
    }

    public static boolean isUrlBlacklisted(String url) {
        final List<String> parts = Arrays.asList(url.split("\\."));
        if(isBlacklisted(url)) {
            return true;
        }
        for (int i = 1; i < parts.size(); i++) {
            final String finalUrl = "*." + String.join(".", parts.subList(i, parts.size()));
            if(isBlacklisted(finalUrl)) {
                return true;
            }
        }
        try {
            final InetAddress inetAddress = InetAddress.getByName(url);
            return isIPBlacklisted(inetAddress.getHostAddress());
        } catch (UnknownHostException ignored) {
            //not a valid url, doesnt matter
            return false;
        }
    }

    public static boolean isIPBlacklisted(String ip) {
        if(!isIP(ip)) {
            return false;
        }
        if(isBlacklisted(ip)) {
            return true;
        }
        final List<String> parts = Arrays.asList(ip.split("\\."));
        for (int i = 1; i < parts.size(); i++) {
            final String finalUrl = String.join(".", parts.subList(0, parts.size()-i)) + ".*";
            if(isBlacklisted(finalUrl)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlacklisted(String match) {
        return blacklisted.contains(getHash(match.toLowerCase()));
    }

    public static boolean isIP(String host) {
        final String[] parts = host.split("\\.");
        return parts.length==4 && Arrays.stream(parts)
                .map(Integer::valueOf)
                .allMatch(octec -> octec >= 0 && octec <= 255);
    }

    public static String getHash(String url)
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

    public static String byteToHex(final byte[] hash)
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

}
