package mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ExtendedPageTurnWidget extends PageTurnWidget {
    Identifier texture;
    boolean isNextPageButton;

    public ExtendedPageTurnWidget(int x, int y, boolean isNextPageButton, PressAction action, boolean playPageTurnSound, Identifier texture) {
        super(x, y, isNextPageButton, action, playPageTurnSound);
        this.isNextPageButton = isNextPageButton;
        this.texture = texture;
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        MinecraftClient.getInstance().getTextureManager().bindTexture(texture);

        int i = 0;
        int j = 192;
        if (this.isHovered()) {
            i += 23;
        }

        if (!this.isNextPageButton) {
            j += 13;
        }

        this.drawTexture(matrices, this.x, this.y, i, j, 23, 13);
    }
}
