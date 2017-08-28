package mca.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.data.TransitiveVillagerData;
import mca.enums.EnumMemorialType;
import mca.enums.EnumRelation;
import mca.tile.TileMemorial;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;

/** Class for an item dropped containing data about the villager who died. */
public class ItemMemorial extends Item
{
	private EnumMemorialType type;

	public ItemMemorial(EnumMemorialType type)
	{
		super();

		this.type = type;
		this.setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) 
	{
		ItemStack stack = playerIn.getHeldItem(hand);
		
		if (!worldIn.isRemote && stack.hasTagCompound())
		{
			pos = pos.add(0, 1, 0);
			worldIn.setBlockState(pos, BlocksMCA.memorial.getDefaultState(), 2);
			TileMemorial tile = (TileMemorial) worldIn.getTileEntity(pos);
			
			tile.setType(this.type);
			tile.setTransitiveVillagerData(new TransitiveVillagerData(stack.getTagCompound()));
			tile.setOwnerName(stack.getTagCompound().getString("ownerName"));
			tile.setOwnerUUID(stack.getTagCompound().getUniqueId("ownerUUID"));
			
			if (stack.hasTagCompound())
			{
				tile.setRelation(EnumRelation.getById(stack.getTagCompound().getInteger("ownerRelation")));
			}
			
			else
			{
				tile.setRelation(EnumRelation.NONE);
			}

			playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, ItemStack.EMPTY);
			return EnumActionResult.SUCCESS;
		}
		
		return EnumActionResult.PASS;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) 
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (stack.hasTagCompound())
		{
			TransitiveVillagerData data = new TransitiveVillagerData(stack.getTagCompound());
			String ownerName = stack.getTagCompound().getString("ownerName");
			String name = data.getName();
			String relationId = EnumRelation.getById(stack.getTagCompound().getInteger("ownerRelation")).getPhraseId(); 
			
			tooltip.add(Color.WHITE + "Belonged to: ");

			if (!relationId.equals("relation.none"))
			{
				tooltip.add(Color.GREEN + name + ", " + MCA.getLocalizer().getString(relationId) + " of " + ownerName);
			}
			
			else
			{
				tooltip.add(Color.GREEN + name + " the " + MCA.getLocalizer().getString(data.getProfession().getLocalizationId()));
				tooltip.add("Captured by: " + ownerName);
			}
		}

		else
		{
			tooltip.add(Color.GREEN + "CREATIVE " + Format.RESET + "- No villager attached.");
			tooltip.add("Right-click a villager to attach them");
			tooltip.add("to this object.");
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			tooltip.add("");
			tooltip.add("An item once owned by a");
			tooltip.add("villager who has died. Revive ");
			tooltip.add("them using the " + Color.YELLOW + "Staff of Life" + Color.GRAY + ".");
		}

		else
		{
			tooltip.add("");
			tooltip.add("Hold " + Color.YELLOW + "SHIFT" + Color.GRAY + " for info.");
		}
	}
}
