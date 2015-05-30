package mca.items;

import mca.core.MCA;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemGemCutter extends Item
{
	public ItemGemCutter()
	{
		super();
		
		this.setUnlocalizedName("GemCutter");
		GameRegistry.registerItem(this, "GemCutter");
		this.setCreativeTab(MCA.getCreativeTabGemCutting());
		this.setMaxStackSize(1);
		this.setMaxDamage(16);
	}
}
