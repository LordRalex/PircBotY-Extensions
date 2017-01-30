package net.ae97.pokebot.extensions.mcnames;

import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extension.Extension;

/**
 * Created by urielsalis on 1/26/2017
 */
public class McNamesExtension extends Extension {
    @Override
    public String getName() {
        return "NamesParser";
    }

    @Override
    public void load() {
        McNamesListener listener = new McNamesListener();
        PokeBot.getEventHandler().registerListener(listener);
        PokeBot.getEventHandler().registerCommandExecutor(listener);

    }
}
