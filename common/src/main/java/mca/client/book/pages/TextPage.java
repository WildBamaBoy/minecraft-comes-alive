package mca.client.book.pages;

import java.util.List;
import mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

public class TextPage extends Page {
    protected String content;
    private List<OrderedText> cachedPage;

    public TextPage(String name, int page) {
        content = String.format("{ \"translate\": \"mca.books.%s.%d\" }", name, page);
    }

    public TextPage(String content) {
        this.content = content;
    }

    public void render(ExtendedBookScreen screen, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //prepare page
        if (content != null) {
            if (cachedPage == null) {
                StringVisitable stringVisitable = StringVisitable.plain(content);
                try {
                    stringVisitable = Text.Serializer.fromJson(content);
                } catch (Exception ignored) {
                }

                cachedPage = screen.getTextRenderer().wrapLines(stringVisitable, 114);
            }

            // text
            int l = Math.min(128 / 9, cachedPage.size());
            int i = (screen.width - 192) / 2;
            for (int m = 0; m < l; ++m) {
                OrderedText orderedText = cachedPage.get(m);
                float x = i + 36;
                screen.getTextRenderer().draw(matrices, orderedText, x, (32.0f + m * 9.0f), 0);
            }
        }
    }
}
