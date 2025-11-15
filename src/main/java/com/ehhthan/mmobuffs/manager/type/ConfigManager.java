package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public final class ConfigManager {
    private final MMOBuffs plugin;

    public ConfigManager(MMOBuffs plugin) {
        this.plugin = plugin;
        createDirectory("language");
        for (DefaultFile file : DefaultFile.values()) {
            file.checkFile();
        }
    }

    private void createDirectory(String path) {
        File folder = new File(plugin.getDataFolder(), path);
        if (!folder.exists() && !folder.mkdir()) {
            plugin.getLogger().log(Level.WARNING, "Could not create directory " + path);
        }
    }

    public enum DefaultFile {
        EFFECTS("effects.yml", "", "effects.yml"),
        LANGUAGE("language/language.yml", "language", "language.yml");

        private final String resourceName;
        private final String folderPath;
        private final String fileName;

        DefaultFile(String resourceName, String folderPath, String fileName) {
            this.resourceName = resourceName;
            this.folderPath = folderPath;
            this.fileName = fileName;
        }

        public void checkFile() {
            MMOBuffs plugin = MMOBuffs.getInst();
            File file = getFile(plugin);
            if (!file.exists()) {
                InputStream in = plugin.getResource("default/" + resourceName);
                if (in == null) {
                    plugin.getLogger().log(Level.SEVERE, "Default resource not found " + resourceName);
                    return;
                }
                try {
                    Files.copy(in, file.toPath());
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to copy default file " + resourceName, e);
                }
            }
        }

        public File getFile(MMOBuffs plugin) {
            if (folderPath.isEmpty()) {
                return new File(plugin.getDataFolder(), fileName);
            }
            return new File(plugin.getDataFolder(), folderPath + "/" + fileName);
        }
    }
}
