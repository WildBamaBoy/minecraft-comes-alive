package mca.items;

import mca.core.MCA;
import radixcore.item.ItemColorable;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemColoredDiamondDust extends ItemColorable
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
}
