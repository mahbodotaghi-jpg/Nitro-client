package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.BoolSetting;
import dev.nitro.util.Targets;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class TriggerBot extends Module {
    private final BoolSetting players = add(new BoolSetting("players", true));
    private final BoolSetting mobs = add(new BoolSetting("mobs", true));

    public TriggerBot() {
        super("TriggerBot", "Attacks whatever your crosshair is on when the cooldown is ready.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!Targets.valid(hit.getEntity(), players.get(), mobs.get())) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;
        mc.interactionManager.attackEntity(mc.player, hit.getEntity());
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
