package pluginupdate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginUpdate extends JavaPlugin {

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onLoad() {
        File updateFolder = new File(Bukkit.getUpdateFolder());
        if (!updateFolder.exists()) {
            updateFolder.mkdirs();
        }
        File pluginFolder = new File("plugins");
        for (File file : pluginFolder.listFiles()) {
            if (file == null || file.isDirectory() || !file.getName().endsWith(".jar")) {
                continue;
            }
            String pluginName = file.getName();
            download(pluginName);
        }
    }

    private void download(String pluginName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
        String url = config.getString(pluginName.toLowerCase(), getDLPath(pluginName));
        if (url == null) {
            return;
        }
        if (url.endsWith(".zip")) {
            download(url, "updates" + File.separator + pluginName + ".zip");
        } else if (url.endsWith(".jar")) {
            download(url, "updates" + File.separator + pluginName + ".jar");
        } else {
            System.out.println("Invalid url path");
        }
    }

    private String getDLPath(String name) {
        return ("http://dev.bukkit.org/server-mods/" + name).toLowerCase();
    }

    public void download(String path, String fileName) {
        System.out.println("Downloading " + path + " to " + fileName);
        try {
            URL test = new URL(path);
            HttpURLConnection httpcon = (HttpURLConnection) test.openConnection();
            httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
            ReadableByteChannel rbc = Channels.newChannel(httpcon.getInputStream());
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        } catch (IOException ex) {
            Logger.getLogger(PluginUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void extract(String fileName, String pathToFolder) {
        System.out.println("Extracting " + fileName + " to " + pathToFolder);
        try {
            Enumeration entries;
            ZipFile zipFile;
            zipFile = new ZipFile(pathToFolder + File.separator + fileName + ".zip");
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    (new File(pathToFolder, entry.getName())).mkdir();
                    continue;
                }
                copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(pathToFolder + File.separator + entry.getName())));
                File extractedFile = new File(pathToFolder + File.separator + entry.getName());
                if ((!extractedFile.toString().endsWith(".jar") && extractedFile.isFile()) || extractedFile.isDirectory()) {
                    extractedFile.delete();
                }
            }
            zipFile.close();
            new File(pathToFolder + File.separator + fileName + ".zip").deleteOnExit();
        } catch (IOException ex) {
            Logger.getLogger(PluginUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void copyInputStream(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(PluginUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
