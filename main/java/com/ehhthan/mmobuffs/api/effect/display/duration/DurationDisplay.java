package com.ehhthan.mmobuffs.api.effect.display.duration;

import net.kyori.adventure.text.Component;

public interface DurationDisplay {
    DurationDisplay PERMANENT = () -> Component.text("Permanent");

    Component display();
}