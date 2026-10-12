package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.NumberSetting;
import net.minecraft.entity.player.PlayerAbilities;
import org.lwjgl.glfw.GLFW;

public class Flight extends Module {
    private final NumberSetting speed = add(new NumberSetting("speed", 1.0, 0.1, 10.0, 0.1));

    public Flight() {
        super("Flight", "Lets you fly like in creative mode.", Category.MOVEMENT);
        setKey(GLFW.GLFW_KEY_G);
    }

    @Override
    public void onTick() {
        PlayerAbilities a = mc.player.getAbilities();
        a.allowFlying = true;
        a.flying = true;
        a.setFlySpeed(speed.getF() * 0.05f);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        PlayerAbilities a = mc.player.getAbilities();
        a.setFlySpeed(0.05f);
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            a.allowFlying = false;
            a.flying = false;
        }
    }
}
