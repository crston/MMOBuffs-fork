package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatManager {
    private final MMOBuffs plugin;
    private final Map<String, StatHandler<?>> handlers = new LinkedHashMap<>();
    private final Map<String, StatHandler<?>> cache = new LinkedHashMap<>();

    public StatManager(MMOBuffs plugin) {
        this.plugin = plugin;
    }

    public void register(StatHandler<?> handler) {
        handlers.put(handler.namespace(), handler);
    }

    public void add(EffectHolder holder, ActiveStatusEffect effect) {
        for (var entry : effect.getStatusEffect().getStats().entrySet()) {
            getHandler(entry.getKey()).add(holder, effect, entry.getKey(), entry.getValue());
        }
    }

    public void remove(EffectHolder holder, ActiveStatusEffect effect) {
        for (var key : effect.getStatusEffect().getStats().keySet()) {
            getHandler(key).remove(holder, key);
        }
    }

    public String getValue(EffectHolder holder, StatKey key) {
        StatHandler<?> handler = getHandler(key);
        return handler != null ? handler.getValue(holder, key) : "0";
    }

    private StatHandler<?> getHandler(StatKey key) {
        String namespace = key.getPlugin() != null ? key.getPlugin() :
            plugin.getConfig().getString("stat-handler.default", "mythiclib");
        return cache.computeIfAbsent(namespace, handlers::get);
    }
}