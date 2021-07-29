package mca.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mca.client.gui.widgets.ExtendedPageTurnWidget;
import mca.item.ExtendedWrittenBookItem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

// TODO Mojang code :thonkjang:
public class GuiExtendedBook extends Screen {
    private final BookScreen.Contents contents;
    private int pageIndex;
    private List<OrderedText> cachedPage;
    private int cachedPageIndex;
    private Text pageIndexText;
    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;
    private final Identifier background;
    private final Formatting textFormatting;
    private final boolean pageTurnSound = true;

    public GuiExtendedBook(BookScreen.Contents contents, Identifier background, Formatting textFormatting) {
        super(NarratorManager.EMPTY);
        this.cachedPage = Collections.emptyList();
        this.cachedPageIndex = -1;
        this.pageIndexText = LiteralText.EMPTY;
        this.contents = contents;
        this.background = background;
        this.textFormatting = textFormatting;
    }

    public GuiExtendedBook(BookScreen.Contents pageProvider) {
        this(pageProvider, BookScreen.BOOK_TEXTURE, Formatting.BLACK);
    }

    public boolean setPage(int index) {
        int i = MathHelper.clamp(index, 0, this.contents.getPageCount() - 1);
        if (i != this.pageIndex) {
            this.pageIndex = i;
            this.updatePageButtons();
            this.cachedPageIndex = -1;
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
        this.nextPageButton = this.addButton(new ExtendedPageTurnWidget(i + 116, 159, true, (buttonWidget) -> this.goToNextPage(), this.pageTurnSound, background));
        this.previousPageButton = this.addButton(new ExtendedPageTurnWidget(i + 43, 159, false, (buttonWidget) -> this.goToPreviousPage(), this.pageTurnSound, background));
        this.updatePageButtons();
    }

    private int getPageCount() {
        return this.contents.getPageCount();
    }

    protected void goToPreviousPage() {
        if (this.pageIndex > 0) {
            --this.pageIndex;
        }
        this.updatePageButtons();
    }

    protected void goToNextPage() {
        if (this.pageIndex < this.getPageCount() - 1) {
            ++this.pageIndex;
        }
        this.updatePageButtons();
    }

    private void updatePageButtons() {
        this.nextPageButton.visible = this.pageIndex < this.getPageCount() - 1;
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

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        // background
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(background);
        int i = (this.width - 192) / 2;
        this.drawTexture(matrices, i, 2, 0, 0, 192, 192);

        // page number
        if (this.cachedPageIndex != this.pageIndex) {
            StringVisitable stringVisitable = this.contents.getPage(this.pageIndex);
            this.cachedPage = this.textRenderer.wrapLines(stringVisitable, 114);
            this.pageIndexText = new TranslatableText("book.pageIndicator", this.pageIndex + 1, Math.max(this.getPageCount(), 1)).formatted(textFormatting);
        }
        this.cachedPageIndex = this.pageIndex;
        int k = this.textRenderer.getWidth(this.pageIndexText);
        this.textRenderer.draw(matrices, this.pageIndexText, i - k + 192 - 44, 18.0f, 0);
        int l = Math.min(128 / 9, this.cachedPage.size());

        // text
        for (int m = 0; m < l; ++m) {
            OrderedText orderedText = this.cachedPage.get(m);
            TextRenderer textRenderer = this.textRenderer;
            float y = i + 36;
            textRenderer.draw(matrices, orderedText, y, (32.0f + m * 9.0f), 0);
        }

        // hover
        Style style = this.getTextAt(mouseX, mouseY);
        if (style != null) {
            this.renderTextHoverEffect(matrices, style, mouseX, mouseY);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Style style = this.getTextAt(mouseX, mouseY);
            if (style != null && this.handleTextClick(style)) {
                return true;
            }
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

    @Nullable
    public Style getTextAt(double x, double y) {
        if (this.cachedPage.isEmpty()) {
            return null;
        } else {
            int i = MathHelper.floor(x - (this.width - 192) / 2 - 36.0D);
            int j = MathHelper.floor(y - 2.0D - 30.0D);
            if (i >= 0 && j >= 0) {
                int k = Math.min(128 / 9, this.cachedPage.size());
                if (i <= 114) {
                    this.client.textRenderer.getClass();
                    if (j < 9 * k + k) {
                        this.client.textRenderer.getClass();
                        int l = j / 9;
                        if (l < this.cachedPage.size()) {
                            OrderedText orderedText = this.cachedPage.get(l);
                            return this.client.textRenderer.getTextHandler().getStyleAt(orderedText, i);
                        }

                        return null;
                    }
                }

            }
            return null;
        }
    }

    public static class TranslatedBookContent implements BookScreen.Contents {
        private final List<String> pages;

        public TranslatedBookContent(ItemStack stack) {
            NbtCompound compound = new NbtCompound();

            // fetch book information
            String name = "unknown";
            int count = 1;
            Item item = stack.getItem();
            if (item instanceof ExtendedWrittenBookItem) {
                ExtendedWrittenBookItem book = (ExtendedWrittenBookItem)item;
                name = book.getBookName();
                count = book.getBookPages();
            }

            // build pages
            NbtList pages = new NbtList();
            for (int i = 0; i < count; i++) {
                pages.add(i, NbtString.of(String.format(" { \"translate\": \"mca.books.%s.%d\" }", name, i)));
            }
            compound.put("pages", pages);

            // set title to empty to use default translated name
            compound.put("title", NbtString.of(""));
            compound.put("author", NbtString.of(""));

            this.pages = BookScreen.readPages(compound);
        }

        @Override
        public int getPageCount() {
            return pages.size();
        }

        @Override
        public StringVisitable getPageUnchecked(int index) {
            String string = pages.get(index);

            try {
                StringVisitable stringVisitable = Text.Serializer.fromJson(string);
                if (stringVisitable != null) {
                    return stringVisitable;
                }
            } catch (Exception ignored) {
            }

            return StringVisitable.plain(string);
        }
    }
}
