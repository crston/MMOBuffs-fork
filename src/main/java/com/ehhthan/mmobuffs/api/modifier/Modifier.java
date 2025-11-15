package com.ehhthan.mmobuffs.api.modifier;

public enum Modifier {

    SET {
        @Override public int apply(int cur, int val) { return val; }
    },

    ADD {
        @Override public int apply(int cur, int val) { return cur + val; }
    },

    SUBTRACT {
        @Override public int apply(int cur, int val) { return cur - val; }
    },

    REFRESH {
        @Override public int apply(int cur, int val) { return cur >= val ? cur : val; }
    },

    KEEP {
        @Override public int apply(int cur, int val) { return cur; }
    };

    public abstract int apply(int cur, int val);
}
