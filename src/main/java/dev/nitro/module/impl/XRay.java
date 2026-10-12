package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

public class XRay extends Module {
    /** Read by BlockMixin on every block face, so it is a plain static flag. */
    public static volatile boolean active;

    private static final Set<Block> VISIBLE = Set.of(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS,
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
        Blocks.SPAWNER, Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL, Blocks.NETHER_PORTAL
    );

    public XRay() {
        super("XRay", "Shows only ores and valuables. Works best with Fullbright.", Category.RENDER);
        setKey(GLFW.GLFW_KEY_X);
    }

    public static void apply(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (active) cir.setReturnValue(VISIBLE.contains(state.getBlock()));
    }

    @Override
    public void onEnable() {
        active = true;
        if (mc.worldRenderer != null) mc.worldRenderer.reload();
    }

    @Override
    public void onDisable() {
        active = false;
        if (mc.worldRenderer != null) mc.worldRenderer.reload();
    }
}
