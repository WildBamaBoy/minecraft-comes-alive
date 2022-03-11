package mca.client.book.pages;

import java.util.List;
import mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class TitlePage extends Page {
    Text title;
    Text subtitle;

    public TitlePage(String book) {
        this(book, Formatting.BLACK);
    }

    public TitlePage(String book, Formatting color) {
        this("item.mca.book_" + book, "mca.books." + book + ".author", color);
    }

    public TitlePage(String title, String subtitle) {
        this(title, subtitle, Formatting.BLACK);
    }

    public TitlePage(String title, String subtitle, Formatting color) {
        this(new TranslatableText(title).formatted(color).formatted(Formatting.BOLD),
                new TranslatableText(subtitle).formatted(color).formatted(Formatting.ITALIC));
    }

    public TitlePage(Text title, Text subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    private static void drawCenteredText(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        OrderedText orderedText = text.asOrderedText();
        drawCenteredText(matrices, textRenderer, orderedText, centerX, y, color);
    }

    private static void drawCenteredText(MatrixStack matrices, TextRenderer textRenderer, OrderedText text, int centerX, int y, int color) {
        textRenderer.draw(matrices, text, (float)(centerX - textRenderer.getWidth(text) / 2), (float)y, color);
    }

    @Override
    public void render(ExtendedBookScreen screen, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        List<OrderedText> texts = screen.getTextRenderer().wrapLines(title, 114);
        int y = 80 - 5 * texts.size();
        for (OrderedText t : texts) {
            drawCenteredText(matrices, screen.getTextRenderer(), t, screen.width / 2 - 2, y, 0xFFFFFF);
            y += 10;
        }
        y = 82 + 5 * texts.size();
        drawCenteredText(matrices, screen.getTextRenderer(), subtitle, screen.width / 2 - 2, y, 0xFFFFFF);
    }
}
