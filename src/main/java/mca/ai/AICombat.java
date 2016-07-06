package mca.ai;

import java.util.List;

import mca.core.Constants;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumCombatBehaviors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import radixcore.data.DataWatcherEx;
import radixcore.data.WatchedInt;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AICombat extends AbstractAI
{
	private WatchedInt attackMethodInt;
	private WatchedInt attackTriggerInt;
	private WatchedInt attackTargetInt;

	private EntityLivingBase attackTarget;
	private int rangedAttackTime;
	
	public AICombat(EntityHuman owner) 
	{
		super(owner);
		attackMethodInt = new WatchedInt(EnumCombatBehaviors.METHOD_DO_NOT_FIGHT.getNumericId(), WatcherIDsHuman.COMBAT_METHOD, owner.getDataWatcherEx());
		attackTriggerInt = new WatchedInt(EnumCombatBehaviors.TRIGGER_PLAYER_TAKE_DAMAGE.getNumericId(), WatcherIDsHuman.COMBAT_TRIGGER, owner.getDataWatcherEx());
		attackTargetInt = new WatchedInt(EnumCombatBehaviors.TARGET_HOSTILE_MOBS.getNumericId(), WatcherIDsHuman.COMBAT_TARGET, owner.getDataWatcherEx());
	}

	@Override
	public void onUpdateCommon() 
	{
	}

	@Override
	public void onUpdateClient() 
	{	
	}

	@Override
	public void onUpdateServer() 
	{
		//Do nothing when we're asleep or when being interacted with.
		if (owner.getAI(AISleep.class).getIsSleeping() || owner.getIsInteracting())
		{
			return;
		}
		
		//Cancel attack targets and stop when we're not supposed to fight.
		if (attackTarget != null && getMethodBehavior() == EnumCombatBehaviors.METHOD_DO_NOT_FIGHT)
		{
			attackTarget = null;
			return;
		}

		//Also clear our attack target if it is dead.
		if (attackTarget != null && (attackTarget.isDead || attackTarget.getHealth() <= 0.0F))
		{
			attackTarget = null;
		}
		
		//Check if we should be searching for a target.
		if (attackTarget == null && getTriggerBehavior() == EnumCombatBehaviors.TRIGGER_ALWAYS)
		{
			findAttackTarget();
		}

		//If we have a target, proceed to attack.
		else if (attackTarget != null)
		{
			double distanceToTarget = RadixMath.getDistanceToEntity(owner, attackTarget);
			
			//Melee attacks
			if (getMethodBehavior() == EnumCombatBehaviors.METHOD_MELEE_ONLY || 
				(getMethodBehavior() == EnumCombatBehaviors.METHOD_MELEE_AND_RANGED && 
				distanceToTarget < 5.0F))
			{
				moveToAttackTarget();

				if (distanceToTarget < 1.5F)
				{
					owner.swingItem();
					
					ItemStack heldItem = owner.getHeldItem();
					Item.ToolMaterial swordMaterial = null;
					
					if (heldItem != null && heldItem.getItem() instanceof ItemSword)
					{
						ItemSword sword = (ItemSword)heldItem.getItem();
						swordMaterial = Item.ToolMaterial.valueOf(sword.getToolMaterialName());
					}
					
					float damage = swordMaterial != null ? 4.0F + swordMaterial.getDamageVsEntity() : 0.5F;
					attackTarget.attackEntityFrom(DamageSource.causeMobDamage(owner), damage);
				}
			}
			
			//Ranged attacks
			else if (getMethodBehavior() == EnumCombatBehaviors.METHOD_RANGED_ONLY ||
					(getMethodBehavior() == EnumCombatBehaviors.METHOD_MELEE_AND_RANGED &&
					distanceToTarget >= 5.0F))
			{
				owner.getLookHelper().setLookPosition(attackTarget.posX, attackTarget.posY + (double)attackTarget.getEyeHeight(), attackTarget.posZ, 10.0F, owner.getVerticalFaceSpeed());
				
				if (rangedAttackTime <= 0)
				{
					owner.worldObj.spawnEntityInWorld(new EntityArrow(owner.worldObj, owner, attackTarget, 1.6F, 12F));
					owner.worldObj.playSoundAtEntity(owner, "random.bow", 1.0F, 1.0F / (owner.getRNG().nextFloat() * 0.4F + 0.8F));
					rangedAttackTime = 60;
				}

				else
				{
					rangedAttackTime--;
				}
			}
		}
	}

	@Override
	public void reset()
	{
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("attackMethod", attackMethodInt.getInt());
		nbt.setInteger("attackTrigger", attackTriggerInt.getInt());
		nbt.setInteger("attackTarget", attackTargetInt.getInt());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		setMethodBehavior(nbt.getInteger("attackMethod"));
		setTriggerBehavior(nbt.getInteger("attackTrigger"));
		setTargetBehavior(nbt.getInteger("attackTarget"));
	}

	public EnumCombatBehaviors getMethodBehavior()
	{
		return EnumCombatBehaviors.getById(attackMethodInt.getInt());
	}

	public EnumCombatBehaviors getTriggerBehavior()
	{
		return EnumCombatBehaviors.getById(attackTriggerInt.getInt());
	}

	public EnumCombatBehaviors getTargetBehavior()
	{
		return EnumCombatBehaviors.getById(attackTargetInt.getInt());
	}

	public void setMethodBehavior(int value)
	{
		DataWatcherEx.allowClientSideModification = true;
		this.attackMethodInt.setValue(value);
		DataWatcherEx.allowClientSideModification = false;
	}

	public void setTriggerBehavior(int value)
	{
		DataWatcherEx.allowClientSideModification = true;
		this.attackTriggerInt.setValue(value);
		DataWatcherEx.allowClientSideModification = false;
	}

	public void setTargetBehavior(int value)
	{
		DataWatcherEx.allowClientSideModification = true;
		this.attackTargetInt.setValue(value);
		DataWatcherEx.allowClientSideModification = false;
	}

	private void findAttackTarget()
	{
		List<Entity> entitiesAroundMe = RadixLogic.getAllEntitiesWithinDistanceOfCoordinates(owner.worldObj, owner.posX, owner.posY, owner.posZ, 10);
		double distance = 100.0D;
		EntityLivingBase target = null;

		for (Entity entity : entitiesAroundMe)
		{
			if (entity instanceof EntityLivingBase)
			{
				EntityLivingBase livingBase = (EntityLivingBase)entity;
				double distanceTo = RadixMath.getDistanceToEntity(owner, livingBase);

				if (isEntityValidToAttack(livingBase) && distanceTo < distance)
				{
					distance = RadixMath.getDistanceToEntity(owner, livingBase);
					target = livingBase;
				}
			}
		}

		attackTarget = target;
	}

	private void moveToAttackTarget()
	{
		owner.getNavigator().tryMoveToEntityLiving(attackTarget, Constants.SPEED_RUN);
	}

	public boolean isEntityValidToAttack(EntityLivingBase entity)
	{
		if (entity instanceof EntityMob &&
				(getTargetBehavior() == EnumCombatBehaviors.TARGET_HOSTILE_MOBS || 
				getTargetBehavior() == EnumCombatBehaviors.TARGET_PASSIVE_OR_HOSTILE_MOBS))
		{
			return true;
		}

		else if (entity instanceof EntityAnimal &&
				(getTargetBehavior() == EnumCombatBehaviors.TARGET_PASSIVE_MOBS || 
				getTargetBehavior() == EnumCombatBehaviors.TARGET_PASSIVE_OR_HOSTILE_MOBS))
		{
			return true;
		}

		else
		{
			return false;
		}
	}

	public void setAttackTarget(EntityLivingBase entity)
	{
		if (entity != owner)
		{
			this.attackTarget = entity;
		}
	}
	
	public EntityLivingBase getAttackTarget()
	{
		return this.attackTarget;
	}
}
