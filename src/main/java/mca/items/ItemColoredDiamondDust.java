package mca.items;

import mca.core.MCA;
import cpw.mods.fml.common.registry.GameRegistry;
import radixcore.item.ItemColorable;

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
