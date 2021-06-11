package mca.api.types;

import lombok.AllArgsConstructor;
import mca.enums.Gender;

@AllArgsConstructor
public class HairGroup {
    private final String gender;
    private final int count;

    public Gender getGender() {
        return Gender.byName(gender);
    }

    public int getCount() {
        return count;
    }
}