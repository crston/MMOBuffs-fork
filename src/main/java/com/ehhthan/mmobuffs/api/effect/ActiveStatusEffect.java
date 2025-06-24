package com.ehhthan.mmobuffs.api.effect;

import com.ehhthan.mmobuffs.api.modifier.Modifier;
import com.ehhthan.mmobuffs.api.effect.stack.StackType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ActiveStatusEffect {
    private final StatusEffect effect;
    private int duration;
    private int stacks;
    private final boolean permanent;
    private TagResolver cachedResolver;

    private ActiveStatusEffect(StatusEffect effect, int duration, int stacks, boolean permanent) {
        this.effect = effect;
        this.duration = duration;
        this.stacks = stacks;
        this.permanent = permanent;
        cacheResolver();
    }

    public static Builder builder(StatusEffect effect) {
        return new Builder(effect);
    }

    public boolean tick() {
        if (permanent) return false;
        if (duration > 0) {
            duration--;
            cacheResolver();
        }
        return duration > 0;
    }

    public boolean isActive() {
        return permanent || duration > 0;
    }

    public ActiveStatusEffect merge(ActiveStatusEffect other, Modifier durationMod, Modifier stackMod) {
        duration = durationMod.apply(this.duration, other.duration);
        stacks = stackMod.apply(this.stacks, other.stacks);
        cacheResolver();
        return this;
    }

    public void triggerStack(StackType type) {
        if (effect.getStackType() == type && stacks < effect.getMaxStacks()) {
            stacks++;
            cacheResolver();
        }
    }

    public TagResolver getResolver() {
        return cachedResolver;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        cacheResolver();
    }

    public int getStacks() {
        return stacks;
    }

    public void setStacks(int stacks) {
        this.stacks = stacks;
        cacheResolver();
    }

    public boolean isPermanent() {
        return permanent;
    }

    public StatusEffect getStatusEffect() {
        return effect;
    }

    public String getDurationDisplay() {
        if (permanent) return "∞"; // Infinity symbol
        int seconds = duration;
        int minutes = seconds / 60;
        seconds %= 60;
        return minutes > 0 ? minutes + "m " + seconds + "s" : seconds + "s";
    }

    private void cacheResolver() {
        this.cachedResolver = TagResolver.builder()
                .resolver(effect.getResolver())
                .resolver(Placeholder.parsed("duration", permanent ? "∞" : duration + "s"))
                .resolver(Placeholder.parsed("stacks", String.valueOf(stacks)))
                .build();
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