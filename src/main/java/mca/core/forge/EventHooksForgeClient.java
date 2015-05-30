package mca.core.forge;

import mca.client.gui.GuiSetup;
import mca.core.MCA;
import mca.data.PlayerData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import radixcore.util.BlockHelper;

public class EventHooksForgeClient 
{
	@SubscribeEvent
	public void playerInteractEventHandler(PlayerInteractEvent event)
	{
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
		{
			if (event.entityPlayer.worldObj.isRemote && Minecraft.getMinecraft().isIntegratedServerRunning())
			{
				Block block = BlockHelper.getBlock(event.world, event.pos.getX(), event.pos.getY(), event.pos.getZ());
				
				if (block instanceof BlockEnchantmentTable)
				{
					PlayerData data = MCA.getPlayerData(event.entityPlayer);
					
					if (!data.hasChosenDestiny.getBoolean())
					{
						event.setCanceled(true);
						Minecraft.getMinecraft().displayGuiScreen(new GuiSetup(event.entityPlayer));
					}
				}
			}
		}
	}
}
