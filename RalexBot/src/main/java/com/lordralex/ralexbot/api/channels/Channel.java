package com.lordralex.ralexbot.api.channels;

import com.lordralex.ralexbot.api.Utilities;
import com.lordralex.ralexbot.api.sender.Sender;
import org.pircbotx.User;

/**
 *
 * @author Joshua
 */
public class Channel extends Utilities implements Sender {

    private org.pircbotx.Channel pircbotxChannel;

    private Channel(String name) {
        pircbotxChannel = bot.getChannel(name);
    }

    @Override
    public void sendMessage(String message) {
        bot.sendMessage(pircbotxChannel, message);
    }

    @Override
    public void sendNotice(String message) {
        bot.sendNotice(pircbotxChannel, message);
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

    public boolean isSecret() {
        return pircbotxChannel.isSecret();
    }

    public String[] getOPs() {
        User[] users = pircbotxChannel.getOps().toArray(new User[0]);
        String[] names = new String[users.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = users[i].getNick();
        }
        return names;
    }

    public String[] getVoiced() {
        User[] users = pircbotxChannel.getVoices().toArray(new User[0]);
        String[] names = new String[users.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = users[i].getNick();
        }
        return names;
    }

    public boolean hasOP(String name) {
        return pircbotxChannel.isOp(bot.getUser(name));
    }

    public boolean hasVoice(String name) {
        return pircbotxChannel.hasVoice(bot.getUser(name));
    }

    public static Channel getChannel(String channel) {
        return new Channel(channel);
    }
}
