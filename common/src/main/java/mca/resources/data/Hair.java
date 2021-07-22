package mca.resources.data;

public final class Hair {

    private final String texture;
    private final String overlay;

    public String texture() {
        return texture;
    }

    public String overlay() {
        return overlay;
    }

    public Hair() {
        this("", "");
    }

    public Hair(String texture, String overlay) {
        this.texture = texture;
        this.overlay = overlay == null ? "" : overlay;
    }
}