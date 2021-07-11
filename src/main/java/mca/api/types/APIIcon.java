package mca.api.types;

public record APIIcon (int u, int v, int x, int y) {
    @Deprecated
    public int getU() {
        return u;
    }

    @Deprecated
    public int getV() {
        return v;
    }

    @Deprecated
    public int getX() {
        return x;
    }

    @Deprecated
    public int getY() {
        return y;
    }
}
