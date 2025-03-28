package com.ehhthan.mmobuffs;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.command.MMOBuffsCommand;
import com.ehhthan.mmobuffs.comp.parser.type.PlaceholderAPIParser;
import com.ehhthan.mmobuffs.comp.placeholderapi.MMOBuffsExpansion;
import com.ehhthan.mmobuffs.listener.CombatListener;
import com.ehhthan.mmobuffs.listener.WorldListener;
import com.ehhthan.mmobuffs.manager.type.ConfigManager;
import com.ehhthan.mmobuffs.manager.type.EffectManager;
import com.ehhthan.mmobuffs.manager.type.LanguageManager;
import com.ehhthan.mmobuffs.manager.type.ParserManager;
import com.ehhthan.mmobuffs.manager.type.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class MMOBuffs extends JavaPlugin {
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private EffectManager effectManager;
    private final ParserManager parserManager = new ParserManager();
    private static MMOBuffs INSTANCE;
    private StatManager statManager;

    public static MMOBuffs getInst() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();

        final int configVersion = getConfig().contains("config-version", true) ? getConfig().getInt("config-version") : -1;
        final int defConfigVersion = Objects.requireNonNull(getConfig().getDefaults()).getInt("config-version", -1);
        if (configVersion != defConfigVersion) {
            getLogger().warning("You may be using an outdated config.yml!");
            getLogger().warning("(Your config version: '" + configVersion + "' | Expected config version: '"
                    + defConfigVersion + "')");
        }

        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.effectManager = new EffectManager(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            parserManager.register(new PlaceholderAPIParser());
            new MMOBuffsExpansion().register();
            getLogger().log(Level.INFO, "PlaceholderAPI support detected.");
        }

        this.statManager = new StatManager(this);

        getServer().getPluginManager().registerEvents(new EffectHolder.PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new CombatListener(), this);

        registerCommands();
    }

    private void registerCommands() {
        MMOBuffsCommand command = new MMOBuffsCommand(this, languageManager, parserManager);

        PluginCommand pluginCommand = getCommand("mmobuffs");
        Objects.requireNonNull(pluginCommand).setExecutor(command);
        Objects.requireNonNull(pluginCommand).setTabCompleter(command);
    }

    public void reload() {
        reloadConfig();

        languageManager.reload();
        effectManager.reload();
        // statManager.reload(); // Removed as StatManager no longer has a reload method
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