package com.ehhthan.mmobuffs.manager;

import com.ehhthan.mmobuffs.MMOBuffs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class ConfigFile {

    private final Plugin plugin;
    private final String path;
    private final String name;
    private final FileConfiguration config;

    public ConfigFile(String name) {
        this(MMOBuffs.getInst(), "", name);
    }

    public ConfigFile(String path, String name) {
        this(MMOBuffs.getInst(), path, name);
    }

    public ConfigFile(Plugin plugin, String path, String name) {
        this.plugin = plugin;
        this.path = path;
        this.name = name;

        File dir;
        if (path == null || path.isEmpty()) {
            dir = plugin.getDataFolder();
        } else {
            String clean = path.startsWith("/") ? path.substring(1) : path;
            dir = new File(plugin.getDataFolder(), clean);
        }

        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().log(Level.WARNING, "Could not create config directory: " + dir.getPath());
        }

        File file = new File(dir, name + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        File dir;
        if (path == null || path.isEmpty()) {
            dir = plugin.getDataFolder();
        } else {
            String clean = path.startsWith("/") ? path.substring(1) : path;
            dir = new File(plugin.getDataFolder(), clean);
        }

        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().log(Level.WARNING, "Could not create config directory: " + dir.getPath());
            return;
        }

        File file = new File(dir, name + ".yml");
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + name + ".yml", exception);
        }
    }
}
