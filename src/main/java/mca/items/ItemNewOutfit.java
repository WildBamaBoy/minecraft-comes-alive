package mca.items;

import java.util.List;

import mca.core.MCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemNewOutfit extends Item
{
	public ItemNewOutfit()
	{
		super();
		
		this.setMaxStackSize(1);
		this.setMaxDamage(16);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) 
	{
		tooltip.add("Use on your spouse or children ");
		tooltip.add("to change their outfit.");
	}
}
