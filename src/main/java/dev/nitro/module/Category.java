package dev.nitro.module;

public enum Category {
    MOVEMENT("Movement"),
    COMBAT("Combat"),
    RENDER("Render"),
    WORLD("World"),
    UTILITY("Utility");

    public final String label;

    Category(String label) { this.label = label; }
}
