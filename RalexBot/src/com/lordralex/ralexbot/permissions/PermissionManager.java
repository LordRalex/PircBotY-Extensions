/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.permissions;

import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.events.JoinEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Joshua
 */
public class PermissionManager extends Listener {

    Map<String, PermissionUser> users = new ConcurrentHashMap<>();

    public PermissionManager() {
    }

    @Override
    public void onJoin(JoinEvent event) {
        PermissionUser user;
    }

    @Override
    public void declarePriorities() {
    }
}
