package com.ehhthan.mmobuffs.comp.stat.type;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.stats.Stat;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AureliumSkillsStatHandler implements StatHandler<PlayerData> {
    private static final String NAMESPACE = "aureliumskills";

    @Override
    public @NotNull String namespace() {
        return NAMESPACE;
    }

    @Override
    public @Nullable PlayerData adapt(@NotNull EffectHolder holder) {
        return AureliumAPI.getPlugin().getPlayerManager().getPlayerData(holder.getPlayer());
    }

    @Override
    public void add(@NotNull EffectHolder holder, @NotNull ActiveStatusEffect effect,
                    @NotNull StatKey key, @NotNull StatValue value) {
        PlayerData data = adapt(holder);
        if (data == null) return;

        double modifiedValue = switch (effect.getStatusEffect().getStackType()) {
            case NORMAL, CASCADING -> value.getValue() * effect.getStacks();
            default -> value.getValue();
        };

        Stat stat = AureliumAPI.getPlugin().getStatRegistry().getStat(key.getStat());
        if (stat != null) {
            data.addStatModifier(new StatModifier(key.toString(), stat, modifiedValue));
        }
    }

    @Override
    public void remove(@NotNull EffectHolder holder, @NotNull StatKey key) {
        PlayerData data = adapt(holder);
        if (data != null) {
            data.removeStatModifier(key.toString());
        }
    }

    @Override
    public @NotNull String getValue(@NotNull EffectHolder holder, @NotNull StatKey key) {
        PlayerData data = adapt(holder);
        if (data == null || data.getStatModifier(key.toString()) == null) return "0";
        return String.valueOf(data.getStatModifier(key.toString()).getValue());
    }
}
