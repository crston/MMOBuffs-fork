package com.ehhthan.mmobuffs.api.tag.custom;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static com.ehhthan.mmobuffs.api.tag.CustomTagTypes.*;
import static com.ehhthan.mmobuffs.util.KeyUtil.key;

public class ActiveEffectTag implements PersistentDataType<PersistentDataContainer, ActiveStatusEffect> {
    private static final NamespacedKey EFFECT = key("effect");
    private static final NamespacedKey START_DURATION = key("start_duration");
    private static final NamespacedKey START_STACKS = key("start_stacks");
    private static final NamespacedKey DURATION = key("duration");
    private static final NamespacedKey STACKS = key("stacks");
    private static final NamespacedKey PERMANENT = key("permanent");

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<ActiveStatusEffect> getComplexType() {
        return ActiveStatusEffect.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull ActiveStatusEffect effect, @NotNull PersistentDataAdapterContext ctx) {
        PersistentDataContainer container = ctx.newPersistentDataContainer();

        container.set(EFFECT, NAMESPACED_KEY, effect.getStatusEffect().getKey());
        container.set(START_DURATION, INTEGER, effect.getDuration());
        container.set(START_STACKS, INTEGER, effect.getStacks());
        container.set(STACKS, INTEGER, effect.getStacks());

        if (effect.isPermanent()) {
            container.set(PERMANENT, BOOLEAN, true);
        } else {
            container.set(DURATION, INTEGER, effect.getDuration());
        }

        return container;
    }

    @Override
    public @NotNull ActiveStatusEffect fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext ctx) {
        NamespacedKey statusKey = container.get(EFFECT, NAMESPACED_KEY);
        StatusEffect status = MMOBuffs.getInst().getEffectManager().get(statusKey);

        int duration = container.has(DURATION, INTEGER)
                ? container.get(DURATION, INTEGER)
                : container.getOrDefault(START_DURATION, INTEGER, 0);

        return ActiveStatusEffect.builder(status)
                .startDuration(duration)
                .startStacks(container.getOrDefault(START_STACKS, INTEGER, container.getOrDefault(STACKS, INTEGER, 1)))
                .permanent(container.getOrDefault(PERMANENT, BOOLEAN, false))
                .build();
    }
}