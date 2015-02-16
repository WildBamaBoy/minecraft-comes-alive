package mca.core.forge;

import java.io.IOException;
import java.util.Map;

import mca.core.MCA;
import mca.core.TutorialManager;
import mca.core.TutorialMessage;
import mca.data.PlayerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import radixcore.data.BlockObj;
import radixcore.helpers.ExceptHelper;
import radixcore.math.Point3D;
import radixcore.packets.PacketDataContainer;
import radixcore.util.SchematicHandler;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHooksFML 
{
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
		PlayerData data = new PlayerData(event.player);

		if (data.dataExists())
		{
			data = data.readDataFromFile(event.player, PlayerData.class);
		}

		else
		{
			data.initializeNewData(event.player);
		}

		MCA.playerDataMap.put(event.player.getUniqueID().toString(), data);
		MCA.getPacketHandler().sendPacketToPlayer(new PacketDataContainer(MCA.ID, data), (EntityPlayerMP)event.player);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTickEventHandler(ClientTickEvent event)
	{
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		
		if (mc.isIntegratedServerRunning())
		{
			if (MCA.playerDataContainer != null)
			{
				EntityPlayer player = mc.thePlayer;
				PlayerData data = MCA.playerDataContainer.getPlayerData(PlayerData.class);

				if (!data.hasChosenDestiny.getBoolean() && MCA.destinyCenterPoint == null)
				{
					MCA.destinyCenterPoint = new Point3D(player.posX - 1, player.posY, player.posZ);
					player.setPositionAndRotation(Math.floor(player.posX) - 0.5F, player.posY, Math.floor(player.posZ), 180.0F, 0.0F);
					TutorialManager.setTutorialMessage(new TutorialMessage("Right-click the enchantment table to begin.", ""));
				}

				if (!data.hasChosenDestiny.getBoolean() && player != null && MCA.destinyCenterPoint != null)
				{
					SchematicHandler.spawnStructureRelativeToPoint("/assets/mca/schematic/destiny-test.schematic", MCA.destinyCenterPoint, player.worldObj);
				}
			}

			if (mc.currentScreen instanceof net.minecraft.client.gui.GuiMainMenu)
			{
				MCA.destinyCenterPoint = null;
				MCA.playerDataContainer = null;
			}
		}
	}
}
