package dev.nitro.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Chat {
    public static void send(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.inGameHud == null) return;
        mc.inGameHud.getChatHud().addMessage(Text.literal("\u00a7b[Nitro] \u00a7f" + msg));
    }
}
