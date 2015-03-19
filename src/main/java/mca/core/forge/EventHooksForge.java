package mca.core.forge;

import radixcore.util.RadixLogic;
import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.enums.EnumProfession;
import mca.util.TutorialManager;
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

			if (event.entity instanceof EntityVillager && !(event.entity instanceof EntityHuman) && MCA.getConfig().overwriteOriginalVillagers)
			{
				doOverwriteVillager(event, (EntityVillager) event.entity);
			}
		}
	}

	private void doOverwriteVillager(EntityJoinWorldEvent event, EntityVillager entity) 
	{
		entity.setDead();
		
		boolean hasFamily = RadixLogic.getBooleanWithProbability(20);
		boolean isMale = RadixLogic.getBooleanWithProbability(50);
		
		final EntityHuman human = new EntityHuman(event.world, isMale, entity.getProfession(), true);
		human.setPosition(entity.posX, entity.posY, entity.posZ);
		
		if (hasFamily)
		{
			final EntityHuman spouse = new EntityHuman(event.world, !isMale, EnumProfession.getAtRandom().getId(), false);
			spouse.setPosition(human.posX, human.posY, human.posZ);
			event.world.spawnEntityInWorld(spouse);
			
			human.setIsMarried(true, spouse);
			spouse.setIsMarried(true, human);
			
			String motherName = !isMale ? human.getName() : spouse.getName();
			String fatherName = isMale ? human.getName() : spouse.getName();
			int motherID = !isMale ? human.getPermanentId() : spouse.getPermanentId();
			int fatherID = isMale ? human.getPermanentId() : spouse.getPermanentId();
			
			//Children
			for (int i = 0; i < 2; i++)
			{
				if (RadixLogic.getBooleanWithProbability(66))
				{
					continue;
				}
				
				final EntityHuman child = new EntityHuman(event.world, RadixLogic.getBooleanWithProbability(50), true, motherName, fatherName, motherID, fatherID, false);
				child.setPosition(entity.posX, entity.posY, entity.posZ);
				event.world.spawnEntityInWorld(child);
			}
		}
		
		event.world.spawnEntityInWorld(human);
	}

	@SubscribeEvent
	public void renderGameOverlayEventHandler(RenderGameOverlayEvent.Text event)
	{
		TutorialManager.onUpdate();
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
