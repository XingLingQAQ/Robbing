package com.frahhs.robbing.item;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * Enum representing the custom Robbing materials.
 */
public enum RobbingMaterial {
    HANDCUFFS,
    HANDCUFFS_KEY,
    LOCKPICK,
    SAFE,
    PANEL_NUMBER_0,
    PANEL_NUMBER_1,
    PANEL_NUMBER_2,
    PANEL_NUMBER_3,
    PANEL_NUMBER_4,
    PANEL_NUMBER_5,
    PANEL_NUMBER_6,
    PANEL_NUMBER_7,
    PANEL_NUMBER_8,
    PANEL_NUMBER_9,
    PANEL_NUMBER_CANCEL,
    PANEL_NUMBER_CHECK,
    CYLINDER,
    CYLINDER_WRONG,
    CYLINDER_CORRECT;

    public boolean isItem() {
        switch (this) {
            case HANDCUFFS:
            case HANDCUFFS_KEY:
            case LOCKPICK:
            case PANEL_NUMBER_0:
            case PANEL_NUMBER_1:
            case PANEL_NUMBER_2:
            case PANEL_NUMBER_3:
            case PANEL_NUMBER_4:
            case PANEL_NUMBER_5:
            case PANEL_NUMBER_6:
            case PANEL_NUMBER_7:
            case PANEL_NUMBER_8:
            case PANEL_NUMBER_9:
            case PANEL_NUMBER_CANCEL:
            case PANEL_NUMBER_CHECK:
            case CYLINDER:
            case CYLINDER_WRONG:
            case CYLINDER_CORRECT:
                return true;
            default:
                return false;
        }
    }

    public boolean isBlock() {
        switch (this) {
            case SAFE:
                return true;
            default:
                return false;
        }
    }

    @NotNull
    public static RobbingMaterial matchMaterial(@NotNull final String name) {
        String filtered = name;
        if (filtered.startsWith(NamespacedKey.MINECRAFT + ":")) {
            filtered = filtered.substring((NamespacedKey.MINECRAFT + ":").length());
        }

        filtered = filtered.toUpperCase();
        filtered = filtered.replaceAll("\\s+", "_").replaceAll("\\W", "");

        return RobbingMaterial.valueOf(filtered);
    }
}
