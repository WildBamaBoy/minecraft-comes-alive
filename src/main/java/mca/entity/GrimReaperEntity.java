package mca.entity;


import mca.api.cobalt.minecraft.network.datasync.CDataManager;
import mca.api.cobalt.minecraft.network.datasync.CIntegerParameter;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.SoundsMCA;
import mca.entity.ai.GrimReaperIdleGoal;
import mca.entity.ai.GrimReaperMeleeGoal;
import mca.entity.ai.GrimReaperRestGoal;
import mca.entity.ai.GrimReaperTargetGoal;
import mca.enums.ReaperAttackState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

public class GrimReaperEntity extends CreatureEntity {
    public final CDataManager data = new CDataManager(this);

    private final CIntegerParameter attackStage = data.newInteger("attackStage");

    private final ServerBossInfo bossInfo = (ServerBossInfo) (new ServerBossInfo(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenScreen(true);

    public GrimReaperEntity(EntityType<? extends GrimReaperEntity> type, World world) {
        super(type, world);

        this.xpReward = 100;

        data.register();
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 225.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.30F)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public CreatureAttribute getMobType() {
        return CreatureAttribute.UNDEAD;
    }

    @Override
    protected void registerGoals() {
        //TODO seems to be ignored
        this.goalSelector.addGoal(5, new GrimReaperIdleGoal(this, 1.0D));

        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));

        this.goalSelector.addGoal(2, new GrimReaperRestGoal(this));
        this.goalSelector.addGoal(4, new GrimReaperMeleeGoal(this));

        this.targetSelector.addGoal(2, new GrimReaperTargetGoal(this));
    }

    @Override
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.remove();
        }
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    protected PathNavigator createNavigation(World world) {
        FlyingPathNavigator flyingpathnavigator = new FlyingPathNavigator(this, world) {
            public boolean isStableDestination(BlockPos p_188555_1_) {
                return true;
            }
        };
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanFloat(false);
        flyingpathnavigator.setCanPassDoors(true);
        return flyingpathnavigator;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingLvl, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, lootingLvl, hitByPlayer);
        ItemEntity itementity = spawnAtLocation(ItemsMCA.STAFF_OF_LIFE.get());
        if (itementity != null) {
            itementity.setExtendedLifetime();
        }
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    public ReaperAttackState getAttackState() {
        return ReaperAttackState.fromId(this.attackStage.get());
    }

    public void setAttackState(ReaperAttackState state) {
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


    @Override
    public boolean hurt(DamageSource source, float damage) {
        // Ignore wall damage, fire and explosion damage
        if (source == DamageSource.IN_WALL || source == DamageSource.ON_FIRE || source.isExplosion() || source == DamageSource.IN_FIRE) {
            // Teleport out of any walls we may end up in
            if (source == DamageSource.IN_WALL) {
                teleportTo(this.getX(), this.getY() + 3, this.getZ());
            }
            return false;
        }

        Entity attacker = source.getDirectEntity();

        // Ignore damage when blocking, and teleport behind the attacker when attacked
        if (!level.isClientSide && this.getAttackState() == ReaperAttackState.BLOCK && attacker != null) {

            double deltaX = this.getX() - attacker.getX();
            double deltaZ = this.getZ() - attacker.getZ();

            this.playSound(SoundsMCA.reaper_block, 1.0F, 1.0F);
            teleportTo(attacker.getX() - (deltaX * 2), attacker.getY() + 2, this.getZ() - (deltaZ * 2));
            return false;
        }

        // Randomly portal behind the player who just attacked.
        if (!level.isClientSide && random.nextFloat() >= 0.30F && attacker != null) {
            double deltaX = this.getX() - attacker.getX();
            double deltaZ = this.getZ() - attacker.getZ();

            teleportTo(attacker.getX() - (deltaX * 2), attacker.getY() + 2, this.getZ() - (deltaZ * 2));
        }

        // Teleport behind the player who fired an arrow and ignore its damage.
        if (source.getDirectEntity() instanceof ArrowEntity) {
            ArrowEntity arrow = (ArrowEntity) attacker;

            if (arrow != null && getAttackState() != ReaperAttackState.REST) {
                Entity owner = arrow.getOwner();

                if (owner != null) {
                    double newX = owner.getX() + (random.nextFloat() >= 0.50F ? 2 : -2);
                    double newZ = owner.getZ() + (random.nextFloat() >= 0.50F ? 2 : -2);

                    teleportTo(newX, owner.getY(), newZ);
                }
                arrow.remove();
            }
            return false;
        }

        // 25% damage when healing
        if (this.getAttackState() == ReaperAttackState.REST) {
            damage *= 0.25;
        }

        return super.hurt(source, damage);
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
        return SoundEvents.WITHER_HURT;
    }

    @Override
    public void tick() {
        super.tick();
        clearFire(); // No fire.

        //update bossinfo
        bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        if (!MCA.getConfig().allowGrimReaper) {
            remove();
        }

        // Prevent flying off into oblivion on death...
        if (this.getHealth() <= 0.0F) {
            setDeltaMovement(Vector3d.ZERO);
            return;
        }

        // look at the player. Aways.
        PlayerEntity player = level.getNearestPlayer(this, 10.D);
        if (player != null) {
            getLookControl().setLookAt(player.getX(), player.getEyeY(), player.getZ());
        }

        LivingEntity entityToAttack = this.getTarget();

        // See if our entity to attack has died at any point.
        if (entityToAttack != null && entityToAttack.isDeadOrDying()) {
            this.setTarget(null);
            setAttackState(ReaperAttackState.IDLE);
        }

        // Logic for flying.
        fallDistance = 0.0F;
    }

    @Override
    public void teleportTo(double x, double y, double z) {
        if (this.level instanceof ServerWorld) {
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            super.teleportTo(x, y, z);
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
    }

    public void startSeenByPlayer(ServerPlayerEntity p_184178_1_) {
        super.startSeenByPlayer(p_184178_1_);
        this.bossInfo.addPlayer(p_184178_1_);
    }

    public void stopSeenByPlayer(ServerPlayerEntity p_184203_1_) {
        super.stopSeenByPlayer(p_184203_1_);
        this.bossInfo.removePlayer(p_184203_1_);
    }
}