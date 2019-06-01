package mca.enums;

import mca.core.MCA;

public enum EnumMoveState {
    MOVE(0, ""),
    STAY(1, "gui.label.staying"),
    FOLLOW(2, "gui.label.following");

    int id;
    String friendlyName;

    EnumMoveState(int id, String friendlyName) {
        this.id = id;
        this.friendlyName = friendlyName;
    }

    public static EnumMoveState byId(int id) {
        for (EnumMoveState state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        return MOVE;
    }

    public String getFriendlyName() {
        return MCA.getLocalizer().localize(friendlyName);
    }

    public int getId() {
        return id;
    }
}

