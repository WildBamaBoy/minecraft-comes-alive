package mca.api.types;

import java.util.Random;

import mca.api.PoolUtil;

public record NameSet (
        String separator,
        String[] first,
        String[] second) {

    public static final NameSet DEFAULT = new NameSet(" ", new String[] {"unknown"}, new String[] {"names"});

    public String toName(Random rng) {
        String first = PoolUtil.pickOne(first(), null, rng);
        String second = PoolUtil.pickOne(second(), null, rng);

        return toTitleCase(first) + separator() + toTitleCase(second);
    }

    static String toTitleCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
