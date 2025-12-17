package com.ehhthan.mmobuffs.comp.stat.type;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class MythicMobsStatHandler implements StatHandler<StatRegistry> {
    private static final String NAMESPACE = "mythicmobs";

    @Override
    public @NotNull String namespace() {
        return NAMESPACE;
    }

    @Override
    public @Nullable StatRegistry adapt(@NotNull EffectHolder holder) {
        return MythicBukkit.inst().getPlayerManager().getProfile(holder.getPlayer()).getStatRegistry();
    }

    @Override
    public void add(@NotNull EffectHolder holder, @NotNull ActiveStatusEffect effect,
                    @NotNull StatKey key, @NotNull StatValue value) {
        StatRegistry registry = adapt(holder);
        Optional<StatType> optStat = getExecutor().getStat(key.getStat().toUpperCase(Locale.ROOT));
        if (registry == null || optStat.isEmpty()) return;

        double finalValue = switch (effect.getStatusEffect().getStackType()) {
            case NORMAL, CASCADING -> value.getValue() * effect.getStacks();
            default -> value.getValue();
        };

        optStat.flatMap(registry::getStatData).ifPresent(data ->
                data.put(MythicKey.of(key), adaptModifier(value.getType()), finalValue)
        );
    }

    @Override
    public void remove(@NotNull EffectHolder holder, @NotNull StatKey key) {
        StatRegistry registry = adapt(holder);
        Optional<StatType> optStat = getExecutor().getStat(key.getStat().toUpperCase(Locale.ROOT));
        optStat.ifPresent(stat -> {
            if (registry != null) {
                registry.removeValue(stat, MythicKey.of(key));
            }
        });
    }

    @Override
    public @NotNull String getValue(@NotNull EffectHolder holder, @NotNull StatKey key) {
        StatRegistry registry = adapt(holder);
        Optional<StatType> optStat = getExecutor().getStat(key.getStat().toUpperCase(Locale.ROOT));
        return optStat.map(stat -> {
            assert registry != null;
            return String.valueOf(registry.get(stat));
        }).orElse("0");
    }

    private StatExecutor getExecutor() {
        return MythicBukkit.inst().getStatManager();
    }

    private StatModifierType adaptModifier(StatValue.ValueType type) {
        return switch (type) {
            case FLAT -> StatModifierType.ADDITIVE;
            case RELATIVE -> StatModifierType.ADDITIVE_MULTIPLIER;
        };
    }

    static class MythicKey implements StatSource {
        private final StatKey key;

        private MythicKey(StatKey key) {
            this.key = key;
        }

        public static MythicKey of(StatKey key) {
            return new MythicKey(key);
        }

        @Override
        public boolean removeOnReload() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof MythicKey other && Objects.equals(this.key, other.key));
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
}