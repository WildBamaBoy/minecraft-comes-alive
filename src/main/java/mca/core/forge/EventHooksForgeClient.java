package mca.core.forge;

import mca.client.gui.GuiSetup;
import mca.core.MCA;
import mca.core.TutorialManager;
import mca.data.PlayerData;
import mca.entity.EntityHuman;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHooksForgeClient 
{
	@SubscribeEvent
	public void playerInteractEventHandler(PlayerInteractEvent event)
	{
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
		{
			if (event.entityPlayer.riddenByEntity instanceof EntityHuman)
			{
				event.entityPlayer.riddenByEntity.mountEntity(null);
			}
			
			else if (event.entityPlayer.worldObj.isRemote && Minecraft.getMinecraft().isIntegratedServerRunning())
			{
				Block block = event.entityPlayer.worldObj.getBlock(event.x, event.y, event.z);
				
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
