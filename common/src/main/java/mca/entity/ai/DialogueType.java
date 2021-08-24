package mca.entity.ai;

import mca.entity.ai.relationship.AgeState;
import net.minecraft.util.Language;

public enum DialogueType {
    // TODO we currently use adult as generic default rather than null, we should change that both here and in the lang file
    ADULT(null),
    UNASSIGNED(ADULT),
    BABY(UNASSIGNED),
    CHILD(UNASSIGNED),
    CHILDP(CHILD),
    TODDLER(CHILD),
    TODDLERP(CHILDP),
    SPOUSE(ADULT),
    TEEN(ADULT),
    TEENP(TEEN);

    DialogueType fallback;

    DialogueType(DialogueType fallback) {
        this.fallback = fallback;
    }

    private static final DialogueType[] VALUES = values();

    public String getTranslationKey(String phrase) {
        DialogueType t = this;
        while (t != null) {
            String s = t.name().toLowerCase() + "." + phrase;
            if (Language.getInstance().hasTranslation(s)) {
                return s;
            } else {
                t = t.fallback;
            }
        }
        return phrase;
    }

    public DialogueType toChild() {
        switch (this) {
            case TODDLER:
                return TODDLERP;
            case CHILD:
                return CHILDP;
            case TEEN:
                return TEENP;
        }
        return UNASSIGNED;
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
