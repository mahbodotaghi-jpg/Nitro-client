package dev.nitro.module.impl;

import dev.nitro.mixin.MinecraftClientAccessor;
import dev.nitro.module.Category;
import dev.nitro.module.Module;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "Removes the delay between placing blocks.", Category.WORLD);
    }

    @Override
    public void onTick() {
        ((MinecraftClientAccessor) mc).nitro$setItemUseCooldown(0);
    }
}
