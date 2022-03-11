package mca.client.book.pages;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.text.Text;

public class DynamicListPage extends ListPage {
    private final Function<Page, List<Text>> generator;

    public DynamicListPage(String title, Function<Page, List<Text>> generator) {
        super(title, new LinkedList<>());

        this.generator = generator;
    }

    public DynamicListPage(Text title, Function<Page, List<Text>> generator) {
        super(title, new LinkedList<>());

        this.generator = generator;
    }

    @Override
    public void open(boolean back) {
        text.clear();
        text.addAll(generator.apply(this));

        super.open(back);
    }
}
