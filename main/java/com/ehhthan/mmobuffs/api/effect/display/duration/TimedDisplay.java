package com.ehhthan.mmobuffs.api.effect.display.duration;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class TimedDisplay implements DurationDisplay {
    private static final Function<Duration, Component> LONG_DISPLAY_FORMAT = (duration) -> {
        List<Component> components = new LinkedList<>();
        for (TimeType type : TimeType.values()) {
            Component format = type.format(duration);
            if (format != Component.empty())
                components.add(format);
        }

        Component separator = Component.text(", "); // Default separator

        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < components.size(); i++) {
            builder.append(components.get(i));
            if (i != components.size() - 1) {
                builder.append(separator);
            }
        }

        return builder.build();
    };

    private static final Function<Duration, Component> SHORT_DISPLAY_FORMAT = (duration) -> {
        for (TimeType type : TimeType.values()) {
            Component format = type.format(duration);
            if (format != Component.empty())
                return format;
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
        return (MMOBuffs.getInst().getConfig().getBoolean("shorten-duration-display", true))
                ? SHORT_DISPLAY_FORMAT.apply(duration)
                : LONG_DISPLAY_FORMAT.apply(duration);
    }

    enum TimeType {
        DAYS {
            @Override
            int partFromDuration(Duration duration) {
                return (int) duration.toDaysPart();
            }
        },
        HOURS {
            int partFromDuration(Duration duration) {
                return duration.toHoursPart();
            }
        },
        MINUTES {
            @Override
            int partFromDuration(Duration duration) {
                return duration.toMinutesPart();
            }
        },
        SECONDS {
            @Override
            int partFromDuration(Duration duration) {
                return duration.toSecondsPart();
            }
        };


        int partFromDuration(Duration duration) {
            throw new IllegalArgumentException("This method should be overridden.");
        }

        Component format(Duration duration) {
            int value = partFromDuration(duration);
            if (value > 0) {
                // Remove the unit string
                return Component.text(String.valueOf(value));
            } else {
                return Component.empty();
            }
        }
    }
}