package mca.ai;

import java.util.List;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import radixcore.constant.Time;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AIDefend extends AbstractAI
{
	private static final int TARGET_SEARCH_INTERVAL = Time.SECOND * 1;

	private EntityLiving target;
	private int timeUntilTargetSearch;
	private int rangedAttackTime;

	public AIDefend(EntityHuman owner) 
	{
		super(owner);
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
		if (owner.getAI(AISleep.class).getIsSleeping())
		{
			return;
		}
		
		if (owner.getProfessionGroup() == EnumProfessionGroup.Guard && !owner.getIsMarried() && !owner.getIsInfected())
		{
			if (target == null)
			{
				if (timeUntilTargetSearch <= 0)
				{
					tryAssignTarget();
					timeUntilTargetSearch = TARGET_SEARCH_INTERVAL;
				}

				else
				{
					timeUntilTargetSearch--;
				}
			}

			else if (target != null)
			{
				double distanceToTarget = RadixMath.getDistanceToEntity(owner, target);
				
				if (target.isDead || distanceToTarget >= 15.0D)
				{
					reset();
					return;
				}

				if (owner.getProfessionEnum() == EnumProfession.Archer)
				{
					owner.getLookHelper().setLookPosition(target.posX, target.posY + (double)target.getEyeHeight(), target.posZ, 10.0F, owner.getVerticalFaceSpeed());
					
					if (rangedAttackTime <= 0)
					{
						owner.attackEntityWithRangedAttack(target, 12F);
						owner.playSound(SoundEvents.entity_skeleton_shoot, 1.0F, 1.0F / (owner.getRNG().nextFloat() * 0.4F + 0.8F));
						rangedAttackTime = 60;
					}

					else
					{
						rangedAttackTime--;
					}
				}

				else
				{
					if (distanceToTarget <= 2.0F)
					{
						owner.swingItem();

						if (owner.onGround)
						{
							owner.motionY += 0.45F;
						}

						try
						{
							target.attackEntityFrom(DamageSource.causeMobDamage(owner), MCA.getConfig().guardAttackDamage);
						}
						
						catch (NullPointerException e) //Noticing a crash with the human mob mod.
						{
							reset();
						}
					}

					else if (distanceToTarget > 2.0F && owner.getNavigator().noPath())
					{
						owner.getNavigator().tryMoveToEntityLiving(target, Constants.SPEED_RUN);
					}
				}
			}
		}
	}

	@Override
	public void reset() 
	{
		target = null;
		rangedAttackTime = 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{	
	}

	private void tryAssignTarget()
	{
		List<Entity> possibleTargets = RadixLogic.getAllEntitiesWithinDistanceOfCoordinates(owner.worldObj, owner.posX, owner.posY, owner.posZ, 15);
		double closestDistance = 100.0D;

		for (Entity entity : possibleTargets)
		{
			if (entity instanceof EntityMob && !(entity instanceof EntityCreeper) && owner.canEntityBeSeen(entity))
			{
				double distance = RadixMath.getDistanceToEntity(owner, entity);

				if (distance < closestDistance)
				{
					closestDistance = distance;
					target = (EntityLiving) entity;
				}
			}
		}
	}
}
