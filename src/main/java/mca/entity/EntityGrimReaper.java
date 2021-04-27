package mca.entity;


import cobalt.minecraft.network.datasync.CDataManager;
import cobalt.minecraft.network.datasync.CIntegerParameter;
import mca.core.MCA;
import mca.core.minecraft.SoundsMCA;
import mca.enums.EnumReaperAttackState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

import java.util.function.Predicate;

public class EntityGrimReaper extends CreatureEntity {
    public final CDataManager data = new CDataManager(this);

    private final CIntegerParameter attackStage = data.newInteger("attackStage");
    private final CIntegerParameter stateTransitionCooldown = data.newInteger("stateTransitionCooldown");

    private final ServerBossInfo bossInfo = (ServerBossInfo) (new ServerBossInfo(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenScreen(true);
    private int healingCooldown;
    private int timesHealed;

    private float floatingTicks;

    private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = (p_213797_0_) -> p_213797_0_.getMobType() != CreatureAttribute.UNDEAD && p_213797_0_.attackable();
    private static final EntityPredicate TARGETING_CONDITIONS = (new EntityPredicate()).range(20.0D).selector(LIVING_ENTITY_SELECTOR);

    public EntityGrimReaper(EntityType<? extends EntityGrimReaper> type, World world) {
        super(type, world);

//        setSize(1.0F, 2.6F);
        this.xpReward = 100;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, MobEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        //TODO this list is hardcoded somehwere in GlobalEntityTypeAttributes
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 225.0F)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.30F)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingLvl, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, lootingLvl, hitByPlayer);
        ItemEntity itementity = spawnAtLocation(MCA.ITEM_STAFF_OF_LIFE.get());
        if (itementity != null) {
            itementity.setExtendedLifetime();
        }
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        data.register();
    }

    public EnumReaperAttackState getAttackState() {
        return EnumReaperAttackState.fromId(this.attackStage.get());
    }

    public void setAttackState(EnumReaperAttackState state) {
        // Only update if needed so that sounds only play once.
        if (this.attackStage.get() != state.getId()) {
            this.attackStage.set(state.getId());

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

//    @Override
//    public boolean attackEntityFrom(DamageSource source, float damage) {
//        bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
//
//        // Ignore wall damage and fire damage.
//        if (source == DamageSource.IN_WALL || source == DamageSource.ON_FIRE || source.isExplosion() || source == DamageSource.IN_FIRE) {
//            // Teleport out of any walls we may end up in.
//            if (source == DamageSource.IN_WALL) {
//                teleportTo(this.posX, this.posY + 3, this.posZ);
//            }
//
//            return false;
//        }
//
//        // Ignore damage when blocking, and teleport behind the player when they attempt to block.
//        else if (!world.isRemote && this.getAttackState() == EnumReaperAttackState.BLOCK && source.getImmediateSource() instanceof CPlayer) {
//            CPlayer player = (CPlayer) source.getImmediateSource();
//
//            double deltaX = this.posX - player.posX;
//            double deltaZ = this.posZ - player.posZ;
//
//            this.playSound(SoundsMCA.reaper_block, 1.0F, 1.0F);
//            teleportTo(player.posX - (deltaX * 2), player.posY + 2, this.posZ - (deltaZ * 2));
//            setStateTransitionCooldown(0);
//            return false;
//        }
//
//        // Randomly portal behind the player who just attacked.
//        else if (!world.isRemote && source.getImmediateSource() instanceof CPlayer && rand.nextFloat() >= 0.30F) {
//            CPlayer player = (CPlayer) source.getImmediateSource();
//
//            double deltaX = this.posX - player.posX;
//            double deltaZ = this.posZ - player.posZ;
//
//            teleportTo(player.posX - (deltaX * 2), player.posY + 2, this.posZ - (deltaZ * 2));
//        }
//
//        // Teleport behind the player who fired an arrow and ignore its damage.
//        else if (source.getImmediateSource() instanceof EntityArrow) {
//            EntityArrow arrow = (EntityArrow) source.getImmediateSource();
//
//            if (arrow.shootingEntity instanceof CPlayer && getAttackState() != EnumReaperAttackState.REST) {
//                CPlayer player = (CPlayer) arrow.shootingEntity;
//                double newX = player.posX + rand.nextFloat() >= 0.50F ? 2 : -2;
//                double newZ = player.posZ + rand.nextFloat() >= 0.50F ? 2 : -2;
//
//                teleportTo(newX, player.posY, newZ);
//            }
//
//            arrow.setDead();
//            return false;
//        }
//
//        // Still take damage when healing, but reduced by a third.
//        else if (this.getAttackState() == EnumReaperAttackState.REST) {
//            damage /= 3;
//        }
//
//        super.attackEntityFrom(source, damage);
//
//        if (!world.isRemote && this.getHealth() <= (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2) && healingCooldown == 0) {
//            setAttackState(EnumReaperAttackState.REST);
//            healingCooldown = 4200; // 3 minutes 30 seconds
//            teleportTo(this.posX, this.posY + 8, this.posZ);
//            setStateTransitionCooldown(1200); // 1 minute
//        }
//
//        return true;
//    }
//
//    protected void attackEntity(Entity entity, float damage) {
//        LivingEntity entityToAttack = this.getAttackTarget();
//        if (entityToAttack == null) return;
//
//        // Set attack state to post attack.
//        // If we're blocking, we will teleport away instead of attacking to prevent an unfair attack.
//        // Attacking us WHILE we're blocking will cause us to attack, however.
//        if (this.getDistance(entityToAttack) <= 0.8D && getAttackState() == EnumReaperAttackState.PRE) {
//            if (getAttackState() == EnumReaperAttackState.BLOCK) {
//                int rX = this.getRNG().nextInt(10);
//                int rZ = this.getRNG().nextInt(10);
//                teleportTo(this.posX + 5 + rX, this.posY, this.posZ + rZ);
//            } else {
//                entity.attackEntityFrom(DamageSource.causeMobDamage(this), this.world.getDifficulty().getDifficultyId() * 5.75F);
//
//                if (entity instanceof LivingEntity) {
//                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(MobEffects.WITHER, this.world.getDifficulty().getDifficultyId() * 20, 1));
//                }
//
//                setAttackState(EnumReaperAttackState.POST);
//                setStateTransitionCooldown(10); // For preventing immediate return to the PRE or IDLE stage. Ticked down in onUpdate()
//            }
//        }
//
//        // Check if we're waiting for cooldown from the last attack.
//        if (getStateTransitionCooldown() == 0) {
//            // Within 3 blocks from the target, ready the scythe
//            if (getDistance(entityToAttack) <= 3.5D) {
//                // Check to see if the player's blocking, then teleport behind them.
//                // Also randomly swap their selected item with something else in the hotbar and apply blindness.
//                if (entityToAttack instanceof CPlayer) {
//                    CPlayer player = (CPlayer) entityToAttack;
//
//                    if (player.isActiveItemStackBlocking()) {
//                        double dX = this.posX - player.posX;
//                        double dZ = this.posZ - player.posZ;
//
//                        teleportTo(player.posX - (dX * 2), player.posY + 2, this.posZ - (dZ * 2));
//
//                        if (!world.isRemote && rand.nextFloat() >= 0.20F) {
//                            int currentItem = player.inventory.currentItem;
//                            int randomItem = rand.nextInt(InventoryPlayer.getHotbarSize());
//                            ItemStack currentItemStack = player.inventory.mainInventory.get(currentItem);
//                            ItemStack randomItemStack = player.inventory.mainInventory.get(randomItem);
//
//                            player.inventory.mainInventory.set(currentItem, randomItemStack);
//                            player.inventory.mainInventory.set(randomItem, currentItemStack);
//
//                            player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, this.world.getDifficulty().getDifficultyId() * 40, 1));
//                        }
//                    } else // If the player is not blocking, ready the scythe, or randomly block their attack.
//                    {
//                        // Don't block if we've already committed to an attack.
//                        if (rand.nextFloat() >= 40.0F && getAttackState() != EnumReaperAttackState.PRE) {
//                            setStateTransitionCooldown(20);
//                            setAttackState(EnumReaperAttackState.BLOCK);
//                        } else {
//                            setAttackState(EnumReaperAttackState.PRE);
//                            setStateTransitionCooldown(20);
//                        }
//                    }
//                }
//            } else // Reset the attacking state when we're more than 3 blocks away.
//            {
//                setAttackState(EnumReaperAttackState.IDLE);
//            }
//        }
//    }

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
        return SoundEvents.WITHER_HURT;
    }

//    @Override
//    public void tick() {
//        super.tick();
//        clearFire(); // No fire.
//
//        if (!MCA.getConfig().allowGrimReaper) {
//            remove();
//        }
//
//        LivingEntity entityToAttack = this.getAttackTarget();
//
//        if (entityToAttack != null && getAttackState() != EnumReaperAttackState.REST) {
//            attackEntity(entityToAttack, 5.0F);
//            this.getMoveHelper().setMoveTo(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, 6.0F);
//        }
//
//        // Increment floating ticks on the client when resting.
//        if (world.isRemote && getAttackState() == EnumReaperAttackState.REST) {
//            floatingTicks += 0.1F;
//        }
//
//        // Increase health when resting and check to stop rest state.
//        // Runs on common to spawn lightning.
//        if (getAttackState() == EnumReaperAttackState.REST) {
//            if (!world.isRemote && getStateTransitionCooldown() == 1) {
//                setAttackState(EnumReaperAttackState.IDLE);
//                timesHealed++;
//            } else if (!world.isRemote && getStateTransitionCooldown() % 100 == 0) {
//                this.setHealth(this.getHealth() + MathHelper.clamp(10.5F - (timesHealed * 3.5F), 3.0F, 10.5F));
//
//                // Let's have a light show.
//                int dX = rand.nextInt(8) + 4 * rand.nextFloat() >= 0.50F ? 1 : -1;
//                int dZ = rand.nextInt(8) + 4 * rand.nextFloat() >= 0.50F ? 1 : -1;
//                int y = Util.getSpawnSafeTopLevel(world, (int) posX + dX, 256, (int) posZ + dZ);
//
//                EntityLightningBolt bolt = new EntityLightningBolt(world, dX, y, dZ, false);
//                world.addWeatherEffect(bolt);
//
//                // Also spawn a random skeleton or zombie.
//                if (!world.isRemote) {
//                    EntityMob mob = rand.nextFloat() >= 0.50F ? new EntityZombie(world) : new EntitySkeleton(world);
//                    mob.setPosition(posX + dX + 4, y, posZ + dZ + 4);
//
//                    if (mob instanceof EntitySkeleton) {
//                        mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
//                    }
//
//                    world.spawnEntity(mob);
//                }
//            }
//        }
//
//        // Prevent flying off into oblivion on death...
//        if (this.getHealth() <= 0.0F) {
//            motionX = 0;
//            motionY = 0;
//            motionZ = 0;
//            return;
//        }
//
//        // Stop at our current position if resting
//        if (getAttackState() == EnumReaperAttackState.REST) {
//            motionX = 0;
//            motionY = 0;
//            motionZ = 0;
//        }
//
//        // Logic for flying.
//        fallDistance = 0.0F;
//
//        if (motionY > 0) {
//            motionY = motionY * 1.04F;
//        } else {
//            double yMod = Math.sqrt((motionX * motionX) + (motionZ * motionZ));
//            motionY = motionY * 0.6F + yMod * 0.3F;
//        }
//
//        // Tick down cooldowns.
//        if (getStateTransitionCooldown() > 0) {
//            setStateTransitionCooldown(getStateTransitionCooldown() - 1);
//        }
//
//        if (healingCooldown > 0) {
//            healingCooldown--;
//        }
//
//        // See if our entity to attack has died at any point.
//        if (entityToAttack != null && entityToAttack.isDead) {
//            this.setAttackTarget(null);
//            setAttackState(EnumReaperAttackState.IDLE);
//        }
//
//        // Move towards target if we're not resting
//        if (entityToAttack != null && getAttackState() != EnumReaperAttackState.REST) {
//            // If we have a creature to attack, we need to move downwards if we're above it, and vice-versa.
//            double sqDistanceTo = Math.sqrt(Math.pow(entityToAttack.posX - posX, 2) + Math.pow(entityToAttack.posZ - posZ, 2));
//            float moveAmount = 0.0F;
//
//            if (sqDistanceTo < 8F) {
//                moveAmount = MathHelper.clamp(((8F - (float) sqDistanceTo) / 8F) * 4F, 0, 2.5F);
//            }
//
//            if (entityToAttack.posY + 0.2F < posY) {
//                motionY = motionY - 0.05F * moveAmount;
//            }
//
//            if (entityToAttack.posY - 0.5F > posY) {
//                motionY = motionY + 0.01F * moveAmount;
//            }
//        }
//    }

    //    @Override
    public ITextComponent setCustomName() {
        return new StringTextComponent("Grim Reaper");
    }

    public int getStateTransitionCooldown() {
        return this.stateTransitionCooldown.get();
    }

    public void setStateTransitionCooldown(int value) {
        this.stateTransitionCooldown.set(value);
    }

    public float getFloatingTicks() {
        return floatingTicks;
    }

    public void teleport(double x, double y, double z) {
        if (level.isClientSide) {
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            this.randomTeleport(x, y, z, true);
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
    }
}