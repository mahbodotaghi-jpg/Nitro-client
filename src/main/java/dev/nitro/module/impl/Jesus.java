package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import net.minecraft.util.math.Vec3d;

public class Jesus extends Module {
    public Jesus() {
        super("Jesus", "Float on top of water (sneak to sink).", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player.isTouchingWater() && !mc.player.isSneaking()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.11, v.z);
        }
    }
}
