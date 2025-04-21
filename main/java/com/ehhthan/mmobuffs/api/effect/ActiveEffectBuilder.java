package com.ehhthan.mmobuffs.api.effect;

public class ActiveEffectBuilder {
    private final StatusEffect effect;
    private int startDuration = 0;
    private int startStacks = 1;
    private int duration = -1;
    private int stacks = -1;
    private boolean permanent = false;

    public ActiveEffectBuilder(StatusEffect effect) {
        this.effect = effect;
    }

    public ActiveEffectBuilder startDuration(int startDuration) {
        this.startDuration = startDuration;
        return this;
    }

    public ActiveEffectBuilder startStacks(int startStacks) {
        this.startStacks = startStacks;
        return this;
    }

    public ActiveEffectBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public ActiveEffectBuilder stacks(int stacks) {
        this.stacks = stacks;
        return this;
    }

    public ActiveEffectBuilder permanent(boolean permanent) {
        this.permanent = permanent;
        return this;
    }

    public ActiveStatusEffect build() {
        int finalDuration = duration >= 0 ? duration : startDuration;
        int finalStacks = stacks >= 0 ? stacks : startStacks;
        return new ActiveStatusEffect(
                effect,
                startDuration,
                startStacks,
                finalDuration,
                finalStacks,
                permanent
        );
    }
}
