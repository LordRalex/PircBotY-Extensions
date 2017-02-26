/*
 * Copyright (C) 2017 Joshua Taylor (lordralex@gmail.com)
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
package net.ae97.pokebot.extensions.removal;

import java.util.List;
import net.ae97.pircboty.User;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import org.apache.commons.lang3.StringUtils;

public class RemoveExtension extends Extension implements CommandExecutor {
    
    private final String extensionName = "removal";
    private final String[] aliases = new String[] {"remove", "terminate"};

    @Override
    public void load() {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public String getName() {
        return extensionName;
    }

    @Override
    public void runEvent(CommandEvent ce) {
        if (ce.getArgs().length == 0) {
            return;
        }
        if (ce.getChannel() == null) {
            return;
        }
        List<String> enabledChannels = getConfig().getStringList("channels");
        if(!enabledChannels.contains(ce.getChannel().getName())) {
            return;
        }        
        if(!ce.getUser().getChannelsVoiceIn().contains(ce.getChannel())) {
            return;
        }
        String targetUser = ce.getArgs()[0];
        String kickReason = getConfig().getString("kickmessage", "You have been removed from this channel");
        if (ce.getArgs().length > 1) {
            kickReason = StringUtils.join(ce.getArgs(), " ", 1, ce.getArgs().length);
        }
        User target = PokeBot.getUser(targetUser);
        if(!target.getChannels().contains(ce.getChannel())) {
            return;
        }
        if(ce.getCommand().equalsIgnoreCase("terminate")) {            
            String hostname = "*!*@" + target.getHostmask();
            ce.getChannel().send().ban(hostname);
        }
        ce.getChannel().send().kick(target, kickReason);
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }

}
