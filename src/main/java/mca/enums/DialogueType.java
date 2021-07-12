package mca.enums;

import net.minecraft.util.Language;

public enum DialogueType {
    UNASSIGNED,
    CHILDP,
    CHILD,
    ADULT,
    SPOUSE;

    private static final DialogueType[] VALUES = values();

    public String getTranslationKey(String phrase) {
        String fullPhrase = name().toLowerCase() + "." + phrase;
        return Language.getInstance().hasTranslation(fullPhrase) ? fullPhrase : "generic." + phrase;
    }

    public static DialogueType byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }
}