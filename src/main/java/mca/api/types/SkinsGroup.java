package mca.api.types;

import mca.enums.EnumGender;

public class SkinsGroup {
    private EnumGender gender;
    private String profession;
    private String[] paths;

    public SkinsGroup(String profession, String gender, String[] paths) {
        this.profession = profession;
        this.gender = EnumGender.valueOf(gender);
        this.paths = paths;
    }

    public String getProfession() {
        return profession;
    }

    public EnumGender getGender() {
        return gender;
    }

    public String[] getPaths() {
        return paths;
    }
}
