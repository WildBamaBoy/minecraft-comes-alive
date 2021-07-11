package mca.enums;

import mca.cobalt.localizer.Localizer;
import net.minecraft.item.*;

import org.jetbrains.annotations.Nullable;

public enum Chore {
    NONE    ("none", null),
    PROSPECT("gui.label.prospecting", PickaxeItem.class),
    HARVEST ("gui.label.harvesting", HoeItem.class),
    CHOP    ("gui.label.chopping", AxeItem.class),
    HUNT    ("gui.label.hunting", SwordItem.class),
    FISH    ("gui.label.fishing", FishingRodItem.class);

    private static final Chore[] VALUES = values();

    private final String friendlyName;

    @Nullable
    private final Class<?> toolType;

    Chore(String friendlyName, @Nullable Class<?> toolType) {
        this.friendlyName = friendlyName;
        this.toolType = toolType;
    }

    @Deprecated
    public int getId() {
        return ordinal();
    }

    @Nullable
    public Class<?> getToolType() {
        return toolType;
    }

    public static Chore byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return NONE;
        }
        return VALUES[id];
    }

    public String getFriendlyName() {
        return Localizer.getInstance().localize(this.friendlyName);
    }
}

