package com.ehhthan.mmobuffs.comp.placeholderapi;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MMOBuffsExpansion extends PlaceholderExpansion {
    private final MMOBuffs plugin;

    public MMOBuffsExpansion() {
        this.plugin = MMOBuffs.getInst();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mmobuffs";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return null;

        var bukkitPlayer = player.getPlayer();
        if (!EffectHolder.has(bukkitPlayer)) return null;

        EffectHolder holder = EffectHolder.get(bukkitPlayer);
        String[] split = params.split("_", 2);
        if (split.length != 2) return null;

        String option = split[0].toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(split[1].toLowerCase(Locale.ROOT), plugin);
        if (key == null) return "0";

        switch (option) {
            case "name" -> {
                if (plugin.getEffectManager().has(key))
                    return PlainTextComponentSerializer.plainText().serialize(plugin.getEffectManager().get(key).getName());
                return "";
            }
            case "has" -> {
                return String.valueOf(holder.hasEffect(key));
            }
            case "duration" -> {
                if (holder.hasEffect(key)) {
                    ActiveStatusEffect effect = holder.getEffect(key);
                    return effect.getDurationDisplay();
                }
                return "0";
            }
            case "seconds" -> {
                return holder.hasEffect(key)
                        ? String.valueOf(holder.getEffect(key).getDuration())
                        : "0";
            }
            case "stacks" -> {
                return holder.hasEffect(key)
                        ? String.valueOf(holder.getEffect(key).getStacks())
                        : "0";
            }
            case "maxstacks" -> {
                return holder.hasEffect(key)
                        ? String.valueOf(holder.getEffect(key).getStatusEffect().getMaxStacks())
                        : "0";
            }
            default -> {
                String[] optionSplit = split[1].split("_", 2);
                if (optionSplit.length != 2) return "0";

                String[] statSplit = optionSplit[0].split(":", 2);
                key = NamespacedKey.fromString(optionSplit[1].toLowerCase(Locale.ROOT), plugin);
                if (key == null || !holder.hasEffect(key)) return "0";

                StatKey statKey = switch (statSplit.length) {
                    case 1 -> new StatKey(plugin.getEffectManager().get(key), statSplit[0]);
                    case 2 -> new StatKey(plugin.getEffectManager().get(key), statSplit[1], statSplit[0]);
                    default -> null;
                };
                if (statKey == null) return "0";

                return switch (option) {
                    case "value" -> plugin.getStatManager().getValue(holder, statKey);
                    case "basevalue" -> {
                        var value = holder.getEffect(key).getStatusEffect().getStats().get(statKey);
                        yield value != null ? value.toString() : "0";
                    }
                    default -> "0";
                };
            }
        }
    }
}