package mca.enums;

public enum EnumReaperAttackState {
    IDLE(0),
    PRE(1),
    POST(2),
    REST(3),
    BLOCK(4);

    int id;

    EnumReaperAttackState(int id) {
        this.id = id;
    }

    public static EnumReaperAttackState fromId(int id) {
        for (EnumReaperAttackState state : values()) {
            if (id == state.id) {
                return state;
            }
        }

        return IDLE;
    }

    public int getId() {
        return this.id;
    }
}
