package mca.entity.interaction.gifts;

import java.util.Locale;

public enum Response {
    FAIL,
    GOOD,
    BETTER,
    BEST;

    public String getDefaultDialogue() {
        return "gift." + name().toLowerCase(Locale.ENGLISH);
    }
}
