package dev.nitro.util;

import dev.nitro.module.Friends;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Targets {
    public static boolean valid(Entity e, boolean players, boolean mobs) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) return false;
        if (Friends.is(e)) return false;
        if (e instanceof PlayerEntity pe) return players && !pe.isSpectator();
        return mobs && e instanceof HostileEntity;
    }
}
