package mca.items;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
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
		if (!world.isRemote)
		{
			for (final Object obj : world.loadedEntityList)
			{
				if (obj instanceof EntityHuman)
				{
					EntityHuman human = (EntityHuman)obj;
					
					if (human.isPlayerAParent(player) || human.getPlayerSpouse() == player)
					{
						human.setPosition(player.posX, player.posY, player.posZ);
						human.getNavigator().clearPathEntity();
						human.halt();
						human.setMovementState(EnumMovementState.STAY);
					}
				}
			}
		}

		return itemStack;
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:Whistle");
	}
}
