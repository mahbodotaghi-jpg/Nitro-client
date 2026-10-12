package dev.nitro.module.setting;

public class BoolSetting extends Setting {
    private boolean value;

    public BoolSetting(String name, boolean value) {
        super(name);
        this.value = value;
    }

    public boolean get() { return value; }

    @Override
    public String valueString() { return value ? "on" : "off"; }

    @Override
    public boolean setFromString(String s) {
        switch (s.toLowerCase()) {
            case "true", "on", "yes", "1" -> value = true;
            case "false", "off", "no", "0" -> value = false;
            default -> { return false; }
        }
        return true;
    }

    @Override
    public void cycle(int direction) { value = !value; }
}
