package com.ehhthan.mmobuffs.api.effect.display;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class EffectDisplay {

    private final String icon;
    private final String text;

    public EffectDisplay(@NotNull ConfigurationSection section) {
        this.icon = section.getString("icon", "");
        this.text = section.getString("text", "<icon> <duration>");
    }

    public String getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public Component build(@NotNull Player player, @NotNull ActiveStatusEffect effect) {
        String parsedIcon = StringEscapeUtils.unescapeJava(icon);
        String parsedText = StringEscapeUtils.unescapeJava(text);

        TagResolver resolver = TagResolver.builder()
                .resolver(effect.getResolver())
                .resolver(Placeholder.parsed("icon", parsedIcon))
                .build();

        String parsedWithPlaceholders = MMOBuffs.getInst().getParserManager().parse(player, parsedText);

        Component component = MiniMessage.miniMessage().deserialize(parsedWithPlaceholders, resolver);

        FileConfiguration config = MMOBuffs.getInst().getConfig();
        if (config.getBoolean("resource-pack.enabled")) {
            String font = config.getString("resource-pack.font", "mmobuffs:default");
            component = component.style(component.style().font(Key.key(font)));
        }

        return component;
    }
}
