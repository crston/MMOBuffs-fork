package com.ehhthan.mmobuffs.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigFile {

    private final FileConfiguration config;

    public ConfigFile(Plugin plugin, String path, String name) {

        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + path, name + ".yml"));
    }

    public ConfigFile(Plugin plugin, String name) {
        this(plugin, "", name);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}