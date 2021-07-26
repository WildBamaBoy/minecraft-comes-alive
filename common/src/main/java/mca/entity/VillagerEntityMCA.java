package mca.entity;

import com.mojang.serialization.Dynamic;
import mca.Config;
import mca.ParticleTypesMCA;
import mca.TagsMCA;
import mca.block.TombstoneBlock;
import mca.entity.ai.*;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.ai.brain.VillagerTasksMCA;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.CompassionateEntity;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.Personality;
import mca.item.ItemsMCA;
import mca.resources.API;
import mca.resources.ClothingList;
import mca.resources.data.Hair;
import mca.server.world.data.GraveyardManager;
import mca.server.world.data.GraveyardManager.TombstoneState;
import mca.server.world.data.SavedVillagers;
import mca.util.InventoryUtils;
import mca.util.network.datasync.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class VillagerEntityMCA extends VillagerEntity implements CTrackedEntity<VillagerEntityMCA>, NamedScreenHandlerFactory, Infectable, Messenger, CompassionateEntity<Relationship> {
    private static final CDataParameter<String> VILLAGER_NAME = CParameter.create("villagerName", "");
    private static final CDataParameter<String> CLOTHES = CParameter.create("clothes", "");
    private static final CDataParameter<String> HAIR = CParameter.create("hair", "");
    private static final CDataParameter<String> HAIR_OVERLAY = CParameter.create("hairOverlay", "");
    private static final CEnumParameter<DyeColor> HAIR_COLOR = CParameter.create("hairColor", (DyeColor)null);
    private static final CEnumParameter<AgeState> AGE_STATE = CParameter.create("ageState", AgeState.UNASSIGNED);
    private static final CDataParameter<Boolean> IS_INFECTED = CParameter.create("isInfected", false);

    private static final CDataManager<VillagerEntityMCA> DATA = createTrackedData(VillagerEntityMCA.class).build();

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(Class<E> type) {
        return new CDataManager.Builder<>(type)
                .addAll(VILLAGER_NAME, CLOTHES, HAIR, HAIR_OVERLAY, HAIR_COLOR, AGE_STATE, IS_INFECTED)
                .add(Genetics::createTrackedData)
                .add(Residency::createTrackedData)
                .add(VillagerBrain::createTrackedData)
                .add(Relationship::createTrackedData);
    }

    public static DefaultAttributeContainer.Builder createVillagerAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48);
    }

    private final Genetics genetics = new Genetics(this);
    private final Residency residency = new Residency(this);
    private final VillagerBrain mcaBrain = new VillagerBrain(this);
    private final Interactions interactions = new Interactions(this);
    private final Relationship relations = new Relationship(this);

    private final SimpleInventory inventory = new SimpleInventory(27);

    public VillagerEntityMCA(EntityType<VillagerEntityMCA> type, World w, Gender gender) {
        super(type, w);
        inventory.addListener(this::onInvChange);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager requires those fields
        getTypeDataManager().register(this);
        genetics.setGender(gender);

        // TODO: sounds
        setSilent(true);
    }

    @Override
    public CDataManager<VillagerEntityMCA> getTypeDataManager() {
        return DATA;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new VillagerNavigation(this, world);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return VillagerTasksMCA.initializeTasks(this, VillagerTasksMCA.createProfile().deserialize(dynamic));
    }

    @Override
    public void reinitializeBrain(ServerWorld world) {
        Brain<VillagerEntityMCA> brain = getMCABrain();
        brain.stopAllTasks(world, this);
        //copyWithoutBehaviors will copy the memories of the old brain to the new brain
        this.brain = brain.copy();
        VillagerTasksMCA.initializeTasks(this, getMCABrain());
    }

    @SuppressWarnings("unchecked")
    public Brain<VillagerEntityMCA> getMCABrain() {
        return (Brain<VillagerEntityMCA>) brain;
    }

    public Genetics getGenetics() {
        return genetics;
    }

    @Override
    public Relationship getRelationships() {
        return relations;
    }

    public VillagerBrain getVillagerBrain() {
        return mcaBrain;
    }

    public Residency getResidency() {
        return residency;
    }

    public Interactions getInteractions() {
        return interactions;
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);

        if (spawnReason != SpawnReason.CONVERSION) {
            if (spawnReason != SpawnReason.BREEDING) {
                genetics.randomize(this);

                if (spawnReason != SpawnReason.SPAWN_EGG && spawnReason != SpawnReason.DISPENSER) {
                    genetics.setGender(Gender.getRandom());
                }
            }

            setName(API.getVillagePool().pickCitizenName(getGenetics().getGender()));

            initializeSkin();

            mcaBrain.randomize();
        }

        calculateDimensions();

        return data;
    }

    public String getClothes() {
        return getTrackedValue(CLOTHES);
    }

    public void setClothes(String clothes) {
        setTrackedValue(CLOTHES, clothes);
    }

    public final VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }

    public final void setProfession(VillagerProfession profession) {
        setVillagerData(getVillagerData().withProfession(profession));
    }

    public boolean isProfessionImportant() {
        return getProfession() == ProfessionsMCA.GUARD || getProfession() == ProfessionsMCA.OUTLAW;
    }

    @Override
    public void setVillagerData(VillagerData data) {
        if (getProfession() != data.getProfession() && data.getProfession() != ProfessionsMCA.OUTLAW) {
            setTrackedValue(CLOTHES, ClothingList.getInstance().byGender(getGenetics().getGender()).byProfession(data.getProfession()).pickOne());
        }
        super.setVillagerData(data);
    }

    @Override
    public boolean tryAttack(Entity target) {
        //villager is peaceful and wont hurt as long as not necessary
        if (mcaBrain.getPersonality() == Personality.PEACEFUL && getHealth() == getMaxHealth()) {
            return false;
        }

        //we don't use attributes
        // why not?
        float damage = getProfession() == ProfessionsMCA.GUARD ? 9 : 3;
        float knockback = 3;

        //personality bonus
        damage *= mcaBrain.getPersonality().getDamageModifier();

        //enchantment
        if (target instanceof LivingEntity) {
            damage += EnchantmentHelper.getAttackDamage(getMainHandStack(), ((LivingEntity) target).getGroup());
            knockback += EnchantmentHelper.getKnockback(this);
        }

        //fire aspect
        int i = EnchantmentHelper.getFireAspect(this);
        if (i > 0) {
            target.setOnFireFor(i * 4);
        }

        boolean damageDealt = target.damage(DamageSource.mob(this), damage);

        //knockback and post damage stuff
        if (damageDealt) {
            if (knockback > 0 && target instanceof LivingEntity) {
                ((LivingEntity) target).takeKnockback(
                        knockback / 2, MathHelper.sin(yaw * ((float) Math.PI / 180F)),
                        -MathHelper.cos(yaw * ((float) Math.PI / 180F))
                );

                setVelocity(getVelocity().multiply(0.6D, 1, 0.6));
            }

            dealDamage(this, target);
            onAttacking(target);
        }

        return damageDealt;
    }

    @Override
    public final ActionResult interactAt(PlayerEntity player, Vec3d pos, @NotNull Hand hand) {

        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() != ItemsMCA.EGG_MALE && stack.getItem() != ItemsMCA.EGG_FEMALE) {
            return interactions.interactAt(player, pos, hand);
        }
        return super.interactAt(player, pos, hand);
    }

    @Override
    public VillagerEntityMCA createChild(ServerWorld world, PassiveEntity partner) {

        VillagerEntityMCA child = partner instanceof VillagerEntityMCA
                ? relations.getPregnancy().createChild((VillagerEntityMCA) partner, Gender.getRandom())
                : relations.getPregnancy().createSoloChild(Gender.getRandom());

        child.setVillagerData(child.getVillagerData().withType(getRandomType(partner)));

        child.initialize(world, world.getLocalDifficulty(child.getBlockPos()), SpawnReason.BREEDING, null, null);
        return child;
    }

    private VillagerType getRandomType(PassiveEntity partner) {
        double d = random.nextDouble();

        if (d < 0.5D) {
            return VillagerType.forBiome(world.getBiomeKey(getBlockPos()));
        }

        if (d < 0.75D) {
            return getVillagerData().getType();
        }

        return ((VillagerEntity) partner).getVillagerData().getType();
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        getTypeDataManager().load(this, nbt);
        relations.readFromNbt(nbt);

        //set speed
        float speed = mcaBrain.getPersonality().getSpeedModifier();

        speed /= genetics.getGene(Genetics.WIDTH);
        speed *= genetics.getGene(Genetics.SIZE);

        setMovementSpeed(speed);
        InventoryUtils.readFromNBT(inventory, nbt);
    }

    @Override
    public final void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        getTypeDataManager().save(this, nbt);
        relations.writeToNbt(nbt);
        InventoryUtils.saveToNBT(inventory, nbt);
    }

    private void initializeSkin() {
        setClothes(ClothingList.getInstance().getPool(this).pickOne());
        setHair(API.getHairPool().pickOne(this));
    }

    @Override
    public final boolean damage(DamageSource source, float damageAmount) {
        // Guards take 50% less damage
        if (getProfession() == ProfessionsMCA.GUARD) {
            damageAmount *= 0.5;
        }

        damageAmount *= mcaBrain.getPersonality().getWeaknessModifier();

        if (!world.isClient) {
            if (source.getAttacker() instanceof PlayerEntity) {
                sendChatMessage((PlayerEntity) source.getAttacker(), "villager.hurt");
            }

            if (source.getSource() instanceof ZombieEntity
                    && getProfession() != ProfessionsMCA.GUARD
                    && Config.getInstance().enableInfection
                    && random.nextFloat() < Config.getInstance().infectionChance / 100.0) {
                setInfected(true);
            }
        }

        @Nullable
        Entity attacker = source != null ? source.getAttacker() : null;

        // Notify the surrounding guards when a villager is attacked. Yoinks!
        if (attacker instanceof LivingEntity) {
            Vec3d pos = getPos();
            world.getNonSpectatingEntities(VillagerEntityMCA.class, new Box(pos, pos).expand(10)).forEach(v -> {
                if (v.squaredDistanceTo(v) <= 100 && v.getProfession() == ProfessionsMCA.GUARD) {
                    v.setTarget((LivingEntity) attacker);
                }
            });
        }

        return super.damage(source, damageAmount);
    }

    @Override
    public void tickMovement() {
        tickHandSwing();
        super.tickMovement();

        if (!world.isClient) {
            // Natural regeneration every 10 seconds
            if (age % 200 == 0 && getHealth() < getMaxHealth()) {
                heal(1);
            }

            // Update age state for current entity growth
            setAgeState(AgeState.byCurrentAge(getBreedingAge()));

            residency.tick();

            // Grow up
            if (getProfession() == ProfessionsMCA.CHILD && this.getAgeState() == AgeState.ADULT) {
                setProfession(API.randomProfession());
            }

            relations.tick(age);

            // Brain and pregnancy depend on the above states, so we tick them last
            // Every 1 second
            mcaBrain.think();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient) {
            if (relations.isProcreating()) {
                headYaw += 50;
            }

            if (age % 20 == 0) {
                if (world.random.nextBoolean()) {
                    if (mcaBrain.getMoodLevel() <= -15) {
                        mcaBrain.getPersonality().getMoodGroup().getParticles().ifPresent(this::produceParticles);
                    } else if (mcaBrain.getMoodLevel() >= 15) {
                        produceParticles(ParticleTypes.HAPPY_VILLAGER);
                    }
                }
            }
        }
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        if (getVehicle() instanceof PathAwareEntity) {
            bodyYaw = ((PathAwareEntity) getVehicle()).bodyYaw;
        }
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

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (world.isClient) {
            return;
        }

        InventoryUtils.dropAllItems(this, inventory);

        if (!GraveyardManager.get((ServerWorld) world).findNearest(getBlockPos(), TombstoneState.EMPTY, 7).filter(pos -> {
            if (world.getBlockState(pos).isIn(TagsMCA.Blocks.TOMBSTONES)) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof TombstoneBlock.Data) {
                    ((TombstoneBlock.Data) be).setEntity(this);

                    relations.onTragedy(cause, pos);
                    return true;
                }
            }
            return false;
        }).isPresent()) {
            SavedVillagers.get((ServerWorld) world).saveVillager(this);
            relations.onTragedy(cause, null);
        }
    }

    @Override
    public MoveControl getMoveControl() {
        return isRidingHorse() ? moveControl : super.getMoveControl();
    }

    @Override
    public JumpControl getJumpControl() {
        return jumpControl;
    }

    @Override
    public EntityNavigation getNavigation() {
        return isRidingHorse() ? navigation : super.getNavigation();
    }

    protected boolean isRidingHorse() {
        return hasVehicle() && getVehicle() instanceof HorseBaseEntity;
    }

    @Override
    public void requestTeleport(double destX, double destY, double destZ) {
        if (hasVehicle()) {
            Entity rootVehicle = getRootVehicle();
            if (rootVehicle instanceof MobEntity) {
                rootVehicle.requestTeleport(destX, destY, destZ);
                return; // villagers can travel by teleporting, so make sure they take their mount with
            }
        }

        super.requestTeleport(destX, destY, destZ);
    }

    @Override
    public final SoundEvent getDeathSound() {
        // TODO: Custom sounds
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Override
    protected final SoundEvent getAmbientSound() {
        // TODO: Custom sounds
        return null;
    }

    @Override
    protected final SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_GENERIC_HURT;
    }

    public final void playWelcomeSound() {
        //TODO custom sounds?
        //playSound(SoundEvents.VILLAGER_CELEBRATE, getSoundVolume(), getVoicePitch());
    }

    public void setName(String name) {
        setTrackedValue(VILLAGER_NAME, name);
    }

    @Override
    public final Text getDisplayName() {
        Text name = super.getDisplayName();

        MoveState state = getVillagerBrain().getMoveState();
        if (state != MoveState.MOVE) {
            name = name.shallowCopy().append(" (").append(getVillagerBrain().getMoveState().getName()).append(")");
        }

        if (getProfession() == ProfessionsMCA.OUTLAW) {
            return name.shallowCopy().formatted(Formatting.RED);
        }
        return name;
    }

    @Override
    public final Text getDefaultName() {
        return new LiteralText(getTrackedValue(VILLAGER_NAME));
    }

    @Override
    public boolean isInfected() {
        return getTrackedValue(IS_INFECTED);
    }

    @Override
    public void setInfected(boolean infected) {
        setTrackedValue(IS_INFECTED, infected);
    }

    @Override
    public void playSpeechEffect() {
        if (isInfected()) {
            playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, getSoundVolume(), getSoundPitch());
        } else {
            // TODO: Custom sounds
            // playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, getSoundVolume(), getSoundPitch());
        }
    }

    @Override
    public DialogueType getDialogueType(PlayerEntity receiver) {
        return mcaBrain.getMemoriesForPlayer(receiver).getDialogueType();
    }

    public Hair getHair() {
        return new Hair(getTrackedValue(HAIR), getTrackedValue(HAIR_OVERLAY));
    }

    public void setHair(Hair hair) {
        setTrackedValue(HAIR, hair.texture());
        setTrackedValue(HAIR_OVERLAY, hair.overlay());
    }

    // we make it public here
    @Override
    public void produceParticles(ParticleEffect parameters) {
        super.produceParticles(parameters);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, inventory);
    }

    public AgeState getAgeState() {
        return getTrackedValue(AGE_STATE);
    }

    public void setAgeState(AgeState state) {
        if (state == getAgeState()) {
            return;
        }

        setTrackedValue(AGE_STATE, state);
        calculateDimensions();

        if (state == AgeState.ADULT) {
            // Notify player parents of the age up and set correct dialogue type.
            relations.getParents().filter(e -> e instanceof PlayerEntity).map(e -> (PlayerEntity) e).forEach(p -> {
                mcaBrain.getMemoriesForPlayer(p).setDialogueType(DialogueType.ADULT);
                sendEventMessage(new TranslatableText("notify.child.grownup", getName()), p);
            });
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public float getScaleFactor() {
        return genetics == null ? 1 : genetics.getVerticalScaleFactor() * getAgeState().getHeight();
    }

    public float getHorizontalScaleFactor() {
        return genetics.getVerticalScaleFactor() * getAgeState().getHeight() * getAgeState().getWidth();
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions size) {
        return getScaleFactor() * 1.6f;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> par) {
        if (getTypeDataManager().isParam(AGE_STATE, par) || getTypeDataManager().isParam(Genetics.SIZE.getParam(), par)) {
            calculateDimensions();
        }

        super.onTrackedDataSet(par);
    }

    @Override
    public SimpleInventory getInventory() {
        return inventory;
    }

    public void moveTowards(BlockPos pos, float speed, int closeEnoughDist) {
        BlockPosLookTarget blockposwrapper = new BlockPosLookTarget(pos);
        this.brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(blockposwrapper, speed, closeEnoughDist));
        this.lookAt(pos);
    }

    public void moveTowards(BlockPos pos) {
        moveTowards(pos, 0.5F, 1);
    }

    public void lookAt(BlockPos pos) {
        this.brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(pos));
    }

    @Override
    public void handleStatus(byte id) {
        switch (id) {
            case Status.MCA_VILLAGER_NEG_INTERACTION:
                world.addImportantParticle(ParticleTypesMCA.NEG_INTERACTION, true, getX(), getEyeY() + 0.5, getZ(), 0, 0, 0);
                break;
            case Status.MCA_VILLAGER_POS_INTERACTION:
                world.addImportantParticle(ParticleTypesMCA.POS_INTERACTION, true, getX(), getEyeY() + 0.5, getZ(), 0, 0, 0);
                break;
            case Status.MCA_VILLAGER_TRAGEDY:
                this.produceParticles(ParticleTypes.DAMAGE_INDICATOR);
                break;
            default:
                super.handleStatus(id);
        }
    }

    public void onInvChange(Inventory inventoryFromListener) {
        SimpleInventory inv = this.getInventory();

        for (EquipmentSlot type : EquipmentSlot.values()) {
            if (type.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack stack = InventoryUtils.getBestArmorOfType(inv, type);
                if (!stack.isEmpty()) {
                    equipStack(type, stack);
                }
            }
        }
    }

    @Override
    public void setBaby(boolean isBaby) {
        this.setBreedingAge(isBaby ? AgeState.startingAge : 0);
    }

    //TODO: Reputation
    @Override
    public int getReputation(PlayerEntity player) {
        return super.getReputation(player);
    }

    public void setHairDye(DyeColor color) {
        setTrackedValue(HAIR_COLOR, color);
    }

    public void clearHairDye() {
        setTrackedValue(HAIR_COLOR, null);
    }

    public Optional<DyeColor> getHairDye() {
        return Optional.ofNullable(getTrackedValue(HAIR_COLOR));
    }
}