package net.ae97.pokebot.extensions.dxdiag.download;

import nu.xom.Element;
import nu.xom.Elements;

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
public class AMD {

    public static class Platform {



        public Platform(String platformName, String platformID) {

        }

        public static class ProductFamily {




            public ProductFamily(String productFamilyName, String productFamilyID) {

            }

            public static class Product {


                public List<Version> versions = new ArrayList<>();

                public Product(String productName, String productID) {

                }

                public static class Version {
                    public boolean shouldDownload = false;


                    public List<Download> downloads = new ArrayList<>();

                    public Version(String type, String number, Elements downloads) {
                        if (number != null) {
                            for (int i = 0; i < downloads.size(); i++) {
                                Element download = downloads.get(i);
                                if (download.getAttribute("name") == null) continue;
                                if (download.getAttributeValue("name").contains("Windows")) {
                                    String code = download.getAttributeValue("osrev");
                                    if (code == null) continue;
                                    String file = "support.amd.com/en-us" + download.getValue();
                                    boolean is64 = download.getAttributeValue("bit").equals("64");
                                    String minified = null;
                                    if (code.startsWith("10.0")) {
                                        minified = "10";
                                    } else if (code.startsWith("6.3")) {
                                        minified = "8.1";
                                    } else if (code.startsWith("6.2")) {
                                        minified = "8";
                                    } else if (code.startsWith("6.1")) {
                                        minified = "7";
                                    } else if (code.startsWith("6.0")) {
                                        minified = "Vista";
                                    } else if (code.startsWith("5.1")) {
                                        minified = "XP";
                                    }
                                    if (minified == null) continue;
                                    this.downloads.add(new Download(minified, file, is64));
                                    shouldDownload = true;
                                }
                            }
                        }
                    }

                    public static class Download {
                        public String minified;
                        public String file;
                        public boolean is64;

                        private Download(String minified, String file, boolean is64) {
                            this.minified = minified;
                            this.file = file;
                            this.is64 = is64;
                        }
                    }
                }
            }
        }
    }
}
