package mca.items;

import mca.core.MCA;
import mca.core.TutorialManager;
import mca.core.TutorialMessage;
import mca.core.forge.EventHooksFML;
import mca.data.PlayerData;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import radixcore.data.DataWatcherEx;
import radixcore.item.ItemSingle;
import radixcore.math.Point3D;
import radixcore.util.SchematicHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCrystalBall extends ItemSingle
{
	public ItemCrystalBall()
	{
		super();
		this.setCreativeTab(MCA.getCreativeTab());
		this.setMaxStackSize(1);
		this.setUnlocalizedName("CrystalBall");
		this.setTextureName("mca:CrystalBall");

		GameRegistry.registerItem(this, "CrystalBall");
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World worldObj, int posX, int posY, int posZ, int meta, float playerPosX, float playerPosY, float playerPosZ) 
	{	
		onItemRightClick(stack, worldObj, player);
		return true;
	}	

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) 
	{
		if (world.isRemote)
		{
			net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
			net.minecraft.client.gui.GuiScreen currentScreen = mc.currentScreen;
			
			if (mc.isIntegratedServerRunning() && MCA.playerDataContainer != null)
			{
				spawnDestinyRoom(player);
			}
		}
		
		else
		{
			world.playSoundAtEntity(player, "portal.travel", 0.8F, 1.0F);
		}
		
		player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
		return itemStack;
	}
	
	
	@Override
	public boolean hasEffect(ItemStack par1ItemStack, int pass) 
	{
		return true;
	}

	private void spawnDestinyRoom(EntityPlayer player)
	{
		EntityPlayerSP playerSP = (EntityPlayerSP)player;
		EventHooksFML.playPortalAnimation = true;
		playerSP.timeInPortal = 6.0F;
		playerSP.prevTimeInPortal = 0.0F;
		
		PlayerData data = MCA.playerDataContainer.getPlayerData(PlayerData.class);
		
		DataWatcherEx.allowClientSideModification = true;
		data.hasChosenDestiny.setValue(false);
		DataWatcherEx.allowClientSideModification = false;
		
		MCA.destinyCenterPoint = new Point3D(player.posX - 1, player.posY, player.posZ);
		TutorialManager.setTutorialMessage(new TutorialMessage("Right-click the enchantment table to begin.", ""));

		SchematicHandler.spawnStructureRelativeToPoint("/assets/mca/schematic/destiny-test.schematic", MCA.destinyCenterPoint, player.worldObj);
		player.setPositionAndRotation(player.posX, player.posY, player.posZ, 180.0F, 0.0F);
	}
}
