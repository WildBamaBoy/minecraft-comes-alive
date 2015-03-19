package mca.items;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ModBlocks;
import mca.tile.TileTombstone;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemTombstone extends Item
{
	public ItemTombstone()
	{
		super();
		maxStackSize = 1;
		setCreativeTab(MCA.getCreativeTabMain());
		setUnlocalizedName("tombstone");
		GameRegistry.registerItem(this, "tombstone");
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int posX, int posY, int posZ, int meta, float xOffset, float yOffset, float zOffset)
	{
		if (meta == 0)
		{
			return false;
		}

		else if (!world.getBlock(posX, posY, posZ).getMaterial().isSolid())
		{
			return false;
		}

		else
		{
			if (meta == 1)
			{
				++posY;
			}

			if (meta == 2)
			{
				--posZ;
			}

			if (meta == 3)
			{
				++posZ;
			}

			if (meta == 4)
			{
				--posX;
			}

			if (meta == 5)
			{
				++posX;
			}

			if (!player.canPlayerEdit(posX, posY, posZ, meta, itemStack))
			{
				return false;
			}

			else if (!ModBlocks.tombstone.canPlaceBlockAt(world, posX, posY, posZ) && !world.isAirBlock(posX, posY, posZ))
			{
				return false;
			}

			else
			{
				if (meta == 1)
				{
					final int newMeta = MathHelper.floor_double((player.rotationYaw + 180F) * 16F / 360F + 0.5D) & 15;
					world.setBlock(posX, posY, posZ, ModBlocks.tombstone, newMeta, 2);
				}

				else
				{
					world.setBlock(posX, posY, posZ, ModBlocks.tombstone, meta, 2);
				}

				--itemStack.stackSize;
				final TileTombstone tombstone = (TileTombstone) world.getTileEntity(posX, posY, posZ);

				if (tombstone != null)
				{
					player.openGui(MCA.getInstance(), Constants.GUI_ID_TOMBSTONE, world, tombstone.xCoord, tombstone.yCoord, tombstone.zCoord);
				}

				return true;
			}
		}
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:Tombstone");
	}
}
