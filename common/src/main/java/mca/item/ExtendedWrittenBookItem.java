package mca.item;

import mca.cobalt.network.NetworkHandler;
import mca.network.client.OpenGuiRequest;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExtendedWrittenBookItem extends WrittenBookItem {
    private final String bookName;
    private final int bookPages;
    private Identifier background;
    private Formatting textFormatting;

    public ExtendedWrittenBookItem(Settings settings) {
        this(settings, "unknown", 1);
    }

    public ExtendedWrittenBookItem(Settings settings, String bookName, int bookPages) {
        this(settings, bookName, bookPages, BookScreen.BOOK_TEXTURE);
    }

    public ExtendedWrittenBookItem(Settings settings, String bookName, int bookPages, Identifier background) {
        this(settings, bookName, bookPages, background, Formatting.BLACK);
    }

    public ExtendedWrittenBookItem(Settings settings, String bookName, int bookPages, Identifier background, Formatting textFormatting) {
        super(settings);
        this.bookName = bookName;
        this.bookPages = bookPages;
        this.background = background;
        this.textFormatting = textFormatting;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        String key = String.format("mca.books.%s.author", bookName);
        tooltip.add(new TranslatableText(key).formatted(Formatting.GRAY));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.BOOK), (ServerPlayerEntity)player);
        }

        return TypedActionResult.success(itemStack);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    public String getBookName() {
        return bookName;
    }

    public int getBookPages() {
        return bookPages;
    }

    public Identifier getBackground() {
        return background;
    }

    public Formatting getTextFormatting() {
        return textFormatting;
    }
}
