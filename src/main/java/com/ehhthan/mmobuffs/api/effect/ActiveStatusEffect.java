package com.ehhthan.mmobuffs.api.effect;

import com.ehhthan.mmobuffs.api.effect.stack.StackType;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ActiveStatusEffect {

    private final StatusEffect effect;
    private int duration;
    private int stacks;
    private final boolean permanent;

    private TagResolver cachedResolver;
    private boolean resolverDirty = true;

    private ActiveStatusEffect(StatusEffect effect, int duration, int stacks, boolean permanent) {
        this.effect = effect;
        this.duration = duration;
        this.stacks = Math.min(stacks, effect.getMaxStacks());
        this.permanent = permanent;
    }

    public static Builder builder(StatusEffect effect) {
        return new Builder(effect);
    }

    public boolean tick() {
        if (permanent) {
            return false;
        }
        if (duration > 0) {
            duration--;
            resolverDirty = true;
        }
        return duration > 0;
    }

    public boolean isActive() {
        return permanent || duration > 0;
    }

    public ActiveStatusEffect merge(ActiveStatusEffect other, Modifier durationMod, Modifier stackMod) {
        this.duration = durationMod.apply(this.duration, other.duration);
        int newStacks = stackMod.apply(this.stacks, other.stacks);
        this.stacks = Math.min(newStacks, effect.getMaxStacks());

        this.resolverDirty = true;
        return this;
    }

    public void triggerStack(StackType type) {
        if (effect.getStackType() == type && stacks < effect.getMaxStacks()) {
            stacks++;
            resolverDirty = true;
        }
    }

    public TagResolver getResolver() {
        if (cachedResolver == null || resolverDirty) {
            cachedResolver = TagResolver.builder()
                    .resolver(effect.getResolver())
                    .resolver(Placeholder.parsed("duration", permanent ? "∞" : duration + "s"))
                    .resolver(Placeholder.parsed("stacks", String.valueOf(stacks)))
                    .build();
            resolverDirty = false;
        }
        return cachedResolver;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        this.resolverDirty = true;
    }

    public int getStacks() {
        return stacks;
    }

    public void setStacks(int stacks) {
        this.stacks = Math.min(stacks, effect.getMaxStacks());
        this.resolverDirty = true;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public StatusEffect getStatusEffect() {
        return effect;
    }

    public String getDurationDisplay() {
        if (permanent) {
            return "∞";
        }

        int total = duration;
        int minutes = total / 60;
        int seconds = total % 60;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public static class Builder {
        private final StatusEffect effect;
        private int duration = 0;
        private int stacks = 1;
        private boolean permanent = false;

        public Builder(StatusEffect effect) {
            this.effect = effect;
        }

        public Builder startDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder startStacks(int stacks) {
            this.stacks = stacks;
            return this;
        }

        public Builder permanent(boolean permanent) {
            this.permanent = permanent;
            return this;
        }

        public ActiveStatusEffect build() {
            return new ActiveStatusEffect(effect, duration, stacks, permanent);
        }
    }
}