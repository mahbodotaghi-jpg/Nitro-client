package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.BoolSetting;
import dev.nitro.module.setting.NumberSetting;
import dev.nitro.util.Targets;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {
    private final NumberSetting range = add(new NumberSetting("range", 4.5, 1.0, 8.0, 0.25));
    private final NumberSetting fov = add(new NumberSetting("fov", 90, 10, 360, 10));
    private final NumberSetting strength = add(new NumberSetting("strength", 0.25, 0.05, 1.0, 0.05));
    private final BoolSetting onlyWhileAttacking = add(new BoolSetting("onlyWhileAttacking", true));
    private final BoolSetting players = add(new BoolSetting("players", true));
    private final BoolSetting mobs = add(new BoolSetting("mobs", true));

    public AimAssist() {
        super("AimAssist", "Smoothly pulls your aim toward the nearest target.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (onlyWhileAttacking.get() && !mc.options.attackKey.isPressed()) return;
        ClientPlayerEntity p = mc.player;

        Entity best = null;
        double bestAngle = Double.MAX_VALUE;
        float bestYawDiff = 0, bestPitchDiff = 0;

        for (Entity e : mc.world.getEntities()) {
            if (!Targets.valid(e, players.get(), mobs.get())) continue;
            if (p.squaredDistanceTo(e) > range.get() * range.get()) continue;

            Vec3d eye = p.getEyePos();
            Vec3d t = e.getBoundingBox().getCenter();
            double dx = t.x - eye.x, dy = t.y - eye.y, dz = t.z - eye.z;
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

            float dYaw = MathHelper.wrapDegrees(yaw - p.getYaw());
            float dPitch = pitch - p.getPitch();
            if (Math.abs(dYaw) > fov.get() / 2.0) continue;

            double angle = Math.hypot(dYaw, dPitch);
            if (angle < bestAngle) {
                bestAngle = angle;
                best = e;
                bestYawDiff = dYaw;
                bestPitchDiff = dPitch;
            }
        }

        if (best == null) return;
        float s = strength.getF();
        p.setYaw(p.getYaw() + bestYawDiff * s);
        p.setPitch(MathHelper.clamp(p.getPitch() + bestPitchDiff * s, -90f, 90f));
    }
}
