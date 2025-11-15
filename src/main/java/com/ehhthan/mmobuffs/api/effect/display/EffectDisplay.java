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

public class EffectDisplay {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final String icon;
    private final String text;
    private final boolean useFont;
    private final String fontKey;

    public EffectDisplay(@NotNull ConfigurationSection section) {
        this.icon = StringEscapeUtils.unescapeJava(section.getString("icon", ""));
        this.text = StringEscapeUtils.unescapeJava(section.getString("text", "<icon> <duration>"));

        FileConfiguration config = MMOBuffs.getInst().getConfig();
        this.useFont = config.getBoolean("resource-pack.enabled");
        this.fontKey = config.getString("resource-pack.font", "mmobuffs:default");
    }

    public String getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public Component build(@NotNull Player player, @NotNull ActiveStatusEffect effect) {
        TagResolver resolver = TagResolver.builder()
                .resolver(effect.getResolver())
                .resolver(Placeholder.parsed("icon", icon))
                .build();

        String parsed = MMOBuffs.getInst().getParserManager().parse(player, text);
        Component component = MM.deserialize(parsed, resolver);

        if (useFont) {
            component = component.style(component.style().font(Key.key(fontKey)));
        }

        return component;
    }
}
