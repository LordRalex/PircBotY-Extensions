package com.lordralex.ralexbot.api.users;

import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.channels.Channel;
import com.lordralex.ralexbot.api.sender.Sender;

/**
 *
 * @author Joshua
 */
public class User extends Utilities implements Sender {

    protected final org.pircbotx.User pircbotxUser;

    protected User(String nick) {
        pircbotxUser = bot.getUser(nick);
    }

    public static User getUser(String nick) {
        return new User(nick);
    }

    @Override
    public void sendMessage(String message) {
        bot.sendMessage(pircbotxUser, message);
    }

    @Override
    public void sendNotice(String message) {
        bot.sendNotice(pircbotxUser, message);
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendNotice(String[] messages) {
        for (String message : messages) {
            sendNotice(message);
        }
    }

    public boolean hasVoice(String channel) {
        return bot.getChannel(channel).hasVoice(pircbotxUser);
    }

    public boolean hasOP(String channel) {
        return bot.getChannel(channel).isOp(pircbotxUser);
    }

    public boolean isVerified() {
        return pircbotxUser.isIdentified();
    }

    public String[] getChannels() {
        Channel[] chanArray = pircbotxUser.getChannels().toArray(new Channel[0]);
        String[] channelList = new String[chanArray.length];
        for (int i = 0; i < channelList.length; i++) {
            channelList[i] = chanArray[i].getName();
        }
        return channelList;
    }

    public String getIP() {
        return pircbotxUser.getHostmask();
    }

    public String getNick() {
        return pircbotxUser.getNick();
    }

    public void quiet(String channel) {
        bot.sendMessage("chanserv", "quiet " + channel + " *!*" + pircbotxUser.getLogin() + "@" + pircbotxUser.getHostmask());
    }

    public void quiet(Channel channel) {
        quiet(channel.getName());
    }

    public void unquiet(String channel) {
        bot.sendMessage("chanserv", "unquiet " + channel + " *!*" + pircbotxUser.getLogin() + "@" + pircbotxUser.getHostmask());
    }

    public String getQuietLine() {
        return "*!*" + pircbotxUser.getLogin() + "@" + pircbotxUser.getHostmask();
    }
}
