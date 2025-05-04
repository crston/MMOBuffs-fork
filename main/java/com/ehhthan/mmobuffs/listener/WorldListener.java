package com.ehhthan.mmobuffs.listener;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.option.EffectOption;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            removeByOption(player, EffectOption.KEEP_ON_DEATH);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        removeByOption(event.getPlayer(), EffectOption.KEEP_ON_WORLD_CHANGE);
    }

    private void removeByOption(Player player, EffectOption option) {
        if (!EffectHolder.has(player)) return;

        EffectHolder holder = EffectHolder.get(player);
        var toRemove = holder.getEffects(true).stream()
                .filter(effect -> !effect.getStatusEffect().getOption(option))
                .map(effect -> effect.getStatusEffect().getKey())
                .toList();

        toRemove.forEach(holder::removeEffect);
    }
}
