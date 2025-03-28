package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;
import com.ehhthan.mmobuffs.comp.stat.type.AureliumSkillsStatHandler;
import com.ehhthan.mmobuffs.comp.stat.type.MythicLibStatHandler;
import com.ehhthan.mmobuffs.comp.stat.type.MythicMobsStatHandler;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class StatManager {
    private final MMOBuffs plugin;
    private final Map<String, StatHandler<?>> handlers = new LinkedHashMap<>();
    private String defaultStatHandlerKey;
    private StatHandler<?> defaultStatHandler;

    public StatManager(MMOBuffs plugin) {
        this.plugin = plugin;
        loadDefaultStatHandler();
        registerStatHandlers();
    }

    private void loadDefaultStatHandler() {
        this.defaultStatHandlerKey = plugin.getConfig().getString("stat-handler.default", "mythiclib");
        this.defaultStatHandler = handlers.get(defaultStatHandlerKey);

        if (defaultStatHandler == null) {
            plugin.getLogger().log(Level.WARNING, "Default stat handler '" + defaultStatHandlerKey + "' not found. Using MythicLib if available.");
            defaultStatHandlerKey = "mythiclib";
            defaultStatHandler = handlers.get(defaultStatHandlerKey);
        }

        if (defaultStatHandler == null) {
            plugin.getLogger().log(Level.SEVERE, "No stat handlers available! Stats will not function.");
        }
    }

    private void registerStatHandlers() {
        if (Bukkit.getPluginManager().isPluginEnabled("MythicLib"))
            register(new MythicLibStatHandler());

        if (Bukkit.getPluginManager().isPluginEnabled("AureliumSkills"))
            register(new AureliumSkillsStatHandler());

        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs"))
            register(new MythicMobsStatHandler());
    }

    public void register(StatHandler<?> handler) {
        handlers.put(handler.namespace(), handler);
        plugin.getLogger().log(Level.INFO, "StatHandler registered successfully: " + handler.namespace());
    }

    public void add(EffectHolder holder, ActiveStatusEffect effect) {
        if (holder == null) return;

        for (Map.Entry<StatKey, StatValue> entry : effect.getStatusEffect().getStats().entrySet()) {
            StatHandler<?> handler = getHandler(entry.getKey());
            if (handler != null) {
                handler.add(holder, effect, entry.getKey(), entry.getValue());
            }
        }
    }

    public void remove(EffectHolder holder, ActiveStatusEffect effect) {
        if (holder == null) return;

        for (StatKey key : effect.getStatusEffect().getStats().keySet()) {
            StatHandler<?> handler = getHandler(key);
            if (handler != null) {
                handler.remove(holder, key);
            }
        }
    }

    public String getValue(EffectHolder holder, StatKey key) {
        if (holder == null || key == null) return "0";

        StatHandler<?> handler = getHandler(key);
        if (handler != null) {
            return handler.getValue(holder, key);
        }
        return "0";
    }

    private StatHandler<?> getHandler(StatKey key) {
        String handlerKey = (key.getPlugin() != null) ? key.getPlugin() : defaultStatHandlerKey;
        StatHandler<?> handler = handlers.get(handlerKey);

        if (handler == null) {
            MMOBuffs.getInst().getLogger().warning("Stat handler '" + handlerKey + "' not found. Using default.");
            handler = defaultStatHandler;
        }

        if (handler == null) {
            MMOBuffs.getInst().getLogger().severe("No stat handler available for key: " + key);
            return null;
        }

        return handler;
    }

    public void reload() {
        loadDefaultStatHandler();
    }
}