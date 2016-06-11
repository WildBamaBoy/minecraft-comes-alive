package mca.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import mca.core.MCA;
import mca.core.minecraft.ModBlocks;
import mca.data.VillagerSaveData;
import mca.enums.EnumMemorialType;
import mca.enums.EnumRelation;
import mca.tile.TileMemorial;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
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
		GameRegistry.registerItem(this, type.getTypeName());
		this.setCreativeTab(MCA.getCreativeTabMain());
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) 
	{
		if (!worldIn.isRemote && stack.hasTagCompound())
		{
			int posX = pos.getX();
			int posY = pos.getY();
			int posZ = pos.getZ();
			
			posY++;
			
			BlockHelper.setBlock(worldIn, new Point3D(posX, posY, posZ), ModBlocks.memorial);
			TileMemorial tile = (TileMemorial) BlockHelper.getTileEntity(worldIn, posX, posY, posZ);
			
			tile.setType(this.type);
			tile.setVillagerSaveData(VillagerSaveData.fromNBT(stack.getTagCompound()));
			tile.setOwnerName(stack.getTagCompound().getString("ownerName"));
			tile.setRelation(EnumRelation.getById(stack.getTagCompound().getInteger("relation")));
			
			playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, null);
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
			String relation = MCA.getLanguageManager().getString(EnumRelation.getById(itemStack.getTagCompound().getInteger("relation")).getPhraseId());
			
			infoList.add(Color.WHITE + "Belonged to: ");
			infoList.add(Color.GREEN + name + ", " + relation + " of " + ownerName);
		}

		else
		{
			infoList.add(Color.GREEN + "CREATIVE " + Format.RESET + "- No villager attached.");
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
