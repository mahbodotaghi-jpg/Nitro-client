package dev.nitro.module.setting;

public abstract class Setting {
    public final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public abstract String valueString();

    public abstract boolean setFromString(String s);

    /** Used by the GUI: +1 for left click, -1 for right click. */
    public abstract void cycle(int direction);
}
