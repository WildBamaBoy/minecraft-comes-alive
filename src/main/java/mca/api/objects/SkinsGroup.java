package mca.api.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.enums.EnumGender;

@AllArgsConstructor
public class SkinsGroup {
    private String gender;
    @Getter private String profession;
    @Getter private String[] paths;

    public EnumGender getGender() {
        return EnumGender.byName(gender);
    }
}
