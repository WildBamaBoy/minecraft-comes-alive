package mca.items;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.EventHooksFML;
import mca.data.NBTPlayerData;
import mca.util.TutorialManager;
import mca.util.TutorialMessage;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import radixcore.item.ItemSingle;
import radixcore.math.Point3D;

public class ItemCrystalBall extends ItemSingle
{
	public ItemCrystalBall()
	{
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		this.onItemRightClick(world, player, hand);
		return EnumActionResult.PASS;
	}	

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) 
	{
		if (world.isRemote)
		{
			net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
			
			if (mc.isIntegratedServerRunning())
			{
				spawnDestinyRoom(player);
			}
			
			else if (!mc.isIntegratedServerRunning())
			{
				player.openGui(MCA.getInstance(), Constants.GUI_ID_SETUP, world, (int)player.posX, (int)player.posY, (int)player.posZ);
			}
		}
		
		player.playSound(SoundEvents.ENTITY_FIREWORK_LARGE_BLAST_FAR, 0.5F, 1.0F);
		player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, 0.5F, 2.0F);
		
		player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
		return super.onItemRightClick(world, player, hand);
	}
	
	
	@Override
	public boolean hasEffect(ItemStack stack) 
	{
		return true;
	}

	private void spawnDestinyRoom(EntityPlayer player)
	{
		EntityPlayerSP playerSP = (EntityPlayerSP)player;
		EventHooksFML.playPortalAnimation = true;
		playerSP.timeInPortal = 6.0F;
		playerSP.prevTimeInPortal = 0.0F;
		
		NBTPlayerData data = MCA.myPlayerData;
		
		data.setHasChosenDestiny(false);
		
		MCA.destinySpawnFlag = true; //Will hand off spawning to clientTickEvent
		MCA.destinyCenterPoint = new Point3D(player.posX, player.posY + 1, player.posZ);
		TutorialManager.setTutorialMessage(new TutorialMessage("Right-click the enchantment table to begin.", ""));
		player.setPositionAndRotation(player.posX, player.posY, player.posZ, 180.0F, 0.0F);
	}
}
