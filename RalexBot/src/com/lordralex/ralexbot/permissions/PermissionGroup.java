/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.permissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Joshua
 */
public class PermissionGroup {

    public static List<String> getPerms(String groupName) {
        List<String> perms = new ArrayList<>();
        try {
            Scanner reader = new Scanner(new File("perms" + File.separator + "groups.yml"));
            boolean isGroup = false;

            while (reader.hasNext()) {
                String line = reader.nextLine().toLowerCase().trim();
                if (!line.startsWith("-")) {
                    isGroup = false;
                    if (line.equalsIgnoreCase(groupName)) {
                        isGroup = true;
                    }
                } else if (isGroup) {
                    perms.add(line.substring(1).trim());
                }
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PermissionGroup.class.getName()).log(Level.SEVERE, "No groups.yml", ex);
        }

        return perms;
    }
}
