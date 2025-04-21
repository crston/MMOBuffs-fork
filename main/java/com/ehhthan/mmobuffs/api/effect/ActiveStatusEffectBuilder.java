package com.ehhthan.mmobuffs.api.effect;

public class ActiveStatusEffectBuilder {
    private final StatusEffect statusEffect;
    private int startDuration = 0;
    private int startStacks = 1;
    private int duration = -1;
    private int stacks = -1;
    private boolean permanent = false;

    public ActiveStatusEffectBuilder(StatusEffect effect) {
        this.statusEffect = effect;
    }

    public ActiveStatusEffectBuilder startDuration(int duration) {
        this.startDuration = duration;
        return this;
    }

    public ActiveStatusEffectBuilder startStacks(int stacks) {
        this.startStacks = stacks;
        return this;
    }

    public ActiveStatusEffectBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public ActiveStatusEffectBuilder stacks(int stacks) {
        this.stacks = stacks;
        return this;
    }

    public ActiveStatusEffectBuilder permanent(boolean permanent) {
        this.permanent = permanent;
        return this;
    }

    public ActiveStatusEffect build() {
        int finalDuration = duration >= 0 ? duration : startDuration;
        int finalStacks = stacks >= 0 ? stacks : startStacks;
        return new ActiveStatusEffect(statusEffect, startDuration, startStacks, finalDuration, finalStacks, permanent);
    }
}