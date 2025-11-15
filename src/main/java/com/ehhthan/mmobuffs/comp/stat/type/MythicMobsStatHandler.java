package com.ehhthan.mmobuffs.comp.stat.type;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.StatExecutor;
import io.lumine.mythic.core.skills.stats.StatModifierType;
import io.lumine.mythic.core.skills.stats.StatRegistry;
import io.lumine.mythic.core.skills.stats.StatType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class MythicMobsStatHandler implements StatHandler<StatRegistry> {

    private static final String NAMESPACE = "mythicmobs";
    private static final StatExecutor EXECUTOR = MythicBukkit.inst().getStatManager();

    @Override
    public @NotNull String namespace() {
        return NAMESPACE;
    }

    @Override
    public @Nullable StatRegistry adapt(@NotNull EffectHolder holder) {
        return MythicBukkit.inst().getPlayerManager().getProfile(holder.getPlayer()).getStatRegistry();
    }

    @Override
    public void add(@NotNull EffectHolder holder,
                    @NotNull ActiveStatusEffect effect,
                    @NotNull StatKey key,
                    @NotNull StatValue value) {
        StatRegistry registry = adapt(holder);
        if (registry == null) {
            return;
        }

        Optional<StatType> optStat = EXECUTOR.getStat(key.getStat().toUpperCase(Locale.ROOT));
        if (optStat.isEmpty()) {
            return;
        }

        double finalValue = switch (effect.getStatusEffect().getStackType()) {
            case NORMAL, CASCADING -> value.getValue() * effect.getStacks();
            default -> value.getValue();
        };

        optStat.flatMap(registry::getStatData).ifPresent(data ->
                data.put(MythicKey.of(key), adaptModifier(value.getType()), finalValue)
        );
    }

    @Override
    public void remove(@NotNull EffectHolder holder,
                       @NotNull StatKey key) {
        StatRegistry registry = adapt(holder);
        if (registry == null) {
            return;
        }

        Optional<StatType> optStat = EXECUTOR.getStat(key.getStat().toUpperCase(Locale.ROOT));
        optStat.ifPresent(stat -> registry.removeValue(stat, MythicKey.of(key)));
    }

    @Override
    public @NotNull String getValue(@NotNull EffectHolder holder,
                                    @NotNull StatKey key) {
        StatRegistry registry = adapt(holder);
        if (registry == null) {
            return "0";
        }
        Optional<StatType> optStat = EXECUTOR.getStat(key.getStat().toUpperCase(Locale.ROOT));
        return optStat.map(stat -> String.valueOf(registry.get(stat))).orElse("0");
    }

    private StatModifierType adaptModifier(StatValue.ValueType type) {
        return switch (type) {
            case FLAT -> StatModifierType.ADDITIVE;
            case RELATIVE -> StatModifierType.ADDITIVE_MULTIPLIER;
        };
    }

    private static final class MythicKey implements io.lumine.mythic.core.skills.stats.StatSource {
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
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MythicKey other)) {
                return false;
            }
            return Objects.equals(this.key, other.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
}
