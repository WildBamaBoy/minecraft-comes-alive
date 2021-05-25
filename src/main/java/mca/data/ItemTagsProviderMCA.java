package mca.data;

import mca.core.MCA;
import mca.core.forge.TagsMCA;
import mca.core.minecraft.ItemsMCA;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemTagsProviderMCA extends ItemTagsProvider {
    public ItemTagsProviderMCA(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagProvider, MCA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(TagsMCA.Blocks.ORES_ROSE_GOLD, TagsMCA.Items.ORES_ROSE_GOLD);

        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_ROSE_GOLD, TagsMCA.Items.STORAGE_BLOCKS_ROSE_GOLD);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_VILLAGER_SPAWNER, TagsMCA.Items.STORAGE_BLOCKS_VILLAGER_SPAWNER);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_TOMBSTONE, TagsMCA.Items.STORAGE_BLOCKS_TOMBSTONE);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_JEWELER_WORKBENCH, TagsMCA.Items.STORAGE_BLOCKS_JEWELER_WORKBENCH);


        tag(TagsMCA.Items.INGOTS_ROSE_GOLD).add(ItemsMCA.ITEM_ENGAGEMENT_RING.get());
        tag(TagsMCA.Items.INGOTS_ROSE_GOLD).add(ItemsMCA.ROSE_GOLD_INGOT.get());
        tag(TagsMCA.Items.INGOTS_ROSE_GOLD).add(ItemsMCA.ITEM_ROSE_GOLD_DUST.get(),ItemsMCA.ITEM_GOLD_DUST.get());
        tag(Tags.Items.INGOTS).addTag(TagsMCA.Items.INGOTS_ROSE_GOLD);
        tag(Tags.Items.DUSTS).addTag(TagsMCA.Items.INGOTS_ROSE_GOLD);
        tag(Tags.Items.DUSTS).addTag(Tags.Items.INGOTS_GOLD);
    }
}
