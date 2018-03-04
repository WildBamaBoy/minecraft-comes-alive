package mca.actions;

import java.util.List;

import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumCombatBehaviors;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class ActionCombat extends AbstractAction
{
	private static final DataParameter<Integer> ATTACK_METHOD_ID = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> ATTACK_TRIGGER_ID = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> ATTACK_TARGET_ID = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);

	private EntityLivingBase attackTarget;
	private int rangedAttackTime;
	
	public ActionCombat(EntityVillagerMCA actor) 
	{
		super(actor);

		setMethodBehavior(EnumCombatBehaviors.METHOD_DO_NOT_FIGHT);
		setTriggerBehavior(EnumCombatBehaviors.TRIGGER_PLAYER_TAKE_DAMAGE);
		setTargetBehavior(EnumCombatBehaviors.TARGET_HOSTILE_MOBS);
	}

	@Override
	public void onUpdateServer() 
	{
		//Do nothing when we're asleep
		if (actor.getBehavior(ActionSleep.class).getIsSleeping())
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
			double distanceToTarget = RadixMath.getDistanceToEntity(actor, attackTarget);
			
			//Melee attacks
			if (getMethodBehavior() == EnumCombatBehaviors.METHOD_MELEE_ONLY || 
				(getMethodBehavior() == EnumCombatBehaviors.METHOD_MELEE_AND_RANGED && 
				distanceToTarget < 5.0F))
			{
				moveToAttackTarget();

				if (distanceToTarget < 1.5F)
				{
					actor.swingItem();
					
					ItemStack heldItem = actor.getHeldItem(EnumHand.MAIN_HAND);
					Item.ToolMaterial swordMaterial = null;
					
					if (heldItem != null && heldItem.getItem() instanceof ItemSword)
					{
						ItemSword sword = (ItemSword)heldItem.getItem();
						swordMaterial = Item.ToolMaterial.valueOf(sword.getToolMaterialName());
					}
					
					float damage = swordMaterial != null ? 4.0F + swordMaterial.getDamageVsEntity() : 0.5F;
					attackTarget.attackEntityFrom(DamageSource.causeMobDamage(actor), damage);
				}
			}
			
			//Ranged attacks
			else if (getMethodBehavior() == EnumCombatBehaviors.METHOD_RANGED_ONLY ||
					(getMethodBehavior() == EnumCombatBehaviors.METHOD_MELEE_AND_RANGED &&
					distanceToTarget >= 5.0F))
			{
				actor.getLookHelper().setLookPosition(attackTarget.posX, attackTarget.posY + (double)attackTarget.getEyeHeight(), attackTarget.posZ, 10.0F, actor.getVerticalFaceSpeed());
				
				if (rangedAttackTime <= 0)
				{
					EntityTippedArrow arrow = new EntityTippedArrow(actor.world, actor);
			        double dX = attackTarget.posX - actor.posX;
			        double dY = attackTarget.getEntityBoundingBox().minY + (double)(attackTarget.height / 3.0F) - arrow.posY;
			        double dZ = attackTarget.posZ - actor.posZ;
			        double d3 = (double)MathHelper.sqrt(dX * dX + dZ * dZ);
			        
			        arrow.setThrowableHeading(dX, dY + d3 * 0.20000000298023224D, dZ, 1.6F, (float)(14 - actor.world.getDifficulty().getDifficultyId() * 4));
			        arrow.setDamage((double)(5.0F) + actor.getRNG().nextGaussian() * 0.25D + (double)((float)actor.world.getDifficulty().getDifficultyId() * 0.11F));
			        actor.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (actor.getRNG().nextFloat() * 0.4F + 0.8F));
					actor.world.spawnEntity(arrow);
					
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
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("attackMethodId", actor.getDataManager().get(ATTACK_METHOD_ID));
		nbt.setInteger("attackTriggerId", actor.getDataManager().get(ATTACK_TRIGGER_ID));
		nbt.setInteger("attackTargetId", actor.getDataManager().get(ATTACK_TARGET_ID));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		setMethodBehavior(EnumCombatBehaviors.getById(nbt.getInteger("attackMethodId")));
		setTriggerBehavior(EnumCombatBehaviors.getById(nbt.getInteger("attackTriggerId")));
		setTargetBehavior(EnumCombatBehaviors.getById(nbt.getInteger("attackTargetId")));
	}

	public EnumCombatBehaviors getMethodBehavior()
	{
		return EnumCombatBehaviors.getById(actor.getDataManager().get(ATTACK_METHOD_ID));
	}

	public EnumCombatBehaviors getTriggerBehavior()
	{
		return EnumCombatBehaviors.getById(actor.getDataManager().get(ATTACK_TRIGGER_ID));
	}

	public EnumCombatBehaviors getTargetBehavior()
	{
		return EnumCombatBehaviors.getById(actor.getDataManager().get(ATTACK_TARGET_ID));
	}

	public void setMethodBehavior(EnumCombatBehaviors value)
	{
		actor.getDataManager().set(ATTACK_METHOD_ID, value.getNumericId());
	}

	public void setTriggerBehavior(EnumCombatBehaviors value)
	{
		actor.getDataManager().set(ATTACK_TRIGGER_ID, value.getNumericId());
	}

	public void setTargetBehavior(EnumCombatBehaviors value)
	{
		actor.getDataManager().set(ATTACK_TARGET_ID, value.getNumericId());
	}

	private void findAttackTarget()
	{
		List<EntityLivingBase> entitiesAroundMe = RadixLogic.getEntitiesWithinDistance(EntityLivingBase.class, actor, 10);
		double distance = 100.0D;
		EntityLivingBase target = null;

		for (EntityLivingBase livingBase : entitiesAroundMe)
		{
			double distanceTo = RadixMath.getDistanceToEntity(actor, livingBase);

			if (isEntityValidToAttack(livingBase) && distanceTo < distance)
			{
				distance = RadixMath.getDistanceToEntity(actor, livingBase);
				target = livingBase;
			}
		}

		attackTarget = target;
	}

	private void moveToAttackTarget()
	{
		actor.getNavigator().tryMoveToEntityLiving(attackTarget, Constants.SPEED_RUN);
	}

	public boolean isEntityValidToAttack(EntityLivingBase entity)
	{
		if (entity instanceof IMob &&
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
		if (entity != actor)
		{
			this.attackTarget = entity;
		}
	}
	
	public EntityLivingBase getAttackTarget()
	{
		return this.attackTarget;
	}
	
	protected void registerDataParameters()
	{
		actor.getDataManager().register(ATTACK_METHOD_ID, EnumCombatBehaviors.METHOD_DO_NOT_FIGHT.getNumericId());
		actor.getDataManager().register(ATTACK_TRIGGER_ID, EnumCombatBehaviors.TRIGGER_PLAYER_DEAL_DAMAGE.getNumericId());
		actor.getDataManager().register(ATTACK_TARGET_ID, EnumCombatBehaviors.TARGET_PASSIVE_MOBS.getNumericId());
	}

	public ItemStack getHeldItem() 
	{
		if (getMethodBehavior() == EnumCombatBehaviors.METHOD_RANGED_ONLY)
		{
			return actor.attributes.getInventory().getBestItemOfType(ItemBow.class);
		}
		
		else
		{
			return actor.attributes.getInventory().getBestItemOfType(ItemSword.class);	
		}
	}
}
