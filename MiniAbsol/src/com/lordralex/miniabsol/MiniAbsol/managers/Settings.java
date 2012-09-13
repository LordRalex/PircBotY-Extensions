package com.lordralex.miniabsol.MiniAbsol.managers;

import com.lordralex.miniabsol.MiniAbsol.MiniAbsol;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Settings {

    MiniAbsol bot;
    private Map<String, Object> settings;

    public Settings(MiniAbsol instance) {
        bot = instance;
        settings = new ConcurrentHashMap<>();
        loadFile();
    }

    public void saveRem(String name, String line) {
        try {
            try (FileWriter writer = new FileWriter(new File("data" + File.separator + "rem" + File.separator + name + ".txt"))) {
                writer.write(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deleteRem(String name) {
        new File("data" + File.separator + "rem" + File.separator + name + ".txt").delete();
    }

    private void loadFile() {
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
                        list = new ArrayList<>();
                    }
                    list.add(line.substring(2));
                    settings.put(lastAdded, list);
                } else {
                    String name = line.split(":")[0];
                    lastAdded = name;
                    String obj;
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

    public void saveDefaultSettings() throws IOException {

        if (new File("settings" + File.separator + "settings.txt").exists()) {
            return;
        }
        String[] lines = new String[]{
            "auto-join:",
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
        try (FileWriter writer = new FileWriter(new File("settings" + File.separator + "settings.txt"))) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    public void saveTells(String name, String[] lines) {
        if (name == null || lines == null || name.length() == 0) {
            return;
        }
        name = name.toLowerCase();
        new File("data" + File.separator + "tells").mkdirs();
        new File("data" + File.separator + "tells" + File.separator + name + ".txt").delete();
        try {
            try (FileWriter writer = new FileWriter(new File("data" + File.separator + "tells" + File.separator + name + ".txt"))) {
                for (String line : lines) {
                    writer.write(line + "\n");
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void clearTells(String name) {
        saveTells(name, new String[0]);
    }

    public String[] getTells(String name) {
        try {
            if (name == null || name.length() == 0) {
                return new String[0];
            }
            name = name.toLowerCase();
            List<String> lines;
            try (Scanner reader = new Scanner(new File("data" + File.separator + "tells" + File.separator + name + ".txt"))) {
                lines = new ArrayList<>();
                while (reader.hasNext()) {
                    lines.add(reader.nextLine().trim());
                }
            }
            String[] result = lines.toArray(new String[0]);
            return result;
        } catch (FileNotFoundException ex) {
            return new String[0];
        }
    }

    public void addTell(String sender, String target, String message) {
        if (target == null || sender == null || message == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        String[] old = getTells(target);
        lines.addAll(Arrays.asList(old));
        lines.add("From " + sender + "-> " + message);
        saveTells(target, lines.toArray(new String[0]));
    }

    public int getInt(String path) {
        Object obj = settings.get(path);
        String ob = (String) obj;
        try {
            Integer value = Integer.parseInt(ob);
            return value.intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public List<String> getStringList(String path) {
        Object obj = settings.get(path);
        if (obj instanceof List) {
            List<String> list = (List<String>) obj;
            return list;
        }
        return new ArrayList<>();
    }

    public String getString(String path) {
        Object obj = settings.get(path);
        if (obj instanceof String) {
            String value = (String) obj;
            return value;
        }
        return "";
    }
}
