package mca.data;

import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.TagsMCA;
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
        copy(TagsMCA.Blocks.ORES_ROSE_GOLD, TagsMCA.Items.ORES_ROSE_GOLD);
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(TagsMCA.Blocks.STORAGE_BLOCKS_ROSE_GOLD, TagsMCA.Items.STORAGE_BLOCKS_ROSE_GOLD);
        //copy(TagsMCA.Blocks.STORAGE_BLOCKS_VILLAGER_SPAWNER, TagsMCA.Items.STORAGE_BLOCKS_VILLAGER_SPAWNER);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);



        tag(TagsMCA.Items.INGOTS_ROSE_GOLD).add(ItemsMCA.ITEM_ENGAGEMENT_RING.get());
        tag(TagsMCA.Items.INGOTS_ROSE_GOLD).add(ItemsMCA.ROSE_GOLD_INGOT.get());
        tag(Tags.Items.INGOTS).addTag(TagsMCA.Items.INGOTS_ROSE_GOLD);
    }
}
