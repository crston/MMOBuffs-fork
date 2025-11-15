package com.ehhthan.mmobuffs.api.stat;

import com.ehhthan.mmobuffs.api.effect.StatusEffect;

public final class StatKey {

    private static final String NAMESPACE = "mmobuffs";

    private final StatusEffect effect;
    private final String stat;
    private final String plugin;
    private final String cacheString;

    public StatKey(StatusEffect effect, String stat) {
        this(effect, stat, null);
    }

    public StatKey(StatusEffect effect, String stat, String plugin) {
        this.effect = effect;
        this.stat = stat.toLowerCase();
        this.plugin = plugin == null ? null : plugin.toLowerCase();

        String key = effect.getKey().getKey();
        this.cacheString = NAMESPACE + "." + key + "." + this.stat;
    }

    public StatusEffect getEffect() {
        return effect;
    }

    public String getStat() {
        return stat;
    }

    public String getPlugin() {
        return plugin;
    }

    @Override
    public String toString() {
        return cacheString;
    }

    @Override
    public int hashCode() {
        int h = effect.hashCode();
        h = 31 * h + stat.hashCode();
        return 31 * h + (plugin == null ? 0 : plugin.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StatKey other)) return false;
        return effect.equals(other.effect)
                && stat.equals(other.stat)
                && (plugin == null ? other.plugin == null : plugin.equals(other.plugin));
    }
}
