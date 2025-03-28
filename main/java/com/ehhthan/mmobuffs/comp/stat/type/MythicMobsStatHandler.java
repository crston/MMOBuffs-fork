package com.ehhthan.mmobuffs.comp.stat.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.StatExecutor;
import io.lumine.mythic.core.skills.stats.StatModifierType;
import io.lumine.mythic.core.skills.stats.StatRegistry;
import io.lumine.mythic.core.skills.stats.StatSource;
import io.lumine.mythic.core.skills.stats.StatType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public class MythicMobsStatHandler implements StatHandler<StatRegistry> {
    private static final String NAMESPACE = "mythicmobs";

    @Override
    public @NotNull String namespace() {
        return NAMESPACE;
    }

    @Nullable
    @Override
    public StatRegistry adapt(@NotNull EffectHolder holder) {
        return MythicBukkit.inst().getPlayerManager().getProfile(holder.getPlayer()).getStatRegistry();
    }

    @Override
    public void add(@NotNull EffectHolder holder, @NotNull ActiveStatusEffect effect, @NotNull StatKey key, @NotNull StatValue value) {
        StatRegistry adapted = adapt(holder);
        Optional<StatType> maybeStat = getExecutor().getStat(key.getStat().toUpperCase(Locale.ROOT));
        if (adapted != null && maybeStat.isPresent()) {
            StatType stat = maybeStat.get();
            double modifierValue = switch (effect.getStatusEffect().getStackType()) {
                case NORMAL, CASCADING -> value.getValue() * effect.getStacks();
                default -> value.getValue();
            };

            Optional<StatRegistry.StatMap> maybeData = adapted.getStatData(stat);

            if (maybeData.isPresent()) {
                StatRegistry.StatMap data = maybeData.get();
                data.put(MythicKey.get(key), adaptModifier(value.getType()), modifierValue);
            }
        } else {
            MMOBuffs.getInst().getLogger().log(Level.WARNING, "MythicMobs stat not found: " + key.getStat());
        }
    }

    @Override
    public void remove(@NotNull EffectHolder holder, @NotNull StatKey key) {
        StatRegistry adapted = adapt(holder);
        Optional<StatType> maybeStat = getExecutor().getStat(key.getStat().toUpperCase(Locale.ROOT));

        if (adapted != null && maybeStat.isPresent()) {
            StatType stat = maybeStat.get();
            adapted.removeValue(stat, MythicKey.get(key));
        } else {
            MMOBuffs.getInst().getLogger().log(Level.WARNING, "MythicMobs stat not found: " + key.getStat());
        }
    }

    @Override
    public @NotNull String getValue(@NotNull EffectHolder holder, @NotNull StatKey key) {
        StatRegistry adapted = adapt(holder);
        Optional<StatType> maybeStat = getExecutor().getStat(key.getStat().toUpperCase(Locale.ROOT));

        if (adapted != null && maybeStat.isPresent()) {
            StatType stat = maybeStat.get();
            return String.valueOf(adapted.get(stat));
        } else {
            MMOBuffs.getInst().getLogger().log(Level.WARNING, "MythicMobs stat not found: " + key.getStat());
            return "0";
        }
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

        @Override
        public boolean removeOnReload() {
            return false;
        }

        public static MythicKey get(StatKey key) {
            return new MythicKey(key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MythicKey key1 = (MythicKey) o;
            return Objects.equals(key, key1.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public String toString() {
            return "MythicKey{" +
                    "key=" + key +
                    '}';
        }
    }
}