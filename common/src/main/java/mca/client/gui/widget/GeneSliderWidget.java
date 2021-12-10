package mca.client.gui.widget;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.OrderableTooltip;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GeneSliderWidget extends SliderWidget implements OrderableTooltip {
    private final Consumer<Double> callback;

    public GeneSliderWidget(int x, int y, int width, int height, Text text, double value, Consumer<Double> callback) {
        super(x, y, width, height, text, value);
        this.updateMessage();
        this.callback = callback;
    }

    @Override
    protected void applyValue() {
        callback.accept(value);
    }

    @Override
    protected void updateMessage() {

    }

    @Override
    public Optional<List<OrderedText>> getOrderedTooltip() {
        return Optional.ofNullable(MinecraftClient.getInstance().textRenderer.wrapLines(new TranslatableText("gui.test"), 200));
    }
}
