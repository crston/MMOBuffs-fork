package com.ehhthan.mmobuffs.api.stat;

import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class StatKey {
    private static final String NAMESPACE = "mmobuffs";
    private final UUID uuid = UUID.randomUUID();
    private final StatusEffect effect;
    private final String stat;
    private final String plugin;

    public StatKey(@NotNull StatusEffect effect, @NotNull String stat) {
        this(effect, stat, null);
    }

    public StatKey(@NotNull StatusEffect effect, @NotNull String stat, @Nullable String plugin) {
        this.effect = effect;
        this.stat = stat.toLowerCase(Locale.ROOT);
        this.plugin = (plugin != null) ? plugin.toLowerCase(Locale.ROOT) : null;
    }

    public @NotNull
    StatusEffect getEffect() {
        return effect;
    }

    public @NotNull
    String getStat() {
        return stat;
    }

    public @Nullable
    String getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull
    String toString() {
        return NAMESPACE + '.' + effect.getKey().getKey() + '.' + stat;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatKey key = (StatKey) o;
        return effect.equals(key.effect) && stat.equals(key.stat) && Objects.equals(plugin, key.plugin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effect, stat, plugin);
    }
}