package com.example.ewa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class ElementalWeaponArts extends JavaPlugin implements Listener {

    private PlayerData data;
    private Enchants enchants;

    @Override
    public void onEnable() {
        this.data = new PlayerData(this);
        this.enchants = new Enchants(this);
        getServer().getPluginManager().registerEvents(this, this);

        // Essence regen: once per second (20 ticks).
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player p : getServer().getOnlinePlayers()) {
                if (data.getElement(p) == null) continue;
                double cur = data.getEssence(p);
                if (cur < PlayerData.MAX_ESSENCE) {
                    data.setEssence(p, cur + PlayerData.REGEN_PER_SECOND);
                }
            }
        }, 20L, 20L);

        getLogger().info("Elemental Weapon Arts enabled.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        data.forget(e.getPlayer());
    }

    // ---------------------------------------------------------------- Ability logic

    private enum Result { SUCCESS, NO_ELEMENT, ON_COOLDOWN, NOT_ENOUGH_ESSENCE, WRONG_WEAPON }

    private Result use(Player p, Slot slot) {
        Element element = data.getElement(p);
        if (element == null) return Result.NO_ELEMENT;

        ItemStack held = p.getInventory().getItemInMainHand();
        if (slot == Slot.CONDUIT && !element.isConduitWeapon(held.getType())) return Result.WRONG_WEAPON;

        long now = p.getWorld().getGameTime();
        if (now < data.getCooldownEnd(p, slot)) return Result.ON_COOLDOWN;

        int level = enchants.getLevel(held, element);
        double reduction = 1.0 - level * 0.05;

        double cost = slot.cost * reduction;
        double essence = data.getEssence(p);
        if (essence < cost) return Result.NOT_ENOUGH_ESSENCE;

        data.setEssence(p, essence - cost);
        data.setCooldownEnd(p, slot, now + Math.max(1L, (long) (slot.cooldownTicks * reduction)));

        Abilities.run(p, element, slot);
        return Result.SUCCESS;
    }

    private void useAndReport(Player p, Slot slot) {
        switch (use(p, slot)) {
            case SUCCESS -> { }
            case NO_ELEMENT -> msg(p, "You haven't chosen an element yet. Use /element set <element>.", NamedTextColor.RED);
            case ON_COOLDOWN -> msg(p, "That ability is still on cooldown.", NamedTextColor.RED);
            case NOT_ENOUGH_ESSENCE -> msg(p, "Not enough Essence.", NamedTextColor.RED);
            case WRONG_WEAPON -> {
                Element e = data.getElement(p);
                msg(p, "You need your conduit weapon (" + (e == null ? "?" : e.weaponName)
                    + ") in hand to use your Conduit Power.", NamedTextColor.RED);
            }
        }
    }

    // ---------------------------------------------------------------- Commands

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!p.hasPermission("ewa.use")) {
            msg(p, "You don't have permission.", NamedTextColor.RED);
            return true;
        }

        String name = cmd.getName().toLowerCase(Locale.ROOT);
        switch (name) {
            case "power1" -> { useAndReport(p, Slot.POWER1); return true; }
            case "power2" -> { useAndReport(p, Slot.POWER2); return true; }
            case "power3" -> { useAndReport(p, Slot.POWER3); return true; }
            case "conduit" -> { useAndReport(p, Slot.CONDUIT); return true; }
            default -> { }
        }

        // /element ...
        if (args.length == 0) {
            msg(p, "Usage: /element <set|power1|power2|power3|conduit|status|enchant>", NamedTextColor.YELLOW);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "set" -> {
                if (args.length < 2) {
                    msg(p, "Usage: /element set <" + elementIds() + ">", NamedTextColor.YELLOW);
                    return true;
                }
                Element e = Element.fromId(args[1]);
                if (e == null) {
                    msg(p, "Unknown element '" + args[1] + "'. Choose one of: " + elementIds(), NamedTextColor.RED);
                    return true;
                }
                data.setElement(p, e);
                p.sendMessage(Component.text("You are now attuned to " + e.displayName()
                    + ". Your conduit weapon is the " + e.weaponName + ".", e.color));
            }
            case "power1" -> useAndReport(p, Slot.POWER1);
            case "power2" -> useAndReport(p, Slot.POWER2);
            case "power3" -> useAndReport(p, Slot.POWER3);
            case "conduit" -> useAndReport(p, Slot.CONDUIT);
            case "status" -> status(p);
            case "enchant" -> enchantCommand(p, args);
            default -> msg(p, "Unknown subcommand. Try: set, power1, power2, power3, conduit, status, enchant.",
                NamedTextColor.RED);
        }
        return true;
    }

    private void status(Player p) {
        Element e = data.getElement(p);
        if (e == null) {
            msg(p, "No element chosen. Use /element set <element>.", NamedTextColor.YELLOW);
            return;
        }
        int level = enchants.getLevel(p.getInventory().getItemInMainHand(), e);
        p.sendMessage(Component.text("Element: " + e.id + " | Conduit: " + e.weaponName
            + " | Essence: " + (int) data.getEssence(p) + "/" + (int) PlayerData.MAX_ESSENCE
            + " | Enchant level: " + level, e.color));
    }

    /** /element enchant <0-3> : applies the player's element's Conduit enchant to the held item. */
    private void enchantCommand(Player p, String[] args) {
        if (!p.hasPermission("ewa.enchant")) {
            msg(p, "You don't have permission to enchant items.", NamedTextColor.RED);
            return;
        }
        Element e = data.getElement(p);
        if (e == null) {
            msg(p, "Choose an element first: /element set <element>.", NamedTextColor.RED);
            return;
        }
        if (args.length < 2) {
            msg(p, "Usage: /element enchant <0-3>  (0 removes it)", NamedTextColor.YELLOW);
            return;
        }
        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            msg(p, "Level must be a number from 0 to " + Enchants.MAX_LEVEL + ".", NamedTextColor.RED);
            return;
        }
        if (level < 0 || level > Enchants.MAX_LEVEL) {
            msg(p, "Level must be between 0 and " + Enchants.MAX_LEVEL + ".", NamedTextColor.RED);
            return;
        }
        ItemStack held = p.getInventory().getItemInMainHand();
        if (!e.isConduitWeapon(held.getType())) {
            msg(p, "Hold your conduit weapon (" + e.weaponName + ") to enchant it.", NamedTextColor.RED);
            return;
        }
        enchants.setLevel(held, e, level);
        msg(p, level == 0 ? "Conduit enchantment removed."
            : e.enchantName + " " + level + " applied.", e.color);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("element")) return List.of();
        if (args.length == 1) {
            return filter(List.of("set", "power1", "power2", "power3", "conduit", "status", "enchant"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            List<String> ids = new ArrayList<>();
            for (Element e : Element.values()) ids.add(e.id);
            return filter(ids, args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("enchant")) {
            return filter(Arrays.asList("0", "1", "2", "3"), args[1]);
        }
        return List.of();
    }

    // ---------------------------------------------------------------- Utils

    private static List<String> filter(List<String> options, String prefix) {
        String pre = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) if (o.startsWith(pre)) out.add(o);
        return out;
    }

    private static String elementIds() {
        StringBuilder sb = new StringBuilder();
        for (Element e : Element.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.id);
        }
        return sb.toString();
    }

    private static void msg(Player p, String text, NamedTextColor color) {
        p.sendMessage(Component.text(text, color));
    }
}
