package com.ehhthan.mmobuffs.command;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.manager.type.LanguageManager;
import com.ehhthan.mmobuffs.manager.type.ParserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MMOBuffsCommand implements CommandExecutor {
    private final MMOBuffs plugin;
    private final LanguageManager language;
    private final ParserManager parser;

    public MMOBuffsCommand(MMOBuffs plugin, LanguageManager language, ParserManager parser) {
        this.plugin = plugin;
        this.language = language;
        this.parser = parser;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        sender.sendMessage("MMOBuffs command executed.");
        return true;
    }
}