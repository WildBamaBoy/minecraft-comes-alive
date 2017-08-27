package mca.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.TransitiveVillagerData;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMemorialType;
import mca.tile.TileMemorial;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;

public class ItemStaffOfLife extends Item
{
	public ItemStaffOfLife()
	{
		super();
		maxStackSize = 1;
		setMaxDamage(4);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) 
	{
		ItemStack stack = playerIn.getHeldItem(hand);
		
		if (!worldIn.isRemote)
		{
			TileEntity tile = worldIn.getTileEntity(pos);

			if (tile instanceof TileMemorial)
			{
				TileMemorial memorial = (TileMemorial)tile;
				TransitiveVillagerData data = memorial.getTransitiveVillagerData();
				NBTPlayerData playerData = MCA.getPlayerData(playerIn);

				//Make sure the owner is the one reviving them.
				if (!playerData.getIsSuperUser() && !memorial.getOwnerUUID().equals(playerData.getUUID()))
				{
					playerIn.sendMessage(new TextComponentString(Color.RED + "You cannot revive " + data.getName() + " because they are not related to you."));
					return EnumActionResult.FAIL;
				}
				
				//For rings, they belonged to a spouse. Check for remarriage and forbid.
				if (memorial.getType() == EnumMemorialType.BROKEN_RING && (playerData.getMarriageState() != EnumMarriageState.NOT_MARRIED))
				{
					playerIn.sendMessage(new TextComponentString(Color.RED + "You cannot revive " + data.getName() + " because you are already married."));
					return EnumActionResult.FAIL;
				}
				
				//Once everything is okay, set the tile's revival ticks to begin the revival process.
				memorial.setPlayer(playerIn);
				memorial.setRevivalTicks(Time.SECOND * 5);
				stack.damageItem(1, playerIn);
				worldIn.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.AMBIENT, 3.0F, 1.0F);
				
				return EnumActionResult.SUCCESS;
			}
		}
		
		return EnumActionResult.PASS;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack itemStack)
	{
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) 
	{
		tooltip.add("Uses left: " + (stack.getMaxDamage() - stack.getItemDamage() + 1));
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			tooltip.add("Use on an item owned by a dead");
			tooltip.add("villager to revive them. Item");
			tooltip.add("must be placed in the world.");
		}

		else
		{
			tooltip.add("Hold " + Color.YELLOW + "SHIFT" + Color.GRAY + " for info.");
		}
	}
}
