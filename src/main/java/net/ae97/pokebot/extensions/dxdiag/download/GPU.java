package net.ae97.pokebot.extensions.dxdiag.download;

/**
 * UrielSalads Copyright (C) 2016 Uriel Salischiker
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
public class GPU {
    public String name;
    public String downloadLinkWin6410 = "";
    public String downloadLinkWin6481 = "";
    public String downloadLinkWin648 = "";
    public String downloadLinkWin647 = "";
    public String downloadLinkWin64Vista = "";
    public String downloadLinkWin64XP = "";
    public String downloadLinkWin3210 = "";
    public String downloadLinkWin3281 = "";
    public String downloadLinkWin328 = "";
    public String downloadLinkWin327 = "";
    public String downloadLinkWin32Vista = "";
    public String downloadLinkWin32XP = "";

    public GPU(String name) {
        this.name = name;
    }

    public void addDownload(String version, int arch, String downloadLink) {
        if (arch == 64) {
            switch (version) {
            case "10":
                if (downloadLinkWin6410.isEmpty())
                    downloadLinkWin6410 = downloadLink;
                break;
            case "8.1":
                if (downloadLinkWin6481.isEmpty())
                    downloadLinkWin6481 = downloadLink;
                break;
            case "8":
                if (downloadLinkWin648.isEmpty())
                    downloadLinkWin648 = downloadLink;
                break;
            case "7":
                if (downloadLinkWin647.isEmpty())
                    downloadLinkWin647 = downloadLink;
                break;
            case "Vista":
                if (downloadLinkWin64Vista.isEmpty())
                    downloadLinkWin64Vista = downloadLink;
                break;
            case "XP":
                if (downloadLinkWin64XP.isEmpty())
                    downloadLinkWin64XP = downloadLink;
                break;
            }
        } else {
            switch (version) {
            case "10":
                if (downloadLinkWin3210.isEmpty())
                    downloadLinkWin3210 = downloadLink;
                break;
            case "8.1":
                if (downloadLinkWin3281.isEmpty())
                    downloadLinkWin3281 = downloadLink;
                break;
            case "8":
                if (downloadLinkWin328.isEmpty())
                    downloadLinkWin328 = downloadLink;
                break;
            case "7":
                if (downloadLinkWin327.isEmpty())
                    downloadLinkWin327 = downloadLink;
                break;
            case "Vista":
                if (downloadLinkWin32Vista.isEmpty())
                    downloadLinkWin32Vista = downloadLink;
                break;
            case "XP":
                if (downloadLinkWin32XP.isEmpty())
                    downloadLinkWin32XP = downloadLink;
                break;
            }
        }
    }

    public void addAMD(AMD.Platform.ProductFamily.Product.Version version) {
        for (AMD.Platform.ProductFamily.Product.Version.Download download : version.downloads) {
            int arch = download.is64 ? 64 : 32;
            addDownload(download.minified, arch, download.file);
        }
    }

}
