package mca.api.platforms;

import java.util.Random;

import mca.api.objects.Pos;
import mca.api.wrappers.WorldWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockPlatform extends Block {
	public BlockPlatform(Material materialIn) {
		super(materialIn);
	}
	
	public void onUpdate(WorldWrapper world, Pos pos, IBlockState state, Random random) {
		
	}

	public final void setUnbreakable() {
		this.setBlockUnbreakable();
	}
	
	public final void setRandomlyTicks() {
		this.setTickRandomly(true);
	}
	
    @Override
    public final void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
    	this.onUpdate(new WorldWrapper(world), new Pos(pos), state, random);
    }
}
