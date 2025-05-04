package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
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
            plugin.getLogger().log(Level.WARNING, "Could not create directory: " + path);
        }
    }

    public enum DefaultFile {
        EFFECTS("effects.yml", "", "effects.yml"),
        LANGUAGE("language/language.yml", "language", "language.yml");

        private final String resourceName;
        private final String folderPath;
        private final String fileName;
        private final MMOBuffs plugin = MMOBuffs.getInst();

        DefaultFile(String resourceName, String folderPath, String fileName) {
            this.resourceName = resourceName;
            this.folderPath = folderPath;
            this.fileName = fileName;
        }

        public void checkFile() {
            File file = getFile();
            if (!file.exists()) {
                try {
                    Files.copy(Objects.requireNonNull(plugin.getResource("default/" + resourceName)), file.toPath());
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to copy default file: " + resourceName, e);
                }
            }
        }

        public File getFile() {
            return new File(plugin.getDataFolder(), folderPath.isEmpty() ? fileName : folderPath + "/" + fileName);
        }
    }
}
