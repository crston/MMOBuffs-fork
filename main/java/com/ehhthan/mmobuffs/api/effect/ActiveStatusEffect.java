package com.ehhthan.mmobuffs.api.effect;

import com.ehhthan.mmobuffs.api.effect.display.duration.DurationDisplay;
import com.ehhthan.mmobuffs.api.effect.display.duration.TimedDisplay;
import com.ehhthan.mmobuffs.api.effect.stack.StackType;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public class ActiveStatusEffect implements Resolver, Comparable<ActiveStatusEffect> {

    private final StatusEffect statusEffect;
    private final int startDuration;
    private final int startStacks;

    private int duration;
    private int stacks;
    private final boolean permanent;

    private final DurationDisplay durationDisplay;
    private boolean active = true;

    private ActiveStatusEffect(ActiveEffectBuilder builder) {
        this.statusEffect = builder.effect;
        this.startDuration = builder.startDuration;
        this.startStacks = builder.startStacks;
        this.duration = builder.duration;
        this.stacks = builder.stacks;
        this.permanent = builder.permanent;
        this.durationDisplay = permanent ? DurationDisplay.PERMANENT : new TimedDisplay(this);
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    public int getStartDuration() {
        return startDuration;
    }

    public int getStartStacks() {
        return startStacks;
    }

    public int getDuration() {
        return duration;
    }

    public int getStacks() {
        return stacks;
    }

    public DurationDisplay getDurationDisplay() {
        return durationDisplay;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public boolean isActive() {
        return active;
    }

    public boolean tick() {
        if (!permanent && active) {
            this.duration--;
            if (duration <= 0) {
                switch (statusEffect.getStackType()) {
                    case NORMAL, ATTACK, HURT, COMBAT -> {
                        stacks = 0;
                        active = false;
                    }
                    case CASCADING, TIMESTACK -> {
                        stacks--;
                        if (stacks <= 0) {
                            active = false;
                        } else {
                            duration = startDuration;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void triggerStack(StackType type) {
        if (active && type == statusEffect.getStackType()) {
            if (type == StackType.ATTACK || type == StackType.HURT || type == StackType.COMBAT) {
                stacks--;
                if (stacks <= 0) active = false;
            }
        }
    }

    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    public void setStacks(int stacks) {
        this.stacks = Math.max(0, Math.min(stacks, statusEffect.getMaxStacks()));
    }

    @Override
    public TagResolver getResolver() {
        TagResolver.Builder builder = TagResolver.builder()
                .resolver(Placeholder.parsed("seconds", String.valueOf(duration)))
                .resolver(Placeholder.component("duration", durationDisplay.display()))
                .resolver(Placeholder.parsed("stacks", String.valueOf(stacks)))
                .resolver(Placeholder.parsed("start-duration", String.valueOf(startDuration)))
                .resolver(Placeholder.parsed("start-stacks", String.valueOf(startStacks)));

        for (Map.Entry<StatKey, StatValue> entry : statusEffect.getStats().entrySet()) {
            builder.resolver(Placeholder.parsed("stat-" + entry.getKey().getStat(), entry.getValue().toString()));
        }

        builder.resolver(statusEffect.getResolver());
        return builder.build();
    }

    public ActiveStatusEffect merge(ActiveStatusEffect other, Modifier durationMod, Modifier stackMod) {
        Preconditions.checkArgument(statusEffect.getKey().equals(other.statusEffect.getKey()),
                "Cannot merge effects of different types: %s + %s", statusEffect.getKey(), other.statusEffect.getKey());

        // Duration
        this.duration = switch (durationMod) {
            case REFRESH -> Math.max(this.duration, other.duration);
            case SET -> Math.max(0, other.duration);
            case ADD -> Math.max(0, this.duration + other.duration);
            case SUBTRACT -> Math.max(0, this.duration - other.duration);
            case KEEP -> this.duration;
        };

        // Stacks
        int max = statusEffect.getMaxStacks();
        this.stacks = switch (stackMod) {
            case REFRESH -> Math.max(this.stacks, other.stacks);
            case SET -> Math.max(0, Math.min(max, other.stacks));
            case ADD -> Math.max(0, Math.min(max, this.stacks + other.stacks));
            case SUBTRACT -> Math.max(0, Math.min(max, this.stacks - other.stacks));
            case KEEP -> this.stacks;
        };

        return this;
    }

    @Override
    public int compareTo(@NotNull ActiveStatusEffect o) {
        return Comparator.comparing(ActiveStatusEffect::isPermanent)
                .thenComparingInt(ActiveStatusEffect::getDuration)
                .compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveStatusEffect other)) return false;
        return startDuration == other.startDuration &&
                startStacks == other.startStacks &&
                duration == other.duration &&
                stacks == other.stacks &&
                permanent == other.permanent &&
                active == other.active &&
                statusEffect.equals(other.statusEffect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusEffect, startDuration, startStacks, duration, stacks, permanent, active);
    }

    public static ActiveEffectBuilder builder(StatusEffect effect) {
        return new ActiveEffectBuilder(effect);
    }

    public static class ActiveEffectBuilder {
        private final StatusEffect effect;
        private int startDuration = 0;
        private int startStacks = 0;
        private int duration = -1;
        private int stacks = -1;
        private boolean permanent = false;

        public ActiveEffectBuilder(StatusEffect effect) {
            this.effect = effect;
        }

        public ActiveEffectBuilder(ActiveStatusEffect source) {
            this.effect = source.statusEffect;
            this.startDuration = source.startDuration;
            this.startStacks = source.startStacks;
            this.duration = source.duration;
            this.stacks = source.stacks;
            this.permanent = source.permanent;
        }

        public ActiveEffectBuilder startDuration(int value) {
            this.startDuration = Math.max(0, value);
            return this;
        }

        public ActiveEffectBuilder startStacks(int value) {
            this.startStacks = Math.max(0, Math.min(effect.getMaxStacks(), value));
            return this;
        }

        public ActiveEffectBuilder duration(int value) {
            this.duration = Math.max(0, value);
            return this;
        }

        public ActiveEffectBuilder stacks(int value) {
            this.stacks = Math.max(0, Math.min(effect.getMaxStacks(), value));
            return this;
        }

        public ActiveEffectBuilder permanent(boolean value) {
            this.permanent = value;
            return this;
        }

        public ActiveStatusEffect build() {
            if (duration == -1) duration = startDuration;
            if (stacks == -1) stacks = startStacks;
            return new ActiveStatusEffect(this);
        }
    }
}
