package mca.data;

import mca.core.MCA;
import mca.core.forge.TagsMCA;
import mca.core.minecraft.BlocksMCA;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagsProviderMCA extends BlockTagsProvider {
    public BlockTagsProviderMCA(DataGenerator generatorIn, ExistingFileHelper existingFileHelper) {
        super(generatorIn, MCA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(TagsMCA.Blocks.ORES_ROSE_GOLD).add(BlocksMCA.ROSE_GOLD_ORE.get());
        tag(Tags.Blocks.ORES).addTag(TagsMCA.Blocks.ORES_ROSE_GOLD);
        tag(TagsMCA.Blocks.STORAGE_BLOCKS_ROSE_GOLD).add(BlocksMCA.ROSE_GOLD_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTag(TagsMCA.Blocks.STORAGE_BLOCKS_ROSE_GOLD);
    }
}
