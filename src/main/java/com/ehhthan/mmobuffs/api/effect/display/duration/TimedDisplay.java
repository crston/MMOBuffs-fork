package com.ehhthan.mmobuffs.api.effect.display.duration;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class TimedDisplay implements DurationDisplay {
    private static final TimeType[] TYPES = TimeType.values();

    private static final Function<Duration, Component> LONG_FORMAT = duration -> {
        List<Component> parts = new ArrayList<>();
        for (TimeType type : TYPES) {
            Component part = type.format(duration);
            if (!part.equals(Component.empty())) {
                parts.add(part);
            }
        }

        Component separator = MMOBuffs.getInst().getLanguageManager().getMessage("duration-display.separator", false);
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < parts.size(); i++) {
            builder.append(parts.get(i));
            if (separator != null && i < parts.size() - 1) {
                builder.append(separator);
            }
        }
        return builder.build();
    };

    private static final Function<Duration, Component> SHORT_FORMAT = duration -> {
        for (TimeType type : TYPES) {
            Component result = type.format(duration);
            if (!result.equals(Component.empty())) {
                return result;
            }
        }
        return Component.empty();
    };

    private final ActiveStatusEffect effect;

    public TimedDisplay(ActiveStatusEffect effect) {
        this.effect = effect;
    }

    @Override
    public Component display() {
        Duration duration = Duration.ofSeconds(effect.getDuration());
        boolean shorten = MMOBuffs.getInst().getConfig().getBoolean("shorten-duration-display", true);
        return shorten ? SHORT_FORMAT.apply(duration) : LONG_FORMAT.apply(duration);
    }

    private enum TimeType {
        DAYS {
            int extract(Duration d) { return (int) d.toDaysPart(); }
        },
        HOURS {
            int extract(Duration d) { return d.toHoursPart(); }
        },
        MINUTES {
            int extract(Duration d) { return d.toMinutesPart(); }
        },
        SECONDS {
            int extract(Duration d) { return d.toSecondsPart(); }
        };

        private final String id = name().toLowerCase(Locale.ROOT);

        abstract int extract(Duration duration);

        Component format(Duration duration) {
            int value = extract(duration);
            return value > 0
                    ? MMOBuffs.getInst().getLanguageManager().getMessage(
                    "duration-display." + id,
                    false,
                    Placeholder.parsed("value", String.valueOf(value))
            )
                    : Component.empty();
        }
    }
}