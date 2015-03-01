package radixcore.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import radixcore.constant.Color16;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ItemColorable extends Item
{
	public ItemColorable()
	{
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}
	
	@Override
	public int getColorFromItemStack(ItemStack itemStack, int meta)
	{
		int damage = itemStack.getItemDamage();
		return Color16.fromId(damage).getRGBValue();
	}

	public String getUnlocalizedName(ItemStack stack)
	{
		int meta = MathHelper.clamp_int(stack.getItemDamage(), 0, 15);
		return super.getUnlocalizedName() + "." + Color16.fromId(meta).getName();
	}

	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs creativeTab, List list)
	{
		for (int i = 0; i < 16; ++i)
		{
			list.add(new ItemStack(item, 1, i));
		}
	}
}
