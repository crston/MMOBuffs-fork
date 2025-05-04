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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ehhthan.mmobuffs.util.KeyUtil.key;

public class EffectHolder implements PersistentDataHolder {
    private static final NamespacedKey EFFECTS = key("effects");
    private static final Map<Player, EffectHolder> DATA = new ConcurrentHashMap<>();
    private static final BossBar EMPTY_BAR = BossBar.bossBar(Component.empty(), 1, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);

    private final Player player;
    private final BossBar bossBar;
    private final Component separator;
    private final Map<NamespacedKey, ActiveStatusEffect> effects = new HashMap<>();

    public EffectHolder(Player player) {
        this.player = player;

        FileConfiguration config = MMOBuffs.getInst().getConfig();
        this.separator = Component.text(config.getString("bossbar-display.effect-separator", " "));

        ConfigurationSection section = config.getConfigurationSection("bossbar-display");
        this.bossBar = (section != null && config.getBoolean("bossbar-display.enabled", true))
                ? buildBossBar(section)
                : EMPTY_BAR;

        loadSavedEffects();
        startEffectUpdater();
        startBossBarUpdater();

        if (config.getBoolean("bossbar-display.display-when-empty", false)) {
            player.showBossBar(bossBar);
        }
    }

    private BossBar buildBossBar(ConfigurationSection section) {
        BossBar.Color color = BossBar.Color.NAMES.value(section.getString("color", "white").toLowerCase());
        BossBar.Overlay overlay = BossBar.Overlay.NAMES.value(section.getString("overlay", "progress").toLowerCase());
        assert color != null;
        assert overlay != null;
        return BossBar.bossBar(Component.empty(), 1, color, overlay);
    }

    private void loadSavedEffects() {
        if (!getPersistentDataContainer().has(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS)) return;
        ActiveStatusEffect[] saved = getPersistentDataContainer().get(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS);
        if (saved != null) {
            for (ActiveStatusEffect effect : saved) {
                if (effect != null) addEffect(effect, Modifier.SET, Modifier.SET);
            }
        }
    }

    private void startEffectUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                Iterator<ActiveStatusEffect> it = effects.values().iterator();
                while (it.hasNext()) {
                    ActiveStatusEffect effect = it.next();
                    if (effect.tick()) updateEffect(effect.getStatusEffect().getKey());
                    if (!effect.isActive()) {
                        MMOBuffs.getInst().getStatManager().remove(EffectHolder.this, effect);
                        it.remove();
                    }
                }
                save();
            }
        }.runTaskTimer(MMOBuffs.getInst(), 1, 20);
    }

    private void startBossBarUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration config = MMOBuffs.getInst().getConfig();
                if (!config.getBoolean("bossbar-display.enabled", true)) return;

                List<ActiveStatusEffect> displayable = new ArrayList<>(effects.values().stream()
                        .filter(e -> e.getStatusEffect().hasDisplay())
                        .sorted(Comparator.comparingInt(ActiveStatusEffect::getDuration))
                        .toList());

                if (!config.getBoolean("sorting.duration-ascending", true)) {
                    Collections.reverse(displayable);
                }

                TextComponent.Builder builder = Component.text();
                for (int i = 0; i < displayable.size(); i++) {
                    if (i > 0) builder.append(separator);
                    ActiveStatusEffect effect = displayable.get(i);
                    builder.append(
                            effect.getStatusEffect().getDisplay()
                                    .map(display -> display.build(player, effect))
                                    .orElse(Component.empty())
                    );
                }

                if (!displayable.isEmpty() || config.getBoolean("bossbar-display.display-when-empty", false)) {
                    bossBar.name(builder.build());
                    player.showBossBar(bossBar);
                } else {
                    player.hideBossBar(bossBar);
                }
            }
        }.runTaskTimer(MMOBuffs.getInst(), 2, MMOBuffs.getInst().getConfig().getInt("bossbar-display.update-ticks", 20));
    }

    public void updateEffect(NamespacedKey key) {
        Bukkit.getScheduler().runTask(MMOBuffs.getInst(), () -> {
            ActiveStatusEffect effect = effects.get(key);
            if (effect != null)
                MMOBuffs.getInst().getStatManager().add(this, effect);
        });
    }

    public void addEffect(ActiveStatusEffect effect, Modifier durationMod, Modifier stackMod) {
        NamespacedKey key = effect.getStatusEffect().getKey();
        Bukkit.getScheduler().runTask(MMOBuffs.getInst(), () -> {
            effects.merge(key, effect,
                    (oldEffect, newEffect) -> oldEffect.merge(newEffect, durationMod, stackMod));
            MMOBuffs.getInst().getStatManager().add(this, effects.get(key));
        });
    }

    public void removeEffect(NamespacedKey key) {
        if (!hasEffect(key)) return;
        Bukkit.getScheduler().runTask(MMOBuffs.getInst(), () -> {
            MMOBuffs.getInst().getStatManager().remove(this, effects.get(key));
            effects.remove(key);
        });
    }

    public void removeEffects(boolean includePermanent) {
        getEffects(includePermanent).forEach(e -> removeEffect(e.getStatusEffect().getKey()));
    }

    public boolean hasEffect(NamespacedKey key) {
        return effects.containsKey(key);
    }

    public ActiveStatusEffect getEffect(NamespacedKey key) {
        return effects.get(key);
    }

    public Collection<ActiveStatusEffect> getEffects(boolean includePermanent) {
        return includePermanent
                ? new ArrayList<>(effects.values())
                : effects.values().stream().filter(e -> !e.isPermanent()).toList();
    }

    public Player getPlayer() {
        return player;
    }

    private void save() {
        getPersistentDataContainer().set(
                EFFECTS, CustomTagTypes.ACTIVE_EFFECTS,
                effects.values().toArray(new ActiveStatusEffect[0])
        );
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return player.getPersistentDataContainer();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof EffectHolder that && Objects.equals(player, that.player));
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }

    public static boolean has(Player player) {
        return player != null && DATA.containsKey(player);
    }

    public static EffectHolder get(Player player) {
        return DATA.get(player);
    }

    public static class PlayerListener implements Listener {
        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            DATA.put(e.getPlayer(), new EffectHolder(e.getPlayer()));
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            DATA.remove(e.getPlayer());
        }
    }
}
