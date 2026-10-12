package dev.nitro.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.nitro.NitroClient;
import dev.nitro.module.Friends;
import dev.nitro.module.Module;
import dev.nitro.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Saves keybinds and settings to config/nitro.json. Enabled state is intentionally NOT saved. */
public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Map<String, Entry> modules = new LinkedHashMap<>();
    public List<String> friends = new ArrayList<>();

    public static class Entry {
        public int key = -1;
        public Map<String, String> settings = new LinkedHashMap<>();
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("nitro.json");
    }

    public static void save() {
        if (NitroClient.modules == null) return;
        Config cfg = new Config();
        cfg.friends = new ArrayList<>(Friends.all());
        for (Module m : NitroClient.modules.all()) {
            Entry e = new Entry();
            e.key = m.getKey();
            for (Setting s : m.getSettings()) e.settings.put(s.name, s.valueString());
            cfg.modules.put(m.getName(), e);
        }
        try (Writer w = Files.newBufferedWriter(path())) {
            GSON.toJson(cfg, w);
        } catch (Exception ex) {
            NitroClient.LOG.error("Could not save config", ex);
        }
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p) || NitroClient.modules == null) return;
        try (Reader r = Files.newBufferedReader(p)) {
            Config cfg = GSON.fromJson(r, Config.class);
            if (cfg == null || cfg.modules == null) return;
            if (cfg.friends != null) Friends.set(cfg.friends);
            for (Module m : NitroClient.modules.all()) {
                Entry e = cfg.modules.get(m.getName());
                if (e == null) continue;
                m.setKey(e.key);
                if (e.settings == null) continue;
                for (Setting s : m.getSettings()) {
                    String v = e.settings.get(s.name);
                    if (v != null) s.setFromString(v);
                }
            }
        } catch (Exception ex) {
            NitroClient.LOG.error("Could not load config", ex);
        }
    }
}
