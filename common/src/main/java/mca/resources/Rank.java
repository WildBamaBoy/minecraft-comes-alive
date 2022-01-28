package mca.resources;

import java.util.Locale;

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
            return Rank.KING;
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
            if (r.name().equals(name.toUpperCase(Locale.ENGLISH))) {
                return r;
            }
        }
        return PEASANT;
    }

    public boolean isAtLeast(Rank r) {
        return ordinal() >= r.ordinal();
    }
}
