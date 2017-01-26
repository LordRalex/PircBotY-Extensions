package net.ae97.pokebot.extensions.dxdiag.download;

import java.util.ArrayList;

/**
 * Created by urielsalis on 1/26/2017
 */
public class PartialUpdateData {
    public Intel.Driver driver;
    public ArrayList<Download> downloads;
    public PartialUpdateData(Intel.Driver driver, ArrayList<Download> downloads) {
        this.driver = driver;
        this.downloads = downloads;
    }
}
