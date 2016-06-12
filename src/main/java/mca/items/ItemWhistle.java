package mca.items;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemWhistle extends Item
{
	public ItemWhistle()
	{
		super();
		maxStackSize = 1;
		setCreativeTab(MCA.getCreativeTabMain());
		setUnlocalizedName("whistle");
		GameRegistry.registerItem(this, "whistle");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		player.openGui(MCA.getInstance(), Constants.GUI_ID_WHISTLE, world, (int)player.posX, (int)player.posY, (int)player.posZ);

		return itemStack;
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:Whistle");
	}
}
