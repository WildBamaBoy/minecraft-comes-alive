package mca.entity;


import mca.cobalt.minecraft.network.datasync.CDataManager;
import mca.cobalt.minecraft.network.datasync.CIntegerParameter;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.SoundsMCA;
import mca.entity.ai.GrimReaperIdleGoal;
import mca.entity.ai.GrimReaperMeleeGoal;
import mca.entity.ai.GrimReaperRestGoal;
import mca.entity.ai.GrimReaperTargetGoal;
import mca.enums.ReaperAttackState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class GrimReaperEntity extends PathAwareEntity {
    public final CDataManager data = new CDataManager(this);

    private final CIntegerParameter attackStage = data.newInteger("attackStage");

    private final ServerBossBar bossInfo = (ServerBossBar) (new ServerBossBar(this.getDisplayName(), BossBar.Color.PURPLE, BossBar.Style.PROGRESS)).setDarkenSky(true);

    public GrimReaperEntity(EntityType<? extends GrimReaperEntity> type, World world) {
        super(type, world);

        this.experiencePoints = 100;

        data.register();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 225.0F)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30F)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0D);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }

    @Override
    protected void initGoals() {
        //TODO seems to be ignored
        this.goalSelector.add(5, new GrimReaperIdleGoal(this, 1.0D));

        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.goalSelector.add(2, new GrimReaperRestGoal(this));
        this.goalSelector.add(4, new GrimReaperMeleeGoal(this));

        this.targetSelector.add(2, new GrimReaperTargetGoal(this));
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.isDisallowedInPeaceful()) {
            this.remove();
        }
    }

    @Override
    protected boolean isDisallowedInPeaceful() {
        return true;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation flyingpathnavigator = new BirdNavigation(this, world) {
            public boolean isValidPosition(BlockPos p_188555_1_) {
                return true;
            }
        };
        flyingpathnavigator.setCanPathThroughDoors(false);
        flyingpathnavigator.setCanSwim(false);
        flyingpathnavigator.setCanEnterOpenDoors(true);
        return flyingpathnavigator;
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingLvl, boolean hitByPlayer) {
        super.dropEquipment(source, lootingLvl, hitByPlayer);
        ItemEntity itementity = dropStack(ItemsMCA.STAFF_OF_LIFE.get());
        if (itementity != null) {
            itementity.setCovetedItem();
        }
    }


    @Override
    protected void initDataTracker() {
        super.initDataTracker();
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
    public boolean damage(DamageSource source, float damage) {
        // Ignore wall damage, fire and explosion damage
        if (source == DamageSource.IN_WALL || source == DamageSource.ON_FIRE || source.isExplosive() || source == DamageSource.IN_FIRE) {
            // Teleport out of any walls we may end up in
            if (source == DamageSource.IN_WALL) {
                requestTeleport(this.offsetX(), this.getBodyY() + 3, this.offsetZ());
            }
            return false;
        }

        Entity attacker = source.getSource();

        // Ignore damage when blocking, and teleport behind the attacker when attacked
        if (!world.isClient && this.getAttackState() == ReaperAttackState.BLOCK && attacker != null) {

            double deltaX = this.offsetX() - attacker.offsetX();
            double deltaZ = this.offsetZ() - attacker.offsetZ();

            this.playSound(SoundsMCA.reaper_block, 1.0F, 1.0F);
            requestTeleport(attacker.offsetX() - (deltaX * 2), attacker.getBodyY() + 2, this.offsetZ() - (deltaZ * 2));
            return false;
        }

        // Randomly portal behind the player who just attacked.
        if (!world.isClient && random.nextFloat() >= 0.30F && attacker != null) {
            double deltaX = this.offsetX() - attacker.offsetX();
            double deltaZ = this.offsetZ() - attacker.offsetZ();

            requestTeleport(attacker.offsetX() - (deltaX * 2), attacker.getBodyY() + 2, this.offsetZ() - (deltaZ * 2));
        }

        // Teleport behind the player who fired an arrow and ignore its damage.
        if (source.getSource() instanceof ArrowEntity) {
            ArrowEntity arrow = (ArrowEntity) attacker;

            if (arrow != null && getAttackState() != ReaperAttackState.REST) {
                Entity owner = arrow.getOwner();

                if (owner != null) {
                    double newX = owner.offsetX() + (random.nextFloat() >= 0.50F ? 2 : -2);
                    double newZ = owner.offsetZ() + (random.nextFloat() >= 0.50F ? 2 : -2);

                    requestTeleport(newX, owner.getBodyY(), newZ);
                }
                arrow.remove();
            }
            return false;
        }

        // 25% damage when healing
        if (this.getAttackState() == ReaperAttackState.REST) {
            damage *= 0.25;
        }

        return super.damage(source, damage);
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
    public void tick() {
        super.tick();
        extinguish(); // No fire.

        //update bossinfo
        bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        if (!MCA.getConfig().allowGrimReaper) {
            remove();
        }

        // Prevent flying off into oblivion on death...
        if (this.getHealth() <= 0.0F) {
            setVelocity(Vec3d.ZERO);
            return;
        }

        // look at the player. Aways.
        PlayerEntity player = world.getClosestPlayer(this, 10.D);
        if (player != null) {
            getLookControl().lookAt(player.offsetX(), player.getEyeY(), player.offsetZ());
        }

        LivingEntity entityToAttack = this.getTarget();

        // See if our entity to attack has died at any point.
        if (entityToAttack != null && entityToAttack.isDead()) {
            this.setTarget(null);
            setAttackState(ReaperAttackState.IDLE);
        }

        // Logic for flying.
        fallDistance = 0.0F;
    }

    @Override
    public void requestTeleport(double x, double y, double z) {
        if (this.world instanceof ServerWorld) {
            this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            super.requestTeleport(x, y, z);
            this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
    }

    public void onStartedTrackingBy(ServerPlayerEntity p_184178_1_) {
        super.onStartedTrackingBy(p_184178_1_);
        this.bossInfo.addPlayer(p_184178_1_);
    }

    public void onStoppedTrackingBy(ServerPlayerEntity p_184203_1_) {
        super.onStoppedTrackingBy(p_184203_1_);
        this.bossInfo.removePlayer(p_184203_1_);
    }
}