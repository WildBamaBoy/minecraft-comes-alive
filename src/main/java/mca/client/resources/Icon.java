package mca.client.resources;

public record Icon (
        int u,
        int v,
        int x,
        int y) {
    public static final Icon EMPTY = new Icon(0, 0, 0, 0);
}
