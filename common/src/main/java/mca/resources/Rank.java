package mca.resources;

public enum Rank {
    OUTLAW,
    PEASANT,
    MERCHANT,
    NOBLE,
    MAYOR,
    KING;

    private static final Rank[] VALUES = values();

    public Rank promote() {
        if (ordinal() + 1 < VALUES.length) {
            return VALUES[ordinal() + 1];
        } else {
            return null;
        }
    }

    public Rank degrade() {
        if (ordinal() - 1 >= 0) {
            return VALUES[ordinal() - 1];
        } else {
            return null;
        }
    }

    public static Rank fromName(String name) {
        for (Rank r : VALUES) {
            if (r.name().equals(name.toUpperCase())) {
                return r;
            }
        }
        return PEASANT;
    }

    public boolean isAtLeast(Rank peasant) {
        return ordinal() >= peasant.ordinal();
    }
}
