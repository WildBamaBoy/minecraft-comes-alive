package mca.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemTooltipAppender extends Item 
{
	private List<String> info;

	public ItemTooltipAppender()
	{
		super();
		info = new ArrayList<String>();
	}
	
	public ItemTooltipAppender setTooltip(String... lines)
	{
		for (String line : lines)
		{
			info.add(line);
		}
		
		return this;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) 
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.addAll(info);
	}
}
