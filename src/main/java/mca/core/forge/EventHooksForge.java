package mca.core.forge;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.base.Predicate;

import mca.ai.AICombat;
import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumCombatBehaviors;
import mca.enums.EnumProfession;
import mca.items.ItemBaby;
import mca.packets.PacketInteractWithPlayerC;
import mca.util.TutorialManager;
import mca.util.Utilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
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
		if (!event.world.isRemote && MCA.getConfig().canSpawnInDimension(event.world.provider.getDimensionId()))
		{
			if (event.entity instanceof EntityMob)
			{
				doAddMobTasks((EntityMob) event.entity);
			}

			if (event.entity.getClass() == EntityVillager.class && MCA.getConfig().overwriteOriginalVillagers)
			{
				EntityVillager villager = (EntityVillager)event.entity;

				//Check for a zombie being turned into a villager. Don't overwrite with families in this case.
				List<Entity> zombiesAroundMe = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityZombie.class, event.entity, 3);
				
				for (Entity entity : zombiesAroundMe)
				{
					EntityZombie zombie = (EntityZombie)entity;
					
					if (zombie.isConverting())
					{
						boolean isMale = RadixLogic.getBooleanWithProbability(50);
						final EntityHuman human = new EntityHuman(entity.worldObj, isMale, EnumProfession.getAtRandom().getId(), false);
						human.setPosition(zombie.posX, zombie.posY, zombie.posZ);
						entity.worldObj.spawnEntityInWorld(human);
						event.entity.setDead();
						return;
					}
				}
				
				//Otherwise, no zombie was found so continue overwriting normally.
				if (villager.getProfession() >= 0 && villager.getProfession() <= 4)
				{
					// The server will later check for object 28, and then overwrite the villager.
					// This will prevent ConcurrentModification exceptions when overwriting villagers.
					villager.getDataWatcher().addObject(28, 3577);
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
						EntityAITaskEntry task = (EntityAITaskEntry)mob.targetTasks.taskEntries.get(i);

						if (task.action instanceof EntityAINearestAttackableTarget)
						{
							EntityAINearestAttackableTarget nat = (EntityAINearestAttackableTarget)task.action;

							for (Field f : nat.getClass().getDeclaredFields())
							{	
								if (f.getType().equals(Class.class))
								{
									f.setAccessible(true);
									Class targetClass = (Class) f.get(nat);
									f.setAccessible(false);

									if (targetClass.isAssignableFrom(EntityVillager.class))
									{
										mob.targetTasks.removeTask(nat);
									}
								}
							}
						}
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
		if (event.target instanceof EntityHorse)
		{
			final EntityHorse entityHorse = (EntityHorse) event.target;
			if (entityHorse.riddenByEntity instanceof EntityHuman)
			{
				final EntityHuman entity = (EntityHuman) entityHorse.riddenByEntity;
				entity.interact(event.entityPlayer);
			}
		}

		else if (event.target instanceof EntityPlayerMP && !event.entityPlayer.worldObj.isRemote && !event.entityPlayer.getName().contains("[CoFH]"))
		{
			MCA.getPacketHandler().sendPacketToPlayer(new PacketInteractWithPlayerC(event.entityPlayer, (EntityPlayer)event.target), (EntityPlayerMP) event.entityPlayer);
		}
	}

	@SubscribeEvent
	public void worldSaveEventHandler(WorldEvent.Unload event)
	{
		if (!event.world.isRemote && event.world.provider.getDimensionId() == 0)
		{
			MCA.getCrashWatcher().checkForCrashReports();
		}
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
		}
	}

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event)
	{
		//Handle warrior triggers on player taking damage.
		if (event.entityLiving instanceof EntityPlayer && event.source.getEntity() instanceof EntityLivingBase)
		{
			List<Entity> entityList = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, event.entityLiving, 15);
			
			for (Entity entity : entityList)
			{
				EntityHuman human = (EntityHuman)entity;
				AICombat combat = human.getAI(AICombat.class);
				PlayerMemory memory = human.getPlayerMemory((EntityPlayer)event.entityLiving);
				
				if (memory.getIsHiredBy() && human.getProfessionEnum() == EnumProfession.Warrior && 
					combat.getMethodBehavior() != EnumCombatBehaviors.METHOD_DO_NOT_FIGHT &&
					combat.getTriggerBehavior() == EnumCombatBehaviors.TRIGGER_PLAYER_TAKE_DAMAGE)
				{
					combat.setAttackTarget((EntityLivingBase)event.source.getEntity());
				}
			}
		}

		//Handle warrior triggers on player dealing damage.
		else if (event.source.getEntity() instanceof EntityPlayer && event.entityLiving != null)
		{
			List<Entity> entityList = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, event.source.getEntity(), 15);
			
			for (Entity entity : entityList)
			{
				EntityHuman human = (EntityHuman)entity;
				AICombat combat = human.getAI(AICombat.class);
				PlayerMemory memory = human.getPlayerMemory((EntityPlayer)event.source.getEntity());
				
				if (memory.getIsHiredBy() && human.getProfessionEnum() == EnumProfession.Warrior && 
					combat.getMethodBehavior() != EnumCombatBehaviors.METHOD_DO_NOT_FIGHT &&
					combat.getTriggerBehavior() == EnumCombatBehaviors.TRIGGER_PLAYER_DEAL_DAMAGE)
				{
					combat.setAttackTarget((EntityLivingBase)event.entityLiving);
				}
			}
		}
		
		//Handle infection checks
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
							player.addChatComponentMessage(new ChatComponentText(Color.RED + stack.getTagCompound().getString("name") + " has been " + Color.GREEN + Format.BOLD + "infected" + Color.RED + "!"));
							player.worldObj.playSoundAtEntity(player, "mob.wither.idle", 0.5F, 1.0F);
							Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.SPELL_WITCH, player, 32);
						}
					}
				}

				else if (event.entityLiving instanceof EntityHuman && flag)
				{
					EntityHuman human = (EntityHuman)event.entityLiving;
					
					//Warriors are immune to infection.
					if (human.getProfessionEnum() == EnumProfession.Warrior)
					{
						return;
					}
					
					//Villagers wearing armor are immune to infection.
					for (int i = 1; i < 5; i++)
					{
						if (human.getEquipmentInSlot(i) != null)
						{
							return;
						}
					}

					human.setIsInfected(true);
					human.setHealth(human.getMaxHealth());
					zombie.setAttackTarget(null);

					human.worldObj.playSoundAtEntity(human, "mob.wither.idle", 0.5F, 1.0F);
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
}
