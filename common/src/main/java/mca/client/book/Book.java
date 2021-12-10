package mca.client.book;

import java.util.LinkedList;
import java.util.List;
import mca.client.book.pages.Page;
import mca.client.book.pages.TextPage;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class Book {
    private final String bookName;
    private final List<Page> pages = new LinkedList<>();
    private Identifier background = new Identifier("textures/gui/book.png");
    private Formatting textFormatting = Formatting.BLACK;
    private boolean pageTurnSound = true;

    public Book(String name) {
        bookName = name;
    }

    public Book setBackground(Identifier background) {
        this.background = background;
        return this;
    }

    public Book setTextFormatting(Formatting textFormatting) {
        this.textFormatting = textFormatting;
        return this;
    }

    public Book setPageTurnSound(boolean pageTurnSound) {
        this.pageTurnSound = pageTurnSound;
        return this;
    }

    private String getContentString() {
        return String.format(" { \"translate\": \"mca.books.%s.%d\" }", getBookName(), pages.size());
    }

    public Book addPage(Page page) {
        page.setContent(getContentString());
        pages.add(page);
        return this;
    }

    public Book addSimplePages(int n) {
        for (int i = 0; i < n; i++) {
            addPage(new TextPage());
        }
        return this;
    }

    public int getPageCount() {
        return pages.size();
    }

    public String getBookName() {
        return bookName;
    }

    public List<Page> getPages() {
        return pages;
    }

    public Identifier getBackground() {
        return background;
    }

    public Formatting getTextFormatting() {
        return textFormatting;
    }

    public boolean hasPageTurnSound() {
        return pageTurnSound;
    }

    public Page getPage(int index) {
        return pages.get(index);
    }
}
