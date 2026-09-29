package com.example.ewa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * The six "Conduit" enchantments. Bukkit plugins can't cleanly register brand-new
 * vanilla enchantments, so they're stored as tags on the item (plus a lore line and
 * glint). They only reduce Essence cost and cooldown: 5% per level, max level 3.
 */
public final class Enchants {
    public static final int MAX_LEVEL = 3;

    private final NamespacedKey levelKey;

    public Enchants(ElementalWeaponArts plugin) {
        this.levelKey = new NamespacedKey(plugin, "conduit_level");
    }

    /** Level (0-3) of [element]'s conduit enchant on [stack]. */
    public int getLevel(ItemStack stack, Element element) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) return 0;
        ItemMeta meta = stack.getItemMeta();
        String owner = meta.getPersistentDataContainer().get(levelKey, PersistentDataType.STRING);
        if (owner == null) return 0;
        // stored as "<element>:<level>"
        String[] parts = owner.split(":");
        if (parts.length != 2 || !parts[0].equals(element.id)) return 0;
        try {
            return Math.max(0, Math.min(MAX_LEVEL, Integer.parseInt(parts[1])));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** Apply (level 1-3) or remove (level 0) the element's conduit enchant. */
    public void setLevel(ItemStack stack, Element element, int level) {
        ItemMeta meta = stack.getItemMeta();
        if (level <= 0) {
            meta.getPersistentDataContainer().remove(levelKey);
            meta.lore(null);
            meta.setEnchantmentGlintOverride(null);
        } else {
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.STRING, element.id + ":" + level);
            meta.lore(List.of(
                Component.text(element.enchantName + " " + roman(level), element.color)
                    .decoration(TextDecoration.ITALIC, false)));
            meta.setEnchantmentGlintOverride(true);
        }
        stack.setItemMeta(meta);
    }

    private static String roman(int n) {
        return switch (n) { case 1 -> "I"; case 2 -> "II"; default -> "III"; };
    }
}
