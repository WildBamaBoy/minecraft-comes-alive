package mca.ai;

import java.util.List;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import radixcore.constant.Time;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class AIDefend extends AbstractAI
{
	private static final int TARGET_SEARCH_INTERVAL = Time.SECOND * 1;

	private EntityLiving target;
	private int timeUntilTargetSearch;
	private int rangedAttackTime;

	public AIDefend(EntityVillagerMCA owner) 
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
		
		if (owner.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard && !owner.getIsInfected())
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
						attackEntityWithRangedAttack(owner, target, 12F);
						owner.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (owner.getRNG().nextFloat() * 0.4F + 0.8F));
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
	
	private void attackEntityWithRangedAttack(EntityVillagerMCA shooter, EntityLivingBase target, float velocity)
	{
		EntityArrow entityarrow = new EntityTippedArrow(shooter.worldObj, shooter);
		double d0 = target.posX - shooter.posX;
		double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entityarrow.posY;
		double d2 = target.posZ - shooter.posZ;
		double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
		entityarrow.setThrowableHeading(d0, d1 + d3 * 0.2D, d2, 1.6F, (float)(14 - shooter.worldObj.getDifficulty().getDifficultyId() * 4));
		int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, shooter);
		int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, shooter);
		entityarrow.setDamage((double)(velocity * 2.0F) + shooter.getRNG().nextGaussian() * 0.25D + (double)((float)shooter.worldObj.getDifficulty().getDifficultyId() * 0.11F));

		if (i > 0)
		{
			entityarrow.setDamage(entityarrow.getDamage() + (double)i * 0.5D + 0.5D);
		}

		if (j > 0)
		{
			entityarrow.setKnockbackStrength(j);
		}

		shooter.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (shooter.getRNG().nextFloat() * 0.4F + 0.8F));
		shooter.worldObj.spawnEntityInWorld(entityarrow);
	}
}
