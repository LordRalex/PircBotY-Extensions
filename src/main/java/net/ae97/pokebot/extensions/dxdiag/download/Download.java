package net.ae97.pokebot.extensions.dxdiag.download;

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
    private int epmID;
    public GPU gpu;

    public Download(String name) {
        gpu = new GPU(name);
    }

    public void addDownload(EPMIdResults.ResultsForDisplayImpl display) {
        epmID = display.Id;
        for (String str : display.OperatingSystemSet) {
            String version = "TooOld";
            int arch = str.contains("64") ? 64 : 32;
            if (str.contains("7")) {
                gpu.addDownload("7", arch, "https://downloadcenter.intel.com/download/" + epmID);
            } else if (str.contains("8") && !str.contains("8.1")) {
                gpu.addDownload("8", arch, "https://downloadcenter.intel.com/download/" + epmID);
            } else if (str.contains("8.1")) {
                gpu.addDownload("8.1", arch, "https://downloadcenter.intel.com/download/" + epmID);
            } else if (str.contains("10")) {
                gpu.addDownload("10", arch, "https://downloadcenter.intel.com/download/" + epmID);
            } else if (str.contains("Vista")) {
                gpu.addDownload("Vista", arch, "https://downloadcenter.intel.com/download/" + epmID);
            } else if (str.contains("XP")) {
                gpu.addDownload("XP", arch, "https://downloadcenter.intel.com/download/" + epmID);
            }
            if (version.equals("TooOld")) continue;
        }
    }
}
