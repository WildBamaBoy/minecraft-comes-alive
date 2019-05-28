package mca.enums;

import mca.core.MCA;
import net.minecraft.item.*;

public enum EnumChore {
    NONE(0, "none", null),
    PROSPECT(1, "gui.label.prospecting", ItemPickaxe.class),
    HARVEST(2, "gui.label.harvesting", ItemHoe.class),
    CHOP(3, "gui.label.chopping", ItemAxe.class),
    HUNT(4, "gui.label.hunting", ItemSword.class),
    FISH(5, "gui.label.fishing", ItemFishingRod.class);

    int id;
    String friendlyName;
    Class toolType;

    EnumChore(int id, String friendlyName, Class toolType) {
        this.id = id;
        this.friendlyName = friendlyName;
        this.toolType = toolType;
    }

    public static EnumChore byId(int id) {
        for (EnumChore chore : values()) {
            if (chore.id == id) {
                return chore;
            }
        }
        return NONE;
    }

    public int getId() {
        return id;
    }

    public Class getToolType() {
        return toolType;
    }

    public String getFriendlyName() {
        return MCA.getLocalizer().localize(this.friendlyName);
    }
}

