/*******************************************************************************
 * ChoreCombat.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

/**
 * The combat chore handles fighting other entities.
 */
public class ChoreCombat extends Chore
{
	/** Should ranged be used? */
	public boolean useRange = false;

	/** Should melee be used? */
	public boolean useMelee = false;

	/** Should pigs be attacked? */
	public boolean attackPigs = false;

	/** Should sheep be attacked? */
	public boolean attackSheep = false;

	/** Should sheep be attacked? */
	public boolean attackCows = false;

	/** Should chickens be attacked? */
	public boolean attackChickens = false;

	/** Should spiders be attacked? */
	public boolean attackSpiders = true;

	/** Should zombies be attacked? */
	public boolean attackZombies = true;

	/** Should skeletons be attacked? */
	public boolean attackSkeletons = true;

	/** Should creepers be attacked? */
	public boolean attackCreepers = false;

	/** Should endermen be attacked? */
	public boolean attackEndermen = false;

	/** Should unknown mobs be attacked? */
	public boolean attackUnknown = false;
	
	/**Used to manage the time between each ranged shot.*/
	public int rangedAttackTime = 0;

	/**Has the creeper "woosh" sound been played?*/
	private transient boolean playedSound = false;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity that will be performing the chore.
	 */
	public ChoreCombat(EntityBase entity) 
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
	public void writeChoreToNBT(NBTTagCompound NBT) 
	{
		//Loop through each field in this class and write to NBT.
		for (Field f : this.getClass().getFields())
		{
			try
			{
				if (f.getModifiers() != Modifier.TRANSIENT)
				{
					if (f.getType().toString().contains("int"))
					{
						NBT.setInteger(f.getName(), (Integer)f.get(owner.combatChore));
					}

					else if (f.getType().toString().contains("double"))
					{
						NBT.setDouble(f.getName(), (Double)f.get(owner.combatChore));
					}

					else if (f.getType().toString().contains("float"))
					{
						NBT.setFloat(f.getName(), (Float)f.get(owner.combatChore));
					}

					else if (f.getType().toString().contains("String"))
					{
						NBT.setString(f.getName(), (String)f.get(owner.combatChore));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						NBT.setBoolean(f.getName(), (Boolean)f.get(owner.combatChore));
					}
				}
			}

			catch (Throwable e)
			{
				MCA.instance.log(e);
				continue;
			}
		}
	}

	@Override
	public void readChoreFromNBT(NBTTagCompound NBT) 
	{
		//Loop through each field in this class and read it from NBT.
		for (Field f : this.getClass().getFields())
		{
			try
			{
				if (f.getModifiers() != Modifier.TRANSIENT)
				{
					if (f.getType().toString().contains("int"))
					{
						f.set(owner.combatChore, (Integer)NBT.getInteger(f.getName()));
					}

					else if (f.getType().toString().contains("double"))
					{
						f.set(owner.combatChore, (Double)NBT.getDouble(f.getName()));
					}

					else if (f.getType().toString().contains("float"))
					{
						f.set(owner.combatChore, (Float)NBT.getFloat(f.getName()));
					}

					else if (f.getType().toString().contains("String"))
					{
						f.set(owner.combatChore, (String)NBT.getString(f.getName()));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						f.set(owner.combatChore, (Boolean)NBT.getBoolean(f.getName()));
					}
				}
			}

			catch (Throwable e)
			{
				MCA.instance.log(e);
				continue;
			}
		}
	}

	/**
	 * Runs the melee only section of the combat chore.
	 */
	private void runMeleeAI()
	{
		//Check if the owner has a target.
		if (owner.target == null)
		{
			owner.target = findTarget();
		}

		//The owner has a target to attack.
		else
		{
			//Ensure the target isn't dead. Set it to null if it is dead to get another target.
			if (!owner.target.isEntityAlive())
			{
				owner.target = null;

				//Assume the owner killed their target and check for achievement.
				if (owner instanceof EntityPlayerChild)
				{
					EntityPlayerChild child = (EntityPlayerChild)owner;

					if (child.isAdult)
					{
						child.mobsKilled++;

						if (child.mobsKilled >= 50)
						{
							try
							{
								child.worldObj.getPlayerEntityByName(child.ownerPlayerName).triggerAchievement(MCA.instance.achievementAdultKills);
							}

							catch (NullPointerException e)
							{
								MCA.instance.log("Error unlocking combat achievement.");
								MCA.instance.log(e);
							}
						}
					}
				}
			}

			//The target is not dead, so check if we can actually attack it.
			else
			{
				//Check the distance to the target. Here check if it is within 15 blocks but not closer than 2 blocks.
				if (owner.getDistanceToEntity(owner.target) > 2 && !(owner.getDistanceToEntity(owner.target) > 15))
				{
					//Check for Creeper throwing due to Shepard name.
					if (owner.name.equals("Shepard"))
					{
						if (owner.target instanceof EntityCreeper)
						{
							EntityCreeper theCreeper = (EntityCreeper)owner.target;
							theCreeper.motionY += 0.4D;

							if (!playedSound)
							{
								owner.worldObj.playSoundAtEntity(theCreeper, "mob.enderdragon.wings", 1.0F, 1.0F);
								playedSound = true;
							}
						}
					}
				}

				//The owner is within 2 blocks of the target.
				else if (owner.getDistanceToEntity(owner.target) < 2)
				{
					MCA.instance.log(owner.inventory.getDamageVsEntity(owner.target));
					boolean attackSuccessful = owner.target.attackEntityFrom(DamageSource.causeMobDamage(owner), owner.inventory.getDamageVsEntity(owner.target));
					owner.swingItem();

					if (attackSuccessful)
					{
						owner.damageHeldItem();
					}

					if (owner.onGround)
					{
						double distanceX = owner.target.posX - owner.posX;
						double distanceZ = owner.target.posZ - owner.posZ;
						float realDistance = MathHelper.sqrt_double(distanceX * distanceX + distanceZ * distanceZ);
						owner.motionX = (distanceX / (double)realDistance) * 0.5D * 0.8D + owner.motionX * 0.2D;
						owner.motionZ = (distanceZ / (double)realDistance) * 0.5D * 0.8D + owner.motionZ * 0.2D;
						owner.motionY = 0.4;
					}
				}

				//The target is greater than ten blocks away.
				else if (owner.getDistanceToEntity(owner.target) > 20)
				{
					//Check for making a creeper explode for children who are named Shepard.
					if (owner.name.equals("Shepard") && owner.target instanceof EntityCreeper)
					{
						EntityCreeper targetCreeper = (EntityCreeper)owner.target;

						if (!targetCreeper.worldObj.isRemote)
						{
							boolean mobGreifing = targetCreeper.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");

							if (targetCreeper.getPowered())
							{
								targetCreeper.worldObj.createExplosion(targetCreeper, targetCreeper.posX, targetCreeper.posY, targetCreeper.posZ, (float)(3 * 2), mobGreifing);
							}

							else
							{
								targetCreeper.worldObj.createExplosion(targetCreeper, targetCreeper.posX, targetCreeper.posY, targetCreeper.posZ, (float)3, mobGreifing);
							}

							targetCreeper.setDead();
							targetCreeper.dropItem(Item.gunpowder.itemID, owner.worldObj.rand.nextInt(1) + 1);
							playedSound = false;
						}
					}
				}
			}
		}
	}

	/**
	 * Runs the ranged only section of the combat chore.
	 */
	private void runRangeAI()
	{
		//Check if the owner has a target.
		if (owner.target == null)
		{
			owner.target = findTarget();
		}

		//They do. Continue logic.
		else
		{
			//Make sure the target isn't dead, it can be seen, and its within 10 blocks.
			if (!owner.target.isDead && owner.canEntityBeSeen(owner.target) && Logic.getDistanceToEntity(owner, owner.target) < 10)
			{
				owner.setPathToEntity(null);
				owner.faceCoordinates(owner, owner.target.posX, owner.target.posY, owner.target.posZ);

				if (rangedAttackTime > 0)
				{
					rangedAttackTime--;
				}

				else
				{
					//Check that they have an arrow.
					if (!(owner.inventory.getQuantityOfItem(Item.arrow) > 0))
					{
						useRange = false;
						useMelee = true;
						return;
					}

					//Fire an arrow server side.
					if (!owner.worldObj.isRemote)
					{
						EntityArrow entityarrow = new EntityArrow(owner.worldObj, owner, owner.target, 1.6F, 12F);
						owner.worldObj.spawnEntityInWorld(entityarrow);
					}

					owner.worldObj.playSoundAtEntity(owner, "random.bow", 1.0F, 1.0F / (owner.getRNG().nextFloat() * 0.4F + 0.8F));
					owner.damageHeldItem();
					owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Item.arrow), 1);

					rangedAttackTime = 60;
				}
			}

			//One of the checks did not pass.
			else
			{
				//See if the target is dead.
				if (owner.target.isDead)
				{
					//Assume it was killed by the owner of the chore.
					if (owner instanceof EntityPlayerChild)
					{
						EntityPlayerChild playerChild = (EntityPlayerChild)owner;

						playerChild.mobsKilled += 1;

						if (playerChild.mobsKilled >= 50)
						{
							EntityPlayer player = owner.worldObj.getPlayerEntityByName(playerChild.ownerPlayerName);

							if (player != null)
							{
								player.triggerAchievement(MCA.instance.achievementAdultKills);
							}
						}
					}
				}

				owner.isFollowing = true;
				owner.target = null;
				rangedAttackTime = 0;
			}
		}
	}

	/**
	 * Runs the melee or ranged chore depending on distance to the owner's target.
	 */
	private void runHybridAI()
	{
		//Check if the owner has a target.
		if (owner.target == null)
		{
			owner.target = findTarget();
		}

		//Make sure a target was assigned.
		if (owner.target != null)
		{
			//Determine what AI should be run based on distance.
			if (Logic.getDistanceToEntity(owner, owner.target) < 10)
			{
				if (Logic.getDistanceToEntity(owner, owner.target) <= 3)
				{
					runMeleeAI();
				}

				else
				{
					runRangeAI();
				}
			}
		}
	}

	/**
	 * Gets a valid target for the entity to attack.
	 * 
	 * @return	An entity living that is an acceptable target for attacking.
	 */
	private EntityLivingBase findTarget()
	{
		EntityLivingBase closestEntity = null;
		List<Entity> entitiesAroundMe = owner.worldObj.getEntitiesWithinAABBExcludingEntity(owner, AxisAlignedBB.getBoundingBox(owner.posX - 15, owner.posY - 3, owner.posZ - 15, owner.posX + 15, owner.posY + 3, owner.posZ + 15));

		//Find the closest entity.
		for (Entity entity : entitiesAroundMe)
		{
			if (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer) && !(entity instanceof EntityBase))
			{
				if (closestEntity == null)
				{
					//Determine if they should attack by checking the target's class against entities selected as attackable.
					if (entity instanceof EntityPig && attackPigs           	||
							entity instanceof EntitySheep && attackSheep        ||
							entity instanceof EntityCow && attackCows           ||
							entity instanceof EntityChicken && attackChickens   ||
							entity instanceof EntitySpider && attackSpiders     ||
							entity instanceof EntityZombie && attackZombies     ||
							entity instanceof EntitySkeleton && attackSkeletons ||
							entity instanceof EntityCreeper && attackCreepers   ||
							entity instanceof EntityEnderman && attackEndermen)
					{
						closestEntity = (EntityLivingBase)entity;
					}
					
					else if (entity instanceof EntityMob && attackUnknown)
					{
						closestEntity = (EntityLivingBase)entity;
					}
				}

				else
				{
					//Determine if they should attack by checking the target's class against entities selected as attackable.
					if (entity instanceof EntityPig && attackPigs           	||
							entity instanceof EntitySheep && attackSheep        ||
							entity instanceof EntityCow && attackCows           ||
							entity instanceof EntityChicken && attackChickens   ||
							entity instanceof EntitySpider && attackSpiders     ||
							entity instanceof EntityZombie && attackZombies     ||
							entity instanceof EntitySkeleton && attackSkeletons ||
							entity instanceof EntityCreeper && attackCreepers   ||
							entity instanceof EntityEnderman && attackEndermen)
					{
						if (owner.getDistanceToEntity(entity) < owner.getDistanceToEntity(closestEntity))
						{
							closestEntity = (EntityLivingBase)entity;
						}
					}
					
					else if (entity instanceof EntityMob && attackUnknown)
					{
						closestEntity = (EntityLivingBase)entity;
					}
				}
			}
		}

		//Set the owner's target.
		return closestEntity;
	}
}
