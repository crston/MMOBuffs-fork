package com.ehhthan.mmobuffs.comp.placeholderapi;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import com.ehhthan.mmobuffs.api.stat.StatKey;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.logging.Level;

public class MMOBuffsExpansion extends PlaceholderExpansion {
    private final MMOBuffs plugin;

    public MMOBuffsExpansion() {
        this.plugin = MMOBuffs.getInst();
    }

    @Override
    public @NotNull
    String getIdentifier() {
        return "mmobuffs";
    }

    @Override
    public @NotNull
    String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull
    String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable
    String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null || !EffectHolder.DATA.containsKey(onlinePlayer)) {
            plugin.getLogger().log(Level.WARNING, "Player is not online or EffectHolder is not loaded for player: " + player.getName());
            return null;
        }

        EffectHolder holder = EffectHolder.DATA.get(onlinePlayer);
        String[] split = params.split("_", 2);

        if (split.length != 2) {
            plugin.getLogger().log(Level.WARNING, "Invalid parameter format: " + params);
            return null;
        }

        String option = split[0].toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(split[1].toLowerCase(Locale.ROOT), plugin);

        if (key == null) {
            plugin.getLogger().log(Level.WARNING, "Invalid key format: " + split[1]);
            return null;
        }

        StatusEffect statusEffect = plugin.getEffectManager().get(key);
        if (statusEffect == null) {
            plugin.getLogger().log(Level.WARNING, "No status effect found for key: " + key);
            return "";
        }

        switch (option) {
            case "name":
                return PlainTextComponentSerializer.plainText().serialize(statusEffect.getName());
            case "has":
                return String.valueOf(holder.hasEffect(key));
            case "duration", "seconds": {
                ActiveStatusEffect activeEffect = holder.getEffect(key);
                return (activeEffect != null) ? String.valueOf(activeEffect.getDuration()) : "0";
            }
            case "stacks": {
                ActiveStatusEffect activeEffect = holder.getEffect(key);
                return (activeEffect != null) ? String.valueOf(activeEffect.getStacks()) : "0";
            }
            case "maxstacks":
                return String.valueOf(statusEffect.getMaxStacks());
            case "value": // Fallthrough intended
            case "basevalue": {
                String[] optionSplit = split[1].split("_", 2);
                if (optionSplit.length != 2) {
                    plugin.getLogger().log(Level.WARNING, "Invalid option split format: " + params);
                    return "0";
                }

                String effectKey = optionSplit[1];
                String[] statSplit = optionSplit[0].split(":", 2);
                NamespacedKey statKeyEffect = NamespacedKey.fromString(effectKey.toLowerCase(Locale.ROOT), plugin);
                if (statKeyEffect == null) {
                    plugin.getLogger().log(Level.WARNING, "No effect found for key: " + effectKey);
                    return "0";
                }

                if (!holder.hasEffect(statKeyEffect)) {
                    plugin.getLogger().log(Level.WARNING, "effect not active " + effectKey);
                    return "0";
                }

                ActiveStatusEffect activeEffect = holder.getEffect(statKeyEffect);
                if (activeEffect == null) {
                    plugin.getLogger().log(Level.WARNING, "ActiveStatusEffect is null for key: " + statKeyEffect);
                    return "0";
                }

                StatusEffect se = activeEffect.getStatusEffect();

                StatKey statKey;
                if (statSplit.length == 1) {
                    statKey = new StatKey(se, statSplit[0]);
                } else if (statSplit.length == 2) {
                    statKey = new StatKey(se, statSplit[1], statSplit[0]);
                } else {
                    plugin.getLogger().log(Level.WARNING, "Invalid stat split format: " + String.join(":", statSplit));
                    return "0";
                }

                if (option.equals("value")) {
                    return plugin.getStatManager().getValue(holder, statKey);
                } else { // option.equals("basevalue")
                    if (se.getStats().containsKey(statKey)) {
                        return se.getStats().get(statKey).toString();
                    } else {
                        plugin.getLogger().log(Level.WARNING, "StatKey not found in status effect stats: " + statKey);
                        return "0";
                    }
                }
            }
            default:
                return null;
        }
    }
}