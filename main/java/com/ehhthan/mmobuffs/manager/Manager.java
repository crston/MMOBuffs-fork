package com.ehhthan.mmobuffs.manager;

import com.ehhthan.mmobuffs.MMOBuffs;
import java.util.Collection;
import java.util.logging.Level;

public abstract class Manager<T> {
    protected final String name;

    public Manager() {
        this.name = getClass().getSimpleName();
    }

    public String getName() {
        return name;
    }

    public abstract Collection<T> values();

    public abstract void clear();

    public abstract void register(T property);

    protected void error(String key, Exception e) {
        MMOBuffs.getInst().getLogger().log(Level.WARNING, name + " Error: " + "Could not load '" + key + "' -> " + e.getMessage(), e);
    }
}