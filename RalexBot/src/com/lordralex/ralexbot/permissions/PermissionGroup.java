package com.lordralex.ralexbot.permissions;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @author Joshua
 */
public class PermissionGroup {

    String groupName;
    Map<String, Boolean> perms = new HashMap<>();

    public PermissionGroup(String name)
    {
        groupName = name;
        
    }

}
