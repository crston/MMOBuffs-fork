package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;
import com.ehhthan.mmobuffs.comp.stat.type.AuraSkillsStatHandler;
import com.ehhthan.mmobuffs.comp.stat.type.MythicLibStatHandler;
import com.ehhthan.mmobuffs.comp.stat.type.MythicMobsStatHandler;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class StatManager {
    private final MMOBuffs plugin;
    private final Map<String, StatHandler<?>> handlers = new LinkedHashMap<>();
    private final StatHandler<?> defaultHandler;

    public StatManager(MMOBuffs plugin) {
        this.plugin = plugin;

        if (Bukkit.getPluginManager().getPlugin("MythicLib") != null) {
            register(new MythicLibStatHandler());
        }

        if (Bukkit.getPluginManager().getPlugin("AureliumSkills") != null) {
            register(new AuraSkillsStatHandler());
        }

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            register(new MythicMobsStatHandler());
        }

        StatHandler<?> first = handlers.isEmpty() ? null : handlers.values().iterator().next();
        String selected = plugin.getConfig().getString("stat-handler.default", "mythiclib");
        this.defaultHandler = handlers.getOrDefault(selected, first);

        if (this.defaultHandler == null) {
            plugin.getLogger().log(Level.WARNING, "No StatHandler registered. Stat system will not work correctly.");
        } else {
            plugin.getLogger().log(Level.INFO, "Default StatHandler " + this.defaultHandler.namespace());
        }
    }

    public final void register(StatHandler<?> handler) {
        handlers.put(handler.namespace(), handler);
        plugin.getLogger().log(Level.INFO, "StatHandler registered " + handler.namespace());
    }

    public final void add(EffectHolder holder, ActiveStatusEffect effect) {
        if (holder == null || effect == null) {
            return;
        }

        remove(holder, effect);

        Map<StatKey, com.ehhthan.mmobuffs.api.stat.StatValue> stats = effect.getStatusEffect().getStats();
        for (Map.Entry<StatKey, com.ehhthan.mmobuffs.api.stat.StatValue> entry : stats.entrySet()) {
            StatKey key = entry.getKey();
            com.ehhthan.mmobuffs.api.stat.StatValue value = entry.getValue();
            StatHandler<?> handler = resolveHandler(key);
            if (handler != null) {
                handler.add(holder, effect, key, value);
            }
        }
    }

    public final void remove(EffectHolder holder, ActiveStatusEffect effect) {
        if (holder == null || effect == null) {
            return;
        }

        for (StatKey key : effect.getStatusEffect().getStats().keySet()) {
            StatHandler<?> handler = resolveHandler(key);
            if (handler != null) {
                handler.remove(holder, key);
            }
        }
    }

    public final String getValue(EffectHolder holder, StatKey key) {
        if (holder == null || key == null) {
            return "0";
        }
        StatHandler<?> handler = resolveHandler(key);
        if (handler == null) {
            return "0";
        }
        return handler.getValue(holder, key);
    }

    private StatHandler<?> resolveHandler(StatKey key) {
        if (defaultHandler == null) {
            return null;
        }
        String pluginKey = key.getPlugin();
        if (pluginKey != null) {
            StatHandler<?> handler = handlers.get(pluginKey);
            if (handler != null) {
                return handler;
            }
        }
        return defaultHandler;
    }
}