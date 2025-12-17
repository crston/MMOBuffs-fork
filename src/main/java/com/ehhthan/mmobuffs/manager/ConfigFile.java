package com.ehhthan.mmobuffs.manager;

import com.ehhthan.mmobuffs.MMOBuffs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigFile {
    private final Plugin plugin;
    private final String path;
    private final String name;
    private final File file;
    private final FileConfiguration config;

    public ConfigFile(String name) {
        this(MMOBuffs.getInst(), "", name);
    }

    public ConfigFile(Plugin plugin, String path, String name) {
        this.plugin = plugin;
        this.path = path;
        this.name = name;
        this.file = new File(plugin.getDataFolder() + path, name + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public ConfigFile(String path, String name) {
        this(MMOBuffs.getInst(), path, name);
    }

    public final FileConfiguration getConfig() {
        return config;
    }

    public final void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + name + ".yml", exception);
        }
    }
}