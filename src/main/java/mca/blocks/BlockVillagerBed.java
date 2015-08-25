package mca.blocks;

import java.util.Random;

import mca.ai.AISleep;
import mca.core.minecraft.ModItems;
import mca.entity.EntityHuman;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;

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
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) 
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
        	playerIn.addChatMessage(new ChatComponentText("You cannot sleep in a villager's bed."));
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
			final TileEntity tileEntity = BlockHelper.getTileEntity(worldIn, posX, posY, posZ);

			if (tileEntity instanceof TileVillagerBed)
			{
				final TileVillagerBed villagerBed = (TileVillagerBed) tileEntity;

				if (villagerBed.getSleepingVillagerId() != -1)
				{
					try
					{
						final EntityHuman entity = (EntityHuman) RadixLogic.getEntityByPermanentId(worldIn, villagerBed.getSleepingVillagerId());

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
