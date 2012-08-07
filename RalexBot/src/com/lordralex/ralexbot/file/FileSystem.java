package com.lordralex.ralexbot.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Lord_Ralex
 * @since 1.0
 */
public class FileSystem {

    private static Map<String, Object> settings = new HashMap<String, Object>();

    /**
     * Saves a new rem or overrides an existing rem on the disk. If a rem
     * already exists, this will override it.
     *
     * @param name The name of the new rem
     * @param line The line to save for the rem
     */
    public static void saveRem(String name, String line) {
        try {
            FileWriter writer = new FileWriter(new File("data" + File.separator + "rem" + File.separator + name + ".txt"));
            writer.write(line);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Delete a rem. If a rem does not exist with the name, this will do
     * nothing.
     *
     * @param name The rem to delete
     */
    public static void deleteRem(String name) {
        new File("data" + File.separator + "rem" + File.separator + name + ".txt").delete();
    }

    /**
     * This loads the files for storing the settings for the bot.
     */
    public static void loadFiles() {
        if (!(new File("settings" + File.separator + "settings.txt").exists())) {
            try {
                saveDefaultSettings();
            } catch (IOException ex) {
            }
        }

        String lastAdded = null;
        try {
            Scanner fileReader = new Scanner(new File("settings" + File.separator + "settings.txt"));
            while (fileReader.hasNext()) {
                String line = fileReader.nextLine().trim();
                if (line.startsWith("-")) {
                    Object obj = settings.remove(lastAdded);
                    List<String> list = new ArrayList<>();
                    if (obj instanceof List) {
                        list = (List<String>) obj;
                    } else {
                        if (obj == null || ((String) obj).equalsIgnoreCase("") || ((String) obj).equalsIgnoreCase("null")) {
                        } else {
                            list.add((String) obj);
                        }
                    }

                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(line.substring(2));
                    settings.put(lastAdded, list);
                } else {
                    String name = line.split(":")[0];
                    lastAdded = name;
                    String obj = null;
                    try {
                        obj = line.split(":")[1].trim();
                    } catch (IndexOutOfBoundsException e) {
                        obj = "";
                    }
                    settings.put(name, obj);
                }
            }
            for (String key : settings.keySet()) {
                System.out.println(key + ": " + settings.get(key));
            }
        } catch (FileNotFoundException ex) {
        }
    }

    /**
     * This will save a default settings file if one does not exist
     *
     * @throws IOException Thrown if there is an error saving the default file
     */
    public static void saveDefaultSettings() throws IOException {

        if (new File("settings" + File.separator + "settings.txt").exists()) {
            return;
        }
        FileWriter writer = new FileWriter(new File("settings" + File.separator + "settings.txt"));
        String[] lines = new String[]{
            "auto-join:",
            "    - #ae97",
            "max-channels: 5",
            "nick: RalexBot",
            "password:",
            "mcnick:",
            "mcpass:",
            "sudo-pass: test123",
            "spam-message: 5",
            "spam-time: 3500",
            "spam-dupe: 3"
        };
        for (String line : lines) {
            writer.write(line + "\n");
        }
        writer.close();
    }

    /**
     * Saves a tell to the disk to retrieve later. If the name or lines are
     * null, this will return. If lines contains no variables, this will clear
     * the tells for that person.
     *
     * @param name The receiver of the tell
     * @param lines The message to send
     */
    public static void saveTells(String name, String[] lines) {
        if (name == null || lines == null || name.length() == 0) {
            return;
        }
        name = name.toLowerCase();
        new File("data" + File.separator + "tells").mkdirs();
        new File("data" + File.separator + "tells" + File.separator + name + ".txt").delete();
        try {
            FileWriter writer = new FileWriter(new File("data" + File.separator + "tells" + File.separator + name + ".txt"));
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.close();
        } catch (IOException e) {
            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * This will remove all tells for a nickname. This will delete the files as
     * well, so use only when this is what you want.
     *
     * @param name The name of the person to clear the tells from
     */
    public static void clearTells(String name) {
        saveTells(name, new String[0]);
    }

    /**
     * This will return all the messages for a given name.
     *
     * @param name The nick to get the tells for
     * @return A String[] containing the messages for them
     */
    public static String[] getTells(String name) {
        try {
            if (name == null || name.length() == 0) {
                return new String[0];
            }
            name = name.toLowerCase();
            Scanner reader = new Scanner(new File("data" + File.separator + "tells" + File.separator + name + ".txt"));
            List<String> lines = new ArrayList<>();
            while (reader.hasNext()) {
                lines.add(reader.nextLine().trim());
            }
            reader.close();
            String[] result = lines.toArray(new String[0]);
            return result;
        } catch (FileNotFoundException ex) {
            return new String[0];
        }
    }

    /**
     * Sends a message to a nickname.
     *
     * @param sender The nick who is sending the message
     * @param target The nick to send the message to
     * @param message The message to send
     */
    public static void addTell(String sender, String target, String message) {
        if (target == null || sender == null || message == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        String[] old = getTells(target);
        lines.addAll(Arrays.asList(old));
        lines.add("From " + sender + "-> " + message);
        saveTells(target, lines.toArray(new String[0]));
    }

    /**
     * Gets an int for a particular setting. This is in effect similar to how
     * YAML files work.
     *
     * @param path The path the int is reachable as (in terms of yaml)
     * @return The value of the stored integer for that path (or 0 if not found)
     */
    public static int getInt(String path) {
        Object obj = settings.get(path);
        String ob = (String) obj;
        try {
            Integer value = Integer.parseInt(ob);
            return value.intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Gets a List of Strings for a particular setting. This is in effect
     * similar to how YAML files work.
     *
     * @param path The path the list is stored as (like yaml)
     * @return The list of strings for that setting. If no setting was found, an
     * empty list is returned *
     */
    public static List<String> getStringList(String path) {
        Object obj = settings.get(path);
        if (obj instanceof List) {
            List<String> list = (List<String>) obj;
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * Gets a Strings for a particular setting. This is in effect similar to how
     * YAML files work.
     *
     * @param path The path the string is stored as (like yaml)
     * @return The String that is stored for that path, or "" if there is no
     * value found.
     */
    public static String getString(String path) {
        Object obj = settings.get(path);
        if (obj instanceof String) {
            String value = (String) obj;
            return value;
        }
        return "";
    }
}
