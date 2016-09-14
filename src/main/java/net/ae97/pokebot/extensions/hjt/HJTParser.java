package net.ae97.pokebot.extensions.hjt;

import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extension.Extension;

/**
 * Created by urielsalis on 13/09/16.
 */
public class HJTParser extends Extension {
    @Override
    public String getName() {
        return "HJTParser";
    }

    @Override
    public void load() {
        HJTListener listener = new HJTListener(this);
        PokeBot.getEventHandler().registerListener(listener);
        PokeBot.getEventHandler().registerCommandExecutor(listener);

    }


}
