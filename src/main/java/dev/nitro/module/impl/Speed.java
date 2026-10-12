package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;

public class Speed extends Module {
    private final NumberSetting speed = add(new NumberSetting("speed", 0.4, 0.1, 2.0, 0.05));

    public Speed() {
        super("Speed", "Moves you faster horizontally (blocks per tick).", Category.MOVEMENT);
        setKey(GLFW.GLFW_KEY_H);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        double f = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
        double s = (mc.options.leftKey.isPressed() ? 1 : 0) - (mc.options.rightKey.isPressed() ? 1 : 0);
        if (f == 0 && s == 0) return;

        double len = Math.sqrt(f * f + s * s);
        f /= len;
        s /= len;

        double yaw = Math.toRadians(p.getYaw());
        double sin = Math.sin(yaw), cos = Math.cos(yaw);
        double x = -sin * f + cos * s;
        double z = cos * f + sin * s;
        double v = speed.get();

        p.setVelocity(x * v, p.getVelocity().y, z * v);
    }
}
