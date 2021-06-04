package mca.api.types;

import lombok.AllArgsConstructor;
import mca.enums.EnumGender;

@AllArgsConstructor
public class ClothingGroup {
    private final String gender;
    private final String profession;
    private final int count;

    public EnumGender getGender() {
        return EnumGender.byName(gender);
    }

    public String getProfession() {
        return profession;
    }

    public int getCount() {
        return count;
    }
}