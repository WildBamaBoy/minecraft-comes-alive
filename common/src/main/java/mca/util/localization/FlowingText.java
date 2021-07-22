package mca.util.localization;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class FlowingText {

    private final List<OrderedText> lines;

    private final float scale;

    public FlowingText(List<OrderedText> lines, float scale) {
        this.lines = lines;
        this.scale = scale;
    }

    public List<OrderedText> lines() {
        return lines;
    }

    public float scale() {
        return scale;
    }

    public interface Factory {
        /**
         * Scales the given text to fit a desired width and height.
         */
        static FlowingText wrapLines(TextRenderer renderer, Text text, int maxBlockWidth, int maxBlockHeight) {
            float scale = 1;

            List<OrderedText> output;

            do {
                output = renderer.wrapLines(text, (int)Math.ceil(maxBlockWidth / scale));

                if (output.size() * 10 * scale <= maxBlockHeight) {
                    break;
                }

                scale -= 0.01F;
            } while (scale > 0.08F);

            // We trim excess lines in the event fitting isn't perfect
            int maxLines = (int)Math.ceil(maxBlockHeight / (10 * scale));

            return new FlowingText(output.stream().limit(maxLines).collect(Collectors.toList()), scale);
        }
    }
}
