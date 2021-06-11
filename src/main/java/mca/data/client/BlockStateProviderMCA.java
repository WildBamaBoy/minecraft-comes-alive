package mca.data.client;

import mca.core.MCA;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateProviderMCA extends BlockStateProvider {
    public BlockStateProviderMCA(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, MCA.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }
}
