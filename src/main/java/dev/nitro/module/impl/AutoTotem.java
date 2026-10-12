package dev.nitro.module.impl;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    private int cooldown;

    public AutoTotem() {
        super("AutoTotem", "Keeps a Totem of Undying in your off-hand.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                // Inventory index -> player screen handler slot (hotbar 0-8 is 36-44, main 9-35 is the same)
                int slot = i < 9 ? i + 36 : i;
                mc.interactionManager.clickSlot(
                    mc.player.playerScreenHandler.syncId, slot, 40, SlotActionType.SWAP, mc.player);
                cooldown = 5;
                return;
            }
        }
    }
}
