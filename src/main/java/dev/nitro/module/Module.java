package dev.nitro.module;

import dev.nitro.module.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting> settings = new ArrayList<>();
    private boolean enabled;
    private int key = -1;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    /** Called when switched on. May be called with no world loaded, so null-check mc.player. */
    public void onEnable() {}

    public void onDisable() {}

    /** Called every client tick while enabled and in a world (player and world are non-null). */
    public void onTick() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public List<Setting> getSettings() { return settings; }
    public boolean isEnabled() { return enabled; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }

    public Setting getSetting(String n) {
        for (Setting s : settings) if (s.name.equalsIgnoreCase(n)) return s;
        return null;
    }
}
