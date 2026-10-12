package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.NumberSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Module {
    private final NumberSetting height = add(new NumberSetting("height", 1.0, 0.6, 5.0, 0.5));

    public Step() {
        super("Step", "Walk up blocks without jumping.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        EntityAttributeInstance attr = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(height.get());
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        EntityAttributeInstance attr = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(0.6);
    }
}
