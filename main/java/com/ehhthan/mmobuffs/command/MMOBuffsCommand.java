package com.ehhthan.mmobuffs.command;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import com.ehhthan.mmobuffs.manager.type.LanguageManager;
import com.ehhthan.mmobuffs.manager.type.ParserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MMOBuffsCommand implements CommandExecutor, TabCompleter {

    private final MMOBuffs plugin;
    private final LanguageManager language;
    private final ParserManager parser;

    public MMOBuffsCommand(MMOBuffs plugin, LanguageManager language, ParserManager parser) {
        this.plugin = plugin;
        this.language = language;
        this.parser = parser;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            // Basic help message or plugin info
            sender.sendMessage("MMOBuffs Plugin - Available commands: reload, give, permanent, clear, time, stack, list");
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "reload" -> {
                reloadCommand(sender);
                yield true;
            }
            case "give" -> {
                giveCommand(sender, args);
                yield true;
            }
            case "permanent" -> {
                permanentCommand(sender, args);
                yield true;
            }
            case "clear" -> {
                clearCommand(sender, args);
                yield true;
            }
            case "time" -> {
                timeCommand(sender, args);
                yield true;
            }
            case "stack" -> {
                stackCommand(sender, args);
                yield true;
            }
            case "list" -> {
                listCommand(sender, args);
                yield true;
            }
            default -> {
                sender.sendMessage("Unknown subcommand. Use /mmobuffs for help.");
                yield false;
            }
        };
    }

    private void reloadCommand(CommandSender sender) {
        if (!sender.hasPermission("mmobuffs.reload")) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }
        plugin.reload();
        Component message = language.getMessage("reload-command");
        sender.sendMessage(message);
    }

    private void giveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.give")) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage("Usage: /mmobuffs give <player> <effect> <duration> [duration-modifier] [stacks] [stack-modifier] [-s]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sender.sendMessage("Invalid effect key.");
            return;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sender.sendMessage("Effect not found.");
            return;
        }

        try {
            int duration = Integer.parseInt(args[3]);
            Modifier durationModifier = (args.length > 4) ? parseModifier(args[4]) : Modifier.SET;
            int stacks = (args.length > 5) ? Integer.parseInt(args[5]) : 1;
            Modifier stackModifier = (args.length > 6) ? parseModifier(args[6]) : Modifier.KEEP;
            boolean silent = (args.length > 7) && args[7].equalsIgnoreCase("-s");

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = ActiveStatusEffect.builder(effect).startDuration(duration).startStacks(stacks).build();
            holder.addEffect(activeEffect, durationModifier, stackModifier);

            TagResolver resolver = TagResolver.builder()
                    .resolvers(activeEffect.getStatusEffect().getResolver())
                    .resolver(Placeholder.component("player", target.displayName()))
                    .build();

            Component message = language.getMessage("give-effect", true, resolver);

            if (!silent) {
                sender.sendMessage(message);
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid argument: " + e.getMessage());
        }
    }

    private void permanentCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.permanent")) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: /mmobuffs permanent <player> <effect> [stacks] [stack-modifier] [-s]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sender.sendMessage("Invalid effect key.");
            return;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sender.sendMessage("Effect not found.");
            return;
        }

        try {
            int stacks = (args.length > 3) ? Integer.parseInt(args[3]) : 1;
            Modifier stackModifier = (args.length > 4) ? parseModifier(args[4]) : Modifier.KEEP;
            boolean silent = (args.length > 5) && args[5].equalsIgnoreCase("-s");

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = ActiveStatusEffect.builder(effect).permanent(true).startStacks(stacks).build();
            holder.addEffect(activeEffect, Modifier.SET, stackModifier);

            TagResolver resolver = TagResolver.builder()
                    .resolvers(activeEffect.getStatusEffect().getResolver())
                    .resolver(Placeholder.component("player", target.displayName()))
                    .build();

            Component message = language.getMessage("give-effect-permanent", true, resolver);

            if (!silent) {
                sender.sendMessage(message);
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid argument: " + e.getMessage());
        }
    }

    private void clearCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.clear")) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: /mmobuffs clear <player> <effect|all|permanent> [-s]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        String choice = args[2].toLowerCase();
        boolean silent = (args.length > 3) && args[3].equalsIgnoreCase("-s");
        EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
        Component message;
        TagResolver.Single resolver = Placeholder.component("player", target.displayName());

        switch (choice) {
            case "all":
                holder.removeEffects(false);
                message = language.getMessage("clear-all-effects", true, resolver);
                break;
            case "permanent":
                holder.removeEffects(true);
                message = language.getMessage("clear-permanent-effects", true, resolver);
                break;
            default:
                NamespacedKey key = NamespacedKey.fromString(choice, plugin);
                if (key == null) {
                    sender.sendMessage("Invalid effect key.");
                    return;
                }
                if (holder.hasEffect(key)) {
                    ActiveStatusEffect activeEffect = holder.getEffect(key);
                    holder.removeEffect(key);

                    TagResolver.Builder builder = TagResolver.builder().resolver(resolver);
                    if (activeEffect != null) {
                        builder.resolvers(activeEffect.getStatusEffect().getResolver());
                    }
                    message = language.getMessage("clear-effect", true, builder.build());

                } else {
                    sender.sendMessage("Player does not have that effect.");
                    return;
                }
                break;
        }

        if (!silent) {
            sender.sendMessage(message);
        }
    }

    private void timeCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.time")) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }

        if (args.length < 5) {
            sender.sendMessage("Usage: /mmobuffs time <player> <effect> <set|add|subtract|multiply|divide> <duration> [-s]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sender.sendMessage("Invalid effect key.");
            return;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sender.sendMessage("Effect not found.");
            return;
        }

        try {
            Operation operation = parseOperation(args[3]);
            int duration = Integer.parseInt(args[4]);
            boolean silent = (args.length > 5) && args[5].equalsIgnoreCase("-s");

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = holder.getEffect(effectKey);

            if (activeEffect == null) {
                sender.sendMessage("Player does not have that effect.");
                return;
            }

            int newDuration = calculateDuration(activeEffect.getDuration(), duration, operation);

            activeEffect.setDuration(newDuration);

            TagResolver resolver = TagResolver.builder()
                    .resolvers(activeEffect.getStatusEffect().getResolver())
                    .resolver(Placeholder.component("player", target.displayName()))
                    .build();

            Component message = language.getMessage("time-effect", true, resolver);

            if (!silent) {
                sender.sendMessage(message);
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid argument: " + e.getMessage());
        }
    }

    private void stackCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.stack")) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }

        if (args.length < 5) {
            sender.sendMessage("Usage: /mmobuffs stack <player> <effect> <set|add|subtract|multiply|divide> <stacks> [-s]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sender.sendMessage("Invalid effect key.");
            return;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sender.sendMessage("Effect not found.");
            return;
        }

        try {
            Operation operation = parseOperation(args[3]);
            int stacks = Integer.parseInt(args[4]);
            boolean silent = (args.length > 5) && args[5].equalsIgnoreCase("-s");

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = holder.getEffect(effectKey);

            if (activeEffect == null) {
                sender.sendMessage("Player does not have that effect.");
                return;
            }

            int newStacks = calculateStacks(activeEffect.getStacks(), stacks, operation);
            activeEffect.setStacks(newStacks);

            TagResolver resolver = TagResolver.builder()
                    .resolvers(activeEffect.getStatusEffect().getResolver())
                    .resolver(Placeholder.component("player", target.displayName()))
                    .build();

            Component message = language.getMessage("stack-effect", true, resolver);

            if (!silent) {
                sender.sendMessage(message);
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid argument: " + e.getMessage());
        }
    }

    private void listCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.list")) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }

        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("Player not found.");
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("Usage: /mmobuffs list <player>");
            return;
        }

        EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);

        List<Component> components = new LinkedList<>();
        components.add(language.getMessage("list-display.header", false));

        String text = MMOBuffs.getInst().getLanguageManager().getString("list-display.effect-element");

        for (ActiveStatusEffect activeEffect : holder.getEffects(true)) {
            TagResolver resolver = TagResolver.builder()
                    .resolver(activeEffect.getStatusEffect().getResolver())
                    .resolver(Placeholder.component("player", target.displayName())).build();
            components.add(plugin.getParserManager().parse(target, text) != null ?
                    MiniMessage.miniMessage().deserialize(plugin.getParserManager().parse(target, text), resolver) : Component.empty());
        }

        components.add(language.getMessage("list-display.footer", false));

        final JoinConfiguration config = JoinConfiguration.builder()
                .prefix(Component.empty())
                .separator(Component.newline())
                .suffix(Component.empty())
                .build();

        Component message = Component.join(config, components);
        sender.sendMessage(message);
    }

    private Modifier parseModifier(String input) {
        try {
            return Modifier.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid modifier: " + input);
        }
    }

    private Operation parseOperation(String input) {
        try {
            return Operation.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid operation: " + input);
        }
    }

    private int calculateDuration(int current, int value, Operation operation) {
        return switch (operation) {
            case SET -> value;
            case ADD -> current + value;
            case SUBTRACT -> current - value;
            case MULTIPLY -> current * value;
            case DIVIDE -> (value != 0) ? current / value : current; // Avoid division by zero
        };
    }

    private int calculateStacks(int current, int value, Operation operation) {
        return switch (operation) {
            case SET -> value;
            case ADD -> current + value;
            case SUBTRACT -> current - value;
            case MULTIPLY -> current * value;
            case DIVIDE -> (value != 0) ? current / value : current; // Avoid division by zero
        };
    }

    @Override
    public @Nullable
    List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("reload", "give", "permanent", "clear", "time", "stack", "list")
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("give") || subCommand.equals("permanent") || subCommand.equals("clear") || subCommand.equals("time") || subCommand.equals("stack")) {
            if (args.length == 2) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 3 && (!subCommand.equals("clear"))) {
                return plugin.getEffectManager().keys().stream()
                        .map(NamespacedKey::toString) // Use toString to show namespaced key
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (subCommand.equals("clear") && args.length == 3) {
                List<String> options = plugin.getEffectManager().keys().stream()
                        .map(NamespacedKey::toString).collect(Collectors.toCollection(LinkedList::new)); // Use toString
                options.add("all");
                options.add("permanent");
                return options.stream()
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (subCommand.equals("give") && args.length == 4) {
                return Stream.of("100", "600", "1200")
                        .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (subCommand.equals("time") && args.length == 4) {
                return Stream.of("set", "add", "subtract", "multiply", "divide")
                        .filter(op -> op.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if ((subCommand.equals("stack")) && args.length == 4) {
                return Stream.of("set", "add", "subtract", "multiply", "divide")
                        .filter(op -> op.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if ((subCommand.equals("give")) && args.length == 5) {
                return Stream.of("SET", "ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "KEEP")
                        .filter(op -> op.toLowerCase().startsWith(args[4].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if ((subCommand.equals("give")) && args.length == 6) {
                return Stream.of("1", "2", "3", "4", "5")
                        .filter(op -> op.toLowerCase().startsWith(args[5].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if ((subCommand.equals("give")) && args.length == 7) {
                return Stream.of("SET", "ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "KEEP")
                        .filter(op -> op.toLowerCase().startsWith(args[6].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (subCommand.equals("list") && args.length == 2) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    enum Operation {
        SET,
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }
}