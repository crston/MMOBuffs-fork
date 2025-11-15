package com.ehhthan.mmobuffs.comp.parser;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Parser {
    String parse(@NotNull Player player, @NotNull String text);

    default boolean containsPlaceholders(@NotNull String text) {
        return false;
    }
}
