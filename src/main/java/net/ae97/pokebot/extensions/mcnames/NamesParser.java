package net.ae97.pokebot.extensions.mcnames;

import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extensions.hjt.HJTListener;

/**
 * Created by urielsalis on 1/26/2017
 */
public class NamesParser extends Extension{
    @Override
    public String getName() {
        return "DxdiagParser";
    }

    @Override
    public void load() {
        NamesListener listener = new NamesListener(this);
        PokeBot.getEventHandler().registerListener(listener);
        PokeBot.getEventHandler().registerCommandExecutor(listener);

    }
}
