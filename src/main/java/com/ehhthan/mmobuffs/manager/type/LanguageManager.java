package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.manager.ConfigFile;
import com.ehhthan.mmobuffs.manager.Reloadable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class LanguageManager implements Reloadable {
    private ConfigFile language;
    private boolean hasWarned = false;
    private final Map<String, Component> messageCache = new HashMap<>();

    public LanguageManager() {
        reload();
    }

    @Override
    public void reload() {
        this.language = new ConfigFile("/language", "language");
        this.messageCache.clear();
        this.hasWarned = false;
    }

    public @NotNull String getString(@NotNull String path) {
        String result = language.getConfig().getString(path);
        if (result == null) {
            return "<MNF:" + path + ">";
        }
        if (result.isEmpty() || result.equals("[]")) {
            return "";
        }
        return result;
    }

    public @Nullable Component getMessage(@NotNull String path) {
        return getMessage(path, true, null);
    }

    public @Nullable Component getMessage(@NotNull String path, boolean prefix) {
        return getMessage(path, prefix, null);
    }

    public @Nullable Component getMessage(@NotNull String path, boolean prefix, @Nullable TagResolver resolver) {
        String prefixValue = prefix ? language.getConfig().getString("prefix", "") : "";
        String message = language.getConfig().getString(path);
        String input;

        if (message == null) {
            if (!hasWarned) {
                MMOBuffs.getInst().getLogger().log(Level.WARNING, "Missing message " + path);
                MMOBuffs.getInst().getLogger().log(Level.WARNING, "Please add it to your language.yml or reset the file.");
                hasWarned = true;
            }
            input = "<Message-Missing:" + path + ">";
        } else if (message.isEmpty() || message.equals("[]")) {
            return null;
        } else {
            input = prefixValue + message;
        }

        if (resolver == null) {
            Component cached = messageCache.get(input);
            if (cached != null) {
                return cached;
            }
            Component parsed = MiniMessage.miniMessage().deserialize(input);
            messageCache.put(input, parsed);
            return parsed;
        }

        return MiniMessage.miniMessage().deserialize(input, resolver);
    }
}