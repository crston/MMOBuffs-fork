package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

@SuppressWarnings("ALL")
public final class ConfigManager {
    private final MMOBuffs plugin;
    private final String defaultResourcePath = "default/"; // Make configurable if needed

    public ConfigManager(MMOBuffs plugin) {
        this.plugin = plugin;

        mkdir("language");

        for (DefaultFile file : DefaultFile.values())
            file.checkFile();
    }

    private void mkdir(String path) {
        File folder = new File(plugin.getDataFolder() + "/" + path);
        if (!folder.exists())
            if (!folder.mkdir())
                plugin.getLogger().log(Level.WARNING, "Could not create directory: " + folder.getAbsolutePath());
    }

    /*
     * all config files that have a default configuration are stored here, they
     * get copied into the plugin folder when the plugin enables
     */
    public enum DefaultFile {
        // CONFIGS
        EFFECTS("effects.yml", "", "effects.yml"),
        LANGUAGE("language/language.yml", "language", "language.yml");

        private final String folderPath, fileName, resourceName;

        private final MMOBuffs plugin;

        DefaultFile(String resourceName, String folderPath, String fileName) {
            this.resourceName = resourceName;
            this.folderPath = folderPath;
            this.fileName = fileName;
            this.plugin = MMOBuffs.getInst();
        }

        public void checkFile() {
            File file = getFile();
            if (!file.exists()) {
                try (InputStream resource = plugin.getResource(plugin.getConfig().getString("defaultResourcePath", "default/") + resourceName)) { // Use configurable path
                    if (resource != null) {
                        Path filePath = file.getAbsoluteFile().toPath();
                        Files.copy(resource, filePath);
                        plugin.getLogger().log(Level.INFO, "Created default file: " + file.getAbsolutePath());
                    } else {
                        plugin.getLogger().log(Level.WARNING, "Could not find default resource: " + resourceName);
                    }
                } catch (IOException exception) {
                    plugin.getLogger().log(Level.SEVERE, "Could not copy default file: " + resourceName + " to " + file.getAbsolutePath(), exception); // More informative message
                }
            }
        }

        public File getFile() {
            return new File(plugin.getDataFolder() + (folderPath.equals("") ? "" : "/" + folderPath), fileName);
        }
    }
}