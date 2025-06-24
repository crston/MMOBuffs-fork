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
import java.util.logging.Level;

public final class MMOBuffs extends JavaPlugin {

    private static MMOBuffs INSTANCE;

    private final ParserManager parserManager = new ParserManager();
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private EffectManager effectManager;
    private StatManager statManager;

    public static MMOBuffs getInst() {
        return Optional.ofNullable(INSTANCE)
                .orElseThrow(() -> new IllegalStateException("MMOBuffs is not initialized yet."));
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();

        int configVersion = getConfig().getInt("config-version", -1);
        int defConfigVersion = Objects.requireNonNull(getConfig().getDefaults()).getInt("config-version", -1);
        if (configVersion != defConfigVersion) {
            getLogger().warning(String.format("You may be using an outdated config.yml! (Your: '%d' | Expected: '%d')", configVersion, defConfigVersion));
        }

        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager();
        this.effectManager = new EffectManager();
        this.statManager = new StatManager(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            parserManager.register(new PlaceholderAPIParser());
            new MMOBuffsExpansion().register();
            getLogger().log(Level.INFO, "PlaceholderAPI support detected.");
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
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.getCommandCompletions().registerAsyncCompletion("effects",
                c -> effectManager.keys().stream().map(NamespacedKey::getKey).toList());

        commandManager.getCommandContexts().registerContext(StatusEffect.class, c -> {
            String arg = c.getFirstArg();
            if (arg == null) throw new InvalidCommandArgument("No status effect specified.");

            NamespacedKey key = NamespacedKey.fromString(arg, this);
            if (key == null || !effectManager.has(key)) {
                throw new InvalidCommandArgument("Invalid status effect specified.");
            }

            c.popFirstArg();
            return effectManager.get(key);
        });

        commandManager.getCommandContexts().registerContext(EffectHolder.class, c -> {
            String arg = c.getFirstArg();
            if (arg == null) throw new InvalidCommandArgument("No player specified.");

            Player player = Bukkit.getPlayer(arg);
            if (player == null || !EffectHolder.has(player)) {
                throw new InvalidCommandArgument("Invalid effect holder specified.");
            }

            c.popFirstArg();
            return EffectHolder.get(player);
        });

        commandManager.registerCommand(new MMOBuffsCommand(this, languageManager, parserManager));
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
