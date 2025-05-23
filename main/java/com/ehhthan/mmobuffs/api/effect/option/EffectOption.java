package com.ehhthan.mmobuffs.api.effect.option;

import java.util.Locale;

public enum EffectOption {
    KEEP_ON_DEATH(false),
    KEEP_ON_WORLD_CHANGE(true);

    private final boolean def;

    EffectOption(boolean def) {
        this.def = def;
    }

    public static EffectOption fromPath(String path) {
        try {
            return valueOf(path.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid effect option: " + path, e);
        }
    }

    public boolean defValue() {
        return def;
    }

    public String path() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
