package com.ehhthan.mmobuffs.api.effect;

import com.ehhthan.mmobuffs.api.effect.display.duration.DurationDisplay;
import com.ehhthan.mmobuffs.api.effect.display.duration.TimedDisplay;
import com.ehhthan.mmobuffs.api.effect.stack.StackType;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public final class ActiveStatusEffect {

    private final StatusEffect effect;
    private final boolean permanent;

    private int duration;
    private int stacks;

    private final DurationDisplay durationDisplay;

    private ActiveStatusEffect(StatusEffect effect, int duration, int stacks, boolean permanent) {
        this.effect = effect;
        this.permanent = permanent;

        int baseStacks = stacks <= 0 ? 1 : stacks;
        int maxStacks = effect.getMaxStacks();
        if (maxStacks > 0 && baseStacks > maxStacks) {
            baseStacks = maxStacks;
        }
        this.stacks = baseStacks;

        this.duration = Math.max(0, duration);

        if (permanent) {
            this.durationDisplay = DurationDisplay.PERMANENT;
        } else {
            this.durationDisplay = new TimedDisplay(this);
        }
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
            if (duration < 0) {
                duration = 0;
            }
        }
        return duration > 0;
    }

    public boolean isActive() {
        return permanent || duration > 0;
    }

    public ActiveStatusEffect merge(ActiveStatusEffect other, Modifier durationMod, Modifier stackMod) {
        this.duration = Math.max(0, durationMod.apply(this.duration, other.duration));

        int newStacks = stackMod.apply(this.stacks, other.stacks);
        if (newStacks <= 0) {
            newStacks = 1;
        }
        int maxStacks = effect.getMaxStacks();
        if (maxStacks > 0 && newStacks > maxStacks) {
            newStacks = maxStacks;
        }
        this.stacks = newStacks;

        return this;
    }

    public void triggerStack(StackType type) {
        if (effect.getStackType() != type) {
            return;
        }
        int maxStacks = effect.getMaxStacks();
        if (maxStacks <= 0) {
            return;
        }
        if (stacks < maxStacks) {
            stacks++;
            if (stacks > maxStacks) {
                stacks = maxStacks;
            }
        }
    }

    public TagResolver getResolver() {
        TagResolver base = effect.getResolver();

        TagResolver dynamic = TagResolver.builder()
                .resolver(Placeholder.component("duration", durationDisplay.display()))
                .resolver(Placeholder.parsed("stacks", String.valueOf(stacks)))
                .build();

        return TagResolver.builder()
                .resolver(base)
                .resolver(dynamic)
                .build();
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    public int getStacks() {
        return stacks;
    }

    public void setStacks(int stacks) {
        int newStacks = stacks <= 0 ? 1 : stacks;
        int maxStacks = effect.getMaxStacks();
        if (maxStacks > 0 && newStacks > maxStacks) {
            newStacks = maxStacks;
        }
        this.stacks = newStacks;
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
        int sec = duration;
        if (sec <= 0) {
            return "0s";
        }
        int m = sec / 60;
        int s = sec % 60;
        if (m > 0) {
            return m + "m " + s + "s";
        } else {
            return s + "s";
        }
    }

    public static final class Builder {
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
