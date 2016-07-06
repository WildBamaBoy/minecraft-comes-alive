package mca.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.core.MCA;
import mca.core.minecraft.ModBlocks;
import mca.data.VillagerSaveData;
import mca.enums.EnumMemorialType;
import mca.enums.EnumProfession;
import mca.enums.EnumRelation;
import mca.tile.TileMemorial;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;

/** Class for an item dropped containing data about the villager who died. */
public class ItemMemorial extends Item
{
	private EnumMemorialType type;

	public ItemMemorial(EnumMemorialType type)
	{
		super();

		this.type = type;
		this.setUnlocalizedName(type.getTypeName());
		this.setTextureName("mca:" + type.getTypeName());
		GameRegistry.registerItem(this, type.getTypeName());
		this.setCreativeTab(MCA.getCreativeTabMain());
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int posX, int posY, int posZ, int meta, float xOffset, float yOffset, float zOffset)
	{
		if (!world.isRemote && itemStack.hasTagCompound())
		{
			posY++;
			
			BlockHelper.setBlock(world, new Point3D(posX, posY, posZ), ModBlocks.memorial);
			TileMemorial tile = (TileMemorial) BlockHelper.getTileEntity(world, posX, posY, posZ);
			
			tile.setType(this.type);
			tile.setVillagerSaveData(VillagerSaveData.fromNBT(itemStack.getTagCompound()));
			tile.setOwnerName(itemStack.getTagCompound().getString("ownerName"));
			
			if (itemStack.hasTagCompound())
			{
				tile.setRelation(EnumRelation.getById(itemStack.getTagCompound().getInteger("relation")));
			}
			
			else
			{
				tile.setRelation(EnumRelation.NONE);
			}
			
			player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
		}
		
		return true;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List infoList, boolean unknown)
	{
		super.addInformation(itemStack, entityPlayer, infoList, unknown);

		if (itemStack.hasTagCompound())
		{
			VillagerSaveData data = VillagerSaveData.fromNBT(itemStack.getTagCompound());
			String ownerName = itemStack.getTagCompound().getString("ownerName");
			String name = data.name;
			String relationId = EnumRelation.getById(itemStack.getTagCompound().getInteger("relation")).getPhraseId(); 
			
			infoList.add(Color.WHITE + "Belonged to: ");

			if (!relationId.equals("relation.none"))
			{
				infoList.add(Color.GREEN + name + ", " + MCA.getLanguageManager().getString(relationId) + " of " + ownerName);
			}
			
			else
			{
				infoList.add(Color.GREEN + name + " the " + MCA.getLanguageManager().getString(EnumProfession.getProfessionById(data.professionId).getLocalizationId()));
				infoList.add("Captured by: " + ownerName);
			}
		}

		else
		{
			infoList.add(Color.GREEN + "CREATIVE " + Format.RESET + "- No villager attached.");
			infoList.add("Right-click a villager to attach them");
			infoList.add("to this object.");
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			infoList.add("");
			infoList.add("An item once owned by a");
			infoList.add("villager who has died. Revive ");
			infoList.add("them using the " + Color.YELLOW + "Staff of Life" + Color.GRAY + ".");
		}

		else
		{
			infoList.add("");
			infoList.add("Hold " + Color.YELLOW + "SHIFT" + Color.GRAY + " for info.");
		}
	}
}
