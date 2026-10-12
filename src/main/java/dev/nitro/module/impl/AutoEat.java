package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.NumberSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Set;

public class AutoEat extends Module {
    private static final Set<Item> AVOID = Set.of(
        Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO, Items.PUFFERFISH,
        Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.SUSPICIOUS_STEW,
        Items.CHORUS_FRUIT, Items.HONEY_BOTTLE
    );

    private final NumberSetting hunger = add(new NumberSetting("hunger", 14, 1, 19, 1));
    private boolean eating;
    private int prevSlot = -1;

    public AutoEat() {
        super("AutoEat", "Eats food when you get hungry.", Category.UTILITY);
    }

    private static boolean isGoodFood(ItemStack s) {
        return !s.isEmpty() && s.contains(DataComponentTypes.FOOD) && !AVOID.contains(s.getItem());
    }

    @Override
    public void onTick() {
        int food = mc.player.getHungerManager().getFoodLevel();
        PlayerInventory inv = mc.player.getInventory();

        if (eating) {
            if (food >= 20 || !isGoodFood(mc.player.getMainHandStack())) stop();
            else mc.options.useKey.setPressed(true);
            return;
        }

        if (food > hunger.getInt() || mc.player.isUsingItem()) return;

        for (int i = 0; i < 9; i++) {
            if (isGoodFood(inv.getStack(i))) {
                prevSlot = inv.selectedSlot;
                inv.selectedSlot = i;
                eating = true;
                mc.options.useKey.setPressed(true);
                return;
            }
        }
    }

    private void stop() {
        mc.options.useKey.setPressed(false);
        if (mc.player != null && prevSlot >= 0) mc.player.getInventory().selectedSlot = prevSlot;
        prevSlot = -1;
        eating = false;
    }

    @Override
    public void onDisable() {
        if (eating) stop();
    }
}
