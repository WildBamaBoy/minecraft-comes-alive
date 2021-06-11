package mca.data;

import mca.core.MCA;
import mca.core.forge.TagsMCA;
import mca.core.minecraft.ItemsMCA;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemTagsProviderMCA extends ItemTagsProvider {
    public ItemTagsProviderMCA(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagProvider, MCA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_GOLD, Tags.Items.ORES_GOLD);
        copy(TagsMCA.Blocks.ORES_ROSE_GOLD, TagsMCA.Items.ORES_ROSE_GOLD);

        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_ROSE_GOLD, TagsMCA.Items.STORAGE_BLOCKS_ROSE_GOLD);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_VILLAGER_SPAWNER, TagsMCA.Items.STORAGE_BLOCKS_VILLAGER_SPAWNER);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_TOMBSTONE, TagsMCA.Items.STORAGE_BLOCKS_TOMBSTONE);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_JEWELER_WORKBENCH, TagsMCA.Items.STORAGE_BLOCKS_JEWELER_WORKBENCH);


        tag(TagsMCA.Items.INGOTS_ROSE_GOLD).add(ItemsMCA.ROSE_GOLD_INGOT.get());
        tag(Tags.Items.INGOTS).addTag(TagsMCA.Items.INGOTS_ROSE_GOLD);
        tag(TagsMCA.Items.INGOTS_ROSE_GOLD).add(ItemsMCA.ENGAGEMENT_RING_RG.get());

        tag(Tags.Items.INGOTS_GOLD).add(ItemsMCA.ENGAGEMENT_RING.get());
        tag(TagsMCA.Items.INGOTS_ROSE_GOLD).add(ItemsMCA.ROSE_GOLD_DUST.get(), ItemsMCA.GOLD_DUST.get());
        tag(Tags.Items.DUSTS).addTag(TagsMCA.Items.INGOTS_ROSE_GOLD);
        tag(Tags.Items.DUSTS).addTag(Tags.Items.INGOTS_GOLD);

        tag(Tags.Items.INGOTS).add(ItemsMCA.ENGAGEMENT_RING.get(), ItemsMCA.WEDDING_RING.get(), ItemsMCA.MATCHMAKERS_RING.get());
        tag(Tags.Items.INGOTS).add(ItemsMCA.ENGAGEMENT_RING_RG.get(), ItemsMCA.WEDDING_RING_RG.get());

        tag(TagsMCA.Items.LECTERN_BOOKS).add(ItemsMCA.BOOK_ROSE_GOLD.get(), ItemsMCA.BOOK_DEATH.get(), ItemsMCA.BOOK_ROMANCE.get(), ItemsMCA.BOOK_FAMILY.get(), ItemsMCA.BOOK_INFECTION.get());
    }
}
