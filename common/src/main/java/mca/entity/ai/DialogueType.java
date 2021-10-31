package mca.entity.ai;

import java.util.HashMap;
import java.util.Map;
import mca.entity.ai.relationship.AgeState;

public enum DialogueType {
    ADULT(null),
    UNASSIGNED(ADULT),
    BABY(UNASSIGNED),
    CHILD(ADULT),
    CHILDP(CHILD),
    TODDLER(CHILD),
    TODDLERP(CHILDP),
    SPOUSE(ADULT),
    TEEN(ADULT),
    TEENP(TEEN);

    public final DialogueType fallback;

    DialogueType(DialogueType fallback) {
        this.fallback = fallback;
    }

    private static final DialogueType[] VALUES = values();

    public static final Map<String, DialogueType> MAP = new HashMap<String, DialogueType>();

    static {
        for (DialogueType value : VALUES) {
            MAP.put(value.name(), value);
        }
    }

    public DialogueType toChild() {
        switch (this) {
            case TODDLER:
                return TODDLERP;
            case CHILD:
                return CHILDP;
            case TEEN:
                return TEENP;
            default:
                return UNASSIGNED;
        }
    }

    public static DialogueType fromAge(AgeState state) {
        for (DialogueType t : values()) {
            if (t.name().equals(state.name())) {
                return t;
            }
        }
        return UNASSIGNED;
    }

    public static DialogueType byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }
}
