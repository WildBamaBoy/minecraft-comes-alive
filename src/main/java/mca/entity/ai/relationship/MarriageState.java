package mca.entity.ai.relationship;

public enum MarriageState {
    /**
     * The default state. All entities when born are not married.
     */
    SINGLE("notMarried"),
    /**
     * Unused.
     */
    ENGAGED("engaged"),
    /**
     * Maried to another villager.
     */
    MARRIED_TO_VILLAGER("married"),
    /**
     * Married to a player.
     */
    MARRIED_TO_PLAYER("marriedToPlayer"),
    /**
     * Was once married but the spouse is dead.
     */
    WIDOW("widow");

    private static final MarriageState[] VALUES = values();

    private final String icon;

    MarriageState(String icon) {
        this.icon = icon;
    }

    public boolean isMarried() {
        return this == MARRIED_TO_PLAYER || this == MARRIED_TO_VILLAGER;
    }

    public String getIcon() {
        return icon;
    }

    public static MarriageState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return SINGLE;
        }
        return VALUES[id];
    }
}

