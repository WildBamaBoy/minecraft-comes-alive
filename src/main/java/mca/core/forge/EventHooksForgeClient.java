package mca.core.forge;

import mca.client.gui.GuiSetup;
import mca.core.MCA;
import mca.data.PlayerData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import radixcore.util.BlockHelper;

public class EventHooksForgeClient 
{
	@SubscribeEvent
	public void rightClickBlockEventHandler(RightClickBlock event)
	{
		if (event.getEntityPlayer().worldObj.isRemote && Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			Block block = BlockHelper.getBlock(event.getWorld(), (int)event.getHitVec().xCoord, (int)event.getHitVec().yCoord, (int)event.getHitVec().zCoord);

			if (block instanceof BlockEnchantmentTable)
			{
				PlayerData data = MCA.getPlayerData(event.getEntityPlayer());

				if (!data.getHasChosenDestiny())
				{
					event.setCanceled(true);
					Minecraft.getMinecraft().displayGuiScreen(new GuiSetup(event.getEntityPlayer()));
				}
			}
		}
	}
}
