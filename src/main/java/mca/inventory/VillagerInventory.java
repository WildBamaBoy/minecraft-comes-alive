package mca.inventory;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

public class VillagerInventory extends InventoryBasic
{
	public VillagerInventory() 
	{
		super(new TextComponentString("Villager Inventory"), 41);
	}

	public int getFirstSlotContainingItem(Item item) 
	{
		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			ItemStack stack = this.getStackInSlot(i);
			
			if (stack.getItem() == item)
			{
				return i;
			}
		}
		
		return -1;
	}
}
