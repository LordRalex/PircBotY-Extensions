/*
 * Copyright (C) 2013 Lord_Ralex
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

import net.ae97.aebot.AeBot;
import net.ae97.aebot.api.EventField;
import net.ae97.aebot.api.EventType;
import net.ae97.aebot.api.Listener;
import net.ae97.aebot.api.events.CommandEvent;
import net.ae97.aebot.api.sender.Sender;
import net.ae97.aebot.settings.Settings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Lord_Ralex
 * @version 1.0
 */
public class FileCommand extends Listener {

    private String urlBase;
    private File folder;
    private Settings settings;

    @Override
    public void onLoad() {
        settings = new Settings(new File("settings", "file.yml"));
        urlBase = settings.getString("file-url");
        String folderPath = settings.getString("file-path");
        if (folderPath == null || folderPath.isEmpty()) {
            folderPath = "/var/www/html/";
        }
        folder = new File(folderPath);
    }

    @Override
    @EventType(event = EventField.Command)
    public void runEvent(CommandEvent event) {
        Sender target = event.getChannel();
        if (target == null) {
            target = event.getUser();
            if (target == null) {
                return;
            }
        }
        if (event.getCommand().equalsIgnoreCase("createfile")) {
            if (event.getArgs().length != 2) {
                target.sendMessage("*createfile <link> <file>");
                return;
            }
            try {
                String url = event.getArgs()[0];
                if (url.startsWith("http://pastebin.com")) {
                    if (!url.startsWith("http://pastebin.com/raw.php?i=")) {
                        url = url.replace("http://pastebin.com/", "http://pastebin.com/raw.php?i=");
                    }
                    URL link = new URL(url);
                    InputStream in = link.openStream();
                    BufferedWriter writer;
                    try (BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(in))) {
                        writer = new BufferedWriter(new FileWriter(new File(folder, event.getArgs()[1] + ".txt")));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                    writer.close();
                    target.sendMessage("File written to " + urlBase + "/" + event.getArgs()[1] + ".txt");
                    return;
                } else if (url.startsWith("http://pastie.org")) {
                    if (!url.endsWith("text")) {
                        if (!url.endsWith("/")) {
                            url = url + "/";
                        }
                        url = url + "text";
                    }
                    URL link = new URL(url);
                    InputStream in = link.openStream();
                    BufferedWriter writer;
                    try (BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(in))) {
                        writer = new BufferedWriter(new FileWriter(new File(folder, event.getArgs()[1] + ".txt")));
                        String line;
                        boolean isStuff = false;
                        while ((line = reader.readLine()) != null) {
                            if (isStuff && !line.trim().equalsIgnoreCase("</pre>")) {
                                writer.write(line.replace("<br/>", System.lineSeparator()));
                            } else if (line.trim().equalsIgnoreCase("<pre>")) {
                                isStuff = true;
                            } else {
                                isStuff = false;
                            }
                        }
                    }
                    writer.close();
                    target.sendMessage("File written to " + urlBase + "/" + event.getArgs()[1] + ".txt");
                    return;
                } else {
                    target.sendMessage("I don't support that link yet");
                }
            } catch (MalformedURLException ex) {
                target.sendMessage("Invalid url");
            } catch (IOException ex) {
                target.sendMessage("Error occured");
                AeBot.logSevere("Error on making stuff", ex);
            }
        }
        try {
            File file = new File(folder, event.getArgs()[0] + ".txt");
            Scanner reader = new Scanner(file);
            String title = reader.nextLine();
            if (title.length() > 50) {
                title = title.substring(0, 45) + "...";
            }
            target.sendMessage(urlBase + "/" + event.getArgs()[0] + ".txt - " + title);
        } catch (FileNotFoundException ex) {
            target.sendMessage("Cannot find that file");
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{
            "file",
            "createfile"
        };
    }
}
