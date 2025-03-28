package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.comp.parser.Parser;
import com.ehhthan.mmobuffs.manager.KeylessManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ParserManager extends KeylessManager<Parser> {
    public String parse(@NotNull Player player, @NotNull String text) {
        String parsedText = text;
        for (Parser parser : values()) {
            parsedText = parser.parse(player, parsedText);
        }
        return parsedText;
    }
}