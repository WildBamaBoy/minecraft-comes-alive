package mca.resources.data;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hair hair = (Hair)o;
        return Objects.equals(texture, hair.texture) && Objects.equals(overlay, hair.overlay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, overlay);
    }
}
