package com.example.ewa;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/** Effect implementations for all 24 abilities, plus shared helpers. */
public final class Abilities {
    private Abilities() {}

    public static void run(Player p, Element element, Slot slot) {
        p.swingMainHand();
        switch (element) {
            case FIRE -> fire(p, slot);
            case WATER -> water(p, slot);
            case EARTH -> earth(p, slot);
            case WIND -> wind(p, slot);
            case SHADOW -> shadow(p, slot);
            case LIGHT -> light(p, slot);
        }
    }

    // ---------------------------------------------------------------- Fire
    private static void fire(Player p, Slot slot) {
        World w = p.getWorld();
        switch (slot) {
            case POWER1 -> { // Fire Blast
                w.playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1f, 1f);
                for (LivingEntity t : inFront(p, 8)) { t.damage(4, p); t.setFireTicks(4 * 20); }
                particles(p, Particle.FLAME);
            }
            case POWER2 -> { // Flame Dash
                dash(p, 1.4);
                particles(p, Particle.FLAME);
            }
            case POWER3 -> { // Flame Guard
                effect(p, PotionEffectType.FIRE_RESISTANCE, 8, 0);
                effect(p, PotionEffectType.ABSORPTION, 8, 0);
                for (LivingEntity t : around(p, 3)) { t.damage(3, p); t.setFireTicks(2 * 20); }
                particles(p, Particle.FLAME);
            }
            case CONDUIT -> { // Inferno Slash
                w.playSound(p.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1f, 0.7f);
                for (LivingEntity t : inFront(p, 4)) {
                    t.damage(9, p); t.setFireTicks(5 * 20); pushAway(p, t, 0.6);
                }
                particles(p, Particle.FLAME);
            }
        }
    }

    // --------------------------------------------------------------- Water
    private static void water(Player p, Slot slot) {
        switch (slot) {
            case POWER1 -> { // Water Blast
                for (LivingEntity t : inFront(p, 8)) { t.damage(3, p); pushAway(p, t, 0.9); }
                particles(p, Particle.SPLASH);
            }
            case POWER2 -> { // Water Flow
                dash(p, 1.3);
                particles(p, Particle.BUBBLE);
            }
            case POWER3 -> { // Water Guard
                effect(p, PotionEffectType.ABSORPTION, 8, 0);
                for (LivingEntity t : around(p, 3.5)) pushAway(p, t, 0.8);
                particles(p, Particle.SPLASH);
            }
            case CONDUIT -> { // Tidal Spear
                for (LivingEntity t : inFront(p, 5)) { t.damage(10, p); pushAway(p, t, 1.1); }
                p.getWorld().playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1f);
                particles(p, Particle.SPLASH);
            }
        }
    }

    // --------------------------------------------------------------- Earth
    private static void earth(Player p, Slot slot) {
        switch (slot) {
            case POWER1 -> { // Earth Shot
                for (LivingEntity t : inFront(p, 8)) { t.damage(4, p); pushAway(p, t, 0.6); }
                particles(p, Particle.CRIT);
            }
            case POWER2 -> { // Earth Burst
                for (LivingEntity t : around(p, 3.5)) pushAway(p, t, 0.9);
                p.setVelocity(p.getVelocity().add(new Vector(0, 0.5, 0)));
                particles(p, Particle.CRIT);
            }
            case POWER3 -> // Stone Guard
                effect(p, PotionEffectType.RESISTANCE, 8, 1);
            case CONDUIT -> { // Seismic Strike
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.4f);
                for (LivingEntity t : around(p, 4)) { t.damage(11, p); pushAway(p, t, 1.2); }
                particles(p, Particle.CRIT);
            }
        }
    }

    // ---------------------------------------------------------------- Wind
    private static void wind(Player p, Slot slot) {
        switch (slot) {
            case POWER1 -> { // Wind Shot
                for (LivingEntity t : inFront(p, 10)) { t.damage(3, p); pushAway(p, t, 1.4); }
                particles(p, Particle.CLOUD);
            }
            case POWER2 -> { // Wind Step
                dash(p, 1.6);
                effect(p, PotionEffectType.SLOW_FALLING, 4, 0);
                particles(p, Particle.CLOUD);
            }
            case POWER3 -> { // Wind Guard
                for (LivingEntity t : around(p, 4)) pushAway(p, t, 1.1);
                particles(p, Particle.CLOUD);
            }
            case CONDUIT -> { // Gale Shot
                for (LivingEntity t : inFront(p, 10)) { t.damage(8, p); pushAway(p, t, 1.8); }
                particles(p, Particle.CLOUD);
            }
        }
    }

    // -------------------------------------------------------------- Shadow
    private static void shadow(Player p, Slot slot) {
        switch (slot) {
            case POWER1 -> { // Shadow Bolt (basic)
                for (LivingEntity t : inFront(p, 8)) {
                    t.damage(4, p); effect(t, PotionEffectType.BLINDNESS, 2, 0);
                }
                particles(p, Particle.SMOKE);
            }
            case POWER2 -> { // Shadow Step
                shadowStep(p, 5);
                particles(p, Particle.SQUID_INK);
            }
            case POWER3 -> // Shadow Veil
                effect(p, PotionEffectType.INVISIBILITY, 5, 0);
            case CONDUIT -> { // Shadow Bolt (conduit)
                for (LivingEntity t : inFront(p, 12)) {
                    t.damage(10, p);
                    effect(t, PotionEffectType.BLINDNESS, 4, 0);
                    effect(t, PotionEffectType.WEAKNESS, 5, 0);
                }
                particles(p, Particle.SMOKE);
            }
        }
    }

    // --------------------------------------------------------------- Light
    private static void light(Player p, Slot slot) {
        switch (slot) {
            case POWER1 -> { // Light Bolt (bonus vs undead)
                for (LivingEntity t : inFront(p, 8)) t.damage(isUndead(t) ? 6 : 4, p);
                particles(p, Particle.END_ROD);
            }
            case POWER2 -> { // Light Step
                dash(p, 1.4);
                particles(p, Particle.GLOW);
            }
            case POWER3 -> { // Radiant Guard
                effect(p, PotionEffectType.ABSORPTION, 8, 0);
                for (LivingEntity t : around(p, 3.5)) pushAway(p, t, 0.9);
                particles(p, Particle.END_ROD);
            }
            case CONDUIT -> { // Radiant Cleave
                for (LivingEntity t : around(p, 3.5)) { t.damage(10, p); pushAway(p, t, 0.7); }
                particles(p, Particle.END_ROD);
            }
        }
    }

    // ------------------------------------------------------------- Helpers

    /** Living entities in a ~60 degree cone in front of the player, within [range] blocks. */
    static List<LivingEntity> inFront(Player p, double range) {
        Vector look = p.getLocation().getDirection().setY(0);
        if (look.lengthSquared() < 1.0E-6) return List.of();
        look.normalize();
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : p.getNearbyEntities(range, range, range)) {
            if (!(e instanceof LivingEntity le) || e instanceof ArmorStand || e.equals(p)) continue;
            if (e.getLocation().distance(p.getLocation()) > range) continue;
            Vector to = e.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0);
            if (to.lengthSquared() < 1.0E-6) { out.add(le); continue; }
            if (to.normalize().dot(look) > 0.5) out.add(le);
        }
        return out;
    }

    /** All living entities within [radius] blocks (except the caster). */
    static List<LivingEntity> around(Player p, double radius) {
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !(e instanceof ArmorStand) && !e.equals(p)
                && e.getLocation().distance(p.getLocation()) <= radius) out.add(le);
        }
        return out;
    }

    static void pushAway(Player p, LivingEntity t, double strength) {
        Vector d = t.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0);
        double len = Math.max(0.1, d.length());
        Vector push = new Vector(d.getX() / len * strength, 0.35 * strength, d.getZ() / len * strength);
        t.setVelocity(t.getVelocity().add(push));
    }

    static void dash(Player p, double strength) {
        Vector look = p.getLocation().getDirection().normalize();
        p.setVelocity(p.getVelocity().add(new Vector(look.getX() * strength, 0.15, look.getZ() * strength)));
    }

    static void shadowStep(Player p, double maxDist) {
        Vector dir = p.getLocation().getDirection().setY(0);
        if (dir.lengthSquared() < 1.0E-6) return;
        dir.normalize();
        // Walk back from full distance until feet + head space are clear.
        for (double d = maxDist; d >= 1.0; d -= 0.5) {
            Location dest = p.getLocation().clone().add(dir.clone().multiply(d));
            if (dest.getBlock().isPassable() && dest.clone().add(0, 1, 0).getBlock().isPassable()) {
                p.teleport(dest);
                return;
            }
        }
    }

    static void effect(LivingEntity e, PotionEffectType type, int seconds, int amplifier) {
        e.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier));
    }

    static boolean isUndead(LivingEntity e) {
        return switch (e.getType()) {
            case ZOMBIE, ZOMBIE_VILLAGER, HUSK, DROWNED, SKELETON, STRAY, WITHER_SKELETON, WITHER,
                 PHANTOM, ZOMBIFIED_PIGLIN, ZOGLIN, SKELETON_HORSE, ZOMBIE_HORSE, BOGGED -> true;
            default -> false;
        };
    }

    static void particles(Player p, Particle particle) {
        Vector look = p.getLocation().getDirection().normalize();
        Location at = p.getLocation().add(look.getX(), p.getEyeHeight() * 0.5, look.getZ());
        p.getWorld().spawnParticle(particle, at, 12, 0.3, 0.3, 0.3, 0.02);
    }
}
