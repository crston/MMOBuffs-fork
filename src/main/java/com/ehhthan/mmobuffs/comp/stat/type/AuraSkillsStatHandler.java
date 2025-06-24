package com.ehhthan.mmobuffs.comp.stat.type;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.GlobalRegistry;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.user.SkillsUser;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import com.ehhthan.mmobuffs.comp.stat.StatHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AuraSkillsStatHandler implements StatHandler<SkillsUser> {
    private static final String NAMESPACE = "auraskills";

    @Override
    public @NotNull String namespace() {
        return NAMESPACE;
    }

    @Override
    public @Nullable SkillsUser adapt(@NotNull EffectHolder holder) {
        UUID uuid = holder.getPlayer().getUniqueId();
        return AuraSkillsApi.get().getUserManager().getUser(uuid);
    }

    @Override
    public void add(@NotNull EffectHolder holder,
                    @NotNull ActiveStatusEffect effect,
                    @NotNull StatKey key,
                    @NotNull StatValue value) {
        SkillsUser user = adapt(holder);
        if (user == null) return;

        double modified = switch (effect.getStatusEffect().getStackType()) {
            case NORMAL, CASCADING -> value.getValue() * effect.getStacks();
            default -> value.getValue();
        };

        GlobalRegistry reg = AuraSkillsApi.get().getGlobalRegistry();
        NamespacedId id = NamespacedId.of(NAMESPACE, key.getStat());
        Stat stat = reg.getStat(id);
        if (stat != null) {
            StatModifier modifier = new StatModifier(
                    key.toString(), stat, modified, StatModifier.Operation.ADD
            );
            user.addStatModifier(modifier);
        }
    }

    @Override
    public void remove(@NotNull EffectHolder holder,
                       @NotNull StatKey key) {
        SkillsUser user = adapt(holder);
        if (user != null) {
            user.removeStatModifier(key.toString());
        }
    }

    @Override
    public @NotNull String getValue(@NotNull EffectHolder holder,
                                    @NotNull StatKey key) {
        SkillsUser user = adapt(holder);
        if (user == null) return "0";

        GlobalRegistry reg = AuraSkillsApi.get().getGlobalRegistry();
        Stat stat = reg.getStat(NamespacedId.of(NAMESPACE, key.getStat()));
        if (stat == null) return "0";

        double level = user.getStatLevel(stat);
        return String.valueOf(level);
    }
}
