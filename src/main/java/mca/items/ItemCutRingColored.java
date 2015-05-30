package mca.items;

import mca.api.IGiftableItem;
import mca.core.MCA;
import mca.enums.EnumCut;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.item.ItemColorable;

public class ItemCutRingColored extends ItemColorable implements IGiftableItem
{
	private EnumCut cut;
	private boolean isRoseGold;
//	private IIcon[] icons = new IIcon[3];
	
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

//	@Override
//	public boolean requiresMultipleRenderPasses() 
//	{
//		return true;
//	}

	@Override
	public int getColorFromItemStack(ItemStack itemStack, int pass)
	{
		return pass == 0 ? super.getColorFromItemStack(itemStack, pass) : 0xFFFFFF;
	}
	
//	@Override
//	public IIcon getIconFromDamageForRenderPass(int damage, int pass) 
//	{
//		if (pass == 1)
//		{
//			return icons[isRoseGold ? 2 : 1];
//		}
//		
//		else
//		{
//			return icons[0];
//		}
//	}

//	@Override
//	public void registerIcons(IIconRegister iconRegister) 
//	{
//		String name = "Ring" + cut.toString() + "Colored";
//		
//		icons[0] = iconRegister.registerIcon("mca:" + name);
//		icons[1] = iconRegister.registerIcon("mca:RingCutBottom");
//		icons[2] = iconRegister.registerIcon("mca:RingCutBottomRG");
//	}
	
	@Override
	public int getGiftValue() 
	{
		return 75;
	}
}
