package mca.api.types;

public class APIIcon {
    private final int u;
    private final int v;
    private final int x;
    private final int y;

    public APIIcon(int u, int v, int x, int y) {
        this.u = u;
        this.v = v;
        this.x = x;
        this.y = y;
    }

    public int getU() {
        return u;
    }

    public int getV() {
        return v;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
