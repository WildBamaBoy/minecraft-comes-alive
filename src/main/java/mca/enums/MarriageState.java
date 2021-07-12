package mca.enums;

public enum MarriageState {
    NOT_MARRIED,
    ENGAGED,
    MARRIED,
    MARRIED_TO_PLAYER;

    private static final MarriageState[] VALUES = values();

    public static MarriageState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return NOT_MARRIED;
        }
        return VALUES[id];
    }
}

