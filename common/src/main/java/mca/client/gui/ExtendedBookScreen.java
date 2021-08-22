package mca.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mca.client.book.Book;
import mca.client.book.pages.Page;
import mca.client.gui.widget.ExtendedPageTurnWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ExtendedBookScreen extends Screen {
    private int pageIndex;
    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;
    private final Book book;

    public ExtendedBookScreen(Book book) {
        super(NarratorManager.EMPTY);
        this.book = book;
    }

    public boolean setPage(int index) {
        int i = MathHelper.clamp(index, 0, this.book.getPageCount() - 1);
        if (i != this.pageIndex) {
            this.pageIndex = i;
            this.updatePageButtons();
            return true;
        } else {
            return false;
        }
    }

    protected boolean jumpToPage(int page) {
        return this.setPage(page);
    }

    @Override
    protected void init() {
        this.addCloseButton();
        this.addPageButtons();
    }

    protected void addCloseButton() {
        this.addButton(new ButtonWidget(this.width / 2 - 100, 196, 200, 20, ScreenTexts.DONE, (buttonWidget) -> this.client.openScreen(null)));
    }

    protected void addPageButtons() {
        int i = (this.width - 192) / 2;
        this.nextPageButton = this.addButton(new ExtendedPageTurnWidget(i + 116, 159, true, (buttonWidget) -> goToNextPage(), book.hasPageTurnSound(), book.getBackground()));
        this.previousPageButton = this.addButton(new ExtendedPageTurnWidget(i + 43, 159, false, (buttonWidget) -> goToPreviousPage(), book.hasPageTurnSound(), book.getBackground()));
        this.updatePageButtons();
    }

    protected void goToPreviousPage() {
        if (this.pageIndex > 0) {
            --this.pageIndex;
        }
        this.updatePageButtons();
    }

    protected void goToNextPage() {
        if (this.pageIndex < book.getPageCount() - 1) {
            ++this.pageIndex;
        }
        this.updatePageButtons();
    }

    private void updatePageButtons() {
        this.nextPageButton.visible = this.pageIndex < book.getPageCount() - 1;
        this.previousPageButton.visible = this.pageIndex > 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            switch (keyCode) {
                case 266:
                    this.previousPageButton.onPress();
                    return true;
                case 267:
                    this.nextPageButton.onPress();
                    return true;
                default:
                    return false;
            }
        }
    }

    public void bindTexture(Identifier tex) {
        this.client.getTextureManager().bindTexture(tex);
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        // background
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture(book.getBackground());
        int i = (this.width - 192) / 2;
        this.drawTexture(matrices, i, 2, 0, 0, 192, 192);

        // page number
        Text pageIndexText = new TranslatableText("book.pageIndicator", this.pageIndex + 1, Math.max(book.getPageCount(), 1)).formatted(book.getTextFormatting());
        int k = textRenderer.getWidth(pageIndexText);
        textRenderer.draw(matrices, pageIndexText, i - k + 192 - 44, 18.0f, 0);

        Page page = book.getPage(pageIndex);
        if (page != null) {
            page.render(this, matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {

        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean handleTextClick(Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null) {
            return false;
        } else if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String string = clickEvent.getValue();

            try {
                int i = Integer.parseInt(string) - 1;
                return this.jumpToPage(i);
            } catch (Exception var5) {
                return false;
            }
        } else {
            boolean bl = super.handleTextClick(style);
            if (bl && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.client.openScreen(null);
            }

            return bl;
        }
    }
}
