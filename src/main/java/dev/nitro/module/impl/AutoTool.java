package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
    public AutoTool() {
        super("AutoTool", "Picks the fastest hotbar tool for the block you mine.", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (!mc.options.attackKey.isPressed()) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockState state = mc.world.getBlockState(hit.getBlockPos());
        PlayerInventory inv = mc.player.getInventory();

        int best = inv.selectedSlot;
        float bestSpeed = inv.getStack(best).getMiningSpeedMultiplier(state);
        for (int i = 0; i < 9; i++) {
            float s = inv.getStack(i).getMiningSpeedMultiplier(state);
            if (s > bestSpeed + 0.01f) {
                best = i;
                bestSpeed = s;
            }
        }
        inv.selectedSlot = best;
    }
}
