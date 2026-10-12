package dev.nitro.module.setting;

public class NumberSetting extends Setting {
    private double value;
    public final double min, max, step;

    public NumberSetting(String name, double value, double min, double max, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        set(value);
    }

    public double get() { return value; }
    public float getF() { return (float) value; }
    public int getInt() { return (int) Math.round(value); }

    public void set(double v) {
        v = Math.round(v * 1000.0) / 1000.0;
        this.value = Math.max(min, Math.min(max, v));
    }

    @Override
    public String valueString() {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    @Override
    public boolean setFromString(String s) {
        try {
            set(Double.parseDouble(s));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void cycle(int direction) {
        set(value + direction * step);
    }
}
