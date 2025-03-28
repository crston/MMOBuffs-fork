package com.ehhthan.mmobuffs.api;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import com.ehhthan.mmobuffs.api.tag.CustomTagTypes;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.ehhthan.mmobuffs.util.KeyUtil.key;

public class EffectHolder implements PersistentDataHolder {
    public static final Map<Player, EffectHolder> DATA = new ConcurrentHashMap<>();
    private static final NamespacedKey EFFECTS = key("effects");
    private static final Function<ConfigurationSection, BossBar> BAR_SUPPLIER = (section) -> {
        BossBar.Color color = BossBar.Color.NAMES.value(section.getString("color", "white").toLowerCase(Locale.ROOT));
        if (color == null)
            color = BossBar.Color.WHITE;

        BossBar.Overlay overlay = BossBar.Overlay.NAMES.value(section.getString("overlay", "progress").toLowerCase(Locale.ROOT));
        if (overlay == null)
            overlay = BossBar.Overlay.PROGRESS;

        return BossBar.bossBar(Component.empty(), section.getInt("value", 1), color, overlay);
    };
    private final Player player;
    private final Map<NamespacedKey, ActiveStatusEffect> effects = new HashMap<>();
    private final BossBar bossBar;
    private final Component separator;
    private final BukkitTask effectUpdater;
    private final BukkitTask bossBarUpdater;

    public EffectHolder(@NotNull Player player) {
        this.player = player;
        FileConfiguration config = MMOBuffs.getInst().getConfig();

        // Bossbar setup
        if (config.isConfigurationSection("bossbar-display") && config.getBoolean("bossbar-display.enabled", true)) {
            this.bossBar = BAR_SUPPLIER.apply(config.getConfigurationSection("bossbar-display"));
        } else {
            this.bossBar = null;
        }
        this.separator = Component.text(config.getString("bossbar-display.effect-separator", " "));

        loadSavedEffects();

        // Task scheduling
        this.bossBarUpdater = Bukkit.getScheduler().runTaskTimer(MMOBuffs.getInst(), this::updateBossBar, 2, config.getInt("bossbar-display.update-ticks", 20));
        this.effectUpdater = Bukkit.getScheduler().runTaskTimer(MMOBuffs.getInst(), this::tickEffects, 1, 20);

        if (bossBar != null && MMOBuffs.getInst().getConfig().getBoolean("bossbar-display.display-when-empty", false))
            player.showBossBar(bossBar);
    }

    private void loadSavedEffects() {
        PersistentDataContainer container = getPersistentDataContainer();
        if (container.has(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS)) {
            ActiveStatusEffect[] savedEffects = container.get(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS);
            if (savedEffects != null) {
                for (ActiveStatusEffect effect : savedEffects) {
                    if (effect != null) {
                        addEffect(effect, Modifier.SET, Modifier.SET);
                    }
                }
            }
        }
    }

    private void tickEffects() {
        if (!player.isOnline()) {
            cancelTasks();
            return;
        }

        Iterator<ActiveStatusEffect> iterator = effects.values().iterator();
        while (iterator.hasNext()) {
            ActiveStatusEffect effect = iterator.next();

            if (effect.tick())
                updateEffect(effect.getStatusEffect().getKey());

            if (!effect.isActive()) {
                removeStatModifiers(effect);
                iterator.remove();
            }
        }
        save();
    }

    private void updateBossBar() {
        if (bossBar == null) return;

        FileConfiguration config = MMOBuffs.getInst().getConfig();
        if (!config.getBoolean("bossbar-display.enabled", true)) {
            player.hideBossBar(bossBar);
            return;
        }

        List<ActiveStatusEffect> sortedEffects = new LinkedList<>(effects.values().stream()
                .filter(e -> e.getStatusEffect().hasDisplay())
                .sorted()
                .toList());

        if (sortedEffects.isEmpty()) {
            if (!config.getBoolean("bossbar-display.display-when-empty", false)) {
                player.hideBossBar(bossBar);
            } else {
                bossBar.name(Component.empty());
            }
            return;
        }

        if (!config.getBoolean("sorting.duration-ascending", true))
            Collections.reverse(sortedEffects);

        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < sortedEffects.size(); i++) {
            if (i > 0) {
                builder.append(separator);
            }
            ActiveStatusEffect effect = sortedEffects.get(i);
            builder.append(Objects.requireNonNull(effect.getStatusEffect().getDisplay()).build(player, effect));
        }

        bossBar.name(builder.build());
        player.showBossBar(bossBar);
    }

    private void cancelTasks() {
        Bukkit.getScheduler().cancelTask(effectUpdater.getTaskId());
        Bukkit.getScheduler().cancelTask(bossBarUpdater.getTaskId());
    }

    public Player getPlayer() {
        return player;
    }

    private void save() {
        ActiveStatusEffect[] activeStatusEffects = effects.values().toArray(new ActiveStatusEffect[0]);
        getPersistentDataContainer().set(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS, activeStatusEffects);
    }

    public void updateEffect(NamespacedKey key) {
        Bukkit.getScheduler().runTask(MMOBuffs.getInst(), () -> MMOBuffs.getInst().getStatManager().add(this, effects.get(key)));
    }

    public void addEffect(ActiveStatusEffect effect, Modifier durationModifier, Modifier stackModifier) {
        NamespacedKey key = effect.getStatusEffect().getKey();
        Bukkit.getScheduler().runTask(MMOBuffs.getInst(), () -> {
            ActiveStatusEffect existing = effects.get(key);
            if (existing != null) {
                effects.put(key, existing.merge(effect, durationModifier, stackModifier));
            } else {
                effects.put(key, effect);
            }
            MMOBuffs.getInst().getStatManager().add(this, effects.get(key));
        });
    }

    public void removeEffect(NamespacedKey key) {
        Bukkit.getScheduler().runTask(MMOBuffs.getInst(), () -> {
            ActiveStatusEffect effect = effects.remove(key);
            if (effect != null) {
                removeStatModifiers(effect);
            }
        });
    }

    private void removeStatModifiers(ActiveStatusEffect effect) {
        MMOBuffs.getInst().getStatManager().remove(this, effect);
    }

    public void removeEffects(boolean includePermanent) {
        Bukkit.getScheduler().runTask(MMOBuffs.getInst(), () -> {
            Iterator<Map.Entry<NamespacedKey, ActiveStatusEffect>> iterator = effects.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<NamespacedKey, ActiveStatusEffect> entry = iterator.next();
                if (includePermanent || !entry.getValue().isPermanent()) {
                    removeStatModifiers(entry.getValue());
                    iterator.remove();
                }
            }
        });
    }

    public boolean hasEffect(NamespacedKey key) {
        return effects.containsKey(key);
    }

    public @Nullable
    ActiveStatusEffect getEffect(NamespacedKey key) {
        return effects.get(key);
    }

    public Collection<ActiveStatusEffect> getEffects(boolean includePermanent) {
        Collection<ActiveStatusEffect> values = effects.values();

        if (!includePermanent)
            values.removeIf(ActiveStatusEffect::isPermanent);

        return values;
    }

    @NotNull
    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return player.getPersistentDataContainer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EffectHolder that = (EffectHolder) o;
        return player.equals(that.player);
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }

    public static class PlayerListener implements Listener {
        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            DATA.put(event.getPlayer(), new EffectHolder(event.getPlayer()));
        }

        @EventHandler
        public void onLeave(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            EffectHolder holder = DATA.remove(player);
            if (holder != null) {
                holder.cancelTasks();
            }
        }
    }
}