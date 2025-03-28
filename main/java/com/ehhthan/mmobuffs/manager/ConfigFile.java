package com.ehhthan.mmobuffs.manager;

import com.ehhthan.mmobuffs.MMOBuffs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ConfigFile {
    private final Plugin plugin;
    private final String name;
    private final File file;
    private FileConfiguration config;

    public ConfigFile(String name) {
        this(MMOBuffs.getInst(), "", name);
    }

    public ConfigFile(Plugin plugin, String path, String name) {
        this.plugin = plugin;
        String path1 = (path != null) ? path : ""; // Ensure path is not null
        this.name = name;

        File dataFolder = plugin.getDataFolder();
        if (!path1.isEmpty()) {
            dataFolder = new File(dataFolder, path1);
        }

        // Ensure the data folder exists
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create directory: " + dataFolder.getAbsolutePath());
            }
        }

        this.file = new File(dataFolder, name + ".yml");

        // Create the file and load the configuration
        createAndLoad();
    }

    //This is a duplicate constructor and is not needed. It's not used anywhere.

    private void createAndLoad() {
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to create file: " + file.getAbsolutePath());
                }
                loadDefaultConfig();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create/load " + name + ".yml: " + e.getMessage(), e);
                config = new YamlConfiguration(); // Load empty config if file creation fails
            }
        } else {
            loadConfig();
        }
    }

    private void loadDefaultConfig() throws IOException {
        try (InputStream in = plugin.getResource(name + ".yml")) {
            if (in != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
                loadConfig();
                config.setDefaults(defConfig);
                config.options().copyDefaults(true);
                save();
            } else {
                plugin.getLogger().log(Level.SEVERE, "Could not find default config " + name + ".yml in resources. Using empty config.");
                loadConfig();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not copy default config " + name + ".yml: " + e.getMessage(), e);
            loadConfig();
        }
    }

    private void loadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + name + ".yml: " + exception.getMessage(), exception);
        }
    }

    public void reload() {
        loadConfig();
    }
}