package mca.items;

import java.util.List;

import radixcore.constant.Color16;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemColoredDiamond extends Item
{
	public ItemColoredDiamond()
	{
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName("ColoredDiamond");
		this.setTextureName("mca:ColoredDiamond");
		this.setCreativeTab(MCA.getCreativeTab());

		GameRegistry.registerItem(this, "ColoredDiamond");
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
