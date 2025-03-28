package com.ehhthan.mmobuffs;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.command.MMOBuffsCommand;
import com.ehhthan.mmobuffs.comp.parser.type.PlaceholderAPIParser;
import com.ehhthan.mmobuffs.comp.placeholderapi.MMOBuffsExpansion;
import com.ehhthan.mmobuffs.listener.CombatListener;
import com.ehhthan.mmobuffs.listener.WorldListener;
import com.ehhthan.mmobuffs.manager.type.EffectManager;
import com.ehhthan.mmobuffs.manager.type.LanguageManager;
import com.ehhthan.mmobuffs.manager.type.ParserManager;
import com.ehhthan.mmobuffs.manager.type.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MMOBuffs extends JavaPlugin {
    private LanguageManager languageManager;
    private EffectManager effectManager;
    private StatManager statManager;

    private final ParserManager parserManager = new ParserManager();

    private static MMOBuffs INSTANCE;
    private final Logger logger = getLogger();

    public static MMOBuffs getInst() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();

        validateConfigVersion();

        try {
            this.languageManager = new LanguageManager();
            this.effectManager = new EffectManager();
            this.statManager = new StatManager(this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize managers:", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupPlaceholderAPI();

        registerEventListeners();

        registerCommands();
    }

    private void validateConfigVersion() {
        final int configVersion = getConfig().getInt("config-version", -1);
        final int defConfigVersion = Objects.requireNonNull(getConfig().getDefaults()).getInt("config-version", -1);
        if (configVersion != defConfigVersion) {
            logger.warning("You may be using an outdated config.yml!");
            logger.warning("(Your config version: '" + configVersion + "' | Expected config version: '"
                    + defConfigVersion + "')");
            logger.warning("Please delete your old config.yml and let the plugin regenerate it.");
        }
    }

    private void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            parserManager.register(new PlaceholderAPIParser());
            new MMOBuffsExpansion().register();
            logger.info("PlaceholderAPI support detected.");
        }
    }

    private void registerEventListeners() {
        Bukkit.getPluginManager().registerEvents(new EffectHolder.PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(), this);
    }

    private void registerCommands() {
        MMOBuffsCommand mmoBuffsCommand = new MMOBuffsCommand(this, languageManager, parserManager);
        PluginCommand command = getCommand("mmobuffs");
        if (command != null) {
            command.setExecutor(mmoBuffsCommand);
            command.setTabCompleter(mmoBuffsCommand); // If you implement TabCompleter
        } else {
            logger.log(Level.SEVERE, "Could not register command: mmobuffs");
        }
    }

    @Override
    public void onDisable() {
        // Ensure all tasks are cancelled on disable
        Bukkit.getScheduler().cancelTasks(this);
        EffectHolder.DATA.clear(); // Clear loaded data
    }


    public void reload() {
        try {
            reloadConfig();

            languageManager.reload();
            effectManager.reload();
            statManager.reload();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reload plugin:", e);
        }
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