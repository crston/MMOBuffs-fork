package com.ehhthan.mmobuffs.api.effect;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@FunctionalInterface
public interface Resolver {
    TagResolver getResolver();
}