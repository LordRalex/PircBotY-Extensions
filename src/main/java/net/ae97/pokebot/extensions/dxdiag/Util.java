package net.ae97.pokebot.extensions.dxdiag;

/**
 * Created by urielsalis on 1/26/2017
 */
public class Util {
    public static String removeSpecialChars(String text) {
        return text.contains("(") ? text.substring(0, text.indexOf("(") - 1).replace("®", "").trim() : text.replace("®", "").trim();
    }
}
