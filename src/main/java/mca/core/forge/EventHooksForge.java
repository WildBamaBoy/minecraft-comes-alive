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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHooksForge 
{
	@SubscribeEvent
	public void entityJoinedWorldEventHandler(EntityJoinWorldEvent event)
	{
		if (!event.world.isRemote)
		{			
			if (event.entity instanceof EntityMob)
			{
				doAddMobTasks((EntityMob) event.entity);
			}

			if (event.entity instanceof EntityVillager && !(event.entity instanceof EntityHuman))
			{
				//doOverwriteVillager(event, (EntityVillager) event.entity); TODO
			}
		}
	}

	@SubscribeEvent
	public void renderGameOverlayEventHandler(RenderGameOverlayEvent.Text event)
	{
		TutorialManager.onUpdate();
	}
	
	@SubscribeEvent
	public void playerInteractEventHandler(PlayerInteractEvent event)
	{
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
		{
			if (event.entityPlayer.riddenByEntity instanceof EntityHuman)
			{
				event.entityPlayer.riddenByEntity.mountEntity(null);
			}
			
			else if (event.entityPlayer.worldObj.isRemote)
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
	
	private void doAddMobTasks(EntityMob mob)
	{
		if (mob instanceof EntityEnderman)
		{
			return;
		}

		else if (mob instanceof EntityCreeper)
		{
			mob.tasks.addTask(0, new EntityAIAvoidEntity(mob, EntityVillager.class, 16F, 1.35F, 1.35F));
		}

		else
		{
			float moveSpeed = 0.7F;

			if (mob instanceof EntitySpider)
			{
				moveSpeed = 1.2F;
			}

			else if (mob instanceof EntitySkeleton)
			{
				moveSpeed = 1.1F;
			}

			else if (mob instanceof EntityZombie)
			{
				moveSpeed = 0.9F;
			}

			mob.tasks.addTask(2, new EntityAIAttackOnCollide(mob, EntityHuman.class, moveSpeed, false));
			mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget(mob, EntityHuman.class, 16, false));
		}
	}
}
