package com.ehhthan.mmobuffs.command;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.effect.StatusEffect;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MMOBuffsCommand implements CommandExecutor, TabCompleter {

    private final MMOBuffs plugin;

    public MMOBuffsCommand(MMOBuffs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        return switch (subCommand) {
            case "reload" -> reloadCommand(sender);
            case "give", "add" -> giveCommand(sender, args);
            case "permanent", "perm" -> permanentCommand(sender, args);
            case "clear", "remove" -> clearCommand(sender, args);
            case "time", "duration" -> timeCommand(sender, args);
            case "stack", "stacks" -> stackCommand(sender, args);
            case "list" -> listCommand(sender, args);
            default -> {
                sendUnknownCommandMessage(sender);
                yield true;
            }
        };
    }

    private boolean reloadCommand(CommandSender sender) {
        if (!sender.hasPermission("mmobuffs.reload")) {
            sendNoPermissionMessage(sender);
            return true;
        }
        if (!plugin.reload()) return false;
        sender.sendMessage(Component.text("MMOBuffs reloaded!"));
        return true;
    }

    private boolean giveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmobuffs.give")) {
            sendNoPermissionMessage(sender);
            return true;
        }

        if (args.length < 4) {
            sendUsageMessage(sender, "give <player> <effect> <duration> [duration-modifier] [stacks] [stack-modifier] [-s]");
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return false;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sendInvalidEffectKeyMessage(sender);
            return false;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sendEffectNotFoundMessage(sender);
            return false;
        }

        try {
            int duration = Integer.parseInt(args[3]);
            Modifier durationModifier = Modifier.SET;
            int stacks = 1;
            Modifier stackModifier = Modifier.KEEP;
            boolean silent = false;

            // Parse optional arguments
            int argIndex = 4;
            if (args.length > argIndex) {
                try {
                    durationModifier = parseModifier(args[argIndex]);
                    argIndex++;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Component.text("Invalid duration modifier: " + e.getMessage()));
                    return false;
                }
            }
            if (args.length > argIndex) {
                try {
                    stacks = Integer.parseInt(args[argIndex]);
                    argIndex++;
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Invalid stack number. Please use integers."));
                    return false;
                }
            }
            if (args.length > argIndex) {
                try {
                    stackModifier = parseModifier(args[argIndex]);
                    argIndex++;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Component.text("Invalid stack modifier: " + e.getMessage()));
                    return false;
                }
            }
            if (args.length > argIndex) {
                silent = args[argIndex].equalsIgnoreCase("-s");
            }

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = ActiveStatusEffect.builder(effect).startDuration(duration).startStacks(stacks).build();
            holder.addEffect(activeEffect, durationModifier, stackModifier);

            if (!silent) {
                sendEffectMessage(sender, target, "give-effect");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid duration number. Please use integers."));
            return false;
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
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return false;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sendInvalidEffectKeyMessage(sender);
            return false;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sendEffectNotFoundMessage(sender);
            return false;
        }

        try {
            int stacks = 1;
            Modifier stackModifier = Modifier.KEEP;
            boolean silent = false;

            // Parse optional arguments
            int argIndex = 3;
            if (args.length > argIndex) {
                try {
                    stacks = Integer.parseInt(args[argIndex]);
                    argIndex++;
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Invalid stack number. Please use integers."));
                    return false;
                }
            }
            if (args.length > argIndex) {
                try {
                    stackModifier = parseModifier(args[argIndex]);
                    argIndex++;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Component.text("Invalid stack modifier: " + e.getMessage()));
                    return false;
                }
            }
            if (args.length > argIndex) {
                silent = args[argIndex].equalsIgnoreCase("-s");
            }

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = ActiveStatusEffect.builder(effect).permanent(true).startStacks(stacks).build();
            holder.addEffect(activeEffect, Modifier.SET, stackModifier);

            if (!silent) {
                sendEffectMessage(sender, target, "give-effect-permanent");
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid argument: " + e.getMessage()));
            return false;
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
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return false;
        }

        String choice = args[2].toLowerCase(Locale.ROOT);
        boolean silent = (args.length > 3) && args[3].equalsIgnoreCase("-s");
        EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);

        switch (choice) {
            case "all":
                holder.removeEffects(false);
                sendEffectMessage(sender, target, "clear-all-effects");
                break;
            case "permanent":
                holder.removeEffects(true);
                sendEffectMessage(sender, target, "clear-permanent-effects");
                break;
            default:
                NamespacedKey key = NamespacedKey.fromString(choice, plugin);
                if (key == null) {
                    sendInvalidEffectKeyMessage(sender);
                    return false;
                }
                if (!holder.hasEffect(key)) {
                    sendEffectNotPresentMessage(sender);
                    return false;
                }
                holder.removeEffect(key);
                sendEffectMessage(sender, target, "clear-effect");
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
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return false;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sendInvalidEffectKeyMessage(sender);
            return false;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sendEffectNotFoundMessage(sender);
            return false;
        }

        try {
            Operation operation = parseOperation(args[3]);
            int duration = Integer.parseInt(args[4]);

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = holder.getEffect(effectKey);

            if (activeEffect == null) {
                sendEffectNotPresentMessage(sender);
                return false;
            }

            int newDuration = calculateDuration(activeEffect.getDuration(), duration, operation);
            activeEffect.setDuration(newDuration);

            sender.sendMessage(Component.text("Set " + target.getName() + "'s " + effect.getKey().getKey() + " duration to " + newDuration + "!"));

        }  catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid duration number. Please use integers."));
            return false;
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid argument: " + e.getMessage()));
            return false;
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
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendPlayerNotFoundMessage(sender);
            return false;
        }

        NamespacedKey effectKey = NamespacedKey.fromString(args[2], plugin);
        if (effectKey == null) {
            sendInvalidEffectKeyMessage(sender);
            return false;
        }

        StatusEffect effect = plugin.getEffectManager().get(effectKey);
        if (effect == null) {
            sendEffectNotFoundMessage(sender);
            return false;
        }

        try {
            Operation operation = parseOperation(args[3]);
            int stacks = Integer.parseInt(args[4]);

            EffectHolder holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
            ActiveStatusEffect activeEffect = holder.getEffect(effectKey);

            if (activeEffect == null) {
                sendEffectNotPresentMessage(sender);
                return false;
            }

            int newStacks = calculateStacks(activeEffect.getStacks(), stacks, operation);
            activeEffect.setStacks(newStacks);
            holder.updateEffect(effectKey);

            sender.sendMessage(Component.text("Set " + target.getName() + "'s " + effect.getKey().getKey() + " stacks to " + newStacks + "!"));

        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid stack number. Please use integers."));
            return false;
        }
        catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid argument: " + e.getMessage()));
            return false;
        }

        return true;
    }

    private boolean listCommand(CommandSender sender, String[] args) {
        EffectHolder holder;
        if (args.length > 1) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sendPlayerNotFoundMessage(sender);
                return false;
            }
            holder = EffectHolder.DATA.computeIfAbsent(target, EffectHolder::new);
        } else if (sender instanceof Player player) {
            holder = EffectHolder.DATA.computeIfAbsent(player, EffectHolder::new);
        } else {
            sendUsageMessage(sender, "list [player]");
            return false;
        }

        List<Component> components = new LinkedList<>();
        components.add(Component.text("--- " + (holder.getPlayer() != null ? holder.getPlayer().getName() : "Console") + "'s Effects ---"));

        for (ActiveStatusEffect activeEffect : holder.getEffects(true)) {
            components.add(Component.text(activeEffect.getStatusEffect().getKey().getKey() + " - Duration: " + activeEffect.getDuration() + ", Stacks: " + activeEffect.getStacks()));
        }

        components.add(Component.text("--- End of Effects ---"));

        final JoinConfiguration config = JoinConfiguration.builder().prefix(Component.empty()).suffix(Component.empty()).separator(Component.newline()).build();
        sender.sendMessage(Component.join(config, components));
        return true;
    }

    // Helper Methods for Messages
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("Use /mmobuffs help for help."));
    }

    private void sendUnknownCommandMessage(CommandSender sender) {
        sender.sendMessage(Component.text("Unknown command. Use /mmobuffs help."));
    }

    private void sendNoPermissionMessage(CommandSender sender) {
        sender.sendMessage(Component.text("You don't have permission to use this command."));
    }

    private void sendPlayerNotFoundMessage(CommandSender sender) {
        sender.sendMessage(Component.text("Player not found."));
    }

    private void sendInvalidEffectKeyMessage(CommandSender sender) {
        sender.sendMessage(Component.text("Invalid effect key."));
    }

    private void sendEffectNotFoundMessage(CommandSender sender) {
        sender.sendMessage(Component.text("Effect not found."));
    }

    private void sendEffectNotPresentMessage(CommandSender sender) {
        sender.sendMessage(Component.text("Effect not present on the player."));
    }

    private void sendUsageMessage(CommandSender sender, String usage) {
        sender.sendMessage(Component.text("Usage: /mmobuffs " + usage));
    }

    private void sendEffectMessage(CommandSender sender, Player target, String messageKey) {
        switch (messageKey) {
            case "give-effect" -> sender.sendMessage(Component.text("Given " + target.getName() + " the effect!"));
            case "give-effect-permanent" ->
                    sender.sendMessage(Component.text("Given " + target.getName() + " the permanent effect!"));
            case "clear-all-effects" ->
                    sender.sendMessage(Component.text("Cleared all effects from " + target.getName() + "!"));
            case "clear-permanent-effects" ->
                    sender.sendMessage(Component.text("Cleared all permanent effects from " + target.getName() + "!"));
            case "clear-effect" ->
                    sender.sendMessage(Component.text("Cleared the effect from " + target.getName() + "!"));
        }
    }

    // Helper Methods for Parsing and Calculation
    private Modifier parseModifier(String input) {
        try {
            return Modifier.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid modifier. Valid options: SET, ADD, SUBTRACT, MULTIPLY, DIVIDE");
        }
    }

    private Operation parseOperation(String input) {
        try {
            return Operation.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid operation. Valid options: SET, ADD, SUBTRACT, MULTIPLY, DIVIDE");
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
            return Stream.of("reload", "give", "permanent", "clear", "time", "stack", "list")
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        return switch (subCommand) {
            case "give", "add", "permanent", "perm", "clear", "remove", "time", "duration", "stack", "stacks" ->
                    tabCompleteEffectCommands(sender, args, subCommand);
            case "list" -> tabCompleteListCommand(sender, args);
            default -> Collections.emptyList();
        };
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
            List<String> options = plugin.getEffectManager().keys().stream()
                    .map(NamespacedKey::toString).collect(Collectors.toList());
            options.addAll(Arrays.asList("all", "permanent"));
            return options.stream()
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if ((subCommand.equalsIgnoreCase("give") || subCommand.equalsIgnoreCase("add")) && args.length == 4) {
            return Stream.of("100", "600", "1200")
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[3].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else if ((subCommand.equalsIgnoreCase("time") || subCommand.equalsIgnoreCase("duration") || (subCommand.equalsIgnoreCase("stack") || subCommand.equalsIgnoreCase("stacks")) && args.length == 4) ) {
            return Arrays.stream(Operation.values())
                    .map(op -> op.name().toLowerCase(Locale.ROOT))
                    .filter(op -> op.startsWith(args[3].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        // Corrected logic for give/add commands
        else if ((subCommand.equalsIgnoreCase("give") || subCommand.equalsIgnoreCase("add"))) {
            if (args.length == 5) {
                // Suggest duration modifiers
                return Arrays.stream(Modifier.values())
                        .map(modifier -> modifier.name().toLowerCase(Locale.ROOT))
                        .filter(op -> op.startsWith(args[4].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            } else if (args.length == 6) {
                // Suggest stack values
                return Stream.of("1", "2", "3", "4", "5")
                        .filter(op -> op.startsWith(args[5].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            } else if (args.length == 7) {
                // Suggest stack modifiers
                return Arrays.stream(Modifier.values())
                        .map(modifier -> modifier.name().toLowerCase(Locale.ROOT))
                        .filter(op -> op.startsWith(args[6].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            }
        }
        // Corrected logic for permanent commands
        else if ((subCommand.equalsIgnoreCase("permanent") || subCommand.equalsIgnoreCase("perm"))) {
            if (args.length == 4) {
                // Suggest stack values
                return Stream.of("1", "2", "3", "4", "5")
                        .filter(op -> op.startsWith(args[3].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            } else if (args.length == 5) {
                // Suggest stack modifiers
                return Arrays.stream(Modifier.values())
                        .map(modifier -> modifier.name().toLowerCase(Locale.ROOT))
                        .filter(op -> op.startsWith(args[4].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            }
        }
        else {
            return Collections.emptyList();
        }
        return List.of();
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