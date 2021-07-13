package mca.enums;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum MoveState {
    MOVE(""),
    STAY("gui.label.staying"),
    FOLLOW("gui.label.following");

    private static final MoveState[] VALUES = values();

    String friendlyName;

    MoveState(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Text getName() {
        return new TranslatableText(friendlyName);
    }

    public static MoveState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return MOVE;
        }
        return VALUES[id];
    }

}