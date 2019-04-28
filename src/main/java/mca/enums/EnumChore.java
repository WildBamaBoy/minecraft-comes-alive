package mca.enums;

import net.minecraft.item.*;

public enum EnumChore {
    NONE(0, null),
    PROSPECT(1, ItemPickaxe.class),
    HARVEST(2, ItemHoe.class),
    CHOP(3, ItemAxe.class),
    HUNT(4, ItemSword.class),
    FISH(5, ItemFishingRod.class);

    int id;
    Class toolType;

    EnumChore(int id, Class toolType) {
        this.id = id;
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
}

