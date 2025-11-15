package com.ehhthan.mmobuffs;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
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
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MMOBuffs extends JavaPlugin {

    private static MMOBuffs INSTANCE;

    private ParserManager parserManager;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private EffectManager effectManager;
    private StatManager statManager;

    public static MMOBuffs getInst() {
        return Optional.ofNullable(INSTANCE)
                .orElseThrow(() -> new IllegalStateException("MMOBuffs not initialized"));
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();

        int configVersion = getConfig().getInt("config-version", -1);
        int defVer = Objects.requireNonNull(getConfig().getDefaults()).getInt("config-version", -1);

        if (configVersion != defVer) {
            getLogger().warning("Config version mismatch. Your version: " + configVersion + " expected: " + defVer);
        }

        parserManager = new ParserManager();
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager();
        effectManager = new EffectManager();
        statManager = new StatManager(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            parserManager.register(new PlaceholderAPIParser());
            new MMOBuffsExpansion().register();
        }

        registerListeners();
        registerCommands();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new EffectHolder.PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(), this);
    }

    private void registerCommands() {
        PaperCommandManager mgr = new PaperCommandManager(this);

        mgr.getCommandCompletions().registerAsyncCompletion(
                "effects",
                c -> effectManager.keys().stream().map(NamespacedKey::getKey).collect(Collectors.toList())
        );

        mgr.getCommandContexts().registerContext(StatusEffect.class, c -> {
            String arg = c.getFirstArg();
            if (arg == null) throw new InvalidCommandArgument("Missing effect");
            NamespacedKey key = NamespacedKey.fromString(arg, this);
            if (key == null || !effectManager.has(key)) throw new InvalidCommandArgument("Invalid effect");
            c.popFirstArg();
            return effectManager.get(key);
        });

        mgr.getCommandContexts().registerContext(EffectHolder.class, c -> {
            String arg = c.getFirstArg();
            if (arg == null) throw new InvalidCommandArgument("Missing player");
            Player player = Bukkit.getPlayer(arg);
            if (player == null || !EffectHolder.has(player)) {
                throw new InvalidCommandArgument("Invalid effect holder");
            }
            c.popFirstArg();
            return EffectHolder.get(player);
        });

        mgr.registerCommand(new MMOBuffsCommand(this, languageManager, parserManager));
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
