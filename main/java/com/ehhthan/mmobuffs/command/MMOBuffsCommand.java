package com.ehhthan.mmobuffs.command;

import co.aikar.commands.*;
import co.aikar.commands.annotation.*;
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
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

@CommandAlias("mmobuffs|mmobuff|buffs|buff")
@Description("Main mmobuffs command.")
public class MMOBuffsCommand extends BaseCommand {

    private final MMOBuffs plugin;
    private final LanguageManager language;
    private final ParserManager parser;

    public MMOBuffsCommand(MMOBuffs plugin, LanguageManager language, ParserManager parser) {
        this.plugin = plugin;
        this.language = language;
        this.parser = parser;
    }

    @Subcommand("reload")
    @CommandPermission("mmobuffs.reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        Component message = language.getMessage("reload-command");
        if (message != null) sender.sendMessage(message);
    }

    @Subcommand("give|add")
    @CommandPermission("mmobuffs.give")
    @CommandCompletion("@players @effects @range:1-9 * @range:1-9 * -s")
    @Syntax("<player> <effect> <duration> [duration-modifier] [stacks] [stack-modifier] [-s]")
    public void give(CommandSender sender, EffectHolder holder, StatusEffect effect, Integer duration,
                     @Default("SET") Modifier durationMod, @Default("1") Integer stacks,
                     @Default("KEEP") Modifier stackMod, @Default String silent) {

        var effectInstance = ActiveStatusEffect.builder(effect)
                .startDuration(duration)
                .startStacks(stacks)
                .build();

        holder.addEffect(effectInstance, durationMod, stackMod);
        sendMessageIfNotSilent(sender, "give-effect", effectInstance, holder, silent);
    }

    @Subcommand("permanent|perm")
    @CommandPermission("mmobuffs.permanent")
    @CommandCompletion("@players @effects * @range:1-9 * -s")
    @Syntax("<player> <effect> [duration-modifier] [stacks] [stack-modifier] [-s]")
    public void givePermanent(CommandSender sender, EffectHolder holder, StatusEffect effect,
                              @Default("REPLACE") Modifier durationMod,
                              @Default("1") Integer stacks,
                              @Default("KEEP") Modifier stackMod, @Default String silent) {

        var effectInstance = ActiveStatusEffect.builder(effect)
                .permanent(true)
                .startStacks(stacks)
                .build();

        holder.addEffect(effectInstance, durationMod, stackMod);
        sendMessageIfNotSilent(sender, "give-effect-permanent", effectInstance, holder, silent);
    }

    @Subcommand("clear|remove")
    @CommandPermission("mmobuffs.clear")
    @Syntax("<player> <effect|all|permanent> [-s]")
    public void clear(CommandSender sender, EffectHolder holder, String option, @Default String silent) {
        TagResolver.Single playerTag = Placeholder.component("player", holder.getPlayer().displayName());
        Component message;

        switch (option.toLowerCase()) {
            case "all" -> {
                holder.removeEffects(false);
                message = language.getMessage("clear-all-effects", true, playerTag);
            }
            case "permanent" -> {
                holder.removeEffects(true);
                message = language.getMessage("clear-permanent-effects", true, playerTag);
            }
            default -> {
                NamespacedKey key = NamespacedKey.fromString(option, plugin);
                if (key == null || !holder.hasEffect(key))
                    throw new InvalidCommandArgument("Invalid effect option specified.");

                var effect = holder.getEffect(key);
                holder.removeEffect(key);

                message = language.getMessage("clear-effect", true, TagResolver.builder()
                        .resolver(playerTag)
                        .resolver(effect.getResolver())
                        .build());
            }
        }

        if (message != null && !silent.equalsIgnoreCase("-s"))
            sender.sendMessage(message);
    }

    @Subcommand("time|duration")
    @CommandPermission("mmobuffs.time")
    public void modifyTime(CommandSender sender, EffectHolder holder, StatusEffect effect, Operation op, int value, @Default String silent) {
        var effectInstance = holder.getEffect(effect.getKey());
        if (effectInstance == null) throw new InvalidCommandArgument("Effect not found.");

        int result = applyOperation(effectInstance.getDuration(), value, op);
        effectInstance.setDuration(result);

        sendMessageIfNotSilent(sender, "time-effect", effectInstance, holder, silent);
    }

    @Subcommand("stack|stacks")
    @CommandPermission("mmobuffs.stack")
    public void modifyStacks(CommandSender sender, EffectHolder holder, StatusEffect effect, Operation op, int value, @Default String silent) {
        var effectInstance = holder.getEffect(effect.getKey());
        if (effectInstance == null) throw new InvalidCommandArgument("Effect not found.");

        int result = applyOperation(effectInstance.getStacks(), value, op);
        effectInstance.setStacks(result);
        holder.updateEffect(effect.getKey());

        sendMessageIfNotSilent(sender, "stack-effect", effectInstance, holder, silent);
    }

    @Subcommand("list")
    @CommandPermission("mmobuffs.list")
    public void list(CommandSender sender, @Optional EffectHolder holder) {
        if (holder == null) {
            if (sender instanceof Player player && EffectHolder.has(player)) {
                holder = EffectHolder.get(player);
            } else {
                throw new InvalidCommandArgument("No player specified.");
            }
        }

        List<Component> lines = new LinkedList<>();
        lines.add(language.getMessage("list-display.header", false));

        String template = language.getString("list-display.effect-element");
        for (var effect : holder.getEffects(true)) {
            lines.add(MiniMessage.miniMessage().deserialize(
                    parser.parse(holder.getPlayer(), template),
                    effect.getResolver()
            ));
        }

        lines.add(language.getMessage("list-display.footer", false));

        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < lines.size(); i++) {
            builder.append(lines.get(i));
            if (i < lines.size() - 1) builder.append(Component.newline());
        }

        sender.sendMessage(builder.build());
    }

    @CatchUnknown
    public void onUnknown(CommandSender sender) {
        Component message = language.getMessage("unknown-command");
        if (message != null) sender.sendMessage(message);
    }

    private void sendMessageIfNotSilent(CommandSender sender, String key, ActiveStatusEffect effect, EffectHolder holder, String silent) {
        if (silent.equalsIgnoreCase("-s")) return;
        Component message = language.getMessage(key, true,
                TagResolver.builder()
                        .resolver(effect.getResolver())
                        .resolver(Placeholder.component("player", holder.getPlayer().displayName()))
                        .build());
        if (message != null) sender.sendMessage(message);
    }

    private int applyOperation(int current, int value, Operation op) {
        return switch (op) {
            case SET -> value;
            case ADD -> current + value;
            case SUBTRACT -> current - value;
            case MULTIPLY -> current * value;
            case DIVIDE -> {
                if (value == 0) throw new InvalidCommandArgument("Cannot divide by zero.");
                yield current / value;
            }
        };
    }

    enum Operation {
        SET, ADD, SUBTRACT, MULTIPLY, DIVIDE
    }
}
