package com.ehhthan.mmobuffs.api;

import com.ehhthan.mmobuffs.MMOBuffs;
import com.ehhthan.mmobuffs.api.effect.ActiveStatusEffect;
import com.ehhthan.mmobuffs.api.effect.stack.StackType;
import com.ehhthan.mmobuffs.api.modifier.Modifier;
import com.ehhthan.mmobuffs.api.tag.CustomTagTypes;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.ehhthan.mmobuffs.util.KeyUtil.key;

public final class EffectHolder implements PersistentDataHolder {

    private static final NamespacedKey EFFECTS = key("effects");
    private static final Map<Player, EffectHolder> DATA = new ConcurrentHashMap<>();
    private static final BossBar EMPTY_BAR = BossBar.bossBar(Component.empty(), 1, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);

    private final Player player;
    private final BossBar bossBar;
    private final Component separator;
    private final Map<NamespacedKey, ActiveStatusEffect> effects = new HashMap<>();
    private final List<ActiveStatusEffect> displayableEffects = new ArrayList<>();

    private final boolean bossbarEnabled;
    private final boolean displayWhenEmpty;
    private final boolean durationAscending;
    private final int bossbarUpdateTicks;

    private boolean displayCacheInvalid = true;

    public EffectHolder(Player player) {
        this.player = player;

        FileConfiguration config = MMOBuffs.getInst().getConfig();
        this.separator = Component.text(config.getString("bossbar-display.effect-separator", " "));

        ConfigurationSection section = config.getConfigurationSection("bossbar-display");
        this.bossbarEnabled = config.getBoolean("bossbar-display.enabled", true);
        this.displayWhenEmpty = config.getBoolean("bossbar-display.display-when-empty", false);
        this.durationAscending = config.getBoolean("sorting.duration-ascending", true);
        this.bossbarUpdateTicks = config.getInt("bossbar-display.update-ticks", 20);

        if (section != null && bossbarEnabled) {
            BossBar.Color color = Objects.requireNonNull(BossBar.Color.NAMES.value(section.getString("color", "white")));
            BossBar.Overlay overlay = Objects.requireNonNull(BossBar.Overlay.NAMES.value(section.getString("overlay", "progress")));
            this.bossBar = BossBar.bossBar(Component.empty(), 1, color, overlay);
        } else {
            this.bossBar = EMPTY_BAR;
        }

        if (bossbarEnabled && displayWhenEmpty) {
            player.showBossBar(bossBar);
        }

        loadSavedEffects();
        runSchedulers();
    }

    private void runSchedulers() {
        Bukkit.getScheduler().runTaskTimer(MMOBuffs.getInst(), () -> {
            if (!player.isOnline()) {
                return;
            }

            Iterator<ActiveStatusEffect> it = effects.values().iterator();
            boolean changed = false;

            while (it.hasNext()) {
                ActiveStatusEffect effect = it.next();
                boolean active = effect.tick();

                if (!active) {
                    StackType stackType = effect.getStatusEffect().getStackType();

                    if ((stackType == StackType.CASCADING || stackType == StackType.TIMESTACK) && effect.getStacks() > 1) {
                        effect.setStacks(effect.getStacks() - 1);
                        effect.setDuration(effect.getStatusEffect().getDuration());
                        MMOBuffs.getInst().getStatManager().add(this, effect);
                        changed = true;
                        displayCacheInvalid = true;
                        continue;
                    }

                    MMOBuffs.getInst().getStatManager().remove(this, effect);
                    it.remove();
                    changed = true;
                    displayCacheInvalid = true;
                }
            }

            if (changed) {
                save();
            }
        }, 1, 20);

        Bukkit.getScheduler().runTaskTimer(MMOBuffs.getInst(), () -> {
            if (!player.isOnline() || !bossbarEnabled) {
                return;
            }

            if (effects.isEmpty()) {
                if (displayWhenEmpty) {
                    bossBar.name(Component.empty());
                    player.showBossBar(bossBar);
                } else {
                    player.hideBossBar(bossBar);
                }
                return;
            }

            if (displayCacheInvalid) {
                displayableEffects.clear();
                for (ActiveStatusEffect effect : effects.values()) {
                    if (effect.getStatusEffect().hasDisplay()) {
                        displayableEffects.add(effect);
                    }
                }

                displayableEffects.sort(Comparator.comparingInt(ActiveStatusEffect::getDuration));
                if (!durationAscending) {
                    Collections.reverse(displayableEffects);
                }

                displayCacheInvalid = false;
            }

            List<Component> components = new ArrayList<>();
            for (ActiveStatusEffect effect : displayableEffects) {
                effect.getStatusEffect().getDisplay()
                        .map(display -> display.build(player, effect))
                        .ifPresent(components::add);
            }

            if (!components.isEmpty()) {
                bossBar.name(Component.join(JoinConfiguration.separator(separator), components));
                player.showBossBar(bossBar);
            } else if (displayWhenEmpty) {
                bossBar.name(Component.empty());
                player.showBossBar(bossBar);
            } else {
                player.hideBossBar(bossBar);
            }
        }, 2, Math.max(1, bossbarUpdateTicks));
    }

    private void loadSavedEffects() {
        PersistentDataContainer container = getPersistentDataContainer();
        if (!container.has(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS)) {
            return;
        }
        ActiveStatusEffect[] stored = container.get(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS);
        if (stored == null || stored.length == 0) {
            return;
        }
        for (ActiveStatusEffect effect : stored) {
            if (effect != null) {
                addEffect(effect, Modifier.SET, Modifier.SET);
            }
        }
    }

    public void addEffect(ActiveStatusEffect effect, Modifier durationMod, Modifier stackMod) {
        NamespacedKey key = effect.getStatusEffect().getKey();
        effects.merge(key, effect, (oldEff, newEff) -> oldEff.merge(newEff, durationMod, stackMod));
        ActiveStatusEffect current = effects.get(key);
        MMOBuffs.getInst().getStatManager().add(this, current);
        displayCacheInvalid = true;
        save();
    }

    public void updateEffect(NamespacedKey key) {
        ActiveStatusEffect effect = effects.get(key);
        if (effect != null) {
            MMOBuffs.getInst().getStatManager().add(this, effect);
            displayCacheInvalid = true;
            save();
        }
    }

    public void removeEffect(NamespacedKey key) {
        if (!hasEffect(key)) {
            return;
        }
        ActiveStatusEffect effect = effects.remove(key);
        if (effect != null) {
            MMOBuffs.getInst().getStatManager().remove(this, effect);
            displayCacheInvalid = true;
            save();
        }
    }

    public void removeEffects(boolean includePermanent) {
        Collection<ActiveStatusEffect> snapshot = getEffects(includePermanent);
        for (ActiveStatusEffect effect : snapshot) {
            removeEffect(effect.getStatusEffect().getKey());
        }
    }

    public boolean hasEffect(NamespacedKey key) {
        return effects.containsKey(key);
    }

    public ActiveStatusEffect getEffect(NamespacedKey key) {
        return effects.get(key);
    }

    public Collection<ActiveStatusEffect> getEffects(boolean includePermanent) {
        if (includePermanent) {
            return new ArrayList<>(effects.values());
        }
        return effects.values().stream()
                .filter(e -> !e.isPermanent())
                .collect(Collectors.toList());
    }

    public Player getPlayer() {
        return player;
    }

    private void save() {
        PersistentDataContainer container = getPersistentDataContainer();
        if (effects.isEmpty()) {
            container.remove(EFFECTS);
        } else {
            ActiveStatusEffect[] array = effects.values().toArray(new ActiveStatusEffect[0]);
            container.set(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS, array);
        }
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return player.getPersistentDataContainer();
    }

    public static boolean has(Player player) {
        return player != null && DATA.containsKey(player);
    }

    public static EffectHolder get(Player player) {
        return DATA.get(player);
    }

    public static final class PlayerListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            Player player = event.getPlayer();
            DATA.put(player, new EffectHolder(player));
        }

        @org.bukkit.event.EventHandler
        public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
            Player player = event.getPlayer();
            EffectHolder holder = DATA.remove(player);
            if (holder != null) {
                holder.save();
            }
        }
    }
}
