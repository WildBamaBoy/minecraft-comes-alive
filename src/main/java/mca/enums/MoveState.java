package mca.enums;

import mca.cobalt.localizer.Localizer;
import mca.core.MCA;

public enum MoveState {
    MOVE(""),
    STAY("gui.label.staying"),
    FOLLOW("gui.label.following");

    private static final MoveState[] VALUES = values();

    String friendlyName;

    MoveState(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Deprecated
    public int getId() {
        return ordinal();
    }

    public static MoveState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return MOVE;
        }
        return VALUES[id];
    }

    public String getFriendlyName() {
        return Localizer.getInstance().localize(friendlyName);
    }
}