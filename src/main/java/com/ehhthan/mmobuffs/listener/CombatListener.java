package com.ehhthan.mmobuffs.listener;

import com.ehhthan.mmobuffs.api.EffectHolder;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.effect.stack.StackType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim) {
            trigger(victim, StackType.HURT, StackType.COMBAT);
        }
        if (event.getDamager() instanceof Player attacker) {
            trigger(attacker, StackType.ATTACK, StackType.COMBAT);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof Player player) {
            trigger(player, StackType.HURT);
        }
    }

    private void trigger(Player player, StackType... types) {
        if (!EffectHolder.has(player)) {
            return;
        }

        EffectHolder holder = EffectHolder.get(player);

        outer:
        for (ActiveStatusEffect effect : holder.getEffects(true)) {
            StackType type = effect.getStatusEffect().getStackType();
            for (StackType t : types) {
                if (t == type) {
                    effect.triggerStack(type);
                    holder.updateEffect(effect.getStatusEffect().getKey());
                    continue outer;
                }
            }
        }
    }
}
