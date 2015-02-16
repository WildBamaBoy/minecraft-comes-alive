package radixcore.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemSingleEffect extends Item
{
	public ItemSingleEffect()
	{
		maxStackSize = 1;
	}
	
	@Override
	public boolean hasEffect(ItemStack itemStack, int pass) 
	{
		return true;
	}
}
