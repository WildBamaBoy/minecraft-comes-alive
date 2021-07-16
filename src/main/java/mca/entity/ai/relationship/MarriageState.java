package mca.entity.ai.relationship;

public enum MarriageState {
    NOT_MARRIED("notMarried"),
    ENGAGED("engaged"),
    MARRIED("married"),
    MARRIED_TO_PLAYER("marriedToPlayer");

    private static final MarriageState[] VALUES = values();

    private final String icon;

    MarriageState(String icon) {
        this.icon = icon;
    }

    public MarriageState base() {
        return this == MARRIED_TO_PLAYER ? MARRIED : this;
    }

    public String getIcon() {
        return icon;
    }

    public static MarriageState byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return NOT_MARRIED;
        }
        return VALUES[id];
    }
}

