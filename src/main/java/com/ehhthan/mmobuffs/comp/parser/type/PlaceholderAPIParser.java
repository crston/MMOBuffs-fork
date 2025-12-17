package com.ehhthan.mmobuffs.comp.parser.type;

import com.ehhthan.mmobuffs.comp.parser.Parser;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIParser implements Parser {

    @Override
    public @NotNull String parse(@NotNull Player player, @NotNull String text) {
        return player.isOnline() ? PlaceholderAPI.setPlaceholders(player, text) : text;
    }

    @Override
    public boolean containsPlaceholders(@NotNull String text) {
        return PlaceholderAPI.containsPlaceholders(text);
    }
}