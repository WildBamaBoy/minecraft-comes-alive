package mca.util.localization;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public record FlowingText(
            List<OrderedText> lines,
            float scale
        ) {

    public static interface Factory {
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

            return new FlowingText(output.stream().limit(maxLines).toList(), scale);
        }
    }
}
