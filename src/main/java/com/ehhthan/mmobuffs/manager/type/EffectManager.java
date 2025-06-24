package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import com.ehhthan.mmobuffs.manager.ConfigFile;
import com.ehhthan.mmobuffs.manager.KeyedManager;
import com.ehhthan.mmobuffs.manager.Reloadable;
import org.bukkit.configuration.ConfigurationSection;

public final class EffectManager extends KeyedManager<StatusEffect> implements Reloadable {

    private static final String EFFECTS_FILE_NAME = "effects";

    public EffectManager() {
        reload();
    }

    @Override
    public void reload() {
        clear();

        ConfigFile configFile = new ConfigFile(EFFECTS_FILE_NAME);
        ConfigurationSection root = configFile.getConfig();
        if (root == null) {
            error("Root", new IllegalStateException("Missing root configuration section in effects.yml"));
            return;
        }

        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                error(key, new IllegalArgumentException("Missing configuration section for key: " + key));
                continue;
            }

            try {
                register(new StatusEffect(section));
            } catch (Exception e) {
                error(key, e);
            }
        }
    }
}
