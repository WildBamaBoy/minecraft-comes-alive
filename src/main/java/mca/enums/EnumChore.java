package mca.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.core.MCA;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;

@AllArgsConstructor
public enum EnumChore {
    NONE(0, "none", null),
    PROSPECT(1, "gui.label.prospecting", ItemPickaxe.class),
    HARVEST(2, "gui.label.harvesting", ItemHoe.class),
    CHOP(3, "gui.label.chopping", ItemAxe.class),
    HUNT(4, "gui.label.hunting", ItemSword.class),
    FISH(5, "gui.label.fishing", ItemFishingRod.class);

    @Getter int id;
    String friendlyName;
    @Getter Class toolType;

    public static EnumChore byId(int id) {
        Optional<EnumChore> state = Arrays.stream(values()).filter((e) -> e.id == id).findFirst();
        return state.orElse(NONE);
    }

    public String getFriendlyName() {
        return MCA.getLocalizer().localize(this.friendlyName);
    }
}

