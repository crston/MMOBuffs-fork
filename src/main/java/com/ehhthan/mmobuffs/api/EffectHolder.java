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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ehhthan.mmobuffs.util.KeyUtil.key;

public class EffectHolder implements PersistentDataHolder {
    private static final NamespacedKey EFFECTS = key("effects");
    private static final Map<Player, EffectHolder> DATA = new ConcurrentHashMap<>();
    private static final BossBar EMPTY_BAR = BossBar.bossBar(Component.empty(), 1, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);

    private final MMOBuffs plugin;
    private final Player player;
    private final BossBar bossBar;
    private final Component separator;
    private final Map<NamespacedKey, ActiveStatusEffect> effects = new HashMap<>();

    private final boolean bossbarEnabled;
    private final boolean bossbarDisplayWhenEmpty;
    private final boolean durationAscending;
    private final int bossbarUpdateTicks;

    public EffectHolder(Player player) {
        this.plugin = MMOBuffs.getInst();
        this.player = player;

        FileConfiguration config = plugin.getConfig();
        this.separator = Component.text(config.getString("bossbar-display.effect-separator", " "));

        this.bossbarEnabled = config.getBoolean("bossbar-display.enabled", true);
        this.bossbarDisplayWhenEmpty = config.getBoolean("bossbar-display.display-when-empty", false);
        this.durationAscending = config.getBoolean("sorting.duration-ascending", true);
        this.bossbarUpdateTicks = config.getInt("bossbar-display.update-ticks", 20);

        ConfigurationSection section = config.getConfigurationSection("bossbar-display");
        this.bossBar = (section != null && bossbarEnabled)
                ? BossBar.bossBar(
                Component.empty(),
                1,
                Objects.requireNonNull(BossBar.Color.NAMES.value(section.getString("color", "white"))),
                Objects.requireNonNull(BossBar.Overlay.NAMES.value(section.getString("overlay", "progress")))
        )
                : EMPTY_BAR;

        if (bossbarDisplayWhenEmpty && bossbarEnabled) {
            player.showBossBar(bossBar);
        }

        loadSavedEffects();
        runSchedulers();
    }

    private void runSchedulers() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<ActiveStatusEffect> it = effects.values().iterator();
            while (it.hasNext()) {
                ActiveStatusEffect effect = it.next();
                if (!effect.isPermanent() && !effect.tick()) {
                    plugin.getStatManager().remove(this, effect);
                    it.remove();
                }
            }
            save();
        }, 1, 20);

        Bukkit.getScheduler().runTaskTimer(plugin, this::updateBossbar, 2, bossbarUpdateTicks);
    }

    private void updateBossbar() {
        if (!bossbarEnabled) {
            return;
        }

        if (effects.isEmpty() && !bossbarDisplayWhenEmpty) {
            player.hideBossBar(bossBar);
            return;
        }

        List<ActiveStatusEffect> displayable = new ArrayList<>();
        for (ActiveStatusEffect effect : effects.values()) {
            if (effect.getStatusEffect().hasDisplay()) {
                displayable.add(effect);
            }
        }

        if (displayable.isEmpty() && !bossbarDisplayWhenEmpty) {
            player.hideBossBar(bossBar);
            return;
        }

        if (!displayable.isEmpty()) {
            if (durationAscending) {
                displayable.sort(Comparator.comparingInt(ActiveStatusEffect::getDuration));
            } else {
                displayable.sort((a, b) -> Integer.compare(b.getDuration(), a.getDuration()));
            }
        }

        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < displayable.size(); i++) {
            if (i > 0) {
                builder.append(separator);
            }
            ActiveStatusEffect effect = displayable.get(i);
            builder.append(
                    effect.getStatusEffect().getDisplay()
                            .map(display -> display.build(player, effect))
                            .orElse(Component.empty())
            );
        }

        if (!displayable.isEmpty() || bossbarDisplayWhenEmpty) {
            bossBar.name(builder.build());
            player.showBossBar(bossBar);
        } else {
            player.hideBossBar(bossBar);
        }
    }

    private void loadSavedEffects() {
        PersistentDataContainer container = getPersistentDataContainer();
        if (!container.has(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS)) {
            return;
        }
        ActiveStatusEffect[] array = container.get(EFFECTS, CustomTagTypes.ACTIVE_EFFECTS);
        if (array == null) {
            return;
        }
        for (ActiveStatusEffect effect : array) {
            if (effect != null) {
                addEffect(effect, Modifier.SET, Modifier.SET);
            }
        }
    }

    public void addEffect(ActiveStatusEffect effect, Modifier durationMod, Modifier stackMod) {
        NamespacedKey key = effect.getStatusEffect().getKey();
        ActiveStatusEffect merged = effects.merge(key, effect, (oldEff, newEff) -> oldEff.merge(newEff, durationMod, stackMod));
        plugin.getStatManager().add(this, merged);
    }

    public void updateEffect(NamespacedKey key) {
        ActiveStatusEffect effect = effects.get(key);
        if (effect != null) {
            plugin.getStatManager().add(this, effect);
        }
    }

    public void removeEffect(NamespacedKey key) {
        ActiveStatusEffect removed = effects.remove(key);
        if (removed == null) {
            return;
        }
        plugin.getStatManager().remove(this, removed);
    }

    public void removeEffects(boolean includePermanent) {
        List<NamespacedKey> keys = new ArrayList<>();
        for (ActiveStatusEffect effect : getEffects(includePermanent)) {
            keys.add(effect.getStatusEffect().getKey());
        }
        for (NamespacedKey key : keys) {
            removeEffect(key);
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
        List<ActiveStatusEffect> list = new ArrayList<>();
        for (ActiveStatusEffect effect : effects.values()) {
            if (!effect.isPermanent()) {
                list.add(effect);
            }
        }
        return list;
    }

    public Player getPlayer() {
        return player;
    }

    private void save() {
        getPersistentDataContainer().set(
                EFFECTS,
                CustomTagTypes.ACTIVE_EFFECTS,
                effects.values().toArray(new ActiveStatusEffect[0])
        );
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

    public static class PlayerListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
            DATA.put(e.getPlayer(), new EffectHolder(e.getPlayer()));
        }

        @org.bukkit.event.EventHandler
        public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
            DATA.remove(e.getPlayer());
        }
    }
}
