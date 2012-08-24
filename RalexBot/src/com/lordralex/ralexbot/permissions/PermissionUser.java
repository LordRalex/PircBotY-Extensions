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
public class PermissionUser {

    String userName;
    List<String> perms = new ArrayList<>();

    public PermissionUser(String name) {
        perms.clear();
        userName = name;
        try {
            Scanner reader = new Scanner(new File("perms" + File.separator + "users.yml"));
            boolean isUser = false;
            String section = "";
            while (reader.hasNext()) {
                String line = reader.nextLine().toLowerCase();
                if (!line.startsWith(" ")) {
                    isUser = true;
                    if (line.equalsIgnoreCase(userName)) {
                        isUser = true;
                    }
                } else if (isUser) {
                    line = line.trim();
                    if (line.equalsIgnoreCase("permissions:")) {
                        section = "perms";
                    } else if (line.equalsIgnoreCase("groups:")) {
                        section = "groups";
                    } else if (line.startsWith("- ")) {
                        line = line.substring(1).trim();
                        if (section.equalsIgnoreCase("perms")) {
                            perms.add(line);
                        } else if (section.equalsIgnoreCase("groups")) {
                            perms.addAll(PermissionGroup.getPerms(line));
                        }
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PermissionUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean hasPerm(String node) {
        return perms.contains(node.toLowerCase().trim());
    }
}
