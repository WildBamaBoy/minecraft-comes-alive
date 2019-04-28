package mca.enums;

public enum EnumMoveState {
    MOVE(0),
    STAY(1),
    FOLLOW(2);

    int id;

    EnumMoveState(int id) {
        this.id = id;
    }

    public static EnumMoveState byId(int id) {
        for (EnumMoveState state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        return MOVE;
    }

    public int getId() {
        return id;
    }
}

