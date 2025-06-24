package com.ehhthan.mmobuffs.manager;

import java.util.*;

public abstract class KeylessManager<T> extends Manager<T> {
    protected final Set<T> managed = new LinkedHashSet<>();

    @Override public Collection<T> values() { return managed; }
    @Override public void clear() { managed.clear(); }
    @Override public int size() { return managed.size(); }
    @Override public void register(T property) { managed.add(property); }
}
