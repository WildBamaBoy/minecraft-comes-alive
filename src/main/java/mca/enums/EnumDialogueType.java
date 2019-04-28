package mca.enums;

public enum EnumDialogueType {
    CHILDP("childp"),
    CHILD("child"),
    ADULT("adult");

    String id;

    EnumDialogueType(String id) {
        this.id = id;
    }

    public static EnumDialogueType byValue(String value) {
        for (EnumDialogueType constraint : values()) {
            if (constraint.getId().equals(value)) {
                return constraint;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }
}

