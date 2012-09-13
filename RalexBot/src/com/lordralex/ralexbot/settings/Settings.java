/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.settings;

import java.util.List;

public class Settings {

    public static void loadSettings() {
    }

    public static String getString(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        return key;
    }

    public static int getInt(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        return 0;
    }

    public static List<String> getStringList(String key) {
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("KEY CANNOT BE NULL");
        }
        return null;
    }
}