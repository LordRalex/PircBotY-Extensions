package com.lordralex.ralexbot.api.users;

import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.sender.Sender;
import org.pircbotx.Channel;

/**
 *
 * @author Joshua
 */
public class User extends Utilities implements Sender {

    protected org.pircbotx.User pircbotxUser;

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
}
