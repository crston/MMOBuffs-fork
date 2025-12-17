package com.ehhthan.mmobuffs.api.effect.stack;

public enum StackType {

    NORMAL(true),
    CASCADING(true),
    TIMESTACK(false),
    ATTACK(false),
    HURT(false),
    COMBAT(false);

    private final boolean stacking;

    StackType(boolean stacking) {
        this.stacking = stacking;
    }

    public boolean isStacking() {
        return stacking;
    }
}