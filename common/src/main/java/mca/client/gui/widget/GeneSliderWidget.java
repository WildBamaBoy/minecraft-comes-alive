package mca.client.gui.widget;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.OrderableTooltip;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class GeneSliderWidget extends SliderWidget implements OrderableTooltip {
    private final Consumer<Double> callback;

    public GeneSliderWidget(int x, int y, int width, int height, Text text, double value, Consumer<Double> callback) {
        super(x, y, width, height, text, value);
        this.updateMessage();
        this.callback = callback;
    }

    protected void applyValue() {
        callback.accept(value);
    }

    protected void updateMessage() {

    }

    public Optional<List<OrderedText>> getOrderedTooltip() {
        return Optional.ofNullable(MinecraftClient.getInstance().textRenderer.wrapLines(new TranslatableText("gui.test"), 200));
    }
}
