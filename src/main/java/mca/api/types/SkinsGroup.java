package mca.api.types;

public class SkinsGroup {
    private String gender;
    private String profession;
    private String[] paths;

    public SkinsGroup(String profession, String gender, String[] paths) {
        this.profession = profession;
        this.gender = gender;
        this.paths = paths;
    }

    public String getProfession() {
        return profession;
    }

    public String getGender() {
        return gender;
    }

    public String[] getPaths() {
        return paths;
    }
}
