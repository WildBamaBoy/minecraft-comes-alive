package mca.entity;


import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.SoundsMCA;
import mca.enums.EnumReaperAttackState;
import mca.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

public class EntityGrimReaper extends EntityMob {
    private static final DataParameter<Integer> ATTACK_STATE = EntityDataManager.<Integer>createKey(EntityGrimReaper.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> STATE_TRANSITION_COOLDOWN = EntityDataManager.<Integer>createKey(EntityGrimReaper.class, DataSerializers.VARINT);

    private final BossInfoServer bossInfo = (BossInfoServer) (new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);
    private EntityAINearestAttackableTarget aiNearestAttackableTarget = new EntityAINearestAttackableTarget(this, EntityPlayer.class, true);
    private int healingCooldown;
    private int timesHealed;

    private float floatingTicks;

    public EntityGrimReaper(World world) {
        super(world);
        setSize(1.0F, 2.6F);
        this.experienceValue = 100;

        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, aiNearestAttackableTarget);
    }

    @Override
    protected final void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30F);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.5F);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(225.0F);
    }

    @Override
    protected void dropFewItems(boolean hitByPlayer, int lootingLvl) {
        dropItem(ItemsMCA.STAFF_OF_LIFE, 1);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_STATE, 0);
        this.dataManager.register(STATE_TRANSITION_COOLDOWN, 0);
    }

    public EnumReaperAttackState getAttackState() {
        return EnumReaperAttackState.fromId(this.dataManager.get(ATTACK_STATE));
    }

    public void setAttackState(EnumReaperAttackState state) {
        // Only update if needed so that sounds only play once.
        if (this.dataManager.get(ATTACK_STATE) != state.getId()) {
            this.dataManager.set(ATTACK_STATE, state.getId());

            switch (state) {
                case PRE:
                    this.playSound(SoundsMCA.reaper_scythe_out, 1.0F, 1.0F);
                    break;
                case POST:
                    this.playSound(SoundsMCA.reaper_scythe_swing, 1.0F, 1.0F);
                    break;
            }
        }
    }

    public boolean hasEntityToAttack() {
        return this.getAttackTarget() != null;
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt entity) {
        return;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        // Ignore wall damage and fire damage.
        if (source == DamageSource.IN_WALL || source == DamageSource.ON_FIRE || source.isExplosion() || source == DamageSource.IN_FIRE) {
            // Teleport out of any walls we may end up in.
            if (source == DamageSource.IN_WALL) {
                teleportTo(this.posX, this.posY + 3, this.posZ);
            }

            return false;
        }

        // Ignore damage when blocking, and teleport behind the player when they attempt to block.
        else if (!world.isRemote && this.getAttackState() == EnumReaperAttackState.BLOCK && source.getImmediateSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) source.getImmediateSource();

            double deltaX = this.posX - player.posX;
            double deltaZ = this.posZ - player.posZ;

            this.playSound(SoundsMCA.reaper_block, 1.0F, 1.0F);
            teleportTo(player.posX - (deltaX * 2), player.posY + 2, this.posZ - (deltaZ * 2));
            setStateTransitionCooldown(0);
            return false;
        }

        // Randomly portal behind the player who just attacked.
        else if (!world.isRemote && source.getImmediateSource() instanceof EntityPlayer && rand.nextFloat() >= 0.30F) {
            EntityPlayer player = (EntityPlayer) source.getImmediateSource();

            double deltaX = this.posX - player.posX;
            double deltaZ = this.posZ - player.posZ;

            teleportTo(player.posX - (deltaX * 2), player.posY + 2, this.posZ - (deltaZ * 2));
        }

        // Teleport behind the player who fired an arrow and ignore its damage.
        else if (source.getImmediateSource() instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) source.getImmediateSource();

            if (arrow.shootingEntity instanceof EntityPlayer && getAttackState() != EnumReaperAttackState.REST) {
                EntityPlayer player = (EntityPlayer) arrow.shootingEntity;
                double newX = player.posX + rand.nextFloat() >= 0.50F ? 2 : -2;
                double newZ = player.posZ + rand.nextFloat() >= 0.50F ? 2 : -2;

                teleportTo(newX, player.posY, newZ);
            }

            arrow.setDead();
            return false;
        }

        // Still take damage when healing, but reduced by a third.
        else if (this.getAttackState() == EnumReaperAttackState.REST) {
            damage /= 3;
        }

        super.attackEntityFrom(source, damage);

        if (!world.isRemote && this.getHealth() <= (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2) && healingCooldown == 0) {
            setAttackState(EnumReaperAttackState.REST);
            healingCooldown = 4200; // 3 minutes 30 seconds
            teleportTo(this.posX, this.posY + 8, this.posZ);
            setStateTransitionCooldown(1200); // 1 minute
        }

        return true;
    }

    protected void attackEntity(Entity entity, float damage) {
        EntityLivingBase entityToAttack = this.getAttackTarget();
        if (entityToAttack == null) return;

        // Set attack state to post attack.
        // If we're blocking, we will teleport away instead of attacking to prevent an unfair attack.
        // Attacking us WHILE we're blocking will cause us to attack, however.
        if (this.getDistance(entityToAttack) <= 0.8D && getAttackState() == EnumReaperAttackState.PRE) {
            if (getAttackState() == EnumReaperAttackState.BLOCK) {
                int rX = this.getRNG().nextInt(10);
                int rZ = this.getRNG().nextInt(10);
                teleportTo(this.posX + 5 + rX, this.posY, this.posZ + rZ);
            } else {
                entity.attackEntityFrom(DamageSource.causeMobDamage(this), this.world.getDifficulty().getDifficultyId() * 5.75F);

                if (entity instanceof EntityLivingBase) {
                    ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.WITHER, this.world.getDifficulty().getDifficultyId() * 20, 1));
                }

                setAttackState(EnumReaperAttackState.POST);
                setStateTransitionCooldown(10); // For preventing immediate return to the PRE or IDLE stage. Ticked down in onUpdate()
            }
        }

        // Check if we're waiting for cooldown from the last attack.
        if (getStateTransitionCooldown() == 0) {
            // Within 3 blocks from the target, ready the scythe
            if (getDistance(entityToAttack) <= 3.5D) {
                // Check to see if the player's blocking, then teleport behind them.
                // Also randomly swap their selected item with something else in the hotbar and apply blindness.
                if (entityToAttack instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entityToAttack;

                    if (player.isActiveItemStackBlocking()) {
                        double dX = this.posX - player.posX;
                        double dZ = this.posZ - player.posZ;

                        teleportTo(player.posX - (dX * 2), player.posY + 2, this.posZ - (dZ * 2));

                        if (!world.isRemote && rand.nextFloat() >= 0.20F) {
                            int currentItem = player.inventory.currentItem;
                            int randomItem = rand.nextInt(InventoryPlayer.getHotbarSize());
                            ItemStack currentItemStack = player.inventory.mainInventory.get(currentItem);
                            ItemStack randomItemStack = player.inventory.mainInventory.get(randomItem);

                            player.inventory.mainInventory.set(currentItem, randomItemStack);
                            player.inventory.mainInventory.set(randomItem, currentItemStack);

                            player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, this.world.getDifficulty().getDifficultyId() * 40, 1));
                        }
                    } else // If the player is not blocking, ready the scythe, or randomly block their attack.
                    {
                        // Don't block if we've already committed to an attack.
                        if (rand.nextFloat() >= 40.0F && getAttackState() != EnumReaperAttackState.PRE) {
                            setStateTransitionCooldown(20);
                            setAttackState(EnumReaperAttackState.BLOCK);
                        } else {
                            setAttackState(EnumReaperAttackState.PRE);
                            setStateTransitionCooldown(20);
                        }
                    }
                }
            } else // Reset the attacking state when we're more than 3 blocks away.
            {
                setAttackState(EnumReaperAttackState.IDLE);
            }
        }
    }

    protected Entity findPlayerToAttack() {
        return world.getClosestPlayerToEntity(this, 48.0D);
    }

    @Override
    public int getTalkInterval() {
        return 300;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsMCA.reaper_idle;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsMCA.reaper_death;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITHER_HURT;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        extinguish(); // No fire.

        if (!MCA.getConfig().allowGrimReaper) {
            setDead();
        }

        EntityLivingBase entityToAttack = this.getAttackTarget();

        if (entityToAttack != null && getAttackState() != EnumReaperAttackState.REST) {
            attackEntity(entityToAttack, 5.0F);
            this.getMoveHelper().setMoveTo(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, 6.0F);
        }

        // Increment floating ticks on the client when resting.
        if (world.isRemote && getAttackState() == EnumReaperAttackState.REST) {
            floatingTicks += 0.1F;
        }

        // Increase health when resting and check to stop rest state.
        // Runs on common to spawn lightning.
        if (getAttackState() == EnumReaperAttackState.REST) {
            if (!world.isRemote && getStateTransitionCooldown() == 1) {
                setAttackState(EnumReaperAttackState.IDLE);
                timesHealed++;
            } else if (!world.isRemote && getStateTransitionCooldown() % 100 == 0) {
                this.setHealth(this.getHealth() + MathHelper.clamp(10.5F - (timesHealed * 3.5F), 3.0F, 10.5F));

                // Let's have a light show.
                int dX = rand.nextInt(8) + 4 * rand.nextFloat() >= 0.50F ? 1 : -1;
                int dZ = rand.nextInt(8) + 4 * rand.nextFloat() >= 0.50F ? 1 : -1;
                int y = Util.getSpawnSafeTopLevel(world, (int) posX + dX, 256, (int) posZ + dZ);

                EntityLightningBolt bolt = new EntityLightningBolt(world, dX, y, dZ, false);
                world.addWeatherEffect(bolt);

                // Also spawn a random skeleton or zombie.
                if (!world.isRemote) {
                    EntityMob mob = rand.nextFloat() >= 0.50F ? new EntityZombie(world) : new EntitySkeleton(world);
                    mob.setPosition(posX + dX + 4, y, posZ + dZ + 4);

                    if (mob instanceof EntitySkeleton) {
                        mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                    }

                    world.spawnEntity(mob);
                }
            }
        }

        // Prevent flying off into oblivion on death...
        if (this.getHealth() <= 0.0F) {
            motionX = 0;
            motionY = 0;
            motionZ = 0;
            return;
        }

        // Stop at our current position if resting
        if (getAttackState() == EnumReaperAttackState.REST) {
            motionX = 0;
            motionY = 0;
            motionZ = 0;
        }

        // Logic for flying.
        fallDistance = 0.0F;

        if (motionY > 0) {
            motionY = motionY * 1.04F;
        } else {
            double yMod = Math.sqrt((motionX * motionX) + (motionZ * motionZ));
            motionY = motionY * 0.6F + yMod * 0.3F;
        }

        // Tick down cooldowns.
        if (getStateTransitionCooldown() > 0) {
            setStateTransitionCooldown(getStateTransitionCooldown() - 1);
        }

        if (healingCooldown > 0) {
            healingCooldown--;
        }

        // See if our entity to attack has died at any point.
        if (entityToAttack != null && entityToAttack.isDead) {
            this.setAttackTarget(null);
            setAttackState(EnumReaperAttackState.IDLE);
        }

        // Move towards target if we're not resting
        if (entityToAttack != null && getAttackState() != EnumReaperAttackState.REST) {
            // If we have a creature to attack, we need to move downwards if we're above it, and vice-versa.
            double sqDistanceTo = Math.sqrt(Math.pow(entityToAttack.posX - posX, 2) + Math.pow(entityToAttack.posZ - posZ, 2));
            float moveAmount = 0.0F;

            if (sqDistanceTo < 8F) {
                moveAmount = MathHelper.clamp(((8F - (float) sqDistanceTo) / 8F) * 4F, 0, 2.5F);
            }

            if (entityToAttack.posY + 0.2F < posY) {
                motionY = motionY - 0.05F * moveAmount;
            }

            if (entityToAttack.posY - 0.5F > posY) {
                motionY = motionY + 0.01F * moveAmount;
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
    }

    @Override
    public String getName() {
        return "Grim Reaper";
    }

    @Override
    protected boolean canDespawn() {
        return true;
    }

    public int getStateTransitionCooldown() {
        return this.dataManager.get(STATE_TRANSITION_COOLDOWN);
    }

    public void setStateTransitionCooldown(int value) {
        this.dataManager.set(STATE_TRANSITION_COOLDOWN, value);
    }

    public float getFloatingTicks() {
        return floatingTicks;
    }

    private void teleportTo(double x, double y, double z) {
        if (!world.isRemote) {
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 2.0F, 1.0F);
            this.setPosition(x, y, z);
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 2.0F, 1.0F);
        }
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    /**
     * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in
     * order to view its associated boss bar.
     */
    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    /**
     * Removes the given player from the list of players tracking this entity. See {@link Entity#addTrackingPlayer} for
     * more information on tracking.
     */
    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

}