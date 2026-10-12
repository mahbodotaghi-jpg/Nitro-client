package dev.nitro.module;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Players on this list are ignored by every combat module. */
public class Friends {
    private static final Set<String> NAMES = new LinkedHashSet<>();

    public static boolean add(String name) { return NAMES.add(name.toLowerCase(Locale.ROOT)); }
    public static boolean remove(String name) { return NAMES.remove(name.toLowerCase(Locale.ROOT)); }
    public static Set<String> all() { return NAMES; }

    public static void set(Collection<String> names) {
        NAMES.clear();
        for (String n : names) NAMES.add(n.toLowerCase(Locale.ROOT));
    }

    public static boolean is(Entity e) {
        return e instanceof PlayerEntity
                && NAMES.contains(e.getName().getString().toLowerCase(Locale.ROOT));
    }
}
