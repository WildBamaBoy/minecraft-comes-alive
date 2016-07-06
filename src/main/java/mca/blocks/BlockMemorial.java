package mca.blocks;

import mca.core.minecraft.ModItems;
import mca.tile.TileMemorial;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import radixcore.util.BlockHelper;

public class BlockMemorial extends BlockContainer
{
	public BlockMemorial()
	{
		super(Material.cloth);
		this.setBlockName("memorialblock");
		setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.75F, 0.9F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown)
	{
		return new TileMemorial();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int posX, int posY, int posZ)
	{
		return null; //No collision.
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int posX, int posY, int posZ)
	{
		setBlockBoundsBasedOnState(world, posX, posY, posZ);
		return super.getSelectedBoundingBoxFromPool(world, posX, posY, posZ);
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) 
	{
		return null;
	}

	@Override
	public void onBlockPreDestroy(World world, int posX, int posY, int posZ, int meta) 
	{
		super.onBlockPreDestroy(world, posX, posY, posZ, meta);

		if (!world.isRemote)
		{
			TileMemorial memorial = (TileMemorial) BlockHelper.getTileEntity(world, posX, posY, posZ);			
			Item memorialItem = null;
			ItemStack memorialStack = null;

			switch (memorial.getType())
			{
			case BROKEN_RING: memorialItem = ModItems.brokenRing; break;
			case DOLL: memorialItem = ModItems.childsDoll; break;
			case TRAIN: memorialItem = ModItems.toyTrain; break;
			}

			if (memorial.getRevivalTicks() == 0) //Will be 1 when removed from a villager revival.
			{
				memorialStack = new ItemStack(memorialItem);
				memorialStack.stackTagCompound = new NBTTagCompound();
				memorialStack.stackTagCompound.setInteger("relation", memorial.getRelation().getId());
				memorialStack.stackTagCompound.setString("ownerName", memorial.getOwnerName());
				memorial.getVillagerSaveData().writeDataToNBT(memorialStack.stackTagCompound);

				dropBlockAsItem(world, posX, posY, posZ, memorialStack);
			}
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, int posX, int posY, int posZ, int meta)
	{
		//Do nothing to avoid duplication glitch.
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}
	
	@Override
	public void registerBlockIcons(IIconRegister IIconRegister)
	{
		blockIcon = IIconRegister.registerIcon("mca:Tombstone");
	}
}
