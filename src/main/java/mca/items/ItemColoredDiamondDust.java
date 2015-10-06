package mca.items;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.api.IGiftableItem;
import mca.core.MCA;
import net.minecraft.item.ItemStack;
import radixcore.item.ItemColorable;

public class ItemColoredDiamondDust extends ItemColorable implements IGiftableItem
{
	public ItemColoredDiamondDust()
	{
		super();
		
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName("ColoredDiamondDust");
		this.setTextureName("mca:ColoredDiamondDust");
		this.setCreativeTab(MCA.getCreativeTabGemCutting());

		GameRegistry.registerItem(this, "ColoredDiamondDust");
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) 
	{
		return "item.DiamondDust";
	}
	
	@Override
	public int getGiftValue() 
	{
		return 10;
	}
}
