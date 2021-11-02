package mca.util;

import java.io.Serializable;

public class SerializablePair<L extends Serializable, R extends Serializable> implements Serializable {
    private final L left;
    private final R right;

    public SerializablePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return this.left;
    }

    public R getRight() {
        return this.right;
    }
}
