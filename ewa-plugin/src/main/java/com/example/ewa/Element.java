package com.example.ewa;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.Locale;

/** The six elements. Each owns one vanilla weapon type (its "conduit"). */
public enum Element {
    FIRE("fire", "Flame Conduit", "sword", NamedTextColor.RED),
    WATER("water", "Tidal Conduit", "trident", NamedTextColor.AQUA),
    EARTH("earth", "Terra Conduit", "mace", NamedTextColor.GREEN),
    WIND("wind", "Gale Conduit", "bow", NamedTextColor.WHITE),
    SHADOW("shadow", "Shadow Conduit", "crossbow", NamedTextColor.DARK_PURPLE),
    LIGHT("light", "Radiant Conduit", "axe", NamedTextColor.YELLOW);

    public final String id;
    public final String enchantName;
    public final String weaponName;
    public final NamedTextColor color;

    Element(String id, String enchantName, String weaponName, NamedTextColor color) {
        this.id = id;
        this.enchantName = enchantName;
        this.weaponName = weaponName;
        this.color = color;
    }

    public boolean isConduitWeapon(Material m) {
        String n = m.name();
        return switch (this) {
            case FIRE -> n.endsWith("_SWORD");
            case WATER -> m == Material.TRIDENT;
            case EARTH -> m == Material.MACE;
            case WIND -> m == Material.BOW;
            case SHADOW -> m == Material.CROSSBOW;
            case LIGHT -> n.endsWith("_AXE");
        };
    }

    public String displayName() {
        return id.substring(0, 1).toUpperCase(Locale.ROOT) + id.substring(1);
    }

    public static Element fromId(String s) {
        if (s == null) return null;
        for (Element e : values()) if (e.id.equalsIgnoreCase(s)) return e;
        return null;
    }
}
