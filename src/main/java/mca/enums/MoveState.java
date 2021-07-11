package mca.enums;

import mca.cobalt.localizer.Localizer;
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

    @Deprecated
    public String getFriendlyName() {
        return Localizer.localize(friendlyName);
    }

    public static MoveState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return MOVE;
        }
        return VALUES[id];
    }

}