package mca.api.types;

import lombok.AllArgsConstructor;
import mca.enums.EnumGender;

@AllArgsConstructor
public class HairGroup {
    private final String gender;
    private final int count;

    public EnumGender getGender() {
        return EnumGender.byName(gender);
    }

    public int getCount() {
        return count;
    }
}