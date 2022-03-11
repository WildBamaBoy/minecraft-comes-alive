package mca.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;

public class InfernalFlameBlock extends AbstractFireBlock {
    public InfernalFlameBlock(AbstractBlock.Settings settings) {
        super(settings, 2.0F);
    }

    @Override
    protected boolean isFlammable(BlockState state) {
        return true;
    }
}
