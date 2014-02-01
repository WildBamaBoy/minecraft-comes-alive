package mca.item;

import mca.enums.EnumCrownColor;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemDecorativeCrown extends ItemArmor
{
	private EnumCrownColor color;
	
	/**
	 * Constructor
	 * 
	 * @param 	id	The item's ID.
	 */
	public ItemDecorativeCrown(int itemId, EnumCrownColor color)
	{
		super(itemId, EnumArmorMaterial.GOLD, 0, 0);
		setUnlocalizedName(color.getColorName() + "crown");
		maxStackSize = 1;
		setCreativeTab(CreativeTabs.tabMisc);
		this.color = color;
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		itemIcon = iconRegister.registerIcon("mca:" + color.getColorName() + "Crown");
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) 
	{
		return "mca:textures/armor/" + color.getColorName().toLowerCase() + "crown_layer_1.png";
	}
}
