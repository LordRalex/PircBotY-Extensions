/*
 * Copyright (C) 2014 Joshua
 *
 * This file is a part of pokebot-extensions
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
package net.ae97.pokebot.extensions.scollstopic;

import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionLoadFailedException;

/**
 *
 * @author Joshua
 */
public class ScrollsTopic extends Extension implements CommandExecutor {

    private final String VERSION_URL = "https://s3.amazonaws.com/scrolls-versions/versions/versions.json";

    @Override
    public String getName() {
        return "ScrollsTopic Manager";
    }

    @Override
    public void load() throws ExtensionLoadFailedException {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (!event.getChannel().getName().equalsIgnoreCase("#scrollsguide")) {
            return;
        }
        try {
            URL url = new URL(VERSION_URL);
            try (JsonReader json = new JsonReader(new BufferedReader(new InputStreamReader(url.openStream())))) {
                json.beginObject();
                json.nextName();
                json.beginObject();
                json.nextName();
                char[] prodVersion = json.nextString().replace("version-", "").replace("-production", "").toCharArray();
                String prodVersionCompiled = new String(new char[]{
                    prodVersion[0],
                    '.',
                    prodVersion[2],
                    '.',
                    prodVersion[3]
                });
                json.nextName();
                char[] testVersion = json.nextString().replace("version-", "").replace("-test", "").toCharArray();
                String testVersionCompiled = new String(new char[]{
                    testVersion[0],
                    '.',
                    testVersion[2],
                    '.',
                    testVersion[3]
                });
                switch (event.getCommand()) {
                    case "versions":
                        event.getChannel().send().message("Latest versions - Prod: " + prodVersionCompiled + ", Test: " + testVersionCompiled);
                        break;
                    case "updatetopic":
                        String topic = getConfig().getString("topic").replace("{prod}", prodVersionCompiled).replace("{test}", testVersionCompiled);
                        if (!event.getChannel().getTopic().equals(topic) && event.getUser().isVerified()) {
                            event.getChannel().send().setTopic(topic);
                        }
                        break;
                }
            }
        } catch (IOException e) {
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"versions", "updatetopic"};
    }

}
