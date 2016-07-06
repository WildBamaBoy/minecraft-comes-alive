package mca.core.forge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mca.client.gui.GuiSetup;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
				Block block = BlockHelper.getBlock(event.world, event.x, event.y, event.z);
				
				if (block instanceof BlockEnchantmentTable)
				{
					NBTPlayerData data = MCA.getPlayerData(event.entityPlayer);
					
					if (!data.getHasChosenDestiny())
					{
						event.setCanceled(true);
						Minecraft.getMinecraft().displayGuiScreen(new GuiSetup(event.entityPlayer));
					}
				}
			}
		}
	}
}
