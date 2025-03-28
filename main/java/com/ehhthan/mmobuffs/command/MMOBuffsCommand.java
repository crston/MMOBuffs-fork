package com.ehhthan.mmobuffs.command;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import com.ehhthan.mmobuffs.manager.type.LanguageManager;
import com.ehhthan.mmobuffs.manager.type.ParserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "reload":
                return reloadCommand(sender);
            case "give":
            case "add":
                return giveCommand(sender, args);
            case "permanent":
            case "perm":
                return permanentCommand(sender, args);
            case "clear":
            case "remove":
                return clearCommand(sender, args);
            case "time":
            case "duration":
                return timeCommand(sender, args);
            case "stack":
            case "stacks":
                return stackCommand(sender, args);
            case "list":
                return listCommand(sender, args);
            default:
                sendUnknownCommandMessage(sender);
                return true;
        }
    }

    private boolean reloadCommand(CommandSender sender) {
        if (!sender.hasPermission("mmobuffs.reload")) {
            sendNoPermissionMessage(sender);
            return true;
        }
        plugin.reload();
        Component message = language.getMessage("reload-command");
        if (message != null) {
            sender.sendMessage(message);
        }
        return true;
    }

    private boolean giveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.give")) {
            sendNoPermissionMessage(sender);
            return true;
        }

        if (args.length < 4) {
            sendUsageMessage(sender, "give <player> <effect> <duration> [duration-modifier] [stacks] [stack-modifier] [-s]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return true;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sendInvalidEffectKeyMessage(sender);
            return true;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sendEffectNotFoundMessage(sender);
            return true;
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

            if (!silent) {
                sendEffectMessage(sender, target, activeEffect, "give-effect");
            }
        } catch (IllegalArgumentException e) {
            sendInvalidArgumentMessage(sender);
        }

        return true;
    }

    private boolean permanentCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.permanent")) {
            sendNoPermissionMessage(sender);
            return true;
        }

        if (args.length < 3) {
            sendUsageMessage(sender, "permanent <player> <effect> [stacks] [stack-modifier] [-s]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return true;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sendInvalidEffectKeyMessage(sender);
            return true;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sendEffectNotFoundMessage(sender);
            return true;
        }

        try {
            int stacks = (args.length > 3) ? Integer.parseInt(args[3]) : 1;
            Modifier stackModifier = (args.length > 4) ? parseModifier(args[4]) : Modifier.KEEP;
            boolean silent = (args.length > 5) && args[5].equalsIgnoreCase("-s");

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = ActiveStatusEffect.builder(effect).permanent(true).startStacks(stacks).build();
            holder.addEffect(activeEffect, Modifier.SET, stackModifier);

            if (!silent) {
                sendEffectMessage(sender, target, activeEffect, "give-effect-permanent");
            }
        } catch (IllegalArgumentException e) {
            sendInvalidArgumentMessage(sender);
        }

        return true;
    }

    private boolean clearCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.clear")) {
            sendNoPermissionMessage(sender);
            return true;
        }

        if (args.length < 3) {
            sendUsageMessage(sender, "clear <player> <effect|all|permanent> [-s]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return true;
        }

        String choice = args[2].toLowerCase(Locale.ROOT);
        boolean silent = (args.length > 3) && args[3].equalsIgnoreCase("-s");
        EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);

        switch (choice) {
            case "all":
                holder.removeEffects(false);
                sendEffectMessage(sender, target, null, "clear-all-effects");
                break;
            case "permanent":
                holder.removeEffects(true);
                sendEffectMessage(sender, target, null, "clear-permanent-effects");
                break;
            default:
                NamespacedKey key = NamespacedKey.fromString(choice, plugin);
                if (key == null) {
                    sendInvalidEffectKeyMessage(sender);
                    return true;
                }
                if (!holder.hasEffect(key)) {
                    sendEffectNotPresentMessage(sender);
                    return true;
                }
                holder.removeEffect(key);
                sendEffectMessage(sender, target, null, "clear-effect");
                break;
        }

        return true;
    }

    private boolean timeCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.time")) {
            sendNoPermissionMessage(sender);
            return true;
        }

        if (args.length < 5) {
            sendUsageMessage(sender, "time <player> <effect> <set|add|subtract|multiply|divide> <duration> [-s]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return true;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sendInvalidEffectKeyMessage(sender);
            return true;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sendEffectNotFoundMessage(sender);
            return true;
        }

        try {
            Operation operation = parseOperation(args[3]);
            int duration = Integer.parseInt(args[4]);
            boolean silent = (args.length > 5) && args[5].equalsIgnoreCase("-s");

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = holder.getEffect(effectKey);

            if (activeEffect == null) {
                sendEffectNotPresentMessage(sender);
                return true;
            }

            int newDuration = calculateDuration(activeEffect.getDuration(), duration, operation);
            activeEffect.setDuration(newDuration);

            TagResolver resolver = TagResolver.builder().resolvers(activeEffect.getResolver()).resolver(Placeholder.component("player", target.displayName())).build();
            Component message = language.getMessage("time-effect", true, resolver);
            if (!silent)
                sender.sendMessage(message);

        } catch (IllegalArgumentException e) {
            sendInvalidArgumentMessage(sender);
        }

        return true;
    }

    private boolean stackCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.stack")) {
            sendNoPermissionMessage(sender);
            return true;
        }

        if (args.length < 5) {
            sendUsageMessage(sender, "stack <player> <effect> <set|add|subtract|multiply|divide> <stacks> [-s]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return true;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sendInvalidEffectKeyMessage(sender);
            return true;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sendEffectNotFoundMessage(sender);
            return true;
        }

        try {
            Operation operation = parseOperation(args[3]);
            int stacks = Integer.parseInt(args[4]);
            boolean silent = (args.length > 5) && args[5].equalsIgnoreCase("-s");

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = holder.getEffect(effectKey);

            if (activeEffect == null) {
                sendEffectNotPresentMessage(sender);
                return true;
            }

            int newStacks = calculateStacks(activeEffect.getStacks(), stacks, operation);
            activeEffect.setStacks(newStacks);
            holder.updateEffect(effectKey);

            TagResolver resolver = TagResolver.builder().resolvers(activeEffect.getResolver()).resolver(Placeholder.component("player", target.displayName())).build();
            Component message = language.getMessage("stack-effect", true, resolver);
            if (!silent)
                sender.sendMessage(message);

        } catch (IllegalArgumentException e) {
            sendInvalidArgumentMessage(sender);
        }

        return true;
    }

    private boolean listCommand(CommandSender sender, String[] args) {
        EffectHolder holder;
        if (args.length > 1) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sendPlayerNotFoundMessage(sender);
                return true;
            }
            holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            holder = EffectHolder.DATA.computeIfAbsent(player, EffectHolder::new);
        } else {
            sendUsageMessage(sender, "list [player]");
            return true;
        }

        List<Component> components = new LinkedList<>();
        components.add(language.getMessage("list-display.header", false));

        String text = MMOBuffs.getInst().getLanguageManager().getString("list-display.effect-element");

        for (ActiveStatusEffect activeEffect : holder.getEffects(true)) {
            TagResolver resolver = TagResolver.builder().resolvers(activeEffect.getResolver()).resolver(Placeholder.component("player", holder.getPlayer().displayName())).build();
            components.add(MiniMessage.miniMessage().deserialize((parser.parse(holder.getPlayer(), text)), resolver));
        }

        components.add(language.getMessage("list-display.footer", false));

        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < components.size(); i++) {
            builder.append(components.get(i));
            if (i != components.size() - 1)
                builder.append(Component.newline());
        }

        sender.sendMessage(builder.build());
        return true;
    }

    // Helper Methods for Messages
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(language.getMessage("help-command"));
    }

    private void sendUnknownCommandMessage(CommandSender sender) {
        sender.sendMessage(language.getMessage("unknown-command"));
    }

    private void sendNoPermissionMessage(CommandSender sender) {
        sender.sendMessage(language.getMessage("no-permission"));
    }

    private void sendPlayerNotFoundMessage(CommandSender sender) {
        sender.sendMessage(language.getMessage("player-not-found"));
    }

    private void sendInvalidEffectKeyMessage(CommandSender sender) {
        sender.sendMessage(language.getMessage("invalid-effect-key"));
    }

    private void sendEffectNotFoundMessage(CommandSender sender) {
        sender.sendMessage(language.getMessage("effect-not-found"));
    }

    private void sendEffectNotPresentMessage(CommandSender sender) {
        sender.sendMessage(language.getMessage("effect-not-present"));
    }

    private void sendInvalidArgumentMessage(CommandSender sender) {
        sender.sendMessage(language.getMessage("invalid-argument"));
    }

    private void sendUsageMessage(CommandSender sender, String usage) {
        Component message = language.getMessage("usage-command");
        if (message instanceof TextComponent) {
            sender.sendMessage(((TextComponent) message).content().replace("<usage>", usage));
        } else {
            // Handle the case where the message is not a TextComponent
            sender.sendMessage(message);
        }
    }

    private void sendEffectMessage(CommandSender sender, Player target, ActiveStatusEffect effect, String messageKey) {
        TagResolver resolver;
        if (effect != null) {
            resolver = TagResolver.builder().resolvers(effect.getResolver()).resolver(Placeholder.component("player", target.displayName())).build();
        } else {
            resolver = Placeholder.component("player", target.displayName());
        }

        Component message = language.getMessage(messageKey, true, resolver);
        sender.sendMessage(message);
    }

    // Helper Methods for Parsing and Calculation
    private Modifier parseModifier(String input) {
        try {
            return Modifier.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(language.getString("invalid-modifier"));
        }
    }

    private Operation parseOperation(String input) {
        try {
            return Operation.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(language.getString("invalid-operation"));
        }
    }

    private int calculateDuration(int current, int value, Operation operation) {
        return switch (operation) {
            case SET -> value;
            case ADD -> current + value;
            case SUBTRACT -> current - value;
            case MULTIPLY -> current * value;
            case DIVIDE -> (value != 0) ? current / value : current;
        };
    }

    private int calculateStacks(int current, int value, Operation operation) {
        return switch (operation) {
            case SET -> value;
            case ADD -> current + value;
            case SUBTRACT -> current - value;
            case MULTIPLY -> current * value;
            case DIVIDE -> (value != 0) ? current / value : current;
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "give", "permanent", "clear", "time", "stack", "list").stream()
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "give":
            case "add":
            case "permanent":
            case "perm":
            case "clear":
            case "remove":
            case "time":
            case "duration":
            case "stack":
            case "stacks":
                return tabCompleteEffectCommands(sender, args, subCommand);
            case "list":
                return tabCompleteListCommand(sender, args);
            default:
                return Collections.emptyList();
        }
    }

    private List<String> tabCompleteEffectCommands(CommandSender sender, String[] args, String subCommand) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && !subCommand.equalsIgnoreCase("clear") && !subCommand.equalsIgnoreCase("remove")) {
            return plugin.getEffectManager().keys().stream()
                    .map(NamespacedKey::toString)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if ((subCommand.equalsIgnoreCase("clear") || subCommand.equalsIgnoreCase("remove")) && args.length == 3) {
            List<String> options = new ArrayList<>(plugin.getEffectManager().keys().stream()
                    .map(NamespacedKey::toString).collect(Collectors.toList()));
            options.addAll(Arrays.asList("all", "permanent"));
            return options.stream()
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if ((subCommand.equalsIgnoreCase("give") || subCommand.equalsIgnoreCase("add")) && args.length == 4) {
            return Arrays.asList("100", "600", "1200").stream()
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[3].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if ((subCommand.equalsIgnoreCase("time") || subCommand.equalsIgnoreCase("duration") || subCommand.equalsIgnoreCase("stack") || subCommand.equalsIgnoreCase("stacks")) && args.length == 4) {
            return Arrays.stream(Operation.values())
                    .map(op -> op.name().toLowerCase(Locale.ROOT))
                    .filter(op -> op.startsWith(args[3].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if ((subCommand.equalsIgnoreCase("give") || subCommand.equalsIgnoreCase("add")) && args.length == 5) {
            return Arrays.stream(Modifier.values())
                    .map(modifier -> modifier.name().toLowerCase(Locale.ROOT))
                    .filter(op -> op.startsWith(args[4].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if ((subCommand.equalsIgnoreCase("give") || subCommand.equalsIgnoreCase("add")) && (args.length == 6 || args.length == 7)) { // Stacks and Stack Modifier
            return Arrays.asList("1", "2", "3", "4", "5").stream()
                    .filter(op -> op.startsWith(args[args.length - 1].toLowerCase(Locale.ROOT))) // Handle stack or modifier
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> tabCompleteListCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    enum Operation {
        SET,
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }
}