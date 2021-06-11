package mca.api.types;

import mca.enums.Gender;

public class ClothingGroup {
    private final String gender;
    private final String profession;
    private final int count;
    private final float chance;

    public ClothingGroup() {
        gender = "";
        profession = "";
        count = 0;
        chance = 1.0f;
    }

    public Gender getGender() {
        return Gender.byName(gender);
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