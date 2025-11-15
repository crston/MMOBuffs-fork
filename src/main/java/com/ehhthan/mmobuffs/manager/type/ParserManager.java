package com.ehhthan.mmobuffs.manager.type;

import com.ehhthan.mmobuffs.comp.parser.Parser;
import com.ehhthan.mmobuffs.manager.KeylessManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ParserManager extends KeylessManager<Parser> {

    public String parse(@NotNull Player player, @NotNull String text) {
        String result = text;
        for (Parser parser : managed) {
            String out = parser.parse(player, result);
            if (out != null) {
                result = out;
            }
        }
        return result;
    }

    public boolean hasPlaceholder(@NotNull String text) {
        for (Parser parser : managed) {
            if (parser.containsPlaceholders(text)) {
                return true;
            }
        }
        return false;
    }
}
