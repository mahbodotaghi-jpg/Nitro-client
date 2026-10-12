package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ESP extends Module {
    private final BoolSetting players = add(new BoolSetting("players", true));
    private final BoolSetting mobs = add(new BoolSetting("mobs", true));
    private final BoolSetting animals = add(new BoolSetting("animals", false));

    public ESP() {
        super("ESP", "Outlines entities through walls (glow effect).", Category.RENDER);
    }

    private boolean matches(Entity e) {
        if (e == mc.player || !(e instanceof LivingEntity)) return false;
        if (e instanceof PlayerEntity) return players.get();
        if (e instanceof HostileEntity) return mobs.get();
        return animals.get();
    }

    @Override
    public void onTick() {
        for (Entity e : mc.world.getEntities()) {
            e.setGlowing(matches(e));
        }
    }

    @Override
    public void onDisable() {
        if (mc.world == null) return;
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof LivingEntity && e != mc.player) e.setGlowing(false);
        }
    }
}
