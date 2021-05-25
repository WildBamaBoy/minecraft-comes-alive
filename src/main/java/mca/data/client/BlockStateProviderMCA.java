package mca.data.client;

import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateProviderMCA extends BlockStateProvider {
    public BlockStateProviderMCA(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, MCA.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(BlocksMCA.ROSE_GOLD_ORE.get());
        simpleBlock(BlocksMCA.ROSE_GOLD_BLOCK.get());
        //simpleBlock(BlocksMCA.VILLAGER_SPAWNER.get());
        //simpleBlock(BlocksMCA.TOMBSTONE.grt());
        //simpleBlock(BlocksMCA.JEWELER_WORKBENCH.grt());

    }
}
