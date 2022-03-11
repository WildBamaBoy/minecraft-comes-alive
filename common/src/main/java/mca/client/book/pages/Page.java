package mca.client.book.pages;

import mca.client.book.Book;
import mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Page {
    public abstract void render(ExtendedBookScreen screen, MatrixStack matrices, int mouseX, int mouseY, float delta);

    public void open(boolean back) {

    }

    public boolean previousPage() {
        return true;
    }

    public boolean nextPage() {
        return true;
    }
}
