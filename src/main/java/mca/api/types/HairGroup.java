package mca.api.types;

import lombok.AllArgsConstructor;
import mca.enums.EnumGender;

@AllArgsConstructor
public class HairGroup {
    private final String gender;
    private final Hair[] paths;

    public EnumGender getGender() {
        return EnumGender.byName(gender);
    }

    public Hair[] getPaths() {
        return paths;
    }
}
