package com.example.ewa;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player state. Element + Essence live in the player's PersistentDataContainer
 * (survives relog/restart). Cooldowns are kept in memory (server ticks).
 */
public final class PlayerData {
    public static final double MAX_ESSENCE = 100.0;
    public static final double REGEN_PER_SECOND = 2.0;

    private final NamespacedKey elementKey;
    private final NamespacedKey essenceKey;
    private final Map<UUID, Map<Slot, Long>> cooldownEnds = new ConcurrentHashMap<>();

    public PlayerData(ElementalWeaponArts plugin) {
        this.elementKey = new NamespacedKey(plugin, "element");
        this.essenceKey = new NamespacedKey(plugin, "essence");
    }

    public Element getElement(Player p) {
        String raw = p.getPersistentDataContainer().get(elementKey, PersistentDataType.STRING);
        return Element.fromId(raw);
    }

    public void setElement(Player p, Element e) {
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        pdc.set(elementKey, PersistentDataType.STRING, e.id);
        pdc.set(essenceKey, PersistentDataType.DOUBLE, MAX_ESSENCE);
        cooldownEnds.remove(p.getUniqueId());
    }

    public double getEssence(Player p) {
        Double v = p.getPersistentDataContainer().get(essenceKey, PersistentDataType.DOUBLE);
        return v == null ? MAX_ESSENCE : v;
    }

    public void setEssence(Player p, double value) {
        double clamped = Math.max(0.0, Math.min(MAX_ESSENCE, value));
        p.getPersistentDataContainer().set(essenceKey, PersistentDataType.DOUBLE, clamped);
    }

    public long getCooldownEnd(Player p, Slot s) {
        Map<Slot, Long> m = cooldownEnds.get(p.getUniqueId());
        if (m == null) return 0L;
        return m.getOrDefault(s, 0L);
    }

    public void setCooldownEnd(Player p, Slot s, long tick) {
        cooldownEnds.computeIfAbsent(p.getUniqueId(), k -> new EnumMap<>(Slot.class)).put(s, tick);
    }

    public void forget(Player p) {
        cooldownEnds.remove(p.getUniqueId());
    }
}
