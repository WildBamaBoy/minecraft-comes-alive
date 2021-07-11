package mca.enums;

public enum DialogueType {
    UNASSIGNED,
    CHILDP,
    CHILD,
    ADULT,
    SPOUSE;

    private static final DialogueType[] VALUES = values();

    public int getId() {
        return ordinal() - 1;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public static DialogueType byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }
}