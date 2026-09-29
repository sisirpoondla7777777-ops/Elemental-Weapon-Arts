# Elemental Weapon Arts (Paper plugin)

Six elements, each tied to a vanilla weapon. Shared Essence pool, per-ability cooldowns, one Conduit Power per element.

Target: **Paper 1.21.4** (should also run on later 1.21.x Paper builds). Needs **Java 21**.

## Build the .jar (pick one)

**A. No install: GitHub Actions**
1. Make a free GitHub repo and upload this whole folder (including `.github`).
2. Open the repo's **Actions** tab -> "Build plugin" -> wait for the green tick.
3. Open the finished run -> download the `ElementalWeaponArts-jar` artifact -> unzip it.

**B. Local:** install JDK 21 + Maven, then run `mvn package`. Jar appears in `target/`.

## Install
Put the jar in your Paper server's `plugins/` folder and restart.

## Commands
| Command | What it does |
|---|---|
| `/element set <fire\|water\|earth\|wind\|shadow\|light>` | Pick your element (refills Essence) |
| `/element power1` `power2` `power3` `conduit` | Use abilities |
| `/power1` `/power2` `/power3` `/conduit` | Short aliases, easier to bind to keys |
| `/element status` | Show element, Essence, enchant level |
| `/element enchant <0-3>` | (OP) Put your element's Conduit enchant on the held weapon |

Bind keys with a client keybind/macro mod (e.g. Z/X/C/V -> `/power1`, `/power2`, `/power3`, `/conduit`).

## Rules (same as the mod)
- Costs: Power1 10, Power2 15, Power3 20, Conduit 30 Essence. Max 100, regen 2/sec.
- Cooldowns: 3s, 6s, 10s, 15s.
- Conduit only works while holding: Fire = any sword, Water = trident, Earth = mace, Wind = bow, Shadow = crossbow, Light = any axe.
- Conduit enchants: -5% Essence cost and cooldown per level (max 3 = -15%). Nothing else.

## Differences from the Fabric mod
- Conduit enchants are item tags + lore (plugins can't register true vanilla enchantments), so they don't appear in the enchanting table. Ops apply them with `/element enchant`.
- Cooldowns reset on server restart; element and Essence are saved on the player.
