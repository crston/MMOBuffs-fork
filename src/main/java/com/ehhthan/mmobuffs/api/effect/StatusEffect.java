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
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final String DISPLAY_NAME = "display-name";
    private static final String DESCRIPTION = "description";
    private static final String STATS = "stats";
    private static final String OPTIONS = "options";
    private static final String DISPLAY = "display";
    private static final String STACK_TYPE = "stack-type";
    private static final String MAX_STACKS = "max-stacks";

    private final NamespacedKey key;
    private final Component name;
    private final Component description;
    private final Map<StatKey, StatValue> stats = new LinkedHashMap<>();
    private final Map<EffectOption, Boolean> options = new EnumMap<>(EffectOption.class);
    private final int maxStacks;
    private final StackType stackType;
    private final Optional<EffectDisplay> display;

    public StatusEffect(@NotNull ConfigurationSection section) {
        this.key = Optional.ofNullable(
                        NamespacedKey.fromString(section.getName().toLowerCase(Locale.ROOT), MMOBuffs.getInst())
                )
                .orElseThrow(() -> new IllegalArgumentException("Invalid key for StatusEffect section " + section.getName()));

        this.name = MM.deserialize(
                section.getString(DISPLAY_NAME, WordUtils.capitalize(key.getKey()))
        );

        this.description = MM.deserialize(
                section.getString(DESCRIPTION, "")
        );

        loadStats(section.getConfigurationSection(STATS));
        loadOptions(section.getConfigurationSection(OPTIONS));

        this.maxStacks = section.getInt(MAX_STACKS, 1);
        this.stackType = parseStackType(section.getString(STACK_TYPE));
        this.display = Optional.ofNullable(section.getConfigurationSection(DISPLAY)).map(EffectDisplay::new);
    }

    private void loadStats(@Nullable ConfigurationSection section) {
        if (section == null) {
            return;
        }
        for (String stat : section.getKeys(false)) {
            String[] split = stat.split(":", 2);
            StatKey statKey;
            if (split.length == 1) {
                statKey = new StatKey(this, split[0]);
            } else if (split.length == 2) {
                statKey = new StatKey(this, split[1], split[0]);
            } else {
                continue;
            }
            stats.put(statKey, new StatValue(Objects.requireNonNull(section.getString(stat))));
        }
    }

    private void loadOptions(@Nullable ConfigurationSection section) {
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            options.put(EffectOption.fromPath(key), section.getBoolean(key));
        }
    }

    private StackType parseStackType(@Nullable String type) {
        try {
            return StackType.valueOf(
                    Optional.ofNullable(type).orElse("NORMAL").toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            return StackType.NORMAL;
        }
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
        return display.isPresent();
    }

    public Optional<EffectDisplay> getDisplay() {
        return display;
    }

    @Override
    public TagResolver getResolver() {
        return TagResolver.builder()
                .resolver(Placeholder.parsed("max-stacks", String.valueOf(maxStacks)))
                .resolver(Placeholder.component("name", name))
                .resolver(Placeholder.component("description", description))
                .resolver(Placeholder.parsed(
                        "stack-type",
                        WordUtils.capitalize(stackType.name().toLowerCase(Locale.ROOT))
                ))
                .build();
    }
}