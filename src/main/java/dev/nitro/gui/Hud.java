package dev.nitro.gui;

import dev.nitro.NitroClient;
import dev.nitro.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

import java.util.Comparator;
import java.util.List;

public class Hud {
    public static final int ACCENT = 0xFF3FD0FF;

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (NitroClient.modules == null || mc.options.hudHidden) return;
            TextRenderer tr = mc.textRenderer;

            ctx.drawTextWithShadow(tr, "Nitro", 4, 4, ACCENT);

            List<Module> on = NitroClient.modules.all().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> tr.getWidth(m.getName())).reversed())
                .toList();

            int y = 4;
            int w = ctx.getScaledWindowWidth();
            for (Module m : on) {
                ctx.drawTextWithShadow(tr, m.getName(), w - tr.getWidth(m.getName()) - 4, y, ACCENT);
                y += 10;
            }
        });
    }
}
