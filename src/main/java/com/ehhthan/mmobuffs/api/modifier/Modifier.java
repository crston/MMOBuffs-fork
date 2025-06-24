package com.ehhthan.mmobuffs.api.modifier;

/**
 * Defines how a value (like duration or stacks) should be modified.
 */
public enum Modifier {
    SET {
        @Override public int apply(int current, int value) { return value; }
    },
    ADD {
        @Override public int apply(int current, int value) { return current + value; }
    },
    SUBTRACT {
        @Override public int apply(int current, int value) { return current - value; }
    },
    REFRESH {
        @Override public int apply(int current, int value) { return Math.max(current, value); }
    },
    KEEP {
        @Override public int apply(int current, int value) { return current; }
    };

    public abstract int apply(int current, int value);
}
