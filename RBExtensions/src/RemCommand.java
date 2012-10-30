
import com.lordralex.ralexbot.api.EventField;
import com.lordralex.ralexbot.api.EventType;
import com.lordralex.ralexbot.api.Listener;
import com.lordralex.ralexbot.api.Priority;
import com.lordralex.ralexbot.api.Utils;
import com.lordralex.ralexbot.api.events.CommandEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemCommand extends Listener {

    private Map<String, String> remMap = new ConcurrentHashMap<String, String>();
    private List<String> dontReply = new ArrayList<String>();

    @Override
    public void setup() {
        remMap.clear();
        new File("data" + File.separator + "rem").mkdirs();
        for (File file : new File("data" + File.separator + "rem").listFiles()) {
            try {
                String name = file.getName().substring(0, file.getName().length() - 4).toLowerCase().trim();
                Scanner reader = new Scanner(file);
                String line = reader.nextLine().trim();
                remMap.put(name, line);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(RemCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    @EventType(event = EventField.Command, priority = Priority.LOW)
    public void runEvent(CommandEvent event) {
        String command = event.getCommand().toLowerCase();
        String channel = event.getChannel();
        String sender = event.getSender();
        String[] args = event.getArgs();

        if (command.equalsIgnoreCase("remshutup")) {
            if (!Utils.hasOP(sender, channel)) {
                return;
            }
            String target = channel;
            if (args.length != 0) {
                target = args[0];
            }
            target = target.toLowerCase();
            boolean wasThere = dontReply.remove(channel);
            if (wasThere) {
                if (channel != null) {
                    Utils.sendMessage(channel, "Returning to normal");
                } else {
                    Utils.sendMessage(sender, "Returning to normal");
                }
            } else {
                dontReply.add(target);
                if (channel != null) {
                    Utils.sendMessage(channel, "Shutting up");
                } else {
                    Utils.sendMessage(sender, "Shutting up");
                }
            }
            return;
        }

        if (dontReply.contains(channel)) {
            return;
        }

        if (command.equalsIgnoreCase("remupdate")) {
            setup();
            Utils.sendMessage(sender, "Rems updated");
            return;
        }

        if (isRem(command)) {
            String reply = remMap.get(command);
            if (sender == null) {
                return;
            }
            Map<String, String> placers = new HashMap<String, String>();
            placers.put("User", sender);
            placers.put("Channel", channel);
            for (int i = 0; i < args.length; i++) {
                placers.put(new Integer(i).toString(), args[i]);
            }
            Random gen = new Random();
            String[] names = Utils.getUsers(channel);
            if (names.length > 0) {
                String random = names[gen.nextInt(names.length)];
                placers.put("Random", random);
            }
            reply = Utils.handleArgs(reply, placers);
            String[] entire = reply.split("\n");
            if (channel == null) {
                Utils.sendMessage(sender, entire);
            } else {
                Utils.sendMessage(channel, entire);
            }
            return;
        }

        if (args.length == 0) {
            return;
        }

        String[] part = buildRem(args);
        String name = part[0].toLowerCase().trim();
        String reply = part[1];

        if (reply == null || reply.equalsIgnoreCase("null") || reply.equalsIgnoreCase("forget")) {
            remMap.remove(name);
            deleteRem(name);
            if (channel == null) {
                channel = sender;
            }
            if (sender != null) {
                Utils.sendMessage(channel, name + " is now forgotten");
            }
            return;
        }

        if (remMap.containsKey(name)) {
            if (channel == null) {
                channel = sender;
            }
            if (sender != null) {
                Utils.sendMessage(channel, name + " already exists");
            }
            return;
        }

        remMap.put(name, reply);
        saveRem(name, reply);
        if (channel == null) {
            channel = sender;
        }
        Utils.sendMessage(channel, name + " is now saved");
    }

    private String[] buildRem(String[] args) {
        String[] complete = new String[2];
        complete[0] = args[0];
        complete[1] = "";
        for (int i = 1; i < args.length; i++) {
            complete[1] += args[i] + " ";
        }
        complete[1] = complete[1].trim();
        if (complete[1].equalsIgnoreCase("")) {
            complete[1] = null;
        }
        return complete;
    }

    private boolean isRem(String command) {
        return remMap.containsKey(command.toLowerCase().trim());
    }

    @Override
    public String[] getAliases() {
        List<String> list = new ArrayList<String>();
        list.add("rem");
        list.add("r");
        list.add("remember");
        list.add("remshutup");
        list.add("remupdate");
        for (Object command : remMap.keySet()) {
            list.add(((String) command).toLowerCase());
        }
        return list.toArray(new String[0]);
    }

    public void saveRem(String name, String line) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File("data" + File.separator + "rem" + File.separator + name + ".txt"));
            writer.write(line);
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(RemCommand.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(RemCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void deleteRem(String name) {
        new File("data" + File.separator + "rem" + File.separator + name + ".txt").delete();
    }
}
