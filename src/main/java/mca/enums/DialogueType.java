package mca.enums;

import java.util.stream.Stream;

import net.minecraft.util.Language;

public enum DialogueType {
    UNASSIGNED,
    CHILDP,
    CHILD,
    ADULT,
    SPOUSE;

    private static final DialogueType[] VALUES = values();

    public String getTranslationKey(String phrase) {
        return Stream.of(name().toLowerCase() + "." + phrase, "generic." + phrase)
                .filter(Language.getInstance()::hasTranslation)
                .findFirst()
                .orElse(phrase);
    }

    public static DialogueType byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return UNASSIGNED;
        }
        return VALUES[id];
    }
}