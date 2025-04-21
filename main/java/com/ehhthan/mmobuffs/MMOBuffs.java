package com.ehhthan.mmobuffs;

import com.ehhthan.mmobuffs.manager.type.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class MMOBuffs extends JavaPlugin {
    private static MMOBuffs instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private EffectManager effectManager;
    private ParserManager parserManager;
    private StatManager statManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager();
        this.effectManager = new EffectManager();
        this.parserManager = new ParserManager();
        this.statManager = new StatManager(this);
    }

    public static MMOBuffs getInst() {
        return instance;
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