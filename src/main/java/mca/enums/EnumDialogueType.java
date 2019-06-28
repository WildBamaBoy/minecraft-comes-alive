package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum EnumDialogueType {
    CHILDP("childp"),
    CHILD("child"),
    ADULT("adult"),
    SPOUSE("spouse");

    String id;

    public static EnumDialogueType byValue(String value) {
        return Arrays.stream(values()).filter(c -> c.getId().equals(value)).findFirst().orElse(null);
    }
}

