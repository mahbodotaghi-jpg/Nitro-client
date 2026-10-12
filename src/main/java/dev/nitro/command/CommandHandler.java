package dev.nitro.command;

import dev.nitro.NitroClient;
import dev.nitro.config.Config;
import dev.nitro.module.Friends;
import dev.nitro.module.Module;
import dev.nitro.module.setting.Setting;
import dev.nitro.util.Chat;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.StringJoiner;

public class CommandHandler {
    public static final String PREFIX = ".";

    /** @return true if the message was a Nitro command (and must not be sent to the server). */
    public static boolean handle(String message) {
        if (!message.startsWith(PREFIX) || message.startsWith("..")) return false;
        if (NitroClient.modules == null) return false;

        String[] a = message.substring(1).trim().split("\\s+");
        String cmd = a[0].toLowerCase();

        switch (cmd) {
            case "help" -> {
                Chat.send(".modules  - list all modules");
                Chat.send(".toggle <module>  (or just .<module>)");
                Chat.send(".bind <module> <key|none>   e.g. .bind flight g");
                Chat.send(".set <module> <setting> <value>");
                Chat.send(".settings <module>");
                Chat.send(".friend add|remove|list [name]  - friends are ignored by combat modules");
                Chat.send(".panic  - disable everything");
                Chat.send("Right Shift opens the GUI.");
            }
            case "modules" -> {
                StringJoiner j = new StringJoiner(", ");
                for (Module m : NitroClient.modules.all())
                    j.add((m.isEnabled() ? "\u00a7a" : "\u00a77") + m.getName() + "\u00a7f");
                Chat.send(j.toString());
            }
            case "panic" -> {
                NitroClient.modules.disableAll();
                Chat.send("All modules disabled.");
            }
            case "toggle", "t" -> {
                Module m = need(a, 1);
                if (m != null) toggle(m);
            }
            case "bind" -> {
                Module m = need(a, 1);
                if (m == null) return true;
                if (a.length < 3) {
                    Chat.send("Usage: .bind <module> <key|none>");
                    return true;
                }
                int code;
                if (a[2].equalsIgnoreCase("none")) {
                    code = GLFW.GLFW_KEY_UNKNOWN;
                } else {
                    try {
                        code = InputUtil.fromTranslationKey("key.keyboard." + a[2].toLowerCase()).getCode();
                    } catch (Exception e) {
                        Chat.send("Unknown key: " + a[2]);
                        return true;
                    }
                }
                m.setKey(code);
                Config.save();
                Chat.send(m.getName() + " bound to " + (code < 0 ? "nothing" : a[2].toUpperCase()));
            }
            case "friend", "friends" -> {
                String sub = a.length > 1 ? a[1].toLowerCase() : "list";
                switch (sub) {
                    case "add" -> {
                        if (a.length < 3) Chat.send("Usage: .friend add <name>");
                        else {
                            Friends.add(a[2]);
                            Config.save();
                            Chat.send("Added friend " + a[2]);
                        }
                    }
                    case "remove", "del" -> {
                        if (a.length < 3) Chat.send("Usage: .friend remove <name>");
                        else {
                            Chat.send(Friends.remove(a[2]) ? "Removed " + a[2] : a[2] + " was not a friend");
                            Config.save();
                        }
                    }
                    default -> Chat.send("Friends: " + (Friends.all().isEmpty() ? "(none)" : String.join(", ", Friends.all())));
                }
            }
            case "settings" -> {
                Module m = need(a, 1);
                if (m == null) return true;
                if (m.getSettings().isEmpty()) Chat.send(m.getName() + " has no settings.");
                for (Setting s : m.getSettings()) Chat.send(m.getName() + "." + s.name + " = " + s.valueString());
            }
            case "set" -> {
                Module m = need(a, 1);
                if (m == null) return true;
                if (a.length < 4) {
                    Chat.send("Usage: .set <module> <setting> <value>");
                    return true;
                }
                Setting s = m.getSetting(a[2]);
                if (s == null) {
                    Chat.send("No such setting. Try .settings " + m.getName());
                } else if (s.setFromString(a[3])) {
                    Config.save();
                    Chat.send(m.getName() + "." + s.name + " = " + s.valueString());
                } else {
                    Chat.send("Invalid value: " + a[3]);
                }
            }
            default -> {
                // ".flight" is shorthand for ".toggle flight"
                Module m = NitroClient.modules.get(cmd);
                if (m != null) toggle(m);
                else Chat.send("Unknown command. Type .help");
            }
        }
        return true;
    }

    private static void toggle(Module m) {
        m.toggle();
        Chat.send(m.getName() + (m.isEnabled() ? " \u00a7aenabled" : " \u00a7cdisabled"));
    }

    private static Module need(String[] a, int i) {
        if (a.length <= i) {
            Chat.send("Missing module name. Type .modules");
            return null;
        }
        Module m = NitroClient.modules.get(a[i]);
        if (m == null) Chat.send("Unknown module: " + a[i]);
        return m;
    }
}
