package mca.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.enums.EnumGender;

@AllArgsConstructor
public class ClothingGroup {
    private final String gender;
    @Getter
    private final String profession;
    @Getter
    private final String[] paths;

    public EnumGender getGender() {
        return EnumGender.byName(gender);
    }
}
