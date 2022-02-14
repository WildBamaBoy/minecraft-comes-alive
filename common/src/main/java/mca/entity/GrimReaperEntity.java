package mca.entity;


import mca.Config;
import mca.SoundsMCA;
import mca.entity.ai.goal.GrimReaperIdleGoal;
import mca.entity.ai.goal.GrimReaperMeleeGoal;
import mca.entity.ai.goal.GrimReaperRestGoal;
import mca.entity.ai.goal.GrimReaperTargetGoal;
import mca.item.ItemsMCA;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CEnumParameter;
import mca.util.network.datasync.CParameter;
import mca.util.network.datasync.CTrackedEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.MoveControl;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class GrimReaperEntity extends PathAwareEntity implements CTrackedEntity<GrimReaperEntity> {
    public static final CEnumParameter<ReaperAttackState> ATTACK_STAGE = CParameter.create("attackStage", ReaperAttackState.IDLE);

    public static final CDataManager<GrimReaperEntity> DATA = new CDataManager.Builder<>(GrimReaperEntity.class).addAll(ATTACK_STAGE).build();

    private final ServerBossBar bossInfo = (ServerBossBar)new ServerBossBar(getDisplayName(), BossBar.Color.PURPLE, BossBar.Style.PROGRESS).setDarkenSky(true);

    public GrimReaperEntity(EntityType<? extends GrimReaperEntity> type, World world) {
        super(type, world);

        this.experiencePoints = 100;

        this.moveControl = new FlightMoveControl(this, 10, false);

        getTypeDataManager().register(this);
    }

    @Override
    public CDataManager<GrimReaperEntity> getTypeDataManager() {
        return DATA;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0F)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30F)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.30F)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0D);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }

    @Override
    protected void initGoals() {
        this.targetSelector.add(1, new GrimReaperTargetGoal(this));

        this.goalSelector.add(1, new GrimReaperRestGoal(this));
        this.goalSelector.add(2, new GrimReaperMeleeGoal(this));
        this.goalSelector.add(3, new GrimReaperIdleGoal(this, 1));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        this.goalSelector.add(5, new LookAroundGoal(this));
    }

    @Override
    public MoveControl getMoveControl() {
        return moveControl;
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
        BirdNavigation navigator = new BirdNavigation(this, world) {
            @Override
            public boolean isValidPosition(BlockPos p_188555_1_) {
                return true;
            }

            @Override
            public void tick() {
                super.tick();
            }
        };
        navigator.setCanPathThroughDoors(false);
        navigator.setCanSwim(false);
        navigator.setCanEnterOpenDoors(true);
        return navigator;
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingLvl, boolean hitByPlayer) {
        super.dropEquipment(source, lootingLvl, hitByPlayer);
        ItemEntity itementity = dropItem(ItemsMCA.SCYTHE);
        if (itementity != null) {
            itementity.setCovetedItem();
        }
    }

    public ReaperAttackState getAttackState() {
        return getTrackedValue(ATTACK_STAGE);
    }

    public void setAttackState(ReaperAttackState state) {
        // Only update if needed so that sounds only play once.
        if (getAttackState() == state) {
            return;
        }

        setTrackedValue(ATTACK_STAGE, state);

        switch (state) {
            case PRE:
                playSound(SoundsMCA.reaper_scythe_out, 1, 1);
                break;
            case POST:
                playSound(SoundsMCA.reaper_scythe_swing, 1, 1);
                break;
            default:
        }
    }

    @Override
    public boolean damage(DamageSource source, float damage) {
        // Ignore wall damage, fire and explosion damage
        if (source == DamageSource.IN_WALL || source == DamageSource.ON_FIRE || source.isExplosive() || source == DamageSource.IN_FIRE) {
            // Teleport out of any walls we may end up in
            if (source == DamageSource.IN_WALL) {
                requestTeleport(this.getX(), this.getY() + 3, this.getZ());
            }
            return false;
        }

        Entity attacker = source.getSource();

        // Ignore damage when blocking, and teleport behind the attacker when attacked
        if (!world.isClient && this.getAttackState() == ReaperAttackState.BLOCK && attacker != null) {

            double deltaX = this.getX() - attacker.getX();
            double deltaZ = this.getZ() - attacker.getZ();

            this.playSound(SoundsMCA.reaper_block, 1.0F, 1.0F);
            requestTeleport(attacker.getX() - (deltaX * 2), attacker.getY() + 2, this.getZ() - (deltaZ * 2));
            return false;
        }

        // Randomly portal behind the player who just attacked.
        if (!world.isClient && random.nextFloat() >= 0.30F && attacker != null) {
            double deltaX = this.getX() - attacker.getX();
            double deltaZ = this.getZ() - attacker.getZ();

            requestTeleport(attacker.getX() - (deltaX * 2), attacker.getY() + 2, this.getZ() - (deltaZ * 2));
        }

        // Teleport behind the player who fired an arrow and ignore its damage.
        if (source.getSource() instanceof ArrowEntity) {
            ArrowEntity arrow = (ArrowEntity)attacker;

            if (arrow != null && getAttackState() != ReaperAttackState.REST) {
                Entity owner = arrow.getOwner();

                if (owner != null) {
                    double newX = owner.getX() + (random.nextFloat() >= 0.50F ? 2 : -2);
                    double newZ = owner.getZ() + (random.nextFloat() >= 0.50F ? 2 : -2);

                    requestTeleport(newX, owner.getY(), newZ);
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

        //update bossinfo
        bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        if (!Config.getInstance().allowGrimReaper) {
            remove();
        }

        // Prevent flying off into oblivion on death...
        if (this.getHealth() <= 0.0F) {
            setVelocity(Vec3d.ZERO);
            return;
        }

        // look at the player. Always.
        PlayerEntity player = world.getClosestPlayer(this, 10.D);
        if (player != null) {
            getLookControl().lookAt(player.getX(), player.getEyeY(), player.getZ());
        }

        LivingEntity entityToAttack = this.getTarget();

        // See if our entity to attack has died at any point.
        if (entityToAttack != null && entityToAttack.isDead()) {
            this.setTarget(null);
            setAttackState(ReaperAttackState.IDLE);
        }

        // Logic for flying.
        fallDistance = 0;
    }

    @Override
    public void requestTeleport(double x, double y, double z) {
        if (world instanceof ServerWorld) {
            playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            super.requestTeleport(x, y, z);
            playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossInfo.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossInfo.removePlayer(player);
    }
}
