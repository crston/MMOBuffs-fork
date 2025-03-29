package com.ehhthan.mmobuffs;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.command.MMOBuffsCommand;
import com.ehhthan.mmobuffs.listener.CombatListener;
import com.ehhthan.mmobuffs.listener.WorldListener;
import com.ehhthan.mmobuffs.manager.type.ConfigManager;
import com.ehhthan.mmobuffs.manager.type.EffectManager;
import com.ehhthan.mmobuffs.manager.type.ParserManager;
import com.ehhthan.mmobuffs.manager.type.StatManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MMOBuffs extends JavaPlugin {
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

        new ConfigManager(this);
        this.effectManager = new EffectManager(this);
        this.statManager = new StatManager(this);

        parserManager.registerPAPI(); // Register PAPI

        getServer().getPluginManager().registerEvents(new EffectHolder.PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new CombatListener(), this);

        registerCommands();
    }

    @Override
    public void onDisable() {
        parserManager.unregisterPAPI(); // Unregister PAPI
    }

    private void registerCommands() {
        MMOBuffsCommand command = new MMOBuffsCommand(this);

        PluginCommand pluginCommand = getCommand("mmobuffs");
        Objects.requireNonNull(pluginCommand).setExecutor(command);
        Objects.requireNonNull(pluginCommand).setTabCompleter(command);
    }

    public boolean reload() {
        reloadConfig();
        effectManager.reload();
        return false;
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