package mca.util.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public interface RenderSystemCompat {
    /**
     * @since MC 1.17
     */
    static void setShaderTexture(int index, Identifier texture) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
    }
}
