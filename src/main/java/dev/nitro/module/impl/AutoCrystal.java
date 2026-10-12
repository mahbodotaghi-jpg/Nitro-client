package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.BoolSetting;
import dev.nitro.module.setting.NumberSetting;
import dev.nitro.util.Targets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Places end crystals on obsidian/bedrock next to a target and detonates them.
 * Damage is estimated from distance only (no armor / exposure), so treat it as a rough guide.
 * Needs end crystals in your hotbar or off-hand. It switches your selected hotbar slot.
 */
public class AutoCrystal extends Module {
    private final NumberSetting range = add(new NumberSetting("range", 4.5, 1.0, 6.0, 0.25));
    private final NumberSetting minDamage = add(new NumberSetting("minDamage", 4.0, 1.0, 20.0, 0.5));
    private final NumberSetting maxSelf = add(new NumberSetting("maxSelfDamage", 6.0, 0.0, 20.0, 0.5));
    private final NumberSetting delay = add(new NumberSetting("delay", 2, 0, 10, 1));
    private final BoolSetting place = add(new BoolSetting("place", true));
    private final BoolSetting breakCrystals = add(new BoolSetting("break", true));
    private final BoolSetting players = add(new BoolSetting("players", true));
    private final BoolSetting mobs = add(new BoolSetting("mobs", true));

    private int timer;

    public AutoCrystal() {
        super("AutoCrystal", "Places and breaks end crystals near targets.", Category.COMBAT);
        setKey(GLFW.GLFW_KEY_C);
    }

    /** Rough vanilla crystal damage (power 6) from distance alone. */
    private static double damage(Vec3d center, LivingEntity e) {
        double d = Math.sqrt(e.getPos().squaredDistanceTo(center)) / 12.0;
        if (d > 1.0) return 0;
        double impact = 1.0 - d;
        return Math.floor((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestSq = 12 * 12;
        for (Entity e : mc.world.getEntities()) {
            if (!Targets.valid(e, players.get(), mobs.get())) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < bestSq) {
                bestSq = d;
                best = (LivingEntity) e;
            }
        }
        return best;
    }

    private boolean selfSafe(Vec3d center) {
        double self = damage(center, mc.player);
        return self < maxSelf.get() && self < mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    @Override
    public void onTick() {
        if (timer > 0) {
            timer--;
            return;
        }
        ClientPlayerEntity p = mc.player;
        LivingEntity target = findTarget();
        if (target == null) return;

        double rSq = range.get() * range.get();

        // 1) detonate an existing crystal
        if (breakCrystals.get()) {
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof EndCrystalEntity c) || p.squaredDistanceTo(c) > rSq) continue;
                Vec3d pos = c.getPos();
                if (damage(pos, target) >= minDamage.get() && selfSafe(pos)) {
                    mc.interactionManager.attackEntity(p, c);
                    p.swingHand(Hand.MAIN_HAND);
                    timer = delay.getInt();
                    return;
                }
            }
        }

        // 2) place a new one
        if (!place.get()) return;
        boolean offhand = p.getOffHandStack().isOf(Items.END_CRYSTAL);
        int slot = -1;
        if (!offhand) {
            for (int i = 0; i < 9; i++) {
                if (p.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) {
                    slot = i;
                    break;
                }
            }
            if (slot < 0) return;
        }

        BlockPos origin = p.getBlockPos();
        int r = (int) Math.ceil(range.get());
        BlockPos best = null;
        double bestScore = -Double.MAX_VALUE;

        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++) {
                    BlockPos b = origin.add(x, y, z);
                    BlockState s = mc.world.getBlockState(b);
                    if (!s.isOf(Blocks.OBSIDIAN) && !s.isOf(Blocks.BEDROCK)) continue;
                    if (p.getEyePos().squaredDistanceTo(Vec3d.ofCenter(b)) > rSq) continue;

                    BlockPos up = b.up();
                    if (!mc.world.getBlockState(up).isAir() || !mc.world.getBlockState(up.up()).isAir()) continue;
                    if (!mc.world.getOtherEntities(null, new Box(up)).isEmpty()) continue;

                    Vec3d crystal = new Vec3d(b.getX() + 0.5, b.getY() + 1, b.getZ() + 0.5);
                    if (!selfSafe(crystal)) continue;
                    double tgt = damage(crystal, target);
                    if (tgt < minDamage.get()) continue;

                    double score = tgt - damage(crystal, p);
                    if (score > bestScore) {
                        bestScore = score;
                        best = b;
                    }
                }

        if (best == null) return;

        Hand hand = Hand.OFF_HAND;
        if (!offhand) {
            p.getInventory().selectedSlot = slot;
            hand = Hand.MAIN_HAND;
        }
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(best).add(0, 0.5, 0), Direction.UP, best, false);
        mc.interactionManager.interactBlock(p, hand, hit);
        p.swingHand(hand);
        timer = delay.getInt();
    }
}
