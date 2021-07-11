package mca.api.types;

import mca.enums.Gender;

public record HairGroup(String gender, int count) {
    public Gender getGender() {
        return Gender.byName(gender);
    }
}