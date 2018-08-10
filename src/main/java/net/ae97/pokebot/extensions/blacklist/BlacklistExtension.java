package net.ae97.pokebot.extensions.blacklist;

import net.ae97.pircboty.api.events.MessageEvent;
import net.ae97.pircboty.api.events.NoticeEvent;
import net.ae97.pircboty.generics.GenericChannelUserEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.EventExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.extension.Extension;

import java.util.List;

public class BlacklistExtension extends Extension implements Listener {
    @Override
    public String getName() {
        return "Blacklist";
    }

    @Override
    public void load() {
        PokeBot.getExtensionManager().addListener(this);
    }

    @EventExecutor
    public void runEvent(MessageEvent event) {
        scanMessage(event, event.getMessage());
    }

    @EventExecutor
    public void runEvent(NoticeEvent event) {
        scanMessage(event, event.getMessage());
    }

    public void scanMessage(GenericChannelUserEvent event, String message) {
        if (event.getChannel() == null) {
            return;
        }
        if (!getConfig().getStringList("channels").contains(event.getChannel().getName())) {
            return;
        }
        List<String> blacklist = getConfig().getStringList("blacklist");
        for(String l: blacklist) {
            if (message.contains(l)) {
                String hostname = "*!*@" + event.getUser().getHostmask();
                event.getChannel().send().ban(hostname);
                event.getChannel().send().kick(event.getUser(), "Blacklisted message");
            }
        }
    }
}
