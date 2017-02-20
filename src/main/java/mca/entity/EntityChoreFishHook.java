package mca.entity;

import java.util.List;

import mca.ai.AIFishing;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityChoreFishHook extends Entity
{
    private int xTile;
    private int yTile;
    private int zTile;
    private Block inTile;
    private boolean inGround;
    public int shake;
    public EntityVillagerMCA angler;
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

    public EntityChoreFishHook(World worldIn)
    {
        super(worldIn);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
    }

    @SideOnly(Side.CLIENT)
    public EntityChoreFishHook(World worldIn, double x, double y, double z, EntityVillagerMCA anglerIn)
    {
        this(worldIn);
        this.setPosition(x, y, z);
        this.ignoreFrustumCheck = true;
        this.angler = anglerIn;
        angler.getAI(AIFishing.class).setHookEntity(this);
    }

    public EntityChoreFishHook(World worldIn, EntityVillagerMCA anglerIn)
    {
        super(worldIn);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.ignoreFrustumCheck = true;
        this.angler = anglerIn;
        angler.getAI(AIFishing.class).setHookEntity(this);
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(anglerIn.posX, anglerIn.posY + (double)anglerIn.getEyeHeight(), anglerIn.posZ, anglerIn.rotationYaw, anglerIn.rotationPitch);
        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
        float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.handleHookCasting(this.motionX, this.motionY, this.motionZ, 1.5F, 1.0F);
    }

    protected void entityInit()
    {
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     */
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0))
        {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    public void handleHookCasting(double p_146035_1_, double p_146035_3_, double p_146035_5_, float p_146035_7_, float p_146035_8_)
    {
        float f = MathHelper.sqrt(p_146035_1_ * p_146035_1_ + p_146035_3_ * p_146035_3_ + p_146035_5_ * p_146035_5_);
        p_146035_1_ = p_146035_1_ / (double)f;
        p_146035_3_ = p_146035_3_ / (double)f;
        p_146035_5_ = p_146035_5_ / (double)f;
        p_146035_1_ = p_146035_1_ + this.rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_3_ = p_146035_3_ + this.rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_5_ = p_146035_5_ + this.rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_1_ = p_146035_1_ * (double)p_146035_7_;
        p_146035_3_ = p_146035_3_ * (double)p_146035_7_;
        p_146035_5_ = p_146035_5_ * (double)p_146035_7_;
        this.motionX = p_146035_1_;
        this.motionY = p_146035_3_;
        this.motionZ = p_146035_5_;
        float f1 = MathHelper.sqrt(p_146035_1_ * p_146035_1_ + p_146035_5_ * p_146035_5_);
        this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(p_146035_1_, p_146035_5_) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(p_146035_3_, (double)f1) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_)
    {
        this.fishX = x;
        this.fishY = y;
        this.fishZ = z;
        this.fishYaw = (double)yaw;
        this.fishPitch = (double)pitch;
        this.fishPosRotationIncrements = posRotationIncrements;
        this.motionX = this.clientMotionX;
        this.motionY = this.clientMotionY;
        this.motionZ = this.clientMotionZ;
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z)
    {
        this.clientMotionX = this.motionX = x;
        this.clientMotionY = this.motionY = y;
        this.clientMotionZ = this.motionZ = z;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (this.fishPosRotationIncrements > 0)
        {
            double d7 = this.posX + (this.fishX - this.posX) / (double)this.fishPosRotationIncrements;
            double d8 = this.posY + (this.fishY - this.posY) / (double)this.fishPosRotationIncrements;
            double d9 = this.posZ + (this.fishZ - this.posZ) / (double)this.fishPosRotationIncrements;
            double d1 = MathHelper.wrapDegrees(this.fishYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + d1 / (double)this.fishPosRotationIncrements);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.fishPitch - (double)this.rotationPitch) / (double)this.fishPosRotationIncrements);
            --this.fishPosRotationIncrements;
            this.setPosition(d7, d8, d9);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        else
        {
            if (!this.world.isRemote && this.angler != null)
            {
                ItemStack itemstack = this.angler.getHeldItem(EnumHand.MAIN_HAND);

                if (this.angler.isDead || !this.angler.isEntityAlive() || itemstack == null || itemstack.getItem() != Items.FISHING_ROD || this.getDistanceSqToEntity(this.angler) > 1024.0D)
                {
                    this.setDead();
                    angler.getAI(AIFishing.class).setHookEntity(null);
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
                if (this.world.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile)
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

            Vec3d vec31 = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec31, vec3);
            vec31 = new Vec3d(this.posX, this.posY, this.posZ);
            vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null)
            {
                vec3 = new Vec3d(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);

                if (entity1.canBeCollidedWith() && (entity1 != this.angler || this.ticksInAir >= 5))
                {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
                    RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(vec31, vec3);

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
                movingobjectposition = new RayTraceResult(entity);
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
                move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                float f5 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
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

                    if (this.world.isMaterialInBB(axisalignedbb2, Material.WATER))
                    {
                        d10 += 1.0D / (double)j;
                    }
                }

                if (!this.world.isRemote && d10 > 0.0D)
                {
                    WorldServer worldserver = (WorldServer)this.world;
                    int l = 1;
                    BlockPos blockpos = (new BlockPos(this)).up();

                    if (this.rand.nextFloat() < 0.25F && this.world.isRainingAt(blockpos))
                    {
                        l = 2;
                    }

                    if (this.rand.nextFloat() < 0.5F && !this.world.canSeeSky(blockpos))
                    {
                        --l;
                    }

                    if (this.ticksCatchable > 0)
                    {
                        --this.ticksCatchable;

						if (rand.nextInt(10) == 0)
						{
							if (!world.isRemote)
							{
								angler.getAI(AIFishing.class).doFishCatchAttempt();
								angler.getAI(AIFishing.class).setHookEntity(null);
								setDead();
								ticksCatchable = 0;
								angler.swingItem();
							}
						}
                    }
                    else if (this.ticksCatchableDelay > 0)
                    {
                        this.ticksCatchableDelay -= l;

                        if (this.ticksCatchableDelay <= 0)
                        {
                            this.motionY -= 0.20000000298023224D;
                            this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                            float f8 = (float)MathHelper.floor(this.getEntityBoundingBox().minY);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, (double)(f8 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D, new int[0]);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, (double)(f8 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D, new int[0]);
                            this.ticksCatchable = MathHelper.getInt(this.rand, 10, 30);
                        }
                        else
                        {
                            this.fishApproachAngle = (float)((double)this.fishApproachAngle + this.rand.nextGaussian() * 4.0D);
                            float f7 = this.fishApproachAngle * 0.017453292F;
                            float f10 = MathHelper.sin(f7);
                            float f11 = MathHelper.cos(f7);
                            double d13 = this.posX + (double)(f10 * (float)this.ticksCatchableDelay * 0.1F);
                            double d15 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                            double d16 = this.posZ + (double)(f11 * (float)this.ticksCatchableDelay * 0.1F);
                            Block block1 = worldserver.getBlockState(new BlockPos((int)d13, (int)d15 - 1, (int)d16)).getBlock();

                            if (block1 == Blocks.WATER || block1 == Blocks.FLOWING_WATER)
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
                            float f9 = MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * 0.017453292F;
                            float f2 = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
                            double d12 = this.posX + (double)(MathHelper.sin(f9) * f2 * 0.1F);
                            double d14 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                            double d6 = this.posZ + (double)(MathHelper.cos(f9) * f2 * 0.1F);
                            Block block = worldserver.getBlockState(new BlockPos((int)d12, (int)d14 - 1, (int)d6)).getBlock();

                            if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                            {
                                worldserver.spawnParticle(EnumParticleTypes.WATER_SPLASH, d12, d14, d6, 2 + this.rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D, new int[0]);
                            }
                        }

                        if (this.ticksCaughtDelay <= 0)
                        {
                            this.fishApproachAngle = MathHelper.nextFloat(this.rand, 0.0F, 360.0F);
                            this.ticksCatchableDelay = MathHelper.getInt(this.rand, 20, 80);
                        }
                    }
                    else
                    {
                        this.ticksCaughtDelay = MathHelper.getInt(this.rand, 100, 900);
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
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setShort("xTile", (short)this.xTile);
        tagCompound.setShort("yTile", (short)this.yTile);
        tagCompound.setShort("zTile", (short)this.zTile);
        ResourceLocation resourcelocation = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
        tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
        tagCompound.setByte("shake", (byte)this.shake);
        tagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        this.xTile = tagCompund.getShort("xTile");
        this.yTile = tagCompund.getShort("yTile");
        this.zTile = tagCompund.getShort("zTile");

        if (tagCompund.hasKey("inTile", 8))
        {
            this.inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
        }
        else
        {
            this.inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
        }

        this.shake = tagCompund.getByte("shake") & 255;
        this.inGround = tagCompund.getByte("inGround") == 1;
    }

    public int handleHookRetraction()
    {
        if (this.world.isRemote)
        {
            return 0;
        }
        else
        {
            int i = 0;

            if (this.caughtEntity != null)
            {
                double d0 = this.angler.posX - this.posX;
                double d2 = this.angler.posY - this.posY;
                double d4 = this.angler.posZ - this.posZ;
                double d6 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2 + d4 * d4);
                double d8 = 0.1D;
                this.caughtEntity.motionX += d0 * d8;
                this.caughtEntity.motionY += d2 * d8 + (double)MathHelper.sqrt(d6) * 0.08D;
                this.caughtEntity.motionZ += d4 * d8;
                i = 3;
            }
            else if (this.ticksCatchable > 0)
            {
                EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY, this.posZ, this.getFishingResult());
                double d1 = this.angler.posX - this.posX;
                double d3 = this.angler.posY - this.posY;
                double d5 = this.angler.posZ - this.posZ;
                double d7 = (double)MathHelper.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
                double d9 = 0.1D;
                entityitem.motionX = d1 * d9;
                entityitem.motionY = d3 * d9 + (double)MathHelper.sqrt(d7) * 0.08D;
                entityitem.motionZ = d5 * d9;
                this.world.spawnEntity(entityitem);
                this.angler.world.spawnEntity(new EntityXPOrb(this.angler.world, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, this.rand.nextInt(6) + 1));
                i = 1;
            }

            if (this.inGround)
            {
                i = 2;
            }

            this.setDead();
            angler.getAI(AIFishing.class).setHookEntity(null);
            return i;
        }
    }

    @SuppressWarnings("unused")
    private ItemStack getFishingResult()
    {
        return null;
    }

    /**
     * Will get destroyed next tick.
     */
    public void setDead()
    {
        super.setDead();

        if (this.angler != null)
        {
            angler.getAI(AIFishing.class).setHookEntity(null);
        }
    }
}