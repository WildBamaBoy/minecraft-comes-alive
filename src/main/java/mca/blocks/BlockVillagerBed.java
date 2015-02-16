package mca.blocks;

import java.util.Random;

import mca.ai.AISleep;
import mca.entity.EntityHuman;
import mca.enums.EnumBedColor;
import mca.tile.TileVillagerBed;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import radixcore.helpers.LogicHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockVillagerBed extends BlockDirectional implements ITileEntityProvider
{
	public static final int[][] blockMap = new int[][] { { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 0 } };

	@SideOnly(Side.CLIENT)
	protected IIcon[] textureEnd;
	@SideOnly(Side.CLIENT)
	protected IIcon[] textureSide;
	@SideOnly(Side.CLIENT)
	protected IIcon[] textureTop;

	private EnumBedColor bedColor;
	
	public BlockVillagerBed(EnumBedColor bedColor)
	{
		super(Material.cloth);
		this.setBlockBounds();
		this.bedColor = bedColor;
		
		GameRegistry.registerBlock(this, "BlockVillagerBed" + bedColor.toString());
	}

	@Override
	public boolean onBlockActivated(World worldObj, int posX, int posY, int posZ, EntityPlayer entityPlayer, int unknown, float unknown2, float unknown3, float unknown4)
	{
		if (worldObj.isRemote)
		{
			//TODO Say to player, "you cannot sleep in a villager's bed."
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if (side == 0)
		{
			return Blocks.planks.getBlockTextureFromSide(side);
		}

		else
		{
			final int k = getDirection(meta);
			final int l = Direction.bedDirection[k][side];
			final int i1 = isBlockHeadOfBed(meta) ? 1 : 0;
			return (i1 != 1 || l != 2) && (i1 != 0 || l != 3) ? l != 5 && l != 4 ? textureTop[i1] : textureSide[i1] : textureEnd[i1];
		}
	}

	@Override
	public int getRenderType()
	{
		return 14;
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
	public void setBlockBoundsBasedOnState(IBlockAccess world, int posX, int posY, int posZ)
	{
		this.setBlockBounds();
	}

	@Override
	public void onNeighborBlockChange(World worldObj, int posX, int posY, int posZ, Block block)
	{
		final int l = worldObj.getBlockMetadata(posX, posY, posZ);
		final int i1 = getDirection(l);

		if (isBlockHeadOfBed(l))
		{
			if (worldObj.getBlock(posX - blockMap[i1][0], posY, posZ - blockMap[i1][1]) != this)
			{
				worldObj.setBlockToAir(posX, posY, posZ);
			}
		}
		
		else if (worldObj.getBlock(posX + blockMap[i1][0], posY, posZ + blockMap[i1][1]) != this)
		{
			worldObj.setBlockToAir(posX, posY, posZ);

			if (!worldObj.isRemote)
			{
				this.dropBlockAsItem(worldObj, posX, posY, posZ, l, 0);
			}
		}
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
	{
		return Item.getItemById(0);
	}

	private void setBlockBounds()
	{
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5625F, 1.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown)
	{
		return new TileVillagerBed();
	}

	public static boolean isBlockHeadOfBed(int meta)
	{
		return (meta & 8) != 0;
	}

	public static boolean isBlockFootOfBed(int meta)
	{
		return (meta & 4) != 0;
	}

	@Override
	public void dropBlockAsItemWithChance(World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_, int p_149690_5_, float p_149690_6_, int p_149690_7_)
	{
		if (!isBlockHeadOfBed(p_149690_5_))
		{
			super.dropBlockAsItemWithChance(p_149690_1_, p_149690_2_, p_149690_3_, p_149690_4_, p_149690_5_, p_149690_6_, 0);
		}
	}

	@Override
	public int getMobilityFlag()
	{
		return 1;
	}

	@Override
	public void onBlockHarvested(World world, int posX, int posY, int posZ, int meta, EntityPlayer entityPlayer)
	{
		if (entityPlayer.capabilities.isCreativeMode && isBlockHeadOfBed(meta))
		{
			final int direction = getDirection(meta);
			posX -= blockMap[direction][0];
			posZ -= blockMap[direction][1];

			if (world.getBlock(posX, posY, posZ) == this)
			{
				world.setBlockToAir(posX, posY, posZ);
			}
		}
	}

	@Override
	public void onBlockPreDestroy(World world, int posX, int posY, int posZ, int meta)
	{
		super.onBlockPreDestroy(world, posX, posY, posZ, meta);

		if (!world.isRemote)
		{
			final TileEntity tileEntity = world.getTileEntity(posX, posY, posZ);

			if (tileEntity instanceof TileVillagerBed)
			{
				final TileVillagerBed villagerBed = (TileVillagerBed) tileEntity;

				if (villagerBed.getSleepingVillagerId() != -1)
				{
					try
					{
						final EntityHuman entity = (EntityHuman) LogicHelper.getEntityByPermanentId(world, villagerBed.getSleepingVillagerId());
						
						if (entity != null)
						{
							final AISleep sleepAI = entity.getAI(AISleep.class);
							sleepAI.setIsSleeping(false);
						}
					}

					catch (final NullPointerException e)
					{
					}
				}
			}
		}
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		textureTop = new IIcon[] { iconRegister.registerIcon("mca:VillagerBed-Feet-Top-" + bedColor.toString()), iconRegister.registerIcon("mca:VillagerBed-Head-Top-" + bedColor.toString()) };
		textureEnd = new IIcon[] { iconRegister.registerIcon("mca:VillagerBed-Feet-End-" + bedColor.toString()), iconRegister.registerIcon("mca:VillagerBed-Head-End") };
		textureSide = new IIcon[] { iconRegister.registerIcon("mca:VillagerBed-Feet-Side-" + bedColor.toString()), iconRegister.registerIcon("mca:VillagerBed-Head-Side-" + bedColor.toString()) };
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int posX, int posY, int posZ)
	{
		return Items.bed;
	}
}
