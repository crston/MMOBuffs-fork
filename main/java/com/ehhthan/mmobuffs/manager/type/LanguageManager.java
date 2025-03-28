package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.manager.ConfigFile;
import com.ehhthan.mmobuffs.manager.Reloadable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LanguageManager implements Reloadable {
    private ConfigFile language;
    private boolean hasWarned = false;
    private final File warningFile;

    public LanguageManager() {
        this.warningFile = new File(MMOBuffs.getInst().getDataFolder(), "language_warning.txt");
        loadWarningState();
        reload();
    }

    public void reload() {
        this.language = new ConfigFile("language");
    }

    public String getString(@NotNull String path) {
        String found = language.getConfig().getString(path);

        if (found != null && (found.isEmpty() || found.equals("[]"))) {
            return "";
        }

        return (found == null) ? "<MNF:" + path + ">" : found;
    }

    public @NotNull Component getMessage(@NotNull String path) {
        return getMessage(path, true, null);
    }

    public @NotNull Component getMessage(@NotNull String path, boolean hasPrefix) {
        return getMessage(path, hasPrefix, null);
    }

    public @NotNull Component getMessage(@NotNull String path, boolean hasPrefix, @Nullable TagResolver resolver) {
        String prefix = (hasPrefix) ? language.getConfig().getString("prefix", "") : "";
        String found = language.getConfig().getString(path);

        if (found == null) {
            logMissingMessage(path);
            found = "<Message-Missing:" + path + ">";
        }

        String input = prefix + found;

        return (resolver != null)
                ? MiniMessage.miniMessage().deserialize(input, resolver)
                : MiniMessage.miniMessage().deserialize(input);
    }

    private void logMissingMessage(String path) {
        if (!hasWarned) {
            Logger logger = MMOBuffs.getInst().getLogger();
            logger.log(Level.WARNING, "Message Missing: " + path);
            logger.log(Level.WARNING, "You should either add this field yourself to your language.yml or refresh it.");
            hasWarned = true;
            saveWarningState();
        }
    }

    private void loadWarningState() {
        if (warningFile.exists()) {
            try {
                hasWarned = Boolean.parseBoolean(Files.readAllLines(warningFile.toPath()).get(0));
            } catch (IOException | IndexOutOfBoundsException e) {
                MMOBuffs.getInst().getLogger().log(Level.WARNING, "Could not load language warning state.", e);
            }
        }
    }

    private void saveWarningState() {
        try {
            Files.write(warningFile.toPath(), String.valueOf(hasWarned).getBytes());
        } catch (IOException e) {
            MMOBuffs.getInst().getLogger().log(Level.WARNING, "Could not save language warning state.", e);
        }
    }
}