package mca.core.forge;

import mca.core.MCA;
import mca.core.minecraft.ModItems;
import mca.data.PlayerData;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import radixcore.packets.PacketDataContainer;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHooksFML 
{
	public static boolean playPortalAnimation;

	@SubscribeEvent
	public void onConfigChanges(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
	{
		if (eventArgs.modID.equals(MCA.ID))
		{
			MCA.getConfig().getConfigInstance().save();
			MCA.getConfig().syncConfiguration();
		}
	}

	@SubscribeEvent
	public void playerLoggedInEventHandler(PlayerLoggedInEvent event)
	{
		EntityPlayer player = event.player;
		PlayerData data = null;
		
		if (!MCA.playerDataMap.containsKey(player.getUniqueID().toString()))
		{
			data = new PlayerData(player);

			if (data.dataExists())
			{
				data = data.readDataFromFile(event.player, PlayerData.class, null);
			}

			else
			{
				data.initializeNewData(event.player);
			}

			MCA.playerDataMap.put(event.player.getUniqueID().toString(), data);
		}
		
		else
		{
			data = MCA.getPlayerData(player);
			data = data.readDataFromFile(event.player, PlayerData.class, null);
			data.dumpToConsole();
		}
		
		MCA.getPacketHandler().sendPacketToPlayer(new PacketDataContainer(MCA.ID, data), (EntityPlayerMP)event.player);

		if (!data.hasChosenDestiny.getBoolean() && !player.inventory.hasItem(ModItems.crystalBall))
		{
			player.inventory.addItemStackToInventory(new ItemStack(ModItems.crystalBall));
		}
	}

	@SubscribeEvent
	public void playerLoggedOutEventHandler(PlayerLoggedOutEvent event)
	{
		PlayerData data = MCA.getPlayerData(event.player);

		if (data != null)
		{
			data.saveDataToFile();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTickEventHandler(ClientTickEvent event)
	{	
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		net.minecraft.client.gui.GuiScreen currentScreen = mc.currentScreen;

		if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu && MCA.playerDataContainer != null)
		{
			playPortalAnimation = false;
			MCA.destinyCenterPoint = null;
			MCA.playerDataContainer = null;
		}

		if (playPortalAnimation)
		{
			EntityPlayerSP player = (EntityPlayerSP)mc.thePlayer;
			player.prevTimeInPortal = player.timeInPortal;
			player.timeInPortal -= 0.0125F;

			if (player.timeInPortal <= 0.0F)
			{
				playPortalAnimation = false;
			}
		}
	}
}
