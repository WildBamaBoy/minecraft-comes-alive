package mca.data;

import mca.core.MCA;
import mca.core.forge.TagsMCA;
import mca.core.minecraft.BlocksMCA;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.BlockTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagsProviderMCA extends BlockTagsProvider {
    public BlockTagsProviderMCA(DataGenerator generatorIn, ExistingFileHelper existingFileHelper) {
        super(generatorIn, MCA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void configure() {
        getOrCreateTagBuilder(TagsMCA.Blocks.ORES_ROSE_GOLD).add(BlocksMCA.ROSE_GOLD_ORE.get());
        getOrCreateTagBuilder(Tags.Blocks.ORES).addTag(TagsMCA.Blocks.ORES_ROSE_GOLD);
        getOrCreateTagBuilder(TagsMCA.Blocks.STORAGE_BLOCKS_ROSE_GOLD).add(BlocksMCA.ROSE_GOLD_BLOCK.get());
        getOrCreateTagBuilder(Tags.Blocks.STORAGE_BLOCKS).addTag(TagsMCA.Blocks.STORAGE_BLOCKS_ROSE_GOLD);
    }
}
