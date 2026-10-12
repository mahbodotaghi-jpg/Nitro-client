# Nitro Client (Fabric, Minecraft 1.21.4)

A utility client for **singleplayer and servers you own / have permission to use it on**.
Written from scratch (no code copied from Wurst or any other client).

## Build
1. Install **JDK 21**.
2. This zip has no Gradle wrapper. Either run `gradle wrapper --gradle-version 8.12` in this folder
   (needs Gradle installed), or copy `gradlew`, `gradlew.bat` and the `gradle/` folder from the
   official Fabric example mod (https://github.com/FabricMC/fabric-example-mod).
3. `./gradlew build`  ->  jar is in `build/libs/nitro-client-1.0.0.jar` (use the one WITHOUT `-sources`).
4. Put the jar and **Fabric API 0.119.2+1.21.4** in your `mods` folder (Fabric Loader 0.16.9+).

To run from your IDE instead: `./gradlew runClient`.

## Usage
- **Right Shift**: open the GUI. Drag headers to move panels (right click a header to collapse), left click a module to toggle,
  right click it to expand settings (sliders, switches, and a Bind row: click it, then press a key). Type to search, scroll with the wheel.
- **Chat commands** (prefix `.`, never sent to the server):
  `.help`, `.modules`, `.toggle <mod>` (or `.<mod>`), `.bind <mod> <key|none>`,
  `.set <mod> <setting> <value>`, `.settings <mod>`, `.friend add|remove|list <name>`, `.panic`
- Default binds: Flight=G, Speed=H, KillAura=R, Fullbright=B, XRay=X. Change with `.bind`.
- Keybinds and settings are saved in `config/nitro.json`. Enabled state is not saved.

## Modules
Movement: Flight, Speed, Sprint, Step, NoFall, Jesus
Combat:   KillAura, AutoTotem, AutoCrystal (C), AimAssist, TriggerBot
Render:   Fullbright, XRay, ESP
World:    Nuker, AutoTool, FastPlace
Utility:  AutoEat

## Adding a module
Create a class in `dev.nitro.module.impl` extending `Module`, override `onEnable/onDisable/onTick`,
add settings with `add(new NumberSetting(...))`, and register it in `ModuleManager`.

## Notes
- XRay uses a mixin on `Block.shouldDrawSide`. It will not work with Sodium/Iris-style renderers.
  Both overloads are injected with `require = 0`, so a missing method just disables XRay instead of crashing.
- Flight/Speed/etc. will get you kicked or banned on most public servers. That's expected.

## Combat pack (singleplayer testing)
- AutoCrystal needs end crystals in your hotbar/off-hand and obsidian or bedrock nearby. Damage is estimated from
  distance only (no armor/exposure). Test on hostile mobs (`mobs` on) or a fake player (e.g. Carpet's `/player <name> spawn`).
- AimAssist / TriggerBot / KillAura skip anyone on your friends list.
