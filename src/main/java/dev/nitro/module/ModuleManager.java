package dev.nitro.module;

import dev.nitro.NitroClient;
import dev.nitro.module.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.*;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Module, Boolean> keyDown = new HashMap<>();

    public ModuleManager() {
        // Movement
        modules.add(new Flight());
        modules.add(new Speed());
        modules.add(new Sprint());
        modules.add(new Step());
        modules.add(new NoFall());
        modules.add(new Jesus());
        // Combat
        modules.add(new KillAura());
        modules.add(new AutoTotem());
        modules.add(new AutoCrystal());
        modules.add(new AimAssist());
        modules.add(new TriggerBot());
        // Render
        modules.add(new Fullbright());
        modules.add(new XRay());
        modules.add(new ESP());
        // World
        modules.add(new Nuker());
        modules.add(new AutoTool());
        modules.add(new FastPlace());
        // Utility
        modules.add(new AutoEat());
    }

    public List<Module> all() { return modules; }

    public Module get(String name) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }

    public void disableAll() {
        for (Module m : modules) m.setEnabled(false);
    }

    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Keybinds only fire when no screen (chat, inventory, GUI) is open.
        if (mc.currentScreen == null) {
            long handle = mc.getWindow().getHandle();
            for (Module m : modules) {
                int k = m.getKey();
                if (k <= 0) continue;
                boolean down = InputUtil.isKeyPressed(handle, k);
                boolean was = keyDown.getOrDefault(m, false);
                if (down && !was) m.toggle();
                keyDown.put(m, down);
            }
        }

        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            try {
                m.onTick();
            } catch (Throwable t) {
                NitroClient.LOG.error("Module {} crashed and was disabled", m.getName(), t);
                m.setEnabled(false);
            }
        }
    }
}
