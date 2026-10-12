package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Sprints automatically while moving forward.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.options.forwardKey.isPressed()
                && !mc.player.isSneaking()
                && !mc.player.horizontalCollision
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
