package com.ehhthan.mmobuffs.api.tag.custom;

import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.tag.CustomTagTypes;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ActiveEffectsTag implements PersistentDataType<PersistentDataContainer[], ActiveStatusEffect[]> {

    @Override
    public @NotNull Class<PersistentDataContainer[]> getPrimitiveType() {
        return PersistentDataContainer[].class;
    }

    @Override
    public @NotNull Class<ActiveStatusEffect[]> getComplexType() {
        return ActiveStatusEffect[].class;
    }

    @Override
    public @NotNull PersistentDataContainer[] toPrimitive(ActiveStatusEffect[] effects, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer[] containers = new PersistentDataContainer[effects.length];
        for (int i = 0; i < effects.length; i++) {
            containers[i] = CustomTagTypes.ACTIVE_EFFECT.toPrimitive(effects[i], context);
        }
        return containers;
    }

    @Override
    public @NotNull ActiveStatusEffect[] fromPrimitive(PersistentDataContainer[] containers, @NotNull PersistentDataAdapterContext context) {
        ActiveStatusEffect[] effects = new ActiveStatusEffect[containers.length];
        for (int i = 0; i < containers.length; i++) {
            try {
                effects[i] = CustomTagTypes.ACTIVE_EFFECT.fromPrimitive(containers[i], context);
            } catch (IllegalArgumentException e) {
                effects[i] = null;
            }
        }
        return effects;
    }
}