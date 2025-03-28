package com.ehhthan.mmobuffs.manager;

import com.google.common.base.Preconditions;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class KeyedManager<T> extends Manager<T> {
    protected final Map<NamespacedKey, T> managed = new LinkedHashMap<>();

    @Override
    public Collection<T> values() {
        return managed.values();
    }

    @Override
    public void clear() {
        managed.clear();
    }

    @Override
    public void register(T property) {
        if (property instanceof Keyed)
            managed.put(((Keyed) property).getKey(), property);
    }

    public Collection<NamespacedKey> keys() {
        return managed.keySet();
    }

    public boolean has(NamespacedKey key) {
        return key != null && managed.containsKey(key);
    }

    public T get(NamespacedKey key) {
        Preconditions.checkArgument(has(key), "Key '" + key.asString() + "' does not exist.");
        return managed.get(key);
    }
}