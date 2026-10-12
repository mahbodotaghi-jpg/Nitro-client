package dev.nitro.mixin;

import dev.nitro.module.impl.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * X-Ray: decides which block faces get drawn. Both overloads of shouldDrawSide are targeted with
 * require = 0, so if one doesn't exist in your version it is skipped instead of crashing the game.
 */
@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(
        method = "shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z",
        at = @At("RETURN"), cancellable = true, require = 0)
    private static void nitro$xray3(BlockState state, BlockState other, Direction side,
                                    CallbackInfoReturnable<Boolean> cir) {
        XRay.apply(state, cir);
    }

    @Inject(
        method = "shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("RETURN"), cancellable = true, require = 0)
    private static void nitro$xray5(BlockState state, BlockView world, BlockPos pos, Direction side,
                                    BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        XRay.apply(state, cir);
    }
}
