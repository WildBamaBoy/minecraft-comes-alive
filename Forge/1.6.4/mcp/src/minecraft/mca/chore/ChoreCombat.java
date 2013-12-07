/*******************************************************************************
 * ChoreCombat.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import mca.core.MCA;
import mca.core.util.LogicHelper;
import mca.core.util.PacketHelper;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * The combat chore handles fighting other entities.
 */
public class ChoreCombat extends AbstractChore
{
	/** Should ranged weapons be used? */
	public boolean useRange;

	/** Should melee be used? */
	public boolean useMelee;

	/** Should pigs be attacked? */
	public boolean attackPigs;

	/** Should sheep be attacked? */
	public boolean attackSheep;

	/** Should sheep be attacked? */
	public boolean attackCows;

	/** Should chickens be attacked? */
	public boolean attackChickens;

	/** Should spiders be attacked? */
	public boolean attackSpiders = true;

	/** Should zombies be attacked? */
	public boolean attackZombies = true;

	/** Should skeletons be attacked? */
	public boolean attackSkeletons = true;

	/** Should creepers be attacked? */
	public boolean attackCreepers;

	/** Should endermen be attacked? */
	public boolean attackEndermen;

	/** Should unknown mobs be attacked? */
	public boolean attackUnknown = true;

	/**Is the entity in sentry mode? */
	public boolean sentryMode;

	/**How far the entity will go from the sentry area. */
	public int sentryRadius = 5;

	/**Used to manage the time between each ranged shot.*/
	public int rangedAttackTime;

	/**The X position the entity will be a sentry at. */
	public double sentryPosX;

	/**The Y position the entity will be a sentry at. */
	public double sentryPosY;

	/**The Z position the entity will be a sentry at. */
	public double sentryPosZ;

	/**Has the creeper "woosh" sound been played?*/
	private transient boolean playedWoosh;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity that will be performing the chore.
	 */
	public ChoreCombat(AbstractEntity entity) 
	{
		super(entity);
	}

	@Override
	public void beginChore()
	{
		hasBegun = true;
	}

	@Override
	public void runChoreAI() 
	{
		//Run the appropriate AI depending on the combat chore settings.
		if (sentryMode)
		{
			runSentryAI();
		}

		//The sentry AI only tells when to start moving and when to move back. Fall through to
		//actually make them attack.
		if (useMelee && !useRange)
		{
			runMeleeAI();
		}

		else if (!useMelee && useRange)
		{
			runRangeAI();
		}

		else if (useMelee && useRange)
		{
			runHybridAI();
		}
	}

	@Override
	public String getChoreName() 
	{
		return "Combat";
	}

	@Override
	public void endChore() 
	{
		hasEnded = true;
	}

	@Override
	public void writeChoreToNBT(NBTTagCompound nbt) 
	{
		//Loop through each field in this class and write to NBT.
		for (final Field field : this.getClass().getFields())
		{
			try
			{
				if (field.getModifiers() != Modifier.TRANSIENT)
				{
					if (field.getType().toString().contains("int"))
					{
						nbt.setInteger(field.getName(), (Integer)field.get(owner.combatChore));
					}

					else if (field.getType().toString().contains("double"))
					{
						nbt.setDouble(field.getName(), (Double)field.get(owner.combatChore));
					}

					else if (field.getType().toString().contains("float"))
					{
						nbt.setFloat(field.getName(), (Float)field.get(owner.combatChore));
					}

					else if (field.getType().toString().contains("String"))
					{
						nbt.setString(field.getName(), (String)field.get(owner.combatChore));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						nbt.setBoolean(field.getName(), (Boolean)field.get(owner.combatChore));
					}
				}
			}

			catch (IllegalAccessException e)
			{
				MCA.getInstance().log(e);
				continue;
			}
		}
	}

	@Override
	public void readChoreFromNBT(NBTTagCompound nbt) 
	{
		//Loop through each field in this class and read it from NBT.
		for (final Field field : this.getClass().getFields())
		{
			try
			{
				if (field.getModifiers() != Modifier.TRANSIENT)
				{
					if (field.getType().toString().contains("int"))
					{
						field.set(owner.combatChore, nbt.getInteger(field.getName()));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(owner.combatChore, nbt.getDouble(field.getName()));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(owner.combatChore, nbt.getFloat(field.getName()));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(owner.combatChore, nbt.getString(field.getName()));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(owner.combatChore, nbt.getBoolean(field.getName()));
					}
				}
			}

			catch (IllegalAccessException e)
			{
				MCA.getInstance().log(e);
				continue;
			}
		}
	}

	@Override
	protected int getDelayForToolType(ItemStack toolStack) 
	{
		return 0;
	}

	/**
	 * Runs the melee only section of the combat chore.
	 */
	private void runMeleeAI()
	{
		getTarget();

		if (!doTryHandleTargetDeath())
		{
			if (canDoCreeperThrow())
			{
				doCreeperThrow();
			}

			else if (canDoMeleeAttack())
			{
				doMeleeAttack();
			}

			else if (canDoCreeperExplosion())
			{
				doCreeperExplosion();
			}
		}
	}

	/**
	 * Runs the ranged only section of the combat chore.
	 */
	private void runRangeAI()
	{
		getTarget();

		if (!doTryHandleTargetDeath())
		{
			if (canDoRangedAttack())
			{
				doRangedAttack();
			}

			else
			{
				doUpdateRangedAttack();
			}
		}
	}

	/**
	 * Runs the melee or ranged chore depending on distance to the owner's target.
	 */
	private void runHybridAI()
	{
		getTarget();

		//Make sure a target was assigned.
		if (owner.target != null)
		{
			//Determine what AI should be run based on distance.
			if (LogicHelper.getDistanceToEntity(owner, owner.target) < 10 && LogicHelper.getDistanceToEntity(owner, owner.target) <= 3)
			{
				runMeleeAI();
			}

			else
			{
				runRangeAI();
			}
		}
	}

	private void runSentryAI()
	{
		if (owner.isFollowing)
		{
			owner.isFollowing = false;
			owner.target = null;
		}

		if (!owner.worldObj.isRemote)
		{
			if (owner.target == null || owner.target.isDead)
			{
				if (owner.getNavigator().noPath())
				{
					if (isWithinSentryArea() && !owner.isStaying)
					{
						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(owner.entityId, "isStaying", true));
						owner.isStaying = true;
					}

					else if (!isWithinSentryArea() && owner.isStaying)
					{
						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(owner.entityId, "isStaying", false));
						owner.isStaying = false;
						owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(sentryPosX, sentryPosY, sentryPosZ), 0.6F);
					}
				}
			}

			else if (owner.target != null && owner.isStaying)
			{
				owner.isStaying = false;
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(owner.entityId, "isStaying", false));
			}
		}
	}

	/**
	 * Gets a valid target for the entity to attack.
	 */
	private void getTarget()
	{
		if (owner.target == null)
		{
			final List<EntityLivingBase> nearbyEntities = getNearbyEntities();
			EntityLivingBase closestEntity = nearbyEntities.isEmpty() ? null : nearbyEntities.get(0);

			for (final Entity entity : nearbyEntities)
			{
				if (!(entity instanceof EntityPlayer) && !(entity instanceof AbstractEntity) && 
						isTargetValidForSettings(entity) && owner.getDistanceToEntity(entity) < owner.getDistanceToEntity(closestEntity))
				{
					closestEntity = (EntityLivingBase)entity;
				}
			}

			owner.target = closestEntity;
		}
	}

	private boolean doTryHandleTargetDeath()
	{
		if (owner.target != null && !owner.target.isEntityAlive())
		{
			owner.target = null;
			rangedAttackTime = 0;

			//Assume the owner killed their target and check for achievement.
			if (owner instanceof EntityPlayerChild)
			{
				EntityPlayerChild child = (EntityPlayerChild)owner;

				if (child.isAdult)
				{
					child.mobsKilled++;

					if (child.mobsKilled >= 50)
					{
						child.worldObj.getPlayerEntityByName(child.ownerPlayerName).triggerAchievement(MCA.getInstance().achievementAdultKills);
					}
				}
			}

			return true;
		}

		return false;
	}

	private boolean canDoCreeperThrow()
	{
		return owner.name.equals("Shepard") && 
				owner.target instanceof EntityCreeper && 
				owner.getDistanceToEntity(owner.target) > 3 && 
				owner.getDistanceToEntity(owner.target) < 15 && 
				owner.worldObj.canBlockSeeTheSky((int)owner.target.posX, (int)owner.target.posY, (int)owner.target.posZ);
	}

	private boolean canDoMeleeAttack()
	{
		return owner.target != null && owner.getDistanceToEntity(owner.target) < 3;
	}

	private boolean canDoRangedAttack()
	{
		return owner.target != null &&
				rangedAttackTime <= 0 && 
				owner.inventory.getQuantityOfItem(Item.arrow) > 0 &&
				LogicHelper.getDistanceToEntity(owner, owner.target) < 10 &&
				owner.canEntityBeSeen(owner.target);
	}

	private boolean canDoCreeperExplosion()
	{
		return owner.name.equals("Shepard") && 
				owner.target instanceof EntityCreeper && 
				owner.target.posY - owner.posY >= 15;
	}

	private void doCreeperThrow()
	{
		owner.target.motionY += 0.4D;

		if (!playedWoosh)
		{
			owner.worldObj.playSoundAtEntity(owner.target, "mob.enderdragon.wings", 1.0F, 1.0F);
			playedWoosh = true;
		}
	}

	private void doMeleeAttack()
	{
		final boolean attackSuccessful = owner.target.attackEntityFrom(DamageSource.causeMobDamage(owner), owner.inventory.getDamageVsEntity(owner.target));
		owner.swingItem();

		if (attackSuccessful)
		{
			owner.damageHeldItem();
		}

		if (owner.onGround)
		{
			final double distanceX = owner.target.posX - owner.posX;
			final double distanceZ = owner.target.posZ - owner.posZ;
			final float realDistance = MathHelper.sqrt_double(distanceX * distanceX + distanceZ * distanceZ);

			owner.motionX = (distanceX / realDistance) * 0.5D * 0.8D + owner.motionX * 0.2D;
			owner.motionZ = (distanceZ / realDistance) * 0.5D * 0.8D + owner.motionZ * 0.2D;
			owner.motionY = 0.4;
		}
	}

	private void doRangedAttack()
	{
		//Fire an arrow server side.
		if (!owner.worldObj.isRemote)
		{
			owner.worldObj.spawnEntityInWorld(new EntityArrow(owner.worldObj, owner, owner.target, 1.6F, 12F));
		}

		owner.worldObj.playSoundAtEntity(owner, "random.bow", 1.0F, 1.0F / (owner.getRNG().nextFloat() * 0.4F + 0.8F));
		owner.damageHeldItem();
		owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Item.arrow), 1);

		rangedAttackTime = 60;
	}

	private void doCreeperExplosion()
	{
		final EntityCreeper targetCreeper = (EntityCreeper)owner.target;

		if (!targetCreeper.worldObj.isRemote)
		{
			final boolean mobGreifing = targetCreeper.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");

			if (targetCreeper.getPowered())
			{
				targetCreeper.worldObj.createExplosion(targetCreeper, targetCreeper.posX, targetCreeper.posY, targetCreeper.posZ, 3 * 2, mobGreifing);
			}

			else
			{
				targetCreeper.worldObj.createExplosion(targetCreeper, targetCreeper.posX, targetCreeper.posY, targetCreeper.posZ, 3, mobGreifing);
			}

			targetCreeper.setDead();
			targetCreeper.dropItem(Item.gunpowder.itemID, owner.worldObj.rand.nextInt(1) + 1);
			playedWoosh = false;
		}
	}

	private void doUpdateRangedAttack()
	{
		if (owner.target != null)
		{
			owner.getNavigator().clearPathEntity();
			AbstractEntity.faceCoordinates(owner, owner.target.posX, owner.target.posY, owner.target.posZ);
			rangedAttackTime--;
		}
	}

	private boolean isWithinSentryArea()
	{
		return Math.abs(sentryPosX - owner.posX) < 1.0D && Math.abs(sentryPosY - owner.posY) < 1.0D && Math.abs(sentryPosZ) - owner.posZ < 1.0D;
	}

	private boolean isTargetValidForSettings(Entity entity)
	{
		return entity instanceof EntityPig && attackPigs           ||
				entity instanceof EntitySheep && attackSheep        ||
				entity instanceof EntityCow && attackCows           ||
				entity instanceof EntityChicken && attackChickens   ||
				entity instanceof EntitySpider && attackSpiders     ||
				entity instanceof EntityZombie && attackZombies     ||
				entity instanceof EntitySkeleton && attackSkeletons ||
				entity instanceof EntityCreeper && attackCreepers   ||
				entity instanceof EntityEnderman && attackEndermen  ||
				entity instanceof EntityMob && attackUnknown;
	}

	private List<EntityLivingBase> getNearbyEntities()
	{
		return sentryMode ? 
				(List<EntityLivingBase>)LogicHelper.getAllEntitiesOfTypeWithinDistanceOfEntity(owner, EntityLivingBase.class, sentryRadius) :
					(List<EntityLivingBase>)LogicHelper.getAllEntitiesOfTypeWithinDistanceOfEntity(owner, EntityLivingBase.class, 15);
	}
}
