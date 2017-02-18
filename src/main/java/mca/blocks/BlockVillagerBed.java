package mca.blocks;

import java.util.Random;

import mca.ai.AISleep;
import mca.core.minecraft.ModItems;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumBedColor;
import mca.enums.EnumSleepingState;
import mca.tile.TileVillagerBed;
import net.minecraft.block.BlockBed;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;

public class BlockVillagerBed extends BlockBed implements ITileEntityProvider
{
	private EnumBedColor bedColor;

	public BlockVillagerBed(EnumBedColor bedColor)
	{
		super();
		this.bedColor = bedColor;
		this.setUnlocalizedName("BlockVillagerBed" + bedColor.toString());
		GameRegistry.registerBlock(this, "BlockVillagerBed" + bedColor.toString());
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileVillagerBed();
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) 
	{
		return new ItemStack(getItemDropped(null, world.rand, 0));
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		switch (bedColor)
		{
		case BLUE: return ModItems.bedBlue;
		case GREEN: return ModItems.bedGreen;
		case PINK: return ModItems.bedPink;
		case PURPLE: return ModItems.bedPurple;
		case RED: return ModItems.bedRed;
		default:
			return null;
		}
	}
	
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
        	playerIn.addChatMessage(new TextComponentString("You cannot sleep in a villager's bed."));
        }
        
        return false;
    }
    
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) 
	{
		onBlockDestroyedByExplosion(worldIn, pos, state);
	}
	
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, IBlockState state) 
	{
		int posX = pos.getX();
		int posY = pos.getY();
		int posZ = pos.getZ();
		
		if (!worldIn.isRemote)
		{
			final TileEntity tileEntity = RadixBlocks.getTileEntity(worldIn, posX, posY, posZ);

			if (tileEntity instanceof TileVillagerBed)
			{
				final TileVillagerBed villagerBed = (TileVillagerBed) tileEntity;

				if (villagerBed.getSleepingVillagerId() != -1)
				{
					try
					{
						final EntityVillagerMCA entity = (EntityVillagerMCA) RadixLogic.getEntityByPermanentId(worldIn, villagerBed.getSleepingVillagerId());

						if (entity != null)
						{
							final AISleep sleepAI = entity.getAI(AISleep.class);
							sleepAI.setSleepingState(EnumSleepingState.INTERRUPTED);
						}
					}

					catch (final NullPointerException e)
					{
						//Ignore.
					}
				}
			}
		}
	}
}
