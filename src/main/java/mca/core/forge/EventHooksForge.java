package mca.core.forge;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.base.Predicate;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.items.ItemBaby;
import mca.packets.PacketInteractWithPlayerC;
import mca.util.TutorialManager;
import mca.util.Utilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.util.RadixLogic;

public class EventHooksForge 
{
	@SubscribeEvent
	public void entityJoinedWorldEventHandler(EntityJoinWorldEvent event)
	{
		if (!event.world.isRemote && MCA.getConfig().canSpawnInDimension(event.world.provider.getDimension()))
		{
			if (event.entity instanceof EntityMob)
			{
				doAddMobTasks((EntityMob) event.entity);
			}

			if (event.entity.getClass() == EntityVillager.class && MCA.getConfig().overwriteOriginalVillagers)
			{
				EntityVillager villager = (EntityVillager)event.entity;

				if (villager.getProfession() >= 0 && villager.getProfession() <= 4)
				{
					// The server will later check for object 28, and then overwrite the villager.
					// This will prevent ConcurrentModification exceptions when overwriting villagers.
					villager.getDataManager().register(Constants.OVERWRITE_KEY, 3577);
				}
			}
		}
	}

	@SubscribeEvent
	public void renderGameOverlayEventHandler(RenderGameOverlayEvent.Text event)
	{
		TutorialManager.onUpdate();
	}

	private void doAddMobTasks(EntityMob mob)
	{
		if (MCA.getConfig().allowMobAttacks)
		{
			if (mob instanceof EntityEnderman)
			{
				return;
			}

			else if (mob instanceof EntityCreeper)
			{
				mob.tasks.addTask(3, new EntityAIAvoidEntity(mob, EntityHuman.class, new Predicate()
				{
					public boolean func_179958_a(Entity p_179958_1_)
					{
						return p_179958_1_ instanceof EntityHuman;
					}

					public boolean apply(Object p_apply_1_)
					{
						return this.func_179958_a((Entity)p_apply_1_);
					}
				}, 6.0F, 1.0D, 1.2D));
			}

			else
			{
				double moveSpeed = 0.7D;

				if (mob instanceof EntitySpider)
				{
					moveSpeed = 1.2D;
				}

				else if (mob instanceof EntitySkeleton)
				{
					moveSpeed = 1.1D;
				}

				else if (mob instanceof EntityZombie)
				{
					moveSpeed = 0.9D;
				}

				mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget(mob, EntityHuman.class, false));
			}
		}

		else
		{
			if (mob.tasks != null && mob.targetTasks.taskEntries != null)
			{
				for (int i = 0; i < mob.targetTasks.taskEntries.size(); i++)
				{
					try
					{
						//FIXME
//						EntityAITaskEntry task = (EntityAITaskEntry)mob.targetTasks.taskEntries.get(i);
//
//						if (task.action instanceof EntityAINearestAttackableTarget)
//						{
//							EntityAINearestAttackableTarget nat = (EntityAINearestAttackableTarget)task.action;
//
//							for (Field f : nat.getClass().getDeclaredFields())
//							{	
//								if (f.getType().equals(Class.class))
//								{
//									f.setAccessible(true);
//									Class targetClass = (Class) f.get(nat);
//									f.setAccessible(false);
//
//									if (targetClass.isAssignableFrom(EntityVillager.class))
//									{
//										mob.targetTasks.removeTask(nat);
//									}
//								}
//							}
//						}
					}

					catch (Exception e)
					{
						continue;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void entityInteractEventHandler(EntityInteractEvent event)
	{	
		if (event.getTarget() instanceof EntityHorse)
		{
			final EntityHorse entityHorse = (EntityHorse) event.getTarget();

			if (entityHorse.isBeingRidden())
			{
				try
				{
					final EntityHuman entity = (EntityHuman) entityHorse.getPassengers().get(0);
					entity.processInteract(event.entityPlayer, event.entityPlayer.getActiveHand(), event.entityPlayer.getHeldItem(event.entityPlayer.getActiveHand()));
				}

				catch (Exception e)
				{
					// Yes, it's lazy. What of it?
				}
			}
		}

		else if (event.getTarget() instanceof EntityPlayerMP && !event.entityPlayer.worldObj.isRemote && !event.entityPlayer.getName().contains("[CoFH]"))
		{
			MCA.getPacketHandler().sendPacketToPlayer(new PacketInteractWithPlayerC(event.entityPlayer, (EntityPlayer)event.getTarget()), (EntityPlayerMP) event.entityPlayer);
		}
	}

	@SubscribeEvent
	public void worldSaveEventHandler(WorldEvent.Unload event)
	{
		if (!event.world.isRemote && event.world.provider.getDimension() == 0)
		{
			MCA.getCrashWatcher().checkForCrashReports();
		}
	}

	@SubscribeEvent
	public void playerInteractEventHandler(PlayerInteractEvent event)
	{
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
		{
			if (event.entityPlayer.getControllingPassenger() instanceof EntityHuman)
			{
				event.entityPlayer.getControllingPassenger().dismountRidingEntity();
			}
		}
	}

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event)
	{
		if (MCA.getConfig().enableInfection)
		{
			if (event.source != null && event.source.getSourceOfDamage() instanceof EntityZombie)
			{
				EntityZombie zombie = (EntityZombie)event.source.getSourceOfDamage();
				boolean flag = RadixLogic.getBooleanWithProbability(3);

				if (event.entityLiving instanceof EntityPlayer && flag)
				{
					EntityPlayer player = (EntityPlayer) event.entityLiving;

					for (ItemStack stack : player.inventory.mainInventory)
					{
						if (stack != null && stack.getItem() instanceof ItemBaby)
						{
							stack.getTagCompound().setBoolean("isInfected", true);
							player.addChatComponentMessage(new TextComponentString(Color.RED + stack.getTagCompound().getString("name") + " has been " + Color.GREEN + Format.BOLD + "infected" + Color.RED + "!"));
							player.playSound(SoundEvents.entity_wither_ambient, 0.5F, 1.0F);
							Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.SPELL_WITCH, player, 32);
						}
					}
				}

				else if (event.entityLiving instanceof EntityHuman && flag)
				{
					EntityHuman human = (EntityHuman)event.entityLiving;
					human.setIsInfected(true);
					human.setHealth(human.getMaxHealth());
					zombie.setAttackTarget(null);

					human.playSound(SoundEvents.entity_wither_ambient, 0.5F, 1.0F);
					Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.SPELL_WITCH, human, 32);
				}

				else if (event.entityLiving instanceof EntityHuman && ((EntityHuman)event.entityLiving).getIsInfected())
				{
					event.setCanceled(true);
					zombie.setAttackTarget(null);
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingSetTarget(LivingSetAttackTargetEvent event)
	{
		//Mobs shouldn't attack infected villagers. Account for this when they attempt to set their target.
		if (event.entityLiving instanceof EntityMob && event.target instanceof EntityHuman)
		{
			EntityMob mob = (EntityMob) event.entityLiving;
			EntityHuman target = (EntityHuman) event.target;

			if (target.getIsInfected())
			{
				mob.setAttackTarget(null);
			}
		}
	}

	public static boolean isHorseRiddenByHuman(EntityHorse horse)
	{
		List<Entity> passengers = horse.getPassengers();

		for (Entity passenger : passengers)
		{
			if (passenger instanceof EntityHuman)
			{
				return true;
			}
		}

		return false;
	}
}
