package mca.item;

import mca.client.book.Book;
import mca.client.book.pages.TextPage;
import mca.cobalt.network.NetworkHandler;
import mca.network.client.OpenGuiRequest;
import mca.util.NbtElementCompat;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExtendedWrittenBookItem extends WrittenBookItem {
    private final Book book;

    public ExtendedWrittenBookItem(Settings settings) {
        this(settings, new Book("unknown"));
    }

    public ExtendedWrittenBookItem(Settings settings, Book book) {
        super(settings);
        this.book = book;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        String key = String.format("mca.books.%s.author", book.getBookName());
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

    public Book getBook(ItemStack item) {
        NbtCompound tag = item.getTag();
        if (tag != null && tag.contains("pages")) {
            //seems like a vanilla book, let's make a copy of the book and add
            Book book = this.book.copy();

            //add our text pages
            NbtList pages = tag.getList("pages", NbtElementCompat.STRING_TYPE);
            for (int i = 0; i < pages.size(); i++) {
                book.addPage(new TextPage(pages.getString(i)));
            }

            return book;
        } else {
            return book;
        }
    }
}
