package mca.client.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class NamedTextFieldWidget extends TextFieldWidget {
    private final TextRenderer textRenderer;

    public NamedTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x + width / 2, y, width / 2, height, text);
        this.textRenderer = textRenderer;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        OrderedText orderedText = getMessage().asOrderedText();
        textRenderer.drawWithShadow(matrices, orderedText, (float)(x - textRenderer.getWidth(orderedText) - 4), (float)y + (height - 8) / 2.0f, 0xffffff);
    }
}
