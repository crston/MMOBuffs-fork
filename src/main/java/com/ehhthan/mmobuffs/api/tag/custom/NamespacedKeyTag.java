package com.ehhthan.mmobuffs.api.tag.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class NamespacedKeyTag implements PersistentDataType<String, NamespacedKey> {

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<NamespacedKey> getComplexType() {
        return NamespacedKey.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull NamespacedKey key, @NotNull PersistentDataAdapterContext context) {
        return key.asString();
    }

    @Override
    public @NotNull NamespacedKey fromPrimitive(@NotNull String input, @NotNull PersistentDataAdapterContext context) {
        NamespacedKey key = NamespacedKey.fromString(input);
        if (key == null)
            throw new IllegalArgumentException("Invalid NamespacedKey string: " + input);
        return key;
    }
}
