package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import com.ehhthan.mmobuffs.manager.ConfigFile;
import com.ehhthan.mmobuffs.manager.KeyedManager;
import com.ehhthan.mmobuffs.manager.Reloadable;
import org.bukkit.configuration.ConfigurationSection;

public final class EffectManager extends KeyedManager<StatusEffect> implements Reloadable {
    private final MMOBuffs plugin;

    public EffectManager(MMOBuffs plugin) {
        this.plugin = plugin;
        reload();
    }

    @Override
    public void reload() {
        clear();
        ConfigFile config = new ConfigFile(plugin, "effects");
        for (String key : config.getConfig().getKeys(false)) {
            ConfigurationSection effectSection = config.getConfig().getConfigurationSection(key);
            if (effectSection != null) {
                try {
                    register(new StatusEffect(effectSection));
                } catch (IllegalArgumentException e) {
                    error(key, e);
                }
            } else {
                plugin.getLogger().warning("Invalid effect section for key: " + key);
            }
        }
    }
}