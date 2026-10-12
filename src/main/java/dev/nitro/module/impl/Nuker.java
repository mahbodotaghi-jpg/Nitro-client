package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Nuker extends Module {
    private final NumberSetting range = add(new NumberSetting("range", 4.0, 1.0, 6.0, 0.5));
    private BlockPos target;

    public Nuker() {
        super("Nuker", "Breaks blocks around you.", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.interactionManager == null) return;
        boolean creative = mc.interactionManager.getCurrentGameMode().isCreative();
        double r = range.get();
        double rSq = r * r;
        BlockPos center = mc.player.getBlockPos();
        int ri = (int) Math.ceil(r);

        List<BlockPos> candidates = new ArrayList<>();
        for (int x = -ri; x <= ri; x++)
            for (int y = -ri; y <= ri; y++)
                for (int z = -ri; z <= ri; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (pos.getSquaredDistance(mc.player.getEyePos()) > rSq) continue;
                    BlockState st = mc.world.getBlockState(pos);
                    if (st.isAir() || st.getHardness(mc.world, pos) < 0) continue;
                    if (!st.getFluidState().isEmpty()) continue;
                    candidates.add(pos);
                }
        if (candidates.isEmpty()) {
            target = null;
            return;
        }
        candidates.sort(Comparator.comparingDouble(p -> p.getSquaredDistance(mc.player.getEyePos())));

        if (creative) {
            for (int i = 0; i < Math.min(8, candidates.size()); i++) {
                mc.interactionManager.attackBlock(candidates.get(i), Direction.UP);
            }
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            // Survival: keep mining one block until it breaks, then pick the next one.
            if (target == null || !candidates.contains(target)) target = candidates.get(0);
            mc.interactionManager.updateBlockBreakingProgress(target, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    @Override
    public void onDisable() {
        target = null;
        if (mc.interactionManager != null) mc.interactionManager.cancelBlockBreaking();
    }
}
