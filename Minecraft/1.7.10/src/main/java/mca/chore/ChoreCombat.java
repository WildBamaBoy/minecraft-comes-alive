/*******************************************************************************
 * ChoreCombat.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.network.packets.PacketSetFieldValue;
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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

import com.radixshock.radixcore.logic.LogicHelper;

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
	private transient boolean playedSound;

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
				MCA.getInstance().getLogger().log(e);
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
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}
	}

	@Override
	protected int getDelayForToolType(ItemStack toolStack) 
	{
		return 0;
	}

	@Override
	protected String getChoreXpName() 
	{
		return null;
	}

	@Override
	protected String getBaseLevelUpPhrase() 
	{
		return null;
	}

	@Override
	protected float getChoreXp() 
	{
		return 0;
	}

	@Override
	protected void setChoreXp(float setAmount) 
	{
		//Combat doesn't use XP.
	}

	/**
	 * Runs the melee only section of the combat chore.
	 */
	private void runMeleeAI()
	{
		getTarget();

		if (!tryHandleTargetDeath())
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

		if (!tryHandleTargetDeath())
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
						owner.isStaying = true;
						MCA.packetHandler.sendPacketToAllPlayers(new PacketSetFieldValue(owner.getEntityId(), "isStaying", owner.isStaying));
					}

					else if (!isWithinSentryArea() && owner.isStaying)
					{
						owner.isStaying = false;
						MCA.packetHandler.sendPacketToAllPlayers(new PacketSetFieldValue(owner.getEntityId(), "isStaying", owner.isStaying));
						owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(sentryPosX, sentryPosY, sentryPosZ), Constants.SPEED_WALK);
					}
				}
			}

			else if (owner.target != null && owner.isStaying)
			{
				owner.isStaying = false;
				MCA.packetHandler.sendPacketToAllPlayers(new PacketSetFieldValue(owner.getEntityId(), "isStaying", owner.isStaying));
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
			EntityLivingBase closestEntity = null;
			List<Entity> entitiesAroundMe = null;

			if (sentryMode)
			{
				entitiesAroundMe = owner.worldObj.getEntitiesWithinAABBExcludingEntity(owner, AxisAlignedBB.getBoundingBox(owner.posX - sentryRadius, owner.posY - 3, owner.posZ - sentryRadius, owner.posX + sentryRadius, owner.posY + 3, owner.posZ + sentryRadius));
			}

			else
			{
				entitiesAroundMe = owner.worldObj.getEntitiesWithinAABBExcludingEntity(owner, AxisAlignedBB.getBoundingBox(owner.posX - 15, owner.posY - 3, owner.posZ - 15, owner.posX + 15, owner.posY + 3, owner.posZ + 15));
			}

			//Find the closest entity.
			for (final Entity entity : entitiesAroundMe)
			{
				if (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer) && !(entity instanceof AbstractEntity))
				{
					//Determine if they should attack by checking the target's class against entities selected as attackable.
					if (isSetToAttackEntity(entity))
					{
						if (closestEntity == null)
						{
							closestEntity = (EntityLivingBase)entity;
						}

						else
						{
							if (owner.getDistanceToEntity(entity) < owner.getDistanceToEntity(closestEntity))
							{
								closestEntity = (EntityLivingBase)entity;
							}
						}
					}

					else if (isUnknownEntityValidTarget(entity))
					{
						closestEntity = (EntityLivingBase)entity;
					}
				}
			}

			if (owner.canEntityBeSeen(closestEntity))
			{
				owner.target = closestEntity;
			}
		}
	}

	private boolean tryHandleTargetDeath()
	{
		if (owner.target != null && !owner.target.isEntityAlive())
		{
			owner.target = null;
			rangedAttackTime = 0;

			//Assume the owner killed their target and check for achievement.
			if (owner instanceof EntityPlayerChild)
			{
				final EntityPlayerChild child = (EntityPlayerChild)owner;
				final EntityPlayer owner = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);
				
				if (child.isAdult)
				{
					child.mobsKilled++;

					if (owner != null && child.mobsKilled >= 50)
					{
						owner.triggerAchievement(MCA.getInstance().achievementAdultKills);
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
				owner.inventory.getQuantityOfItem(Items.arrow) > 0 &&
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

		if (!playedSound)
		{
			owner.worldObj.playSoundAtEntity(owner.target, "mob.enderdragon.wings", 1.0F, 1.0F);
			playedSound = true;
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
		owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Items.arrow), 1);

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
			targetCreeper.dropItem(Items.gunpowder, owner.worldObj.rand.nextInt(1) + 1);
			playedSound = false;
		}
	}

	private void doUpdateRangedAttack()
	{
		if (owner.target != null)
		{
			owner.getNavigator().clearPathEntity();
			Utility.faceCoordinates(owner, owner.target.posX, owner.target.posY, owner.target.posZ);
			rangedAttackTime--;
		}
	}

	private boolean isWithinSentryArea()
	{
		return Math.abs(sentryPosX - owner.posX) < 1.0D && Math.abs(sentryPosY - owner.posY) < 1.0D && Math.abs(sentryPosZ) - owner.posZ < 1.0D;
	}

	private boolean isSetToAttackEntity(Entity entity)
	{
		return entity instanceof EntityPig && attackPigs  			|| entity instanceof EntitySheep && attackSheep 		||
				entity instanceof EntityCow && attackCows 			|| entity instanceof EntityChicken && attackChickens   	||
				entity instanceof EntitySpider && attackSpiders     || entity instanceof EntityZombie && attackZombies     	||
				entity instanceof EntitySkeleton && attackSkeletons || entity instanceof EntityCreeper && attackCreepers   	||
				entity instanceof EntityEnderman && attackEndermen;
	}
	
	private boolean isUnknownEntityValidTarget(Entity entity)
	{
		return entity instanceof EntityMob && attackUnknown && !(entity instanceof EntityCreeper) &&
				!(entity instanceof EntitySpider) && !(entity instanceof EntitySkeleton) && ! (entity instanceof EntityZombie) &&
				!(entity instanceof EntityEnderman);
	}
}
