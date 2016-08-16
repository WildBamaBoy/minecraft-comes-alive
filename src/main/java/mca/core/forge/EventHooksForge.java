package mca.core.forge;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;

import mca.ai.AICombat;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumCombatBehaviors;
import mca.enums.EnumProfession;
import mca.items.ItemBaby;
import mca.packets.PacketInteractWithPlayerC;
import mca.util.TutorialManager;
import mca.util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;

public class EventHooksForge 
{
	@SubscribeEvent
	public void entityJoinedWorldEventHandler(EntityJoinWorldEvent event)
	{
		if (!event.getWorld().isRemote && MCA.getConfig().canSpawnInDimension(event.getWorld().provider.getDimension()))
		{
			if (event.getEntity() instanceof EntityMob)
			{
				doAddMobTasks((EntityMob) event.getEntity());
			}

			if (event.getEntity().getClass() == EntityVillager.class && MCA.getConfig().overwriteOriginalVillagers)
			{
				EntityVillager villager = (EntityVillager)event.getEntity();

				//Check for a zombie being turned into a villager. Don't overwrite with families in this case.
				List<Entity> zombiesAroundMe = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityZombie.class, event.getEntity(), 3);

				for (Entity entity : zombiesAroundMe)
				{
					EntityZombie zombie = (EntityZombie)entity;

					if (zombie.isConverting())
					{
						boolean isMale = RadixLogic.getBooleanWithProbability(50);
						final EntityHuman human = new EntityHuman(entity.worldObj, isMale, EnumProfession.getAtRandom().getId(), false);
						human.setPosition(zombie.posX, zombie.posY, zombie.posZ);
						entity.worldObj.spawnEntityInWorld(human);
						event.getEntity().setDead();
						return;
					}
				}

				//Otherwise, no zombie was found so continue overwriting normally.
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
						EntityAITaskEntry task = (EntityAITaskEntry)mob.targetTasks.taskEntries.toArray()[i];

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
	public void entityInteractEventHandler(EntityInteract event)
	{	
		if (event.getTarget() instanceof EntityHorse)
		{
			final EntityHorse entityHorse = (EntityHorse) event.getTarget();

			if (entityHorse.isBeingRidden())
			{
				try
				{
					final EntityHuman entity = (EntityHuman) entityHorse.getPassengers().get(0);
					entity.processInteract(event.getEntityPlayer(), event.getEntityPlayer().getActiveHand(), event.getEntityPlayer().getHeldItem(event.getEntityPlayer().getActiveHand()));
				}

				catch (Exception e)
				{
					// Yes, it's lazy. What of it?
				}
			}
		}

		else if (event.getTarget() instanceof EntityPlayerMP && !event.getEntityPlayer().worldObj.isRemote && !event.getEntityPlayer().getName().contains("[CoFH]"))
		{
			MCA.getPacketHandler().sendPacketToPlayer(new PacketInteractWithPlayerC(event.getEntityPlayer(), (EntityPlayer)event.getTarget()), (EntityPlayerMP) event.getEntityPlayer());
		}
	}

	@SubscribeEvent
	public void worldSaveEventHandler(WorldEvent.Unload event)
	{
		if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0)
		{
			MCA.getCrashWatcher().checkForCrashReports();
		}
	}

	@SubscribeEvent
	public void rightClickBlockEventHandler(RightClickBlock event)
	{
		if (event.getEntityPlayer().getControllingPassenger() instanceof EntityHuman)
		{
			event.getEntityPlayer().getControllingPassenger().dismountRidingEntity();
		}
	}

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event)
	{
		//Not interested in fake players due to the inability to generate player data for them.
		if (event.getEntityLiving() instanceof FakePlayer)
		{
			return;
		}
		
		//Handle warrior triggers on player taking damage.		
		if (event.getEntityLiving() instanceof EntityPlayer && event.getSource().getEntity() instanceof EntityLivingBase)
		{
			List<Entity> entityList = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, event.getEntityLiving(), 15);

			for (Entity entity : entityList)
			{
				EntityHuman human = (EntityHuman)entity;
				AICombat combat = human.getAI(AICombat.class);
				PlayerMemory memory = human.getPlayerMemory((EntityPlayer)event.getEntityLiving());
		
				if (memory.getIsHiredBy() && human.getProfessionEnum() == EnumProfession.Warrior && 
						combat.getMethodBehavior() != EnumCombatBehaviors.METHOD_DO_NOT_FIGHT &&
						combat.getTriggerBehavior() == EnumCombatBehaviors.TRIGGER_PLAYER_TAKE_DAMAGE)
				{
					combat.setAttackTarget((EntityLivingBase)event.getSource().getEntity());
				}
			}
		}

		//Handle warrior triggers on player dealing damage.
		else if (event.getSource().getEntity() instanceof EntityPlayer && event.getEntityLiving() != null)
		{
			List<Entity> entityList = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityHuman.class, event.getSource().getEntity(), 15);

			for (Entity entity : entityList)
			{
				EntityHuman human = (EntityHuman)entity;
				AICombat combat = human.getAI(AICombat.class);
				PlayerMemory memory = human.getPlayerMemory((EntityPlayer)event.getSource().getEntity());

				if (memory.getIsHiredBy() && human.getProfessionEnum() == EnumProfession.Warrior && 
						combat.getMethodBehavior() != EnumCombatBehaviors.METHOD_DO_NOT_FIGHT &&
						combat.getTriggerBehavior() == EnumCombatBehaviors.TRIGGER_PLAYER_DEAL_DAMAGE)
				{
					combat.setAttackTarget((EntityLivingBase)event.getEntityLiving());
				}
			}
		}

		//Handle infection checks
		if (MCA.getConfig().enableInfection)
		{
			if (event.getSource() != null && event.getSource().getSourceOfDamage() instanceof EntityZombie)
			{
				EntityZombie zombie = (EntityZombie)event.getSource().getSourceOfDamage();
				boolean flag = RadixLogic.getBooleanWithProbability(3);

				if (event.getEntityLiving() instanceof EntityPlayer && flag)
				{
					EntityPlayer player = (EntityPlayer) event.getEntityLiving();

					for (ItemStack stack : player.inventory.mainInventory)
					{
						if (stack != null && stack.getItem() instanceof ItemBaby)
						{
							stack.getTagCompound().setBoolean("isInfected", true);
							player.addChatComponentMessage(new TextComponentString(Color.RED + stack.getTagCompound().getString("name") + " has been " + Color.GREEN + Format.BOLD + "infected" + Color.RED + "!"));
							player.playSound(SoundEvents.ENTITY_WITHER_AMBIENT, 0.5F, 1.0F);
							Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.SPELL_WITCH, player, 32);
						}
					}
				}

				else if (event.getEntityLiving() instanceof EntityHuman && flag)
				{
					EntityHuman human = (EntityHuman)event.getEntityLiving();

					//Warriors are immune to infection.
					if (human.getProfessionEnum() == EnumProfession.Warrior)
					{
						return;
					}

					//Villagers wearing armor are immune to infection.
					Iterator<ItemStack> iterator = human.getEquipmentAndArmor().iterator();
					
					while (iterator.hasNext())
					{
						ItemStack stack = iterator.next();
						
						if (stack != null)
						{
							return;
						}
					}

					human.setIsInfected(true);
					human.setHealth(human.getMaxHealth());
					zombie.setAttackTarget(null);

					human.playSound(SoundEvents.ENTITY_WITHER_AMBIENT, 0.5F, 1.0F);
					Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.SPELL_WITCH, human, 32);
				}

				else if (event.getEntityLiving() instanceof EntityHuman && ((EntityHuman)event.getEntityLiving()).getIsInfected())
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
		if (event.getEntityLiving() instanceof EntityMob && event.getTarget() instanceof EntityHuman)
		{
			EntityMob mob = (EntityMob) event.getEntityLiving();
			EntityHuman target = (EntityHuman) event.getTarget();

			if (target.getIsInfected())
			{
				mob.setAttackTarget(null);
			}
		}
	}

	@SubscribeEvent
	public void onPlaceEvent(PlaceEvent event) //Check for grim reaper summoning area.
	{
		int x = event.getPos().getX();
		int y = event.getPos().getY();
		int z = event.getPos().getZ();
		Block placedBlock = event.getPlacedBlock().getBlock();

		if (placedBlock == Blocks.FIRE && BlockHelper.getBlock(event.getWorld(), x, y - 1, z) == Blocks.EMERALD_BLOCK)
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
					Block block = BlockHelper.getBlock(event.getWorld(), x + dX, y + j, z + dZ);

					if (block == Blocks.OBSIDIAN || block == Blocks.FIRE)
					{
					}

					else
					{
						break;
					}

					//If we made it up to 1 without breaking, make sure the block is fire so that it's a lit totem.
					if (j == 1 && block == Blocks.FIRE)
					{
						totemsFound++;
					}

					continue;
				}
			}

			if (totemsFound >= 3 && !event.getWorld().isDaytime())
			{
				Point3D summonPoint = new Point3D(x, y + 5, z);
				NetworkRegistry.TargetPoint summonTarget = new NetworkRegistry.TargetPoint(event.getWorld().provider.getDimension(), summonPoint.iPosX, summonPoint.iPosY + 5, summonPoint.iPosZ, 32);

				EventHooksFML.setReaperSummonPoint(event.getWorld(), new Point3D(x + 1.0D, y + 5, z + 1.0D));

				//FIXME
//				MCA.getPacketHandler().sendPacketToAllAround(new PacketPlaySoundOnPlayer("portal.portal"), summonTarget);

				for (int i = 0; i < 2; i++)
				{
					Utilities.spawnParticlesAroundPointS(EnumParticleTypes.FLAME, event.getWorld(), x, y - i, z, 32);
					BlockHelper.setBlock(event.getWorld(), x, y - i, z, Blocks.AIR);
				}
			}

			else if (totemsFound >= 3 && event.getWorld().isDaytime())
			{
				TutorialManager.sendMessageToPlayer(event.getPlayer(), "The Grim Reaper must be summoned at night.", "");
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
