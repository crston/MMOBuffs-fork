package com.ehhthan.mmobuffs.manager;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.*;

public abstract class KeyedManager<T> extends Manager<T> {
    protected final Map<NamespacedKey, T> managed = new LinkedHashMap<>();

    @Override public Collection<T> values() { return managed.values(); }
    @Override public void clear() { managed.clear(); }
    @Override public int size() { return managed.size(); }

    @Override
    public void register(T property) {
        if (property instanceof Keyed keyed)
            managed.put(keyed.getKey(), property);
    }

    public void register(NamespacedKey key, T property) {
        managed.put(key, property);
    }

    public void registerAll(Map<NamespacedKey, T> properties) {
        managed.putAll(properties);
    }

    public Collection<NamespacedKey> keys() {
        return managed.keySet();
    }

    public boolean has(NamespacedKey key) {
        return managed.containsKey(key);
    }

    public T get(NamespacedKey key) {
        if (!has(key)) throw new IllegalArgumentException(key.asString() + " does not exist.");
        return managed.get(key);
    }
}
