package com.ehhthan.mmobuffs.manager;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class KeylessManager<T> extends Manager<T> {
    protected final Set<T> managed = new LinkedHashSet<>();

    @Override
    public final Collection<T> values() {
        return managed;
    }

    @Override
    public final void clear() {
        managed.clear();
    }

    @Override
    public final int size() {
        return managed.size();
    }

    @Override
    public final void register(T property) {
        managed.add(property);
    }
}