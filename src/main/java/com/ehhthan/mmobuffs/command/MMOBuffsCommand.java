package com.ehhthan.mmobuffs.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
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

import java.util.ArrayList;
import java.util.List;

@CommandAlias("mmobuffs|mmobuff|buffs|buff")
@Description("Main mmobuffs command.")
public class MMOBuffsCommand extends BaseCommand {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final MMOBuffs plugin;
    private final LanguageManager lang;
    private final ParserManager parser;

    public MMOBuffsCommand(MMOBuffs plugin, LanguageManager lang, ParserManager parser) {
        this.plugin = plugin;
        this.lang = lang;
        this.parser = parser;
    }

    @Subcommand("reload")
    @CommandPermission("mmobuffs.reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        send(lang.getMessage("reload-command"), sender);
    }

    @Subcommand("give|add")
    @CommandPermission("mmobuffs.give")
    @CommandCompletion("@players @effects @range:1-9 * @range:1-9 * -s")
    @Syntax("<player> <effect> <duration> [duration-modifier] [stacks] [stack-modifier] [-s]")
    public void give(CommandSender sender,
                     EffectHolder holder,
                     StatusEffect effect,
                     Integer duration,
                     Modifier durationMod,
                     Integer stacks,
                     Modifier stackMod,
                     @Default String silent) {
        ActiveStatusEffect eff = ActiveStatusEffect.builder(effect)
                .startDuration(duration)
                .startStacks(stacks)
                .build();
        holder.addEffect(eff, durationMod, stackMod);
        respond(sender, "give-effect", eff, holder, silent);
    }

    @Subcommand("permanent|perm")
    @CommandPermission("mmobuffs.permanent")
    @CommandCompletion("@players @effects * @range:1-9 * -s")
    @Syntax("<player> <effect> [duration-modifier] [stacks] [stack-modifier] [-s]")
    public void perm(CommandSender sender,
                     EffectHolder holder,
                     StatusEffect effect,
                     Modifier durationMod,
                     Integer stacks,
                     Modifier stackMod,
                     @Default String silent) {
        ActiveStatusEffect eff = ActiveStatusEffect.builder(effect)
                .permanent(true)
                .startStacks(stacks)
                .build();
        holder.addEffect(eff, durationMod, stackMod);
        respond(sender, "give-effect-permanent", eff, holder, silent);
    }

    @Subcommand("clear|remove")
    @CommandPermission("mmobuffs.clear")
    @Syntax("<player> <effect|all|permanent> [-s]")
    public void clear(CommandSender sender, EffectHolder holder, String option, @Default String silent) {
        TagResolver.Single playerTag = Placeholder.component("player", holder.getPlayer().displayName());
        Component msg;

        switch (option.toLowerCase()) {
            case "all" -> {
                holder.removeEffects(false);
                msg = lang.getMessage("clear-all-effects", true, playerTag);
            }
            case "permanent" -> {
                holder.removeEffects(true);
                msg = lang.getMessage("clear-permanent-effects", true, playerTag);
            }
            default -> {
                NamespacedKey key = NamespacedKey.fromString(option, plugin);
                if (key == null || !holder.hasEffect(key)) {
                    throw new InvalidCommandArgument("Invalid effect.");
                }
                ActiveStatusEffect eff = holder.getEffect(key);
                holder.removeEffect(key);
                msg = lang.getMessage(
                        "clear-effect",
                        true,
                        TagResolver.builder()
                                .resolver(playerTag)
                                .resolver(eff.getResolver())
                                .build()
                );
            }
        }

        if (!"-s".equalsIgnoreCase(silent)) {
            send(msg, sender);
        }
    }

    @Subcommand("time")
    @CommandPermission("mmobuffs.time")
    public void time(CommandSender sender,
                     EffectHolder holder,
                     StatusEffect effect,
                     Operation op,
                     int value,
                     @Default String silent) {
        ActiveStatusEffect eff = holder.getEffect(effect.getKey());
        if (eff == null) {
            throw new InvalidCommandArgument("Effect not found.");
        }
        eff.setDuration(op.apply(eff.getDuration(), value));
        respond(sender, "time-effect", eff, holder, silent);
    }

    @Subcommand("stack")
    @CommandPermission("mmobuffs.stack")
    public void stack(CommandSender sender,
                      EffectHolder holder,
                      StatusEffect effect,
                      Operation op,
                      int value,
                      @Default String silent) {
        ActiveStatusEffect eff = holder.getEffect(effect.getKey());
        if (eff == null) {
            throw new InvalidCommandArgument("Effect not found.");
        }
        eff.setStacks(op.apply(eff.getStacks(), value));
        holder.updateEffect(effect.getKey());
        respond(sender, "stack-effect", eff, holder, silent);
    }

    @Subcommand("list")
    @CommandPermission("mmobuffs.list")
    public void list(CommandSender sender, @Optional EffectHolder holder) {
        if (holder == null) {
            if (sender instanceof Player p && EffectHolder.has(p)) {
                holder = EffectHolder.get(p);
            } else {
                throw new InvalidCommandArgument("No player specified.");
            }
        }

        List<Component> lines = new ArrayList<>();
        Component header = lang.getMessage("list-display.header", false);
        if (header != null) {
            lines.add(header);
        }

        String template = lang.getString("list-display.effect-element");
        String parsedTemplate = parser.parse(holder.getPlayer(), template);

        for (ActiveStatusEffect effect : holder.getEffects(true)) {
            Component line = MM.deserialize(parsedTemplate, effect.getResolver());
            lines.add(line);
        }

        Component footer = lang.getMessage("list-display.footer", false);
        if (footer != null) {
            lines.add(footer);
        }

        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < lines.size(); i++) {
            builder.append(lines.get(i));
            if (i < lines.size() - 1) {
                builder.append(Component.newline());
            }
        }

        send(builder.build(), sender);
    }

    @CatchUnknown
    public void onUnknown(CommandSender sender) {
        send(lang.getMessage("unknown-command"), sender);
    }

    private void respond(CommandSender sender, String key, ActiveStatusEffect eff, EffectHolder holder, String silent) {
        if (!"-s".equalsIgnoreCase(silent)) {
            send(
                    lang.getMessage(
                            key,
                            true,
                            TagResolver.builder()
                                    .resolver(eff.getResolver())
                                    .resolver(Placeholder.component("player", holder.getPlayer().displayName()))
                                    .build()
                    ),
                    sender
            );
        }
    }

    private void send(Component msg, CommandSender sender) {
        if (msg != null) {
            sender.sendMessage(msg);
        }
    }

    enum Operation {
        SET, ADD, SUB, MUL, DIV;

        public int apply(int cur, int val) {
            return switch (this) {
                case SET -> val;
                case ADD -> cur + val;
                case SUB -> cur - val;
                case MUL -> cur * val;
                case DIV -> (val == 0 ? cur : cur / val);
            };
        }
    }
}
