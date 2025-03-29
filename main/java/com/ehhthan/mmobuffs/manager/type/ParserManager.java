package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.comp.parser.Parser;
import com.ehhthan.mmobuffs.comp.parser.type.PlaceholderAPIParser;
import com.ehhthan.mmobuffs.comp.placeholderapi.MMOBuffsExpansion;
import com.ehhthan.mmobuffs.manager.KeylessManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ParserManager extends KeylessManager<Parser> {

    private PlaceholderAPIParser papiParser;

    public ParserManager() {
    }

    public void registerPAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiParser = new PlaceholderAPIParser();
            register(papiParser);
            new MMOBuffsExpansion(MMOBuffs.getInst()).register();
            MMOBuffs.getInst().getLogger().log(Level.INFO, "PlaceholderAPI support detected and registered.");
        } else {
            MMOBuffs.getInst().getLogger().log(Level.WARNING, "PlaceholderAPI not found. Placeholders will not be parsed.");
        }
    }

    public void unregisterPAPI() {
        if (papiParser != null) {
            values().remove(papiParser);
            papiParser = null;
            MMOBuffs.getInst().getLogger().log(Level.INFO, "PlaceholderAPI support unregistered.");
        }
    }

    public String parse(@NotNull Player player, @NotNull String text) {
        for (Parser parser : values())
            text = parser.parse(player, text);
        return text;
    }
}