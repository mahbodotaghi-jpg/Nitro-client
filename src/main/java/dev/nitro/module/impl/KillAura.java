package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Friends;
import dev.nitro.module.Module;
import dev.nitro.module.setting.BoolSetting;
import dev.nitro.module.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class KillAura extends Module {
    private final NumberSetting range = add(new NumberSetting("range", 4.0, 1.0, 6.0, 0.25));
    private final BoolSetting players = add(new BoolSetting("players", true));
    private final BoolSetting mobs = add(new BoolSetting("mobs", true));
    private final BoolSetting animals = add(new BoolSetting("animals", false));

    public KillAura() {
        super("KillAura", "Attacks nearby entities automatically.", Category.COMBAT);
        setKey(GLFW.GLFW_KEY_R);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        if (p.getAttackCooldownProgress(0.5f) < 1.0f) return;

        double maxSq = range.get() * range.get();
        Entity best = null;
        double bestSq = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (e == p || !(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (e instanceof PlayerEntity pe && pe.isSpectator()) continue;
            if (Friends.is(e)) continue;
            boolean ok = (e instanceof PlayerEntity && players.get())
                      || (e instanceof HostileEntity && mobs.get())
                      || (e instanceof AnimalEntity && animals.get());
            if (!ok) continue;
            double d = p.squaredDistanceTo(e);
            if (d <= maxSq && d < bestSq) {
                best = e;
                bestSq = d;
            }
        }

        if (best != null) {
            mc.interactionManager.attackEntity(p, best);
            p.swingHand(Hand.MAIN_HAND);
        }
    }
}
