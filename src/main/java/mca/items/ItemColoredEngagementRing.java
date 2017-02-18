package mca.items;

import mca.api.IGiftableItem;
import mca.core.MCA;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemColoredEngagementRing extends ItemColorable implements IGiftableItem
{
	private boolean isRoseGold;

	public ItemColoredEngagementRing(boolean isRoseGold)
	{
		final String name = isRoseGold ? "ColoredEngagementRingRG" : "ColoredEngagementRing";
		
		this.isRoseGold = isRoseGold;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		
		this.setUnlocalizedName(name);
		GameRegistry.registerItem(this, name);

		this.setCreativeTab(MCA.getCreativeTabGemCutting());
	}

	@Override
	public int getColorFromItemstack(ItemStack itemStack, int pass)
	{
		return pass == 0 ? super.getColorFromItemstack(itemStack, pass) : 0xFFFFFF;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) 
	{
		return "item.EngagementRing" + (isRoseGold ? "RG" : "");
	}
	
	@Override
	public int getGiftValue() 
	{
		return 50;
	}
}
