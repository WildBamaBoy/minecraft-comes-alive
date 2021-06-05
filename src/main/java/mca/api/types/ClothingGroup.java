package mca.api.types;

import mca.enums.EnumGender;
import net.minecraft.entity.merchant.villager.VillagerProfession;

public class ClothingGroup {
    private final String gender;
    private final String profession;
    private final int count;
    private float chance;

    public ClothingGroup() {
        gender = "";
        profession = "";
        count = 0;
        chance = 1.0f;
    }

    public EnumGender getGender() {
        return EnumGender.byName(gender);
    }

    public String getProfession() {
        return profession;
    }

    public int getCount() {
        return count;
    }

    public float getChance() {
        return chance;
    }
}