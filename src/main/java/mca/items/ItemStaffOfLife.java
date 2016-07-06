package mca.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.VillagerSaveData;
import mca.enums.EnumMemorialType;
import mca.tile.TileMemorial;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.util.BlockHelper;

public class ItemStaffOfLife extends Item
{
	public ItemStaffOfLife()
	{
		super();
		maxStackSize = 1;
		setCreativeTab(MCA.getCreativeTabMain());
		setUnlocalizedName("StaffOfLife");
		setMaxDamage(4);
		GameRegistry.registerItem(this, "StaffOfLife");
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int posX, int posY, int posZ, int meta, float xOffset, float yOffset, float zOffset)
	{
		if (!world.isRemote)
		{
			TileEntity tile = BlockHelper.getTileEntity(world, posX, posY, posZ);

			if (tile instanceof TileMemorial)
			{
				TileMemorial memorial = (TileMemorial)tile;
				VillagerSaveData data = memorial.getVillagerSaveData();
				NBTPlayerData playerData = MCA.getPlayerData(player);

				//Make sure the owner is the one reviving them.
				if (!data.ownerUUID.equals(player.getUniqueID()))
				{
					player.addChatComponentMessage(new ChatComponentText(Color.RED + "You cannot revive " + data.name + " because they are not related to you."));
					return false;
				}
				
				//For rings, they belonged to a spouse. Check for remarriage and forbid.
				if (memorial.getType() == EnumMemorialType.BROKEN_RING && (playerData.getIsEngaged() || playerData.getIsMarried()))
				{
					player.addChatComponentMessage(new ChatComponentText(Color.RED + "You cannot revive " + data.name + " because you are already married."));
					return false;
				}
				
				//Once everything is okay, set the tile's revival ticks to begin the revival process.
				memorial.setPlayer(player);
				memorial.setRevivalTicks(Time.SECOND * 5);
				itemStack.damageItem(1, player);
				world.playSoundAtEntity(player, "portal.travel", 1.0F, 1.0F);
			}
		}
		
		return false;
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:StaffOfLife");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack itemStack)
	{
		return true;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List infoList, boolean unknown)
	{
		infoList.add("Uses left: " + (itemStack.getMaxDamage() - itemStack.getItemDamage() + 1));
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			infoList.add("Use on an item owned by a dead");
			infoList.add("villager to revive them. Item");
			infoList.add("must be placed in the world.");
		}

		else
		{
			infoList.add("Hold " + Color.YELLOW + "SHIFT" + Color.GRAY + " for info.");
		}
	}
}
