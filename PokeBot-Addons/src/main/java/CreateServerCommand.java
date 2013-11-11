/*
 * Copyright (C) 2013 Lord_Ralex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.hoenn.pokebot.PokeBot;
import org.hoenn.pokebot.api.CommandExecutor;
import org.hoenn.pokebot.api.channels.Channel;
import org.hoenn.pokebot.api.events.CommandEvent;

/**
 * @author Lord_Ralex
 */
public class CreateServerCommand implements CommandExecutor {

    protected volatile Process serverProcess;
    protected final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    protected final String consoleChannel = "#mchconsole";
    protected ScheduledFuture future = null;
    private StopServer cache = null;
    protected final static String JAR_URL = "http://s3.amazonaws.com/Minecraft.Download/versions/{version}/minecraft_server.{version}.jar";
    protected final File serverDir = new File("server");
    protected final String jarName = "minecraft_server-{version}.jar";
    protected final String launchCommand = "java -Xmx64M -Xms64M -jar {filename} nogui";
    protected volatile Thread stdout, stderr;

    @Override
    public String[] getAliases() {
        return new String[]{"createserver", "stopserver"};
    }

    @Override
    public void runEvent(CommandEvent event) {
        if (!event.getUser().hasOP(event.getChannel()) && !event.getUser().hasPermission(event.getChannel(), "server.start")) {
            return;
        }
        if (event.getCommand().equalsIgnoreCase("createserver")) {
            if (serverProcess != null) {
                event.getChannel().sendMessage("I already have a server running");
                return;
            }

            String[] args = event.getArgs();
            if (args.length == 0) {
                return;
            }
            String version = args[0];
            if (!downloadJar(version)) {
                event.getChannel().sendMessage("An error occurred on downloading the server jar for version " + version);
                return;
            }
            try {
                startServer(version);
                event.getChannel().sendMessage("Server " + version + " started");
                cache = new StopServer();
                future = service.schedule(cache, 1, TimeUnit.MINUTES);
            } catch (IOException e) {
                PokeBot.log(Level.SEVERE, "Error occured on starting server version " + version, e);
                event.getChannel().sendMessage("Server could not start due to error");
            }
        } else if (event.getCommand().equalsIgnoreCase("stopserver")) {
            if (serverProcess == null) {
                event.getChannel().sendMessage("There is no server running");
                return;
            }
            service.execute(cache);
            future.cancel(true);
            cache = null;
            future = null;
            event.getChannel().sendMessage("Server has been stopped");
        }
    }

    private boolean downloadJar(String version) {
        //check if the file exists
        if (new File(serverDir, jarName.replace("{version}", version)).exists()) {
            return true;
        }
        serverDir.mkdirs();
        try (FileOutputStream out = new FileOutputStream(new File(serverDir, jarName.replace("{version}", version)))) {
            try (InputStream in = new URL(JAR_URL.replace("{version}", version)).openStream()) {
                ReadableByteChannel rbc = Channels.newChannel(in);
                out.getChannel().transferFrom(rbc, 0, 1 << 24);
            }
        } catch (IOException e) {
            PokeBot.log(Level.SEVERE, "Error downloading server version " + version, e);
            return false;
        }
        return true;
    }

    private void startServer(String version) throws IOException {
        serverProcess = Runtime.getRuntime().exec(launchCommand.replace("{filename}", jarName.replace("{version}", version)), null, serverDir);
        InputStream in = serverProcess.getInputStream();
        InputStream err = serverProcess.getErrorStream();
        stdout = new Thread(new ForwardConsole(in));
        stderr = new Thread(new ForwardConsole(err));
        stdout.start();
        stderr.start();
    }

    private class StopServer implements Runnable {

        @Override
        public void run() {
            try {
                if (serverProcess == null) {
                    return;
                }
                Channel.getChannel(consoleChannel).sendMessage("Stopping server");

                synchronized (serverProcess) {
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()))) {
                        writer.write("stop");
                        serverProcess.waitFor(30, TimeUnit.SECONDS);
                        Channel.getChannel(consoleChannel).sendMessage("Server process over");
                    } catch (InterruptedException | IOException e) {
                        PokeBot.log(Level.SEVERE, "Error: ", e);
                    }
                    serverProcess.destroyForcibly();
                }
                synchronized (stdout) {
                    stdout.interrupt();
                }
                synchronized (stderr) {
                    stderr.interrupt();
                }
                serverProcess = null;
                stdout = null;
                stderr = null;
                Channel.getChannel(consoleChannel).sendMessage("Server stopped");
            } catch (Exception e) {
                PokeBot.log(Level.SEVERE, "Error: ", e);
            }
        }
    }

    private class ForwardConsole implements Runnable {

        private final InputStream in;
        private final Channel target;

        public ForwardConsole(InputStream i) {
            in = i;
            target = Channel.getChannel(consoleChannel);
        }

        @Override
        public void run() {
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                while ((line = reader.readLine()) != null) {
                    target.sendMessage("[Console] " + line);
                }
            } catch (IOException e) {

            } catch (Exception e) {

            }
        }
    }
}
