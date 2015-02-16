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
import radixcore.data.BlockWithMeta;
import radixcore.helpers.ExceptHelper;
import radixcore.math.Point3D;
import radixcore.packets.PacketDataContainer;
import radixcore.util.SchematicReader;
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
					MCA.destinyCenterPoint = new Point3D(player.posX, player.posY, player.posZ);
					player.setPositionAndRotation(Math.floor(player.posX) - 0.5F, player.posY, Math.floor(player.posZ), 180.0F, 0.0F);
					TutorialManager.setTutorialMessage(new TutorialMessage("Right-click the enchantment table to begin.", ""));
				}

				if (!data.hasChosenDestiny.getBoolean())
				{
					buildDestinyRoom();
				}
			}

			if (mc.currentScreen instanceof net.minecraft.client.gui.GuiMainMenu)
			{
				MCA.destinyCenterPoint = null;
				MCA.playerDataContainer = null;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void buildDestinyRoom()
	{
		EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;

		if (player != null)
		{
			World world = player.worldObj;

			Map<Point3D, BlockWithMeta> schemBlocks;

			try 
			{
				schemBlocks = SchematicReader.readSchematic("/assets/mca/schematic/destiny-test.schematic"); 

				for (Map.Entry<Point3D, BlockWithMeta> entry : schemBlocks.entrySet())
				{
					Point3D blockPoint = entry.getKey();
					
					//Align the player with the center of the room.
					int x = blockPoint.iPosX + MCA.destinyCenterPoint.iPosX;
					int y = blockPoint.iPosY + MCA.destinyCenterPoint.iPosY;
					int z = blockPoint.iPosZ + MCA.destinyCenterPoint.iPosZ;

					//Set the new blocks.
					world.setBlock(x, y, z, entry.getValue().getBlock(), entry.getValue().getMeta(), 2);
				}
			}

			catch (IOException e) 
			{
				ExceptHelper.logFatalCatch(e, "Unexpected exception while spawning destiny room.");
			}
		}
	}
}
