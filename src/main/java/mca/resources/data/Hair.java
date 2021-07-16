package mca.resources.data;

public record Hair (String texture, String overlay) {

    public Hair() {
        this("", "");
    }

    public Hair(String texture, String overlay) {
        this.texture = texture;
        this.overlay = overlay == null ? "" : overlay;
    }
}