package com.ehhthan.mmobuffs.manager;

import com.ehhthan.mmobuffs.MMOBuffs;

import java.util.Collection;
import java.util.logging.Level;

public abstract class Manager<T> {
    protected final String name = getClass().getSimpleName();

    public final String getName() {
        return name;
    }

    public abstract Collection<T> values();

    public abstract void clear();

    public abstract int size();

    public abstract void register(T property);

    public final void registerAll(Collection<T> properties) {
        for (T p : properties) {
            register(p);
        }
    }

    @SafeVarargs
    public final void registerAll(T... properties) {
        for (T p : properties) {
            register(p);
        }
    }

    public final void error(String key, Exception e) {
        MMOBuffs.getInst().getLogger().log(Level.WARNING,
                name + " Error Could not load " + key + " -> " + e.getMessage());
    }
}