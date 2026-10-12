package dev.nitro.gui;

import dev.nitro.NitroClient;
import dev.nitro.config.Config;
import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.module.setting.BoolSetting;
import dev.nitro.module.setting.NumberSetting;
import dev.nitro.module.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Nitro click GUI.
 *  - Drag a category header to move it, right click it to collapse.
 *  - Left click a module to toggle, right click to expand its settings.
 *  - Number settings are sliders (click/drag). Bool settings are switches.
 *  - First row of an expanded module is its keybind: click it, then press a key.
 *  - Just type to search. Right Shift closes the GUI.
 */
public class ClickGui extends Screen {
    private static final int W = 110, HEAD = 16, ROW = 15, GAP = 6;
    private static final int ACCENT = Hud.ACCENT;

    // Remembered between openings (until the game is closed)
    private static final Map<Category, int[]> POS = new EnumMap<>(Category.class);
    private static final Set<Category> COLLAPSED = EnumSet.noneOf(Category.class);
    private static final Set<Module> EXPANDED = new HashSet<>();

    private final Map<String, Float> hover = new HashMap<>();
    private final Map<Module, Float> onAnim = new HashMap<>();

    private String query = "";
    private double scroll;
    private Category dragging;
    private int dragOffX, dragOffY;
    private NumberSetting slider;
    private int sliderX;
    private Module listening;

    private enum Kind { HEADER, MODULE, BIND, SETTING }

    private record Row(Kind kind, Category cat, int x, int y, int w, int h, Module module, Setting setting) {
        boolean hit(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        String key() {
            return kind + ":" + (setting != null ? System.identityHashCode(setting)
                    : module != null ? module.getName() : cat.name());
        }
    }

    public ClickGui() {
        super(Text.literal("Nitro"));
    }

    @Override
    protected void init() {
        Category[] cats = Category.values();
        int perRow = Math.max(1, (width - GAP) / (W + GAP));
        for (int i = 0; i < cats.length; i++) {
            if (POS.containsKey(cats[i])) continue;
            int col = i % perRow, row = i / perRow;
            POS.put(cats[i], new int[]{GAP + col * (W + GAP), 30 + row * 150});
        }
    }

    // ---------------------------------------------------------------- layout

    private List<Module> modulesOf(Category c) {
        List<Module> out = new ArrayList<>();
        String q = query.toLowerCase();
        for (Module m : NitroClient.modules.all()) {
            if (m.getCategory() == c && (q.isEmpty() || m.getName().toLowerCase().contains(q))) out.add(m);
        }
        return out;
    }

    private List<Row> layout() {
        List<Row> out = new ArrayList<>();
        for (Category c : Category.values()) {
            List<Module> mods = modulesOf(c);
            if (!query.isEmpty() && mods.isEmpty()) continue;
            int[] p = POS.get(c);
            int x = p[0], y = p[1] + (int) scroll;

            out.add(new Row(Kind.HEADER, c, x, y, W, HEAD, null, null));
            y += HEAD;
            if (COLLAPSED.contains(c) && query.isEmpty()) continue;

            for (Module m : mods) {
                out.add(new Row(Kind.MODULE, c, x, y, W, ROW, m, null));
                y += ROW;
                if (EXPANDED.contains(m)) {
                    out.add(new Row(Kind.BIND, c, x, y, W, ROW, m, null));
                    y += ROW;
                    for (Setting s : m.getSettings()) {
                        out.add(new Row(Kind.SETTING, c, x, y, W, ROW, m, s));
                        y += ROW;
                    }
                }
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- rendering

    private static int lerp(int a, int b, float t) {
        int r = 0;
        for (int s = 0; s <= 24; s += 8) {
            int ca = (a >> s) & 255, cb = (b >> s) & 255;
            r |= ((int) (ca + (cb - ca) * t) & 255) << s;
        }
        return r;
    }

    private static String keyName(int code) {
        if (code <= 0) return "NONE";
        return InputUtil.Type.KEYSYM.createFromCode(code).getLocalizedText().getString().toUpperCase();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        // title + search bar
        ctx.drawTextWithShadow(textRenderer, "NITRO", GAP, 8, ACCENT);
        int sx = width / 2 - 80;
        ctx.fill(sx - 1, 5, sx + 161, 20, ACCENT);
        ctx.fill(sx, 6, sx + 160, 19, 0xFF0E141B);
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (query.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "Type to search...", sx + 5, 9, 0xFF667380);
        } else {
            ctx.drawTextWithShadow(textRenderer, query + (blink ? "_" : ""), sx + 5, 9, 0xFFFFFFFF);
        }

        List<Row> rows = layout();

        // panel backdrops
        Map<Category, int[]> bounds = new EnumMap<>(Category.class);
        for (Row r : rows) {
            int[] b = bounds.computeIfAbsent(r.cat(), k -> new int[]{r.y(), r.y() + r.h()});
            b[1] = Math.max(b[1], r.y() + r.h());
        }
        for (Map.Entry<Category, int[]> e : bounds.entrySet()) {
            int x = POS.get(e.getKey())[0];
            ctx.fill(x - 1, e.getValue()[0] - 1, x + W + 1, e.getValue()[1] + 1, 0xF00A0F14);
        }

        String tooltip = null;
        for (Row r : rows) {
            boolean over = r.hit(mouseX, mouseY);
            float h = hover.getOrDefault(r.key(), 0f);
            h += ((over ? 1f : 0f) - h) * 0.3f;
            hover.put(r.key(), h);

            switch (r.kind()) {
                case HEADER -> {
                    ctx.fillGradient(r.x(), r.y(), r.x() + r.w(), r.y() + r.h(), 0xFF1D2A37, 0xFF121A23);
                    ctx.fill(r.x(), r.y() + r.h() - 1, r.x() + r.w(), r.y() + r.h(), ACCENT);
                    ctx.drawTextWithShadow(textRenderer, r.cat().label, r.x() + 5, r.y() + 4, ACCENT);
                    String ind = COLLAPSED.contains(r.cat()) ? "+" : "-";
                    ctx.drawTextWithShadow(textRenderer, ind, r.x() + r.w() - 10, r.y() + 4, 0xFF8A97A5);
                }
                case MODULE -> {
                    Module m = r.module();
                    float a = onAnim.getOrDefault(m, 0f);
                    a += ((m.isEnabled() ? 1f : 0f) - a) * 0.25f;
                    onAnim.put(m, a);

                    ctx.fill(r.x(), r.y(), r.x() + r.w(), r.y() + r.h(),
                            lerp(0xFF141C25, 0xFF212D3B, h));
                    if (a > 0.02f) {
                        int accentBar = lerp(0x00000000 | (ACCENT & 0xFFFFFF), ACCENT, a);
                        ctx.fill(r.x(), r.y(), r.x() + 2, r.y() + r.h(), accentBar);
                    }
                    int textColor = lerp(0xFFAAB3BC, ACCENT, a);
                    ctx.drawTextWithShadow(textRenderer, m.getName(), r.x() + 6, r.y() + 4, textColor);
                    ctx.drawTextWithShadow(textRenderer, EXPANDED.contains(m) ? "-" : "+",
                            r.x() + r.w() - 10, r.y() + 4, 0xFF5E6B78);
                    if (over) tooltip = m.getDescription();
                }
                case BIND -> {
                    ctx.fill(r.x(), r.y(), r.x() + r.w(), r.y() + r.h(), lerp(0xFF0F161D, 0xFF1A2530, h));
                    boolean wait = listening == r.module();
                    String t = wait ? "Bind: press a key..." : "Bind: " + keyName(r.module().getKey());
                    ctx.drawTextWithShadow(textRenderer, t, r.x() + 10, r.y() + 4,
                            wait ? 0xFFFFD866 : 0xFF8F9BA8);
                }
                case SETTING -> {
                    ctx.fill(r.x(), r.y(), r.x() + r.w(), r.y() + r.h(), lerp(0xFF0F161D, 0xFF1A2530, h));
                    Setting s = r.setting();
                    if (s instanceof NumberSetting n) {
                        double frac = (n.get() - n.min) / (n.max - n.min);
                        int fillW = (int) (r.w() * Math.max(0, Math.min(1, frac)));
                        ctx.fill(r.x(), r.y(), r.x() + fillW, r.y() + r.h(), (ACCENT & 0xFFFFFF) | 0x55000000);
                        ctx.fill(r.x() + Math.max(0, fillW - 1), r.y(), r.x() + fillW + 1, r.y() + r.h(), ACCENT);
                        ctx.drawTextWithShadow(textRenderer, n.name, r.x() + 8, r.y() + 4, 0xFFCCD3DA);
                        String v = n.valueString();
                        ctx.drawTextWithShadow(textRenderer, v,
                                r.x() + r.w() - 6 - textRenderer.getWidth(v), r.y() + 4, 0xFFFFFFFF);
                    } else if (s instanceof BoolSetting b) {
                        ctx.drawTextWithShadow(textRenderer, b.name, r.x() + 8, r.y() + 4, 0xFFCCD3DA);
                        int tx = r.x() + r.w() - 24, ty = r.y() + 4;
                        boolean on = b.get();
                        ctx.fill(tx, ty, tx + 16, ty + 8, on ? ACCENT : 0xFF3A4450);
                        int kx = on ? tx + 9 : tx + 1;
                        ctx.fill(kx, ty + 1, kx + 6, ty + 7, 0xFFFFFFFF);
                    }
                }
            }
        }

        if (tooltip != null) {
            int tw = textRenderer.getWidth(tooltip);
            ctx.fill(mouseX + 8, mouseY - 3, mouseX + 14 + tw, mouseY + 11, 0xF0000000);
            ctx.fill(mouseX + 8, mouseY - 3, mouseX + 9, mouseY + 11, ACCENT);
            ctx.drawTextWithShadow(textRenderer, tooltip, mouseX + 11, mouseY, 0xFFFFFFFF);
        }

        ctx.drawTextWithShadow(textRenderer,
                "Drag headers to move  |  Left: toggle  |  Right: settings  |  Scroll  |  Right Shift: close",
                GAP, height - 12, 0xFF667380);
    }

    // ---------------------------------------------------------------- input

    private void setSlider(double mx) {
        if (slider == null) return;
        double f = Math.max(0, Math.min(1, (mx - sliderX) / W));
        double v = slider.min + f * (slider.max - slider.min);
        v = Math.round((v - slider.min) / slider.step) * slider.step + slider.min;
        slider.set(v);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        List<Row> rows = layout();
        for (int i = rows.size() - 1; i >= 0; i--) {
            Row r = rows.get(i);
            if (!r.hit(mx, my)) continue;
            listening = null;

            switch (r.kind()) {
                case HEADER -> {
                    if (button == 0) {
                        int[] p = POS.get(r.cat());
                        dragging = r.cat();
                        dragOffX = (int) mx - p[0];
                        dragOffY = (int) my - (p[1] + (int) scroll);
                    } else if (button == 1) {
                        if (!COLLAPSED.remove(r.cat())) COLLAPSED.add(r.cat());
                    }
                }
                case MODULE -> {
                    if (button == 0) r.module().toggle();
                    else if (button == 1 && !EXPANDED.remove(r.module())) EXPANDED.add(r.module());
                }
                case BIND -> {
                    if (button == 0) {
                        listening = r.module();
                    } else if (button == 1) {
                        r.module().setKey(-1);
                        Config.save();
                    }
                }
                case SETTING -> {
                    if (r.setting() instanceof NumberSetting n) {
                        if (button == 0) {
                            slider = n;
                            sliderX = r.x();
                            setSlider(mx);
                        } else if (button == 1) {
                            n.cycle(-1);
                            Config.save();
                        }
                    } else {
                        r.setting().cycle(1);
                        Config.save();
                    }
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            int[] p = POS.get(dragging);
            p[0] = (int) mx - dragOffX;
            p[1] = (int) my - dragOffY - (int) scroll;
            return true;
        }
        if (slider != null) {
            setSlider(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (slider != null) Config.save();
        slider = null;
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        scroll += vertical * 16;
        int bottom = 0;
        for (Row r : layout()) bottom = Math.max(bottom, r.y() + r.h() - (int) scroll);
        double limit = Math.min(0, height - bottom - 24);
        scroll = Math.max(limit, Math.min(0, scroll));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listening = null;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                listening.setKey(-1);
            } else if (keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT) {
                listening.setKey(keyCode);
            }
            Config.save();
            listening = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !query.isEmpty()) {
            query = "";
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) {
            query = query.substring(0, query.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (listening != null) return true;
        if (chr >= 32 && chr != 127 && query.length() < 20) {
            query += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
