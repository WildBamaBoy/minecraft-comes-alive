package mca.items;

import mca.core.MCA;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemVillagerEditor extends Item
{
	public ItemVillagerEditor()
	{
		super();
		maxStackSize = 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack itemStack)
	{
		return true;
	}
}
