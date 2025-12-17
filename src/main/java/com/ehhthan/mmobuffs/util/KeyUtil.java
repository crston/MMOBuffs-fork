package com.ehhthan.mmobuffs.util;

import com.ehhthan.mmobuffs.MMOBuffs;
import org.bukkit.NamespacedKey;

public final class KeyUtil {

    private KeyUtil() {
    }

    public static NamespacedKey key(String key) {
        return new NamespacedKey(MMOBuffs.getInst(), key);
    }
}