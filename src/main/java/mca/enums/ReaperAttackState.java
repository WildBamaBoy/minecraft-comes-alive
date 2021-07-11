package mca.enums;

public enum ReaperAttackState {
    IDLE,
    PRE,
    POST,
    REST,
    BLOCK;

    private static final ReaperAttackState[] VALUES = values();

    @Deprecated
    public int getId() {
        return ordinal();
    }

    public static ReaperAttackState fromId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return IDLE;
        }
        return VALUES[id];
    }
}
