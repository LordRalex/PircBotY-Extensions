package net.ae97.pokebot.extensions.dxdiag;

import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.extension.Extension;

/**
 * Created by urielsalis on 1/26/2017
 */
public class DxdiagParser extends Extension {
    @Override
    public String getName() {
        return "DxdiagParser";
    }

    @Override
    public void load() {
        DxdiagListener listener = new DxdiagListener(this);
        PokeBot.getEventHandler().registerListener(listener);
        PokeBot.getEventHandler().registerCommandExecutor(listener);

    }
}
