package com.ehhthan.mmobuffs.comp.stat.type;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MythicLibStatHandler implements StatHandler<MMOPlayerData> {

    private static final String NAMESPACE = "mythiclib";

    @Override
    public @NotNull String namespace() {
        return NAMESPACE;
    }

    @Override
    public @Nullable MMOPlayerData adapt(@NotNull EffectHolder holder) {
        return MMOPlayerData.get(holder.getPlayer().getUniqueId());
    }

    @Override
    public void add(@NotNull EffectHolder holder, @NotNull ActiveStatusEffect effect,
                    @NotNull StatKey key, @NotNull StatValue value) {
        MMOPlayerData data = adapt(holder);
        if (data == null) return;

        // 1. 스택 계산
        double rawAmount = switch (effect.getStatusEffect().getStackType()) {
            case NORMAL, CASCADING -> value.getValue() * effect.getStacks();
            default -> value.getValue();
        };

        double amount = round(rawAmount, 4);

        String stat = key.getStat().toUpperCase(Locale.ROOT);
        data.getStatMap().getInstance(stat).registerModifier(
                new StatModifier(
                        key.getUUID(),
                        key.toString(),
                        stat,
                        amount,
                        adaptModifier(value.getType()),
                        EquipmentSlot.OTHER,
                        ModifierSource.OTHER
                )
        );
    }

    @Override
    public void remove(@NotNull EffectHolder holder, @NotNull StatKey key) {
        MMOPlayerData data = adapt(holder);
        if (data != null) {
            data.getStatMap().getInstance(key.getStat().toUpperCase(Locale.ROOT)).removeModifier(key.getUUID());
        }
    }

    @Override
    public @NotNull String getValue(@NotNull EffectHolder holder, @NotNull StatKey key) {
        MMOPlayerData data = adapt(holder);
        if (data != null) {
            StatModifier modifier = data.getStatMap().getInstance(key.getStat()).getModifier(key.getUUID());
            if (modifier != null) return modifier.toString();
        }
        return "0";
    }

    private ModifierType adaptModifier(StatValue.ValueType type) {
        return switch (type) {
            case FLAT -> ModifierType.FLAT;
            case RELATIVE -> ModifierType.RELATIVE;
        };
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}