package mca.entity;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mca.ai.AIFishing;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityChoreFishHook extends EntityFishHook implements IEntityAdditionalSpawnData
{
    private int xTile;
    private int yTile;
    private int zTile;
    private Block inTile;
    private boolean inGround;
    public int shake;
	public EntityHuman angler;
    private int ticksInGround;
    private int ticksInAir;
    private int ticksCatchable;
    private int ticksCaughtDelay;
    private int ticksCatchableDelay;
    private float fishApproachAngle;
    public Entity caughtEntity;
    private int fishPosRotationIncrements;
    private double fishX;
    private double fishY;
    private double fishZ;
    private double fishYaw;
    private double fishPitch;
    @SideOnly(Side.CLIENT)
    private double clientMotionX;
    @SideOnly(Side.CLIENT)
    private double clientMotionY;
    @SideOnly(Side.CLIENT)
    private double clientMotionZ;

	public EntityChoreFishHook(World world)
	{
		super(world);
	}

	public EntityChoreFishHook(World world, EntityHuman owner)
	{
		super(world);

		angler = owner;
		setSize(0.25F, 0.25F);
		setLocationAndAngles(angler.posX, angler.posY + 1.62D - angler.getEyeHeight(), angler.posZ, angler.rotationYaw, angler.rotationPitch);

		posX -= MathHelper.cos(rotationYaw / 180F * (float) Math.PI) * 0.16F;
		posY -= 0.1D;
		posZ -= MathHelper.sin(rotationYaw / 180F * (float) Math.PI) * 0.16F;
		setPosition(posX, posY, posZ);

		final float f = 0.4F;
		motionX = -MathHelper.sin(rotationYaw / 180F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180F * (float) Math.PI) * f;
		motionZ = MathHelper.cos(rotationYaw / 180F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180F * (float) Math.PI) * f;
		motionY = -MathHelper.sin(rotationPitch / 180F * (float) Math.PI) * f;

        this.handleHookCasting(this.motionX, this.motionY, this.motionZ, 1.5F, 1.0F);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
        super.onUpdate();

        if (this.fishPosRotationIncrements > 0)
        {
            double d7 = this.posX + (this.fishX - this.posX) / (double)this.fishPosRotationIncrements;
            double d8 = this.posY + (this.fishY - this.posY) / (double)this.fishPosRotationIncrements;
            double d9 = this.posZ + (this.fishZ - this.posZ) / (double)this.fishPosRotationIncrements;
            double d1 = MathHelper.wrapAngleTo180_double(this.fishYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + d1 / (double)this.fishPosRotationIncrements);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.fishPitch - (double)this.rotationPitch) / (double)this.fishPosRotationIncrements);
            --this.fishPosRotationIncrements;
            this.setPosition(d7, d8, d9);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        else
        {
            if (!this.worldObj.isRemote)
            {
                ItemStack itemstack = this.angler.getHeldItem();

                if (this.angler.isDead || !this.angler.isEntityAlive() || itemstack == null || itemstack.getItem() != Items.fishing_rod || this.getDistanceSqToEntity(this.angler) > 1024.0D)
                {
                    this.setDead();
                    this.angler.getAI(AIFishing.class).setHookEntity(null);
                    return;
                }

                if (this.caughtEntity != null)
                {
                    if (!this.caughtEntity.isDead)
                    {
                        this.posX = this.caughtEntity.posX;
                        double d17 = (double)this.caughtEntity.height;
                        this.posY = this.caughtEntity.getEntityBoundingBox().minY + d17 * 0.8D;
                        this.posZ = this.caughtEntity.posZ;
                        return;
                    }

                    this.caughtEntity = null;
                }
            }

            if (this.shake > 0)
            {
                --this.shake;
            }

            if (this.inGround)
            {
                if (this.worldObj.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile)
                {
                    ++this.ticksInGround;

                    if (this.ticksInGround == 1200)
                    {
                        this.setDead();
                    }

                    return;
                }

                this.inGround = false;
                this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
            else
            {
                ++this.ticksInAir;
            }

            Vec3 vec31 = new Vec3(this.posX, this.posY, this.posZ);
            Vec3 vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec31, vec3);
            vec31 = new Vec3(this.posX, this.posY, this.posZ);
            vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null)
            {
                vec3 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);

                if (entity1.canBeCollidedWith() && (entity1 != this.angler || this.ticksInAir >= 5))
                {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec31, vec3);

                    if (movingobjectposition1 != null)
                    {
                        double d2 = vec31.squareDistanceTo(movingobjectposition1.hitVec);

                        if (d2 < d0 || d0 == 0.0D)
                        {
                            entity = entity1;
                            d0 = d2;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null)
            {
                if (movingobjectposition.entityHit != null)
                {
                    if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.angler), 0.0F))
                    {
                        this.caughtEntity = movingobjectposition.entityHit;
                    }
                }
                else
                {
                    this.inGround = true;
                }
            }

            if (!this.inGround)
            {
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                float f5 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
                this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

                for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f5) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
                {
                    ;
                }

                while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
                {
                    this.prevRotationPitch += 360.0F;
                }

                while (this.rotationYaw - this.prevRotationYaw < -180.0F)
                {
                    this.prevRotationYaw -= 360.0F;
                }

                while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
                {
                    this.prevRotationYaw += 360.0F;
                }

                this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
                this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
                float f6 = 0.92F;

                if (this.onGround || this.isCollidedHorizontally)
                {
                    f6 = 0.5F;
                }

                int j = 5;
                double d10 = 0.0D;

                for (int k = 0; k < j; ++k)
                {
                    AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
                    double d3 = axisalignedbb1.maxY - axisalignedbb1.minY;
                    double d4 = axisalignedbb1.minY + d3 * (double)k / (double)j;
                    double d5 = axisalignedbb1.minY + d3 * (double)(k + 1) / (double)j;
                    AxisAlignedBB axisalignedbb2 = new AxisAlignedBB(axisalignedbb1.minX, d4, axisalignedbb1.minZ, axisalignedbb1.maxX, d5, axisalignedbb1.maxZ);

                    if (this.worldObj.isAABBInMaterial(axisalignedbb2, Material.water))
                    {
                        d10 += 1.0D / (double)j;
                    }
                }

                if (!this.worldObj.isRemote && d10 > 0.0D)
                {
                    WorldServer worldserver = (WorldServer)this.worldObj;
                    int l = 1;
                    BlockPos blockpos = (new BlockPos(this)).up();

                    if (this.rand.nextFloat() < 0.25F && this.worldObj.canLightningStrike(blockpos))
                    {
                        l = 2;
                    }

                    if (this.rand.nextFloat() < 0.5F && !this.worldObj.canSeeSky(blockpos))
                    {
                        --l;
                    }

                    if (this.ticksCatchable > 0)
                    {
                        --this.ticksCatchable;

                        if (this.ticksCatchable <= 0)
                        {
                            this.ticksCaughtDelay = 0;
                            this.ticksCatchableDelay = 0;
                        }
                    }
                    else if (this.ticksCatchableDelay > 0)
                    {
                        this.ticksCatchableDelay -= l;

                        if (this.ticksCatchableDelay <= 0)
                        {
                            this.motionY -= 0.20000000298023224D;
                            this.playSound("random.splash", 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                            float f8 = (float)MathHelper.floor_double(this.getEntityBoundingBox().minY);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, (double)(f8 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D, new int[0]);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, (double)(f8 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D, new int[0]);
                            this.ticksCatchable = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
                        }
                        else
                        {
                            this.fishApproachAngle = (float)((double)this.fishApproachAngle + this.rand.nextGaussian() * 4.0D);
                            float f7 = this.fishApproachAngle * 0.017453292F;
                            float f10 = MathHelper.sin(f7);
                            float f11 = MathHelper.cos(f7);
                            double d13 = this.posX + (double)(f10 * (float)this.ticksCatchableDelay * 0.1F);
                            double d15 = (double)((float)MathHelper.floor_double(this.getEntityBoundingBox().minY) + 1.0F);
                            double d16 = this.posZ + (double)(f11 * (float)this.ticksCatchableDelay * 0.1F);
                            Block block1 = worldserver.getBlockState(new BlockPos((int)d13, (int)d15 - 1, (int)d16)).getBlock();

                            if (block1 == Blocks.water || block1 == Blocks.flowing_water)
                            {
                                if (this.rand.nextFloat() < 0.15F)
                                {
                                    worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, d13, d15 - 0.10000000149011612D, d16, 1, (double)f10, 0.1D, (double)f11, 0.0D, new int[0]);
                                }

                                float f3 = f10 * 0.04F;
                                float f4 = f11 * 0.04F;
                                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d13, d15, d16, 0, (double)f4, 0.01D, (double)(-f3), 1.0D, new int[0]);
                                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d13, d15, d16, 0, (double)(-f4), 0.01D, (double)f3, 1.0D, new int[0]);
                            }
                        }
                    }
                    else if (this.ticksCaughtDelay > 0)
                    {
                        this.ticksCaughtDelay -= l;
                        float f1 = 0.15F;

                        if (this.ticksCaughtDelay < 20)
                        {
                            f1 = (float)((double)f1 + (double)(20 - this.ticksCaughtDelay) * 0.05D);
                        }
                        else if (this.ticksCaughtDelay < 40)
                        {
                            f1 = (float)((double)f1 + (double)(40 - this.ticksCaughtDelay) * 0.02D);
                        }
                        else if (this.ticksCaughtDelay < 60)
                        {
                            f1 = (float)((double)f1 + (double)(60 - this.ticksCaughtDelay) * 0.01D);
                        }

                        if (this.rand.nextFloat() < f1)
                        {
                            float f9 = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F) * 0.017453292F;
                            float f2 = MathHelper.randomFloatClamp(this.rand, 25.0F, 60.0F);
                            double d12 = this.posX + (double)(MathHelper.sin(f9) * f2 * 0.1F);
                            double d14 = (double)((float)MathHelper.floor_double(this.getEntityBoundingBox().minY) + 1.0F);
                            double d6 = this.posZ + (double)(MathHelper.cos(f9) * f2 * 0.1F);
                            Block block = worldserver.getBlockState(new BlockPos((int)d12, (int)d14 - 1, (int)d6)).getBlock();

                            if (block == Blocks.water || block == Blocks.flowing_water)
                            {
                                worldserver.spawnParticle(EnumParticleTypes.WATER_SPLASH, d12, d14, d6, 2 + this.rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D, new int[0]);
                            }
                        }

                        if (this.ticksCaughtDelay <= 0)
                        {
                            this.fishApproachAngle = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F);
                            this.ticksCatchableDelay = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
                        }
                    }
                    else
                    {
                        this.ticksCaughtDelay = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                        this.ticksCaughtDelay -= EnchantmentHelper.getLureModifier(this.angler) * 20 * 5;
                    }

                    if (this.ticksCatchable > 0)
                    {
                        this.motionY -= (double)(this.rand.nextFloat() * this.rand.nextFloat() * this.rand.nextFloat()) * 0.2D;
                    }
                }

                double d11 = d10 * 2.0D - 1.0D;
                this.motionY += 0.03999999910593033D * d11;

                if (d10 > 0.0D)
                {
                    f6 = (float)((double)f6 * 0.9D);
                    this.motionY *= 0.8D;
                }

                this.motionX *= (double)f6;
                this.motionY *= (double)f6;
                this.motionZ *= (double)f6;
                this.setPosition(this.posX, this.posY, this.posZ);
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