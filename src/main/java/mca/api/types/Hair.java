package mca.api.types;

public class Hair {
    private final String texture;
    private final String overlay;

    public Hair() {
        this("", "");
    }

    public Hair(String texture, String overlay) {
        this.texture = texture;
        this.overlay = overlay == null ? "" : overlay;
    }

    public String getTexture() {
        return texture;
    }

    public String getOverlay() {
        return overlay;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public boolean equals(Hair obj) {
        return texture.equals(obj.texture) && overlay.equals(obj.overlay);
    }
}
