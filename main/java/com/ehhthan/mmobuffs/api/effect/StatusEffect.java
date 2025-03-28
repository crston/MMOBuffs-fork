package com.ehhthan.mmobuffs.api.effect;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.display.EffectDisplay;
import com.ehhthan.mmobuffs.api.effect.option.EffectOption;
import com.ehhthan.mmobuffs.api.effect.stack.StackType;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import com.ehhthan.mmobuffs.api.stat.StatValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StatusEffect implements Keyed, Resolver {
    private final NamespacedKey key;
    private final Component name;
    private final Component description;
    private final Map<StatKey, StatValue> stats;
    private final Map<EffectOption, Boolean> options;
    private final int maxStacks;
    private final StackType stackType;
    private final EffectDisplay display;
    private final TagResolver resolver; // Cache the resolver

    public StatusEffect(@NotNull ConfigurationSection section) {
        this.key = NamespacedKey.fromString(section.getName().toLowerCase(Locale.ROOT), MMOBuffs.getInst());
        assert key != null;
        this.name = MiniMessage.miniMessage().deserialize(section.getString("display-name", WordUtils.capitalize(key.getKey())));
        this.description = MiniMessage.miniMessage().deserialize(section.getString("description", ""));
        this.stats = loadStats(section);
        this.options = loadOptions(section);
        this.maxStacks = section.getInt("max-stacks", 1);
        this.stackType = StackType.valueOf(section.getString("stack-type", "NORMAL").toUpperCase(Locale.ROOT));
        this.display = (section.isConfigurationSection("display"))
                ? new EffectDisplay(Objects.requireNonNull(section.getConfigurationSection("display")))
                : null;

        this.resolver = createResolver();
    }

    private Map<StatKey, StatValue> loadStats(ConfigurationSection section) {
        if (!section.isConfigurationSection("stats")) {
            return Collections.emptyMap();
        }

        ConfigurationSection statSection = section.getConfigurationSection("stats");
        Map<StatKey, StatValue> loadedStats = new LinkedHashMap<>();
        assert statSection != null;
        for (String stat : statSection.getKeys(false)) {
            String[] split = stat.split(":", 2);

            StatKey statKey;
            if (split.length == 1) {
                statKey = new StatKey(this, split[0]);
            } else if (split.length == 2) {
                statKey = new StatKey(this, split[1], split[0]);
            } else {
                continue;
            }
            loadedStats.put(statKey, new StatValue(Objects.requireNonNull(statSection.getString(stat))));
        }
        return Collections.unmodifiableMap(loadedStats);
    }

    private Map<EffectOption, Boolean> loadOptions(ConfigurationSection section) {
        if (!section.isConfigurationSection("options")) {
            return Collections.emptyMap();
        }

        ConfigurationSection optionSection = section.getConfigurationSection("options");
        Map<EffectOption, Boolean> loadedOptions = new EnumMap<>(EffectOption.class);
        assert optionSection != null;
        for (String key : optionSection.getKeys(false)) {
            try {
                EffectOption option = EffectOption.fromPath(key);
                loadedOptions.put(option, optionSection.getBoolean(key));
            } catch (IllegalArgumentException e) {
                MMOBuffs.getInst().getLogger().warning("Invalid option '" + key + "' in status effect " + this.key);
            }
        }
        return Collections.unmodifiableMap(loadedOptions);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public Component getName() {
        return name;
    }

    public Component getDescription() {
        return description;
    }

    public boolean hasStats() {
        return !stats.isEmpty();
    }

    public Map<StatKey, StatValue> getStats() {
        return stats;
    }

    public boolean getOption(EffectOption option) {
        return options.getOrDefault(option, option.defValue());
    }

    public int getMaxStacks() {
        return maxStacks;
    }

    public StackType getStackType() {
        return stackType;
    }

    public boolean hasDisplay() {
        return display != null;
    }

    public @Nullable
    EffectDisplay getDisplay() {
        return display;
    }

    private TagResolver createResolver() {
        TagResolver.Builder resolverBuilder = TagResolver.builder()
                .resolver(Placeholder.parsed("max-stacks", String.valueOf(getMaxStacks())))
                .resolver(Placeholder.component("name", name))
                .resolver(Placeholder.component("description", description))
                .resolver(Placeholder.parsed("stack-type", WordUtils.capitalize(stackType.name().toLowerCase(Locale.ROOT))));

        return resolverBuilder.build();
    }

    @Override
    public TagResolver getResolver() {
        return resolver;
    }
}