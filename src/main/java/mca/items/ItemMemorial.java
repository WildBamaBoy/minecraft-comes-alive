package mca.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
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
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) 
	{
		if (!worldIn.isRemote && stack.hasTagCompound())
		{
			int posX = pos.getX();
			int posY = pos.getY() + 1;
			int posZ = pos.getZ();			
			pos = new BlockPos(posX, posY, posZ);
			
			worldIn.setBlockState(pos, ModBlocks.memorial.getDefaultState(), 2);
			TileMemorial tile = (TileMemorial) BlockHelper.getTileEntity(worldIn, posX, posY, posZ);
			
			tile.setType(this.type);
			tile.setVillagerSaveData(VillagerSaveData.fromNBT(stack.getTagCompound()));
			tile.setOwnerName(stack.getTagCompound().getString("ownerName"));

			if (stack.hasTagCompound())
			{
				tile.setRelation(EnumRelation.getById(stack.getTagCompound().getInteger("relation")));
			}
			
			else
			{
				tile.setRelation(EnumRelation.NONE);
			}

			playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, null);
			return EnumActionResult.SUCCESS;
		}
		
		return EnumActionResult.PASS;
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
