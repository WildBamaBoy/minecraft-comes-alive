package mca.client.book.pages;

import java.util.List;
import mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class ListPage extends Page {
    final Text title;
    final List<Text> text;

    int page;

    public static int entriesPerPage = 11;

    public ListPage(Text title, List<Text> text) {
        this.title = title;
        this.text = text;
    }

    public ListPage(String title, List<Text> text) {
        this(new TranslatableText(title).formatted(Formatting.BLACK).formatted(Formatting.BOLD), text);
    }

    private static void drawCenteredText(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int y, int color) {
        OrderedText orderedText = text.asOrderedText();
        textRenderer.draw(matrices, orderedText, (float)(centerX - textRenderer.getWidth(orderedText) / 2), (float)y, color);
    }

    @Override
    public void render(ExtendedBookScreen screen, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        drawCenteredText(matrices, screen.getTextRenderer(), title, screen.width / 2, 35, 0xFFFFFFFF);

        int y = 48;
        for (int i = page * entriesPerPage; i < Math.min(text.size(), (page + 1) * entriesPerPage); i++) {
            drawCenteredText(matrices, screen.getTextRenderer(), text.get(i), screen.width / 2, y, 0xFFFFFFFF);
            y += 10;
        }
    }

    @Override
    public void open(boolean back) {
        page = back ? text.size() / entriesPerPage : 0;
    }

    @Override
    public boolean previousPage() {
        if (page > 0) {
            page--;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean nextPage() {
        if (page < (text.size() - 1) / entriesPerPage) {
            page++;
            return false;
        } else {
            return true;
        }
    }
}
