package com.ehhthan.mmobuffs.command;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MMOBuffsCommand implements CommandExecutor, TabCompleter {
    private final MMOBuffs plugin;

    public MMOBuffsCommand(MMOBuffs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/mmobuffs reload | give | clear | permanent | list");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> handleReload(sender);
            case "list" -> handleList(sender, args);
            case "give" -> handleGive(sender, args);
            case "permanent" -> handlePermanent(sender, args);
            case "clear" -> handleClear(sender, args);
            default -> sender.sendMessage("Unknown subcommand.");
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("mmobuffs.reload")) {
            sender.sendMessage("You don't have permission.");
            return;
        }
        plugin.reload();
        sender.sendMessage("MMOBuffs reloaded.");
    }

    private void handleList(CommandSender sender, String[] args) {
        Player target = getTarget(sender, args);
        if (target == null) return;
        if (!EffectHolder.has(target)) {
            sender.sendMessage("No effects.");
            return;
        }
        EffectHolder holder = EffectHolder.get(target);
        sender.sendMessage("[Effects List]");
        holder.getEffects(true).forEach(effect ->
                sender.sendMessage("- " + effect.getStatusEffect().getKey().getKey() +
                        " (Duration: " + effect.getDuration() + ", Stacks: " + effect.getStacks() + ")")
        );
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.give")) {
            sender.sendMessage("You don't have permission.");
            return;
        }
        if (args.length < 4) {
            sender.sendMessage("Usage: /mmobuffs give <player> <effect> <duration>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        String effectId = args[2].toLowerCase(Locale.ROOT);
        int duration;
        try {
            duration = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid duration.");
            return;
        }

        NamespacedKey key = NamespacedKey.fromString(effectId, plugin);
        if (key == null || !plugin.getEffectManager().has(key)) {
            sender.sendMessage("Unknown effect.");
            return;
        }

        StatusEffect effect = plugin.getEffectManager().get(key);
        EffectHolder holder = EffectHolder.get(target);
        ActiveStatusEffect active = ActiveStatusEffect.builder(effect).startDuration(duration).startStacks(1).build();
        holder.addEffect(active, Modifier.SET, Modifier.KEEP);
        sender.sendMessage("Effect applied to " + target.getName() + ": " + effectId + " for " + duration + "s");
    }

    private void handlePermanent(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.permanent")) {
            sender.sendMessage("You don't have permission.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /mmobuffs permanent <player> <effect>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        String effectId = args[2].toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(effectId, plugin);
        if (key == null || !plugin.getEffectManager().has(key)) {
            sender.sendMessage("Unknown effect.");
            return;
        }

        StatusEffect effect = plugin.getEffectManager().get(key);
        EffectHolder holder = EffectHolder.get(target);
        ActiveStatusEffect active = ActiveStatusEffect.builder(effect).permanent(true).startStacks(1).build();
        holder.addEffect(active, Modifier.SET, Modifier.KEEP);
        sender.sendMessage("Permanent effect applied to " + target.getName() + ": " + effectId);
    }

    private void handleClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.clear")) {
            sender.sendMessage("You don't have permission.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /mmobuffs clear <player> <effect|all>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        String effectId = args[2].toLowerCase(Locale.ROOT);
        if (!EffectHolder.has(target)) {
            sender.sendMessage("Target has no effects.");
            return;
        }

        EffectHolder holder = EffectHolder.get(target);

        if (effectId.equals("all")) {
            holder.removeEffects(false);
            sender.sendMessage("All removable effects cleared from " + target.getName());
        } else {
            NamespacedKey key = NamespacedKey.fromString(effectId, plugin);
            if (key == null || !holder.hasEffect(key)) {
                sender.sendMessage("Player does not have this effect.");
                return;
            }
            holder.removeEffect(key);
            sender.sendMessage("Effect removed from " + target.getName() + ": " + effectId);
        }
    }

    private Player getTarget(CommandSender sender, String[] args) {
        if (args.length > 1) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("Player not found.");
                return null;
            }
            return target;
        } else if (sender instanceof Player player) {
            return player;
        } else {
            sender.sendMessage("You must specify a player.");
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return filterPrefix(args[0], List.of("reload", "give", "list", "clear", "permanent"));
        }

        if (args.length == 2 && List.of("give", "permanent", "clear").contains(args[0].toLowerCase(Locale.ROOT))) {
            return null; // Let Bukkit complete player names
        }

        if (args.length == 3 && List.of("give", "permanent", "clear").contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> ids = new ArrayList<>();
            for (NamespacedKey key : plugin.getEffectManager().keys()) {
                ids.add(key.getKey());
            }
            if (args[0].equalsIgnoreCase("clear")) {
                ids.add("all");
            }
            return filterPrefix(args[2], ids);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return filterPrefix(args[3], List.of("10", "30", "60", "120"));
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(String input, List<String> options) {
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
