package mca.items;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.core.MCA;
import net.minecraft.item.Item;

public class ItemGemCutter extends Item
{
	public ItemGemCutter()
	{
		super();
		
		this.setUnlocalizedName("GemCutter");
		this.setTextureName("mca:GemCutter");
		GameRegistry.registerItem(this, "GemCutter");
		this.setCreativeTab(MCA.getCreativeTabGemCutting());
		this.setMaxStackSize(1);
		this.setMaxDamage(16);
	}
}
