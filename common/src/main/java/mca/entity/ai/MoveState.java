package mca.entity.ai;

import java.util.Locale;
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
    private static final Map<String, MoveState> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(MoveState::name, Function.identity()));

    protected String friendlyName;

    MoveState(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Text getName() {
        return new TranslatableText("gui.label." + friendlyName);
    }

    public static Optional<MoveState> byCommand(String action) {
        return Optional.ofNullable(REGISTRY.get(action.toUpperCase(Locale.ENGLISH)));
    }

    public static MoveState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return MOVE;
        }
        return VALUES[id];
    }

}
