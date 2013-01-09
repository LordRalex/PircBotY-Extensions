/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lordralex.ralexbot.api.users;

import com.lordralex.ralexbot.api.exceptions.NickNotOnlineException;

/**
 *
 * @author Joshua
 */
public class BotUser extends User {

    public BotUser() throws NickNotOnlineException {
        super(bot.getNick());
    }

    public BotUser getBotUser() throws NickNotOnlineException {
        return new BotUser();
    }

    public void setNick(String newNick) {
        bot.changeNick(newNick);
    }

    public String getNick() {
        return bot.getNick();
    }

    public void kick(String nick, String channel) {
        if (bot.getUserBot().isIrcop()) {
            bot.kick(bot.getChannel(channel), bot.getUser(nick));
        } else {
            bot.sendMessage("chanserv", "kick " + channel + " " + nick);
        }
    }

    public void kick(String nick, String channel, String reason) {
        if (reason == null || reason.isEmpty()) {
            kick(nick, channel);
            return;
        }
        if (bot.getChannel(channel).isOp(bot.getUserBot())) {
            bot.kick(bot.getChannel(channel), bot.getUser(nick), reason);
        } else {
            this.sendMessage("chanserv", "kick " + channel + " " + nick + " " + reason);
        }
    }

    public void joinChannel(String channel) {
        bot.joinChannel(channel);
    }

    public void leaveChannel(String channel) {
        bot.partChannel(bot.getChannel(channel));
    }

    public void sendMessage(String target, String message) {
        bot.sendMessage(target, message);
    }

    public void sendNotice(String target, String message) {
        bot.sendNotice(target, message);
    }
}
