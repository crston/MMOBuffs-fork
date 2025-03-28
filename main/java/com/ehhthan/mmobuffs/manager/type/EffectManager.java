package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import com.ehhthan.mmobuffs.manager.ConfigFile;
import com.ehhthan.mmobuffs.manager.KeyedManager;
import com.ehhthan.mmobuffs.manager.Reloadable;
import org.bukkit.configuration.ConfigurationSection;

public final class EffectManager extends KeyedManager<StatusEffect> implements Reloadable {
    public EffectManager() {
        reload();
    }

    @Override
    public void reload() {
        clear();
        ConfigFile config = new ConfigFile("effects");
        for (String key : config.getConfig().getKeys(false)) {
            ConfigurationSection section = config.getConfig().getConfigurationSection(key);
            if (section != null) { // Check if the section is valid
                try {
                    register(new StatusEffect(section));
                } catch (IllegalArgumentException e) {
                    error(key, e);
                }
            } else {
                MMOBuffs.getInst().getLogger().warning("Invalid configuration section for effect: " + key);
            }
        }
    }
}