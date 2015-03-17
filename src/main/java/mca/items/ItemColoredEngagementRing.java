package mca.items;

import mca.core.MCA;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import radixcore.item.ItemColorable;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemColoredEngagementRing extends ItemColorable
{
	private boolean isRoseGold;
	private IIcon[] icons = new IIcon[2];

	public ItemColoredEngagementRing(boolean isRoseGold)
	{
		final String name = isRoseGold ? "ColoredEngagementRingRG" : "ColoredEngagementRing";
		
		this.isRoseGold = isRoseGold;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		
		this.setUnlocalizedName(name);
		this.setTextureName("mca:" + name);
		GameRegistry.registerItem(this, name);

		this.setCreativeTab(MCA.getCreativeTabGemCutting());
	}

	@Override
	public boolean requiresMultipleRenderPasses() 
	{
		return true;
	}

	@Override
	public int getColorFromItemStack(ItemStack itemStack, int pass)
	{
		return pass == 0 ? super.getColorFromItemStack(itemStack, pass) : 0xFFFFFF;
	}
	
	@Override
	public IIcon getIconFromDamageForRenderPass(int damage, int pass) 
	{
		return icons[pass];
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) 
	{
		final String name = isRoseGold ? "ColoredEngagementRingRG" : "ColoredEngagementRing";
		
		icons[0] = iconRegister.registerIcon("mca:" + name);
		icons[1] = iconRegister.registerIcon(isRoseGold ? "mca:RingBottomRG" : "mca:RingBottom");
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) 
	{
		return "item.EngagementRing" + (isRoseGold ? "RG" : "");
	}
}
