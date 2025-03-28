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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CombatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player p) {
            call(p, StackType.HURT, StackType.COMBAT);
        }
        if (event.getDamager() instanceof Player p) {
            call(p, StackType.ATTACK, StackType.COMBAT);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof Player p) {
            call(p, StackType.HURT);
        }
    }

    private void call(Player player, StackType... types) {
        if (EffectHolder.DATA.containsKey(player)) {
            EffectHolder holder = EffectHolder.DATA.get(player);
            List<StackType> typeList = Arrays.asList(types);
            List<ActiveStatusEffect> effectsToUpdate = new ArrayList<>();

            Collection<ActiveStatusEffect> effects = holder.getEffects(true);
            for (ActiveStatusEffect effect : effects) {
                StackType type = effect.getStatusEffect().getStackType();
                if (typeList.contains(type)) {
                    effect.triggerStack(type);
                    effectsToUpdate.add(effect);
                }
            }

            effectsToUpdate.forEach(effect -> holder.updateEffect(effect.getStatusEffect().getKey()));
        }
    }
}