package mca.core.forge;

import java.lang.reflect.Field;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import mca.ai.AICombat;
import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumCombatBehaviors;
import mca.enums.EnumProfession;
import mca.items.ItemBaby;
import mca.packets.PacketInteractWithPlayerC;
import mca.packets.PacketPlaySoundOnPlayer;
import mca.util.TutorialManager;
import mca.util.Utilities;
import net.minecraft.block.Block;
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
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.WorldEvent;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.constant.Particle;
import radixcore.math.Point3D;
import radixcore.util.RadixLogic;

public class EventHooksForge 
{
	@SubscribeEvent
	public void entityJoinedWorldEventHandler(EntityJoinWorldEvent event)
	{
		if (!event.world.isRemote && MCA.getConfig().canSpawnInDimension(event.world.provider.dimensionId))
		{
			if (event.entity instanceof EntityMob)
			{
				doAddMobTasks((EntityMob) event.entity);
			}

			if (event.entity.getClass() == EntityVillager.class && MCA.getConfig().overwriteOriginalVillagers)
			{
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
				doOverwriteVillager(event, (EntityVillager) event.entity);
			}
		}
	}

	private void doOverwriteVillager(EntityJoinWorldEvent event, EntityVillager entity) 
	{
		if (entity.getProfession() >= 0 && entity.getProfession() <= 4)
		{
			entity.setDead();
			MCA.naturallySpawnVillagers(new Point3D(entity.posX, entity.posY, entity.posZ), event.world, entity.getProfession());
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

		else if (event.target instanceof EntityPlayerMP && !event.entityPlayer.worldObj.isRemote && !event.entityPlayer.getCommandSenderName().contains("[CoFH]"))
		{
			MCA.getPacketHandler().sendPacketToPlayer(new PacketInteractWithPlayerC(event.entityPlayer, (EntityPlayer)event.target), (EntityPlayerMP) event.entityPlayer);
		}
	}

	@SubscribeEvent
	public void worldSaveEventHandler(WorldEvent.Unload event)
	{
		if (!event.world.isRemote && event.world.provider.dimensionId == 0)
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
							stack.stackTagCompound.setBoolean("isInfected", true);
							player.addChatComponentMessage(new ChatComponentText(Color.RED + stack.stackTagCompound.getString("name") + " has been " + Color.GREEN + Format.BOLD + "infected" + Color.RED + "!"));
							player.worldObj.playSoundAtEntity(player, "mob.wither.idle", 0.5F, 1.0F);
							Utilities.spawnParticlesAroundEntityS(Particle.WITCH_MAGIC, player, 32);
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
					Utilities.spawnParticlesAroundEntityS(Particle.WITCH_MAGIC, human, 32);
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
	
	@SubscribeEvent
	public void onPlaceEvent(PlaceEvent event) //Check for grim reaper summoning area.
	{
		if (event.block == Blocks.fire && event.world.getBlock(event.x, event.y - 1, event.z) == Blocks.emerald_block)
		{
			int totemsFound = 0;

			//Check on +/- X and Z for at least 3 totems on fire.
			for (int i = 0; i < 4; i++)
			{
				int dX = 0;
				int dZ = 0;
				
				switch (i)
				{
				case 0: dX = -3; break;
				case 1: dX = 3; break;
				case 2: dZ = -3; break;
				case 3: dZ = 3; break;
				}
				
				//Scan upwards to ensure it's obsidian, and on fire.
				for (int j = -1; j < 2; j++) //-1 since the fire is on top of the emerald.
				{
					Block block = event.world.getBlock(event.x + dX, event.y + j, event.z + dZ);
					
					if (block == Blocks.obsidian || block == Blocks.fire)
					{
					}
					
					else
					{
						break;
					}
					
					//If we made it up to 1 without breaking, make sure the block is fire so that it's a lit totem.
					if (j == 1 && block == Blocks.fire)
					{
						totemsFound++;
					}
					
					continue;
				}
			}
			
			if (totemsFound >= 3 && !event.world.isDaytime())
			{
				Point3D summonPoint = new Point3D(event.x, event.y + 5, event.z);
				NetworkRegistry.TargetPoint summonTarget = new NetworkRegistry.TargetPoint(event.world.provider.dimensionId, summonPoint.iPosX, summonPoint.iPosY + 5, summonPoint.iPosZ, 32);
	
				EventHooksFML.setReaperSummonPoint(event.world, new Point3D(event.x + 1.0D, event.y + 5, event.z + 1.0D));
				MCA.getPacketHandler().sendPacketToAllAround(new PacketPlaySoundOnPlayer("portal.portal"), summonTarget);
					
				for (int i = 0; i < 2; i++)
				{
					Utilities.spawnParticlesAroundPointS(Particle.FLAMES, event.world, event.x, event.y - i, event.z, 32);
					event.world.setBlock(event.x, event.y - i, event.z, Blocks.air);
				}
			}
			
			else if (totemsFound >= 3 && event.world.isDaytime())
			{
				TutorialManager.sendMessageToPlayer(event.player, "The Grim Reaper must be summoned at night.", "");
			}
		}
	}
}
