package dev.nitro;

import dev.nitro.command.CommandHandler;
import dev.nitro.config.Config;
import dev.nitro.gui.ClickGui;
import dev.nitro.gui.Hud;
import dev.nitro.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NitroClient implements ClientModInitializer {
    public static final String NAME = "Nitro Client";
    public static final Logger LOG = LoggerFactory.getLogger("Nitro");

    /** Created once the client has finished starting (null before that). */
    public static ModuleManager modules;

    private boolean guiKeyWasDown;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            modules = new ModuleManager();
            Config.load();
            LOG.info("{} loaded with {} modules", NAME, modules.all().size());
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);

        // Messages starting with "." are Nitro commands and are never sent to the server.
        ClientSendMessageEvents.ALLOW_CHAT.register(msg -> !CommandHandler.handle(msg));

        Hud.register();
    }

    private void tick(MinecraftClient mc) {
        if (modules == null || mc.player == null || mc.world == null) return;

        boolean down = InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (down && !guiKeyWasDown && mc.currentScreen == null) {
            mc.setScreen(new ClickGui());
        }
        guiKeyWasDown = down;

        modules.tick();
    }
}
