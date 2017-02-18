package mca.core.forge;

import mca.client.gui.GuiSetup;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import radixcore.modules.RadixBlocks;

public class EventHooksForgeClient 
{
	@SubscribeEvent
	public void rightClickBlockEventHandler(RightClickBlock event)
	{
		if (event.getEntityPlayer().worldObj.isRemote && Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			Block block = RadixBlocks.getBlock(event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());

			if (block == Blocks.ENCHANTING_TABLE)
			{
				NBTPlayerData data = MCA.getPlayerData(event.getEntityPlayer());

				if (!data.getHasChosenDestiny())
				{
					event.setCanceled(true);
					Minecraft.getMinecraft().displayGuiScreen(new GuiSetup(event.getEntityPlayer()));
				}
			}
		}
	}
}