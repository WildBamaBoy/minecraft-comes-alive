package mca.items;

import mca.core.MCA;
import mca.enums.EnumCut;
import radixcore.item.ItemColorable;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemColoredDiamond extends ItemColorable
{
	private EnumCut cut;
	
	public ItemColoredDiamond(EnumCut cut)
	{
		String name = "ColoredDiamond" + cut.toString();
		
		this.cut = cut;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName(name);
		this.setTextureName("mca:" + name);
		this.setCreativeTab(MCA.getCreativeTabGemCutting());

		GameRegistry.registerItem(this, name);
	}
}
