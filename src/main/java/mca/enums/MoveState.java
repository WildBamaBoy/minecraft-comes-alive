package mca.enums;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum MoveState {
    MOVE("moving"),
    STAY("staying"),
    FOLLOW("following");

    private static final MoveState[] VALUES = values();
    private static final Map<String, MoveState> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(
            c -> c.friendlyName,
            Function.identity())
    );
    protected String friendlyName;

    MoveState(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Text getName() {
        return new TranslatableText("gui.label." + friendlyName);
    }

    public static Optional<MoveState> byAction(String action) {
        return Optional.ofNullable(REGISTRY.get(action.replace("gui.button.", "").toLowerCase()));
    }

    public static MoveState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return MOVE;
        }
        return VALUES[id];
    }

}