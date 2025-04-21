package com.ehhthan.mmobuffs;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.command.MMOBuffsCommand;
import com.ehhthan.mmobuffs.comp.parser.type.PlaceholderAPIParser;
import com.ehhthan.mmobuffs.comp.placeholderapi.MMOBuffsExpansion;
import com.ehhthan.mmobuffs.listener.CombatListener;
import com.ehhthan.mmobuffs.listener.WorldListener;
import com.ehhthan.mmobuffs.manager.type.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MMOBuffs extends JavaPlugin {
    private static MMOBuffs instance;

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private EffectManager effectManager;
    private ParserManager parserManager;
    private StatManager statManager;

    public static MMOBuffs getInst() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Load managers
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager();
        effectManager = new EffectManager();
        parserManager = new ParserManager();
        statManager = new StatManager(this);

        // Register PlaceholderAPI if available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            parserManager.register(new PlaceholderAPIParser());
            new MMOBuffsExpansion().register();
            getLogger().info("PlaceholderAPI support registered.");
        } else {
            getLogger().warning("PlaceholderAPI not found. Expansion not registered.");
        }

        // Register events
        Bukkit.getPluginManager().registerEvents(new EffectHolder.PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(), this);

        MMOBuffsCommand command = new MMOBuffsCommand(this);
        Objects.requireNonNull(getCommand("mmobuffs")).setExecutor(command);
        Objects.requireNonNull(getCommand("mmobuffs")).setTabCompleter(command);

    }

    public void reload() {
        reloadConfig();
        languageManager.reload();
        effectManager.reload();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public ParserManager getParserManager() {
        return parserManager;
    }

    public StatManager getStatManager() {
        return statManager;
    }
}