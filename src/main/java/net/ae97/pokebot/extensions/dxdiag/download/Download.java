package net.ae97.pokebot.extensions.dxdiag.download;


import java.util.ArrayList;
import java.util.List;

/**
 * UrielSalads
 * Copyright (C) 2016 Uriel Salischiker
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Download {
    public int epmID;
    public List<OS> os;

    public Download(EPMIdResults.ResultsForDisplayImpl display, Intel.Driver driver) {
        epmID = display.Id;
        os = new ArrayList<>();
        Config.GPU gpu = new Config.GPU(driver.name);
        for(String str: display.OperatingSystemSet) {
            String version = "TooOld";
            int arch = str.contains("64") ? 64 : 32;
            if(str.contains("7")) {
                version = "7";
            } else if(str.contains("8") && !str.contains("8.1")) {
                version = "8";
            } else if(str.contains("8.1")) {
                version = "8.1";
            } else if(str.contains("10")) {
                version = "10";
            } else if(str.contains("Vista")) {
                version = "Vista";
            } else if(str.contains("XP")) {
                version = "XP";
            }
            if(version.equals("TooOld")) continue;
            gpu.addDownload(version, arch, "https://downloadcenter.intel.com/download/"+epmID);
            os.add(new OS(version, arch));
        }
        DownloadMain.add(gpu, "Intel");
    }

    public Download(int epmID) {
        this.epmID = epmID;
        os = new ArrayList<>();
    }

}
