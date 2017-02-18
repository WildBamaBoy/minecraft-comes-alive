package mca.items;

import mca.api.IGiftableItem;
import mca.core.MCA;
import mca.enums.EnumCut;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemCutRingColored extends ItemColorable implements IGiftableItem
{
	private EnumCut cut;
	private boolean isRoseGold;
	
	public ItemCutRingColored(EnumCut cut, boolean isRoseGold)
	{
		String name = "Ring" + cut.toString() + "Colored";
		name += isRoseGold ? "RG" : "";
		
		this.cut = cut;
		this.isRoseGold = isRoseGold;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		
		this.setUnlocalizedName(name);
		GameRegistry.registerItem(this, name);

		this.setCreativeTab(MCA.getCreativeTabGemCutting());
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) 
	{
		return "item.EngagementRing" + (isRoseGold ? "RG" : "");
	}

	@Override
	public int getColorFromItemstack(ItemStack itemStack, int pass)
	{
		return pass == 0 ? super.getColorFromItemstack(itemStack, pass) : 0xFFFFFF;
	}
	
	@Override
	public int getGiftValue() 
	{
		return 75;
	}
}
