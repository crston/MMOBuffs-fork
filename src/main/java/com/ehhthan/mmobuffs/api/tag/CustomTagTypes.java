package com.ehhthan.mmobuffs.api.tag;

import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.tag.custom.ActiveEffectTag;
import com.ehhthan.mmobuffs.api.tag.custom.ActiveEffectsTag;
import com.ehhthan.mmobuffs.api.tag.custom.BooleanTag;
import com.ehhthan.mmobuffs.api.tag.custom.NamespacedKeyTag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class CustomTagTypes {
    public static final PersistentDataType<Byte, Boolean> BOOLEAN = new BooleanTag();
    public static final PersistentDataType<String, NamespacedKey> NAMESPACED_KEY = new NamespacedKeyTag();
    public static final PersistentDataType<PersistentDataContainer, ActiveStatusEffect> ACTIVE_EFFECT = new ActiveEffectTag();
    public static final PersistentDataType<PersistentDataContainer[], ActiveStatusEffect[]> ACTIVE_EFFECTS = new ActiveEffectsTag();

    private CustomTagTypes() {}
}
