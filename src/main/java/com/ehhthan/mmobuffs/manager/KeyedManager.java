package com.ehhthan.mmobuffs.manager;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class KeyedManager<T> extends Manager<T> {
    protected final Map<NamespacedKey, T> managed = new LinkedHashMap<>();

    @Override
    public final Collection<T> values() {
        return managed.values();
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
        if (property instanceof Keyed keyed) {
            managed.put(keyed.getKey(), property);
        }
    }

    public final void register(NamespacedKey key, T property) {
        managed.put(key, property);
    }

    public final void registerAll(Map<NamespacedKey, T> properties) {
        managed.putAll(properties);
    }

    public final Collection<NamespacedKey> keys() {
        return managed.keySet();
    }

    public final boolean has(NamespacedKey key) {
        return managed.containsKey(key);
    }

    public final T get(NamespacedKey key) {
        T value = managed.get(key);
        if (value == null) {
            throw new IllegalArgumentException(key.asString() + " does not exist.");
        }
        return value;
    }
}
