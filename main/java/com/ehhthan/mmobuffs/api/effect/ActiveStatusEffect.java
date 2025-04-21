package com.ehhthan.mmobuffs.api.effect;

import com.ehhthan.mmobuffs.api.effect.display.duration.DurationDisplay;
import com.ehhthan.mmobuffs.api.effect.display.duration.TimedDisplay;
import com.ehhthan.mmobuffs.api.effect.stack.StackType;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ActiveStatusEffect implements Resolver, Comparable<ActiveStatusEffect> {
    private final StatusEffect statusEffect;
    private final int startDuration;
    private final int startStacks;
    private int duration;
    private int stacks;
    private final boolean permanent;
    private boolean active = true;
    private final DurationDisplay durationDisplay;

    public ActiveStatusEffect(StatusEffect effect, int startDuration, int startStacks,
                              int duration, int stacks, boolean permanent) {
        this.statusEffect = effect;
        this.startDuration = startDuration;
        this.startStacks = startStacks;
        this.duration = duration;
        this.stacks = stacks;
        this.permanent = permanent;
        this.durationDisplay = (permanent) ? DurationDisplay.PERMANENT : new TimedDisplay(this);
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

    public boolean isPermanent() {
        return permanent;
    }

    public boolean isActive() {
        return active;
    }

    public DurationDisplay getDurationDisplay() {
        return durationDisplay;
    }

    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    public void setStacks(int stacks) {
        this.stacks = Math.max(0, Math.min(stacks, statusEffect.getMaxStacks()));
    }

    public void deactivate() {
        this.active = false;
        this.duration = 0;
        this.stacks = 0;
    }

    public boolean tick() {
        if (!permanent && active) {
            this.duration--;
            if (duration <= 0) {
                StackType stackType = statusEffect.getStackType();

                switch (stackType) {
                    case NORMAL, ATTACK, HURT, COMBAT -> deactivate();
                    case CASCADING, TIMESTACK -> {
                        this.stacks--;
                        if (this.stacks <= 0) {
                            deactivate();
                        } else {
                            this.duration = startDuration;
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
            switch (type) {
                case ATTACK, HURT, COMBAT -> {
                    this.stacks--;
                    if (stacks <= 0)
                        deactivate();
                }
            }
        }
    }

    @Override
    public TagResolver getResolver() {
        TagResolver.Builder resolver = TagResolver.builder().resolvers(
                Placeholder.parsed("seconds", getDuration() + ""),
                Placeholder.component("duration", getDurationDisplay().display()),
                Placeholder.parsed("stacks", getStacks() + ""),
                Placeholder.parsed("start-duration", getStartDuration() + ""),
                Placeholder.parsed("start-stacks", getStartStacks() + "")
        );

        for (Map.Entry<StatKey, StatValue> entry : getStatusEffect().getStats().entrySet()) {
            resolver.resolver(Placeholder.parsed("stat-" + entry.getKey().getStat(), entry.getValue().toString()));
        }

        resolver.resolver(getStatusEffect().getResolver());
        return resolver.build();
    }

    public static ActiveEffectBuilder builder(StatusEffect effect) {
        return new ActiveEffectBuilder(effect);
    }

    public ActiveStatusEffect merge(ActiveStatusEffect latest, Modifier durationModifier, Modifier stackModifier) {
        switch (durationModifier) {
            case REFRESH -> {
                if (this.duration < latest.duration)
                    this.duration = latest.duration;
            }
            case SET -> this.duration = Math.max(0, latest.duration);
            case ADD -> this.duration = Math.max(0, this.duration + latest.duration);
            case SUBTRACT -> this.duration = Math.max(0, this.duration - latest.duration);
        }

        int maxStacks = statusEffect.getMaxStacks();
        switch (stackModifier) {
            case REFRESH -> {
                if (this.stacks < latest.stacks)
                    this.stacks = Math.max(0, Math.min(maxStacks, latest.stacks));
            }
            case SET -> this.stacks = Math.max(0, Math.min(maxStacks, latest.stacks));
            case ADD -> this.stacks = Math.max(0, Math.min(maxStacks, this.stacks + latest.stacks));
            case SUBTRACT -> this.stacks = Math.max(0, Math.min(maxStacks, this.stacks - latest.stacks));
        }

        return this;
    }

    @Override
    public int compareTo(@NotNull ActiveStatusEffect o) {
        if (isPermanent()) {
            return o.isPermanent() ? 0 : 1;
        } else {
            return Integer.compare(duration, o.duration);
        }
    }
}
