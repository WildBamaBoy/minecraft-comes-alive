package mca.api;

import java.util.List;
import java.util.Random;

public interface PoolUtil {

    static <T> T pickOne(List<T> selection, T def, Random rng) {

        if (selection.isEmpty()) {
            return def;
        }

        return selection.get(rng.nextInt(selection.size()));
    }

    static <T> T pickOne(T[] selection, T def, Random rng) {

        if (selection.length == 0) {
            return def;
        }

        return selection[rng.nextInt(selection.length)];
    }
}
