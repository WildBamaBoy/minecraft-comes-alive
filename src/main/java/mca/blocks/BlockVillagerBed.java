package mca.blocks;

import mca.enums.EnumBedColor;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockVillagerBed extends BlockDirectional implements ITileEntityProvider
{
	public static final int[][] blockMap = new int[][] { { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 0 } };

	private EnumBedColor bedColor;
	
	public BlockVillagerBed(EnumBedColor bedColor)
	{
		super(Material.cloth);
//		this.setBlockBounds();
		this.bedColor = bedColor;
		
		GameRegistry.registerBlock(this, "BlockVillagerBed" + bedColor.toString());
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public boolean onBlockActivated(World worldObj, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
//	{
//		if (worldObj.isRemote)
//		{
//			player.addChatMessage(new ChatComponentText("You cannot sleep in a villager's bed."));
//		}
//
//		return true;
//	}
//
//	@Override
//	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) 
//	{
//		return new ItemStack(getItemDropped(0, world.rand, 0));
//	}
//
//	@Override
//	public int getRenderType()
//	{
//		return 14;
//	}
//
//	@Override
//	public boolean isOpaqueCube()
//	{
//		return false;
//	}
//
//	@Override
//	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
//	{
//		this.setBlockBounds();
//	}
//
//	@Override
//	public void onNeighborBlockChange(World worldObj, BlockPos pos, IBlockState state, Block blockNeighbor)
//	{
//		int posX = pos.getX();
//		int posY = pos.getY();
//		int posZ = pos.getZ();
//		
//		final int l = BlockHelper.getBlockMetadata(worldObj, posX, posY, posZ);
//		final int i1 = getDirection(l);
//
//		if (isBlockHeadOfBed(l))
//		{
//			if (BlockHelper.getBlock(worldObj, posX - blockMap[i1][0], posY, posZ - blockMap[i1][1]) != this)
//			{
//				BlockHelper.setBlock(worldObj, posX, posY, posZ, Blocks.air);
//			}
//		}
//		
//		else if (BlockHelper.getBlock(worldObj, posX + blockMap[i1][0], posY, posZ + blockMap[i1][1]) != this)
//		{
//			BlockHelper.setBlock(worldObj, posX, posY, posZ, Blocks.air);
//
//			if (!worldObj.isRemote)
//			{
//				this.dropBlockAsItem(worldObj, posX, posY, posZ, l, 0);
//			}
//		}
//	}
//
//	@Override
//	public Item getItemDropped(IBlockState state, Random rand, int fortune)
//	{
//		switch (bedColor)
//		{
//		case BLUE: return ModItems.bedBlue;
//		case GREEN: return ModItems.bedGreen;
//		case PINK: return ModItems.bedPink;
//		case PURPLE: return ModItems.bedPurple;
//		case RED: return ModItems.bedRed;
//		default:
//			return null;
//		}
//	}
//
//	private void setBlockBounds()
//	{
//		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5625F, 1.0F);
//	}
//
//	@Override
//	public TileEntity createNewTileEntity(World world, int unknown)
//	{
//		return new TileVillagerBed();
//	}
//
//	public static boolean isBlockHeadOfBed(int meta)
//	{
//		return (meta & 8) != 0;
//	}
//
//	public static boolean isBlockFootOfBed(int meta)
//	{
//		return (meta & 4) != 0;
//	}
//
//	@Override
//    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune)
//	{
//		int posX = pos.getX();
//		int posY = pos.getY();
//		int posZ = pos.getZ();
//		
//		if (!isBlockHeadOfBed(state))
//		{
//			super.dropBlockAsItemWithChance(world, pos, state, chance, 0);
//		}
//	}
//
//	@Override
//	public int getMobilityFlag()
//	{
//		return 1;
//	}
//
//	@Override
//	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer entityPlayer)
//	{
//		int posX = pos.getX();
//		int posY = pos.getY();
//		int posZ = pos.getZ();
//	
//		if (entityPlayer.capabilities.isCreativeMode && isBlockHeadOfBed(state))
//		{
//			final int direction = getDirection(state);
//			posX -= blockMap[direction][0];
//			posZ -= blockMap[direction][1];
//
//			if (BlockHelper.getBlock(world, posX, posY, posZ) == this)
//			{
//				BlockHelper.setBlock(world, posX, posY, posZ, Blocks.air);
//			}
//		}
//	}
//
//	//FIXME
////	@Override
////	public void onBlockPreDestroy(World world, int posX, int posY, int posZ, int meta)
////	{
////		super.onBlockPreDestroy(world, posX, posY, posZ, meta);
////
////		if (!world.isRemote)
////		{
////			final TileEntity tileEntity = BlockHelper.getTileEntity(world, posX, posY, posZ);
////
////			if (tileEntity instanceof TileVillagerBed)
////			{
////				final TileVillagerBed villagerBed = (TileVillagerBed) tileEntity;
////
////				if (villagerBed.getSleepingVillagerId() != -1)
////				{
////					try
////					{
////						final EntityHuman entity = (EntityHuman) RadixLogic.getEntityByPermanentId(world, villagerBed.getSleepingVillagerId());
////						
////						if (entity != null)
////						{
////							final AISleep sleepAI = entity.getAI(AISleep.class);
////							sleepAI.setIsSleeping(false);
////						}
////					}
////
////					catch (final NullPointerException e)
////					{
////						//Ignore.
////					}
////				}
////			}
////		}
////	}
//
//	@Override
//	public boolean hasTileEntity(IBlockState metadata)
//	{
//		return true;
//	}
}
