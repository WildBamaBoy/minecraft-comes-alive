package mca.entity;

import mca.entity.ai.Traits;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mca.TagsMCA;
import mca.entity.ai.Genetics;
import mca.entity.ai.Relationship;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.CompassionateEntity;
import mca.entity.ai.relationship.Gender;
import mca.entity.interaction.ZombieCommandHandler;
import mca.util.network.datasync.CDataManager;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class ZombieVillagerEntityMCA extends ZombieVillagerEntity implements VillagerLike<ZombieVillagerEntityMCA>, CompassionateEntity<Relationship<ZombieVillagerEntityMCA>> {

    private static final CDataManager<ZombieVillagerEntityMCA> DATA = VillagerEntityMCA.createTrackedData(ZombieVillagerEntityMCA.class).build();

    private final VillagerBrain<ZombieVillagerEntityMCA> mcaBrain = new VillagerBrain<>(this);

    private final Genetics genetics = new Genetics(this);
    private final Traits traits = new Traits(this);

    private final Relationship<ZombieVillagerEntityMCA> relations = new Relationship<>(this);

    private final ZombieCommandHandler interactions = new ZombieCommandHandler(this);

    public ZombieVillagerEntityMCA(EntityType<? extends ZombieVillagerEntity> type, World world, Gender gender) {
        super(type, world);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager requires those fields
        getTypeDataManager().register(this);
        genetics.setGender(gender);
    }

    @Override
    public CDataManager<ZombieVillagerEntityMCA> getTypeDataManager() {
        return DATA;
    }

    @Override
    public Genetics getGenetics() {
        return genetics;
    }

    @Override
    public Traits getTraits() {
        return traits;
    }

    @Override
    public VillagerBrain<?> getVillagerBrain() {
        return mcaBrain;
    }

    @Override
    public ZombieCommandHandler getInteractions() {
        return interactions;
    }

    @Override
    public Relationship<ZombieVillagerEntityMCA> getRelationships() {
        return relations;
    }

    @Override
    public float getInfectionProgress() {
        return MAX_INFECTION;
    }

    @Override
    public void setInfectionProgress(float progress) {
        // noop
    }

    @Override
    @Nullable
    public final Text getCustomName() {
        String value = getTrackedValue(VILLAGER_NAME);
        return value.isEmpty() ? null : new LiteralText(value).formatted(Formatting.RED);
    }

    @Override
    public double getHeightOffset() {
        return -0.35;
    }

    @Override
    public double getMountedHeightOffset() {
        return super.getMountedHeightOffset();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {

        if (pose == EntityPose.SLEEPING) {
            return SLEEPING_DIMENSIONS;
        }

        float height = getScaleFactor() * 2.0F;
        float width = getHorizontalScaleFactor() * 0.6F;

        return EntityDimensions.changing(width, height);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public float getScaleFactor() {
        return Math.min(0.999f, getRawScaleFactor());
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions size) {
        return getScaleFactor() * 1.75f;
    }

    @Override
    public final ActionResult interactAt(PlayerEntity player, Vec3d pos, @NotNull Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (hand.equals(Hand.MAIN_HAND) && !stack.getItem().isIn(TagsMCA.Items.ZOMBIE_EGGS) && stack.getItem() != Items.GOLDEN_APPLE) {
            if (player instanceof ServerPlayerEntity) {
                String t = new String(new char[getRandom().nextInt(8) + 2]).replace("\0", ". ");
                sendChatMessage(new LiteralText(t), player);
            }
        }
        return super.interactAt(player, pos, hand);
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);

        initialize(spawnReason);

        return data;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (!world.isClient) {
            // Natural regeneration every 10 seconds
            if (getAgeState() == AgeState.UNASSIGNED) {
                setAgeState(isBaby() ? AgeState.BABY : AgeState.random());
            } else if (getAgeState() == AgeState.BABY) {
                // baby zombie villager just cause weird bugs, so we skip that stage
                setAgeState(AgeState.TODDLER);
            }

            //an unassigned gender can happen if initialize() has been skipped
            //due the way how SpawnEggs on villagers work, initialize() would never be called
            //and since we have no proper isInitialized() I just check for missing clothes
            if (getClothes().isEmpty() && getHair().texture().isEmpty()) {
                initialize(SpawnReason.TRIGGERED);
            }
        }
    }

    @Override
    public void setBaby(boolean isBaby) {
        super.setBaby(isBaby);
        setAgeState(isBaby ? AgeState.BABY : AgeState.ADULT);
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (!world.isClient) {
            relations.onDeath(cause);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T extends MobEntity> T method_29243/*convertTo*/(EntityType<T> type, boolean keepInventory) {

        if (!removed && type == EntityType.VILLAGER) {
            VillagerEntityMCA mob = super.method_29243(getGenetics().getGender().getVillagerType(), keepInventory);
            mob.copyVillagerAttributesFrom(this);
            mob.setInfected(false);
            return (T)mob;
        }

        T mob = super.method_29243(type, keepInventory);

        if (mob instanceof VillagerLike<?>) {
            ((VillagerLike<?>)mob).copyVillagerAttributesFrom(this);
        }

        return mob;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        getTypeDataManager().load(this, nbt);
        relations.readFromNbt(nbt);
    }

    @Override
    public final void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        getTypeDataManager().save(this, nbt);
        relations.writeToNbt(nbt);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> par) {
        if (getTypeDataManager().isParam(AGE_STATE, par) || getTypeDataManager().isParam(Genetics.SIZE.getParam(), par)) {
            calculateDimensions();
        }

        super.onTrackedDataSet(par);
    }
}
