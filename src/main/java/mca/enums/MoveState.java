package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.core.MCA;

import java.util.Arrays;

@AllArgsConstructor
public enum MoveState {
    MOVE(0, ""),
    STAY(1, "gui.label.staying"),
    FOLLOW(2, "gui.label.following");

    @Getter
    int id;
    String friendlyName;

    public static MoveState byId(int id) {
        return Arrays.stream(values()).filter(s -> s.id == id).findFirst().orElse(MOVE);
    }

    public String getFriendlyName() {
        return MCA.localize(friendlyName);
    }
}