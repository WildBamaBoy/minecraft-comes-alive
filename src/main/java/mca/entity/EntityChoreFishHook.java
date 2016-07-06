package mca.entity;

import java.util.List;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mca.ai.AIFishing;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityChoreFishHook extends EntityFishHook implements IEntityAdditionalSpawnData
{
	private int ticksInAir;
	private int ticksInGround;
	private int ticksCatchable;
	private int fishPosRotationIncrements;
	private boolean inGround;
	private double fishX;
	private double fishY;
	private double fishZ;
	private double fishYaw;
	private double fishPitch;

	public EntityHuman angler;

	@SideOnly(Side.CLIENT)
	private double velocityX;
	@SideOnly(Side.CLIENT)
	private double velocityY;
	@SideOnly(Side.CLIENT)
	private double velocityZ;

	public EntityChoreFishHook(World world)
	{
		super(world);
	}

	public EntityChoreFishHook(World world, EntityHuman owner)
	{
		super(world);

		angler = owner;
		setSize(0.25F, 0.25F);
		setLocationAndAngles(angler.posX, angler.posY + 1.62D - angler.yOffset, angler.posZ, angler.rotationYaw, angler.rotationPitch);

		posX -= MathHelper.cos(rotationYaw / 180F * (float) Math.PI) * 0.16F;
		posY -= 0.1D;
		posZ -= MathHelper.sin(rotationYaw / 180F * (float) Math.PI) * 0.16F;
		yOffset = 0.0F;
		setPosition(posX, posY, posZ);

		final float f = 0.4F;
		motionX = -MathHelper.sin(rotationYaw / 180F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180F * (float) Math.PI) * f;
		motionZ = MathHelper.cos(rotationYaw / 180F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180F * (float) Math.PI) * f;
		motionY = -MathHelper.sin(rotationPitch / 180F * (float) Math.PI) * f;

		func_146035_c(motionX, motionY, motionZ, 1.5F, 1.0F);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		onEntityUpdate();

		if (fishPosRotationIncrements > 0)
		{
			final double var21 = posX + (fishX - posX) / fishPosRotationIncrements;
			final double var22 = posY + (fishY - posY) / fishPosRotationIncrements;
			final double var23 = posZ + (fishZ - posZ) / fishPosRotationIncrements;
			final double var7 = MathHelper.wrapAngleTo180_double(fishYaw - rotationYaw);
			rotationYaw = (float) (rotationYaw + var7 / fishPosRotationIncrements);
			rotationPitch = (float) (rotationPitch + (fishPitch - rotationPitch) / fishPosRotationIncrements);
			--fishPosRotationIncrements;
			setPosition(var21, var22, var23);
			setRotation(rotationYaw, rotationPitch);
		}

		else
		{
			if (!worldObj.isRemote)
			{
				if (angler != null)
				{
					final ItemStack itemStack = angler.getHeldItem();

					if (angler.isDead || !angler.isEntityAlive() || itemStack == null || itemStack.getItem() != Items.fishing_rod || getDistanceSqToEntity(angler) > 1024.0D)
					{
						setDead();
						angler.getAI(AIFishing.class).setHookEntity(null);
						return;
					}
				}

				else
				{
					setDead();
				}

				if (field_146043_c != null)
				{
					if (!field_146043_c.isDead)
					{
						posX = field_146043_c.posX;
						posY = field_146043_c.boundingBox.minY + field_146043_c.height * 0.8D;
						posZ = field_146043_c.posZ;
						return;
					}

					field_146043_c = null;
				}

				if (this != null)
				{
					if (isDead && field_146043_c != null)
					{
						field_146043_c.setDead();
					}
				}
			}

			if (field_146044_a > 0)
			{
				--field_146044_a;
			}

			if (this.inGround)
			{
				++this.ticksInGround;

				if (this.ticksInGround >= 20)
				{
					this.setDead();

					try
					{
						angler.getAI(AIFishing.class).setHookEntity(null);
						angler.getAI(AIFishing.class).resetFishTargetPosition();
					}

					catch (NullPointerException e)
					{
						return;
					}
				}

				return;
			}

			else
			{
				++this.ticksInAir;
			}

			Vec3 vector = Vec3.createVectorHelper(posX, posY, posZ);
			Vec3 motionVector = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
			MovingObjectPosition movingObjectPosition = worldObj.rayTraceBlocks(vector, motionVector);
			vector = Vec3.createVectorHelper(posX, posY, posZ);
			motionVector = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);

			if (movingObjectPosition != null)
			{
				motionVector = Vec3.createVectorHelper(movingObjectPosition.hitVec.xCoord, movingObjectPosition.hitVec.yCoord, movingObjectPosition.hitVec.zCoord);
			}

			Entity entity = null;
			final List entityList = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
			double zeroDistance = 0.0D;
			double vectorDistance;

			for (int i = 0; i < entityList.size(); ++i)
			{
				final Entity entityInList = (Entity) entityList.get(i);

				if (entityInList.canBeCollidedWith() && (entityInList != angler || ticksInAir >= 5))
				{
					final float boundingBoxExpansion = 0.3F;
					final AxisAlignedBB bb = entityInList.boundingBox.expand(boundingBoxExpansion, boundingBoxExpansion, boundingBoxExpansion);
					final MovingObjectPosition interceptPosition = bb.calculateIntercept(vector, motionVector);

					if (interceptPosition != null)
					{
						vectorDistance = vector.distanceTo(interceptPosition.hitVec);

						if (vectorDistance < zeroDistance || zeroDistance == 0.0D)
						{
							entity = entityInList;
							zeroDistance = vectorDistance;
						}
					}
				}
			}

			if (entity != null)
			{
				movingObjectPosition = new MovingObjectPosition(entity);
			}

			if (movingObjectPosition != null)
			{
				if (movingObjectPosition.entityHit != null)
				{
					if (movingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, angler), 0))
					{
						field_146043_c = movingObjectPosition.entityHit;
					}
				}
				else
				{
					inGround = true;
				}
			}

			if (!inGround)
			{
				moveEntity(motionX, motionY, motionZ);
				final float motion = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
				rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);

				for (rotationPitch = (float) (Math.atan2(motionY, motion) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
				{
					;
				}

				while (rotationPitch - prevRotationPitch >= 180.0F)
				{
					prevRotationPitch += 360.0F;
				}

				while (rotationYaw - prevRotationYaw < -180.0F)
				{
					prevRotationYaw -= 360.0F;
				}

				while (rotationYaw - prevRotationYaw >= 180.0F)
				{
					prevRotationYaw += 360.0F;
				}

				rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
				rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
				float motionModifier = 0.92F;

				if (onGround || isCollidedHorizontally)
				{
					motionModifier = 0.5F;
				}

				final byte endByte = 5;
				double yMotion = 0.0D;

				for (int i = 0; i < endByte; ++i)
				{
					final double boundingBoxMinY = boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * (i + 0) / endByte - 0.125D + 0.125D;
					final double boundingBoxMaxY = boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * (i + 1) / endByte - 0.125D + 0.125D;
					final AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(boundingBox.minX, boundingBoxMinY, boundingBox.minZ, boundingBox.maxX, boundingBoxMaxY, boundingBox.maxZ);

					if (worldObj.isAABBInMaterial(bb, Material.water))
					{
						yMotion += 1.0D / endByte;
					}
				}

				if (yMotion > 0.0D)
				{
					if (ticksCatchable > 0)
					{
						--ticksCatchable;

						if (rand.nextInt(10) == 0)
						{
							if (!worldObj.isRemote)
							{
								angler.getAI(AIFishing.class).doFishCatchAttempt();
								angler.getAI(AIFishing.class).setHookEntity(null);
								setDead();
								ticksCatchable = 0;
								angler.swingItem();
							}
						}
					}
					else
					{
						short fishCatchChance = 500;

						if (worldObj.canLightningStrikeAt(MathHelper.floor_double(posX), MathHelper.floor_double(posY) + 1, MathHelper.floor_double(posZ)))
						{
							fishCatchChance = 300;
						}

						if (rand.nextInt(fishCatchChance) == 0)
						{
							ticksCatchable = rand.nextInt(30) + 10;
							motionY -= 0.20000000298023224D;
							playSound("random.splash", 0.25F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);

							final float floorMinY = MathHelper.floor_double(boundingBox.minY);
							int i;
							float randomFloat1;
							float randomFloat2;

							for (i = 0; i < 1.0F + width * 20.0F; ++i)
							{
								randomFloat1 = (rand.nextFloat() * 2.0F - 1.0F) * width;
								randomFloat2 = (rand.nextFloat() * 2.0F - 1.0F) * width;
								worldObj.spawnParticle("bubble", posX + randomFloat1, floorMinY + 1.0F, posZ + randomFloat2, motionX, motionY - rand.nextFloat() * 0.2F, motionZ);
							}

							for (i = 0; i < 1.0F + width * 20.0F; ++i)
							{
								randomFloat1 = (rand.nextFloat() * 2.0F - 1.0F) * width;
								randomFloat2 = (rand.nextFloat() * 2.0F - 1.0F) * width;
								worldObj.spawnParticle("splash", posX + randomFloat1, floorMinY + 1.0F, posZ + randomFloat2, motionX, motionY, motionZ);
							}
						}
					}
				}

				if (ticksCatchable > 0)
				{
					motionY -= rand.nextFloat() * rand.nextFloat() * rand.nextFloat() * 0.2D;
				}

				vectorDistance = yMotion * 2.0D - 1.0D;
				motionY += 0.03999999910593033D * vectorDistance;

				if (yMotion > 0.0D)
				{
					motionModifier = (float) (motionModifier * 0.9D);
					motionY *= 0.8D;
				}

				motionX *= motionModifier;
				motionY *= motionModifier;
				motionZ *= motionModifier;
				setPosition(posX, posY, posZ);
			}
		}

		try
		{
			if (!angler.getAI(AIFishing.class).getIsActive())
			{
				setDead();
			}
		}

		catch (final NullPointerException e)
		{
			return;
		}
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		if (angler != null)
		{
			data.writeInt(angler.getEntityId());
		}
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		try
		{
			final int anglerId = data.readInt();

			angler = (EntityHuman) worldObj.getEntityByID(anglerId);

			if (angler != null)
			{
				angler.getAI(AIFishing.class).setHookEntity(this);
				angler.tasks.taskEntries.clear();
			}
		}

		catch (Throwable e) //When angler is null, probably dead.
		{
			setDead();
		}
	}
}