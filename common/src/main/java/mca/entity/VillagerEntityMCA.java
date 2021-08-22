package mca.entity;

import com.mojang.serialization.Dynamic;
import mca.Config;
import mca.ParticleTypesMCA;
import mca.SoundsMCA;
import mca.TagsMCA;
import mca.advancement.criterion.CriterionMCA;
import mca.entity.ai.*;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.ai.brain.VillagerTasksMCA;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.CompassionateEntity;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.Personality;
import mca.entity.ai.relationship.VillagerDimensions;
import mca.entity.interaction.VillagerCommandHandler;
import mca.resources.API;
import mca.resources.ClothingList;
import mca.util.InventoryUtils;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
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

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VillagerEntityMCA extends VillagerEntity implements VillagerLike<VillagerEntityMCA>, NamedScreenHandlerFactory, CompassionateEntity<BreedableRelationship> {
    private static final CDataParameter<Float> INFECTION_PROGRESS = CParameter.create("infectionProgress", MIN_INFECTION);

    private static final CDataParameter<Integer> GROWTH_AMOUNT = CParameter.create("growthAmount", AgeState.MAX_AGE);

    private static final CDataManager<VillagerEntityMCA> DATA = createTrackedData(VillagerEntityMCA.class).build();

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(Class<E> type) {
        return VillagerLike.createTrackedData(type).addAll(INFECTION_PROGRESS, GROWTH_AMOUNT)
                .add(Residency::createTrackedData)
                .add(BreedableRelationship::createTrackedData);
    }

    private final VillagerBrain<VillagerEntityMCA> mcaBrain = new VillagerBrain<>(this);

    private final Genetics genetics = new Genetics(this);
    private final Residency residency = new Residency(this);
    private final BreedableRelationship relations = new BreedableRelationship(this);

    private final VillagerCommandHandler interactions = new VillagerCommandHandler(this);
    private final SimpleInventory inventory = new SimpleInventory(27);

    private final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.UNASSIGNED);

    private float prevInfectionProgress;
    private int prevGrowthAmount;

    public VillagerEntityMCA(EntityType<VillagerEntityMCA> type, World w, Gender gender) {
        super(type, w);
        inventory.addListener(this::onInvChange);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager requires those fields
        getTypeDataManager().register(this);
        genetics.setGender(gender);
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

    @Override
    public Genetics getGenetics() {
        return genetics;
    }

    @Override
    public BreedableRelationship getRelationships() {
        return relations;
    }

    @Override
    public VillagerBrain<?> getVillagerBrain() {
        return mcaBrain;
    }

    public Residency getResidency() {
        return residency;
    }

    @Override
    public VillagerCommandHandler getInteractions() {
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

        setAgeState(AgeState.byCurrentAge(getBreedingAge()));

        if (getAgeState() != AgeState.ADULT) {
            setProfession(ProfessionsMCA.CHILD);
        }

        return data;
    }

    public final VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }

    public final void setProfession(VillagerProfession profession) {
        setVillagerData(getVillagerData().withProfession(profession));
    }

    public boolean isProfessionImportant() {
        return getProfession() == ProfessionsMCA.GUARD || getProfession() == ProfessionsMCA.OUTLAW || getProfession() == ProfessionsMCA.CHILD;
    }

    @Override
    public void setVillagerData(VillagerData data) {
        if (getProfession() != data.getProfession() && data.getProfession() != ProfessionsMCA.OUTLAW) {
            setTrackedValue(CLOTHES, ClothingList.getInstance().byGender(getGenetics().getGender()).byProfession(data.getProfession()).pickOne());
            getRelationships().getFamilyEntry().setProfession(data.getProfession());
        }
        super.setVillagerData(data);
    }

    @Override
    public void setBaby(boolean isBaby) {
        setBreedingAge(isBaby ? AgeState.MAX_AGE : 0);
    }

    @Override
    public int getBreedingAge() {
        return super.getBreedingAge();
    }

    @Override
    public void setBreedingAge(int age) {
        super.setBreedingAge(age);
        setTrackedValue(GROWTH_AMOUNT, age);
        setAgeState(AgeState.byCurrentAge(age));

        AgeState current = getAgeState();

        dimensions.set(current, 1);

        AgeState next = current.getNext();
        if (current != next) {
            dimensions.set(getAgeState(), AgeState.getDelta(age));
        }
    }

    @Override
    public int getReputation(PlayerEntity player) {
        // TODO: Reputation
        return super.getReputation(player);
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

        if (!stack.getItem().isIn(TagsMCA.Items.VILLAGER_EGGS)) {
            playWelcomeSound();
            return interactions.interactAt(player, pos, hand);
        }
        return super.interactAt(player, pos, hand);
    }

    @Override
    public VillagerEntityMCA createChild(ServerWorld world, PassiveEntity partner) {

        VillagerEntityMCA child = partner instanceof VillagerEntityMCA
                ? relations.getPregnancy().createChild(Gender.getRandom(), (VillagerEntityMCA) partner)
                : relations.getPregnancy().createChild(Gender.getRandom());

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
                sendChatToAllAround("villager.bitten");
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
            if (age % 200 == 0 && getHealth() < getMaxHealth()) {
                // if the villager has food they should try to eat.
                ItemStack food = getMainHandStack();

                if (food.isFood()) {
                    eatFood(world, food);
                } else {
                    if (!findAndEquipToMain(ItemStack::isFood)) {
                        heal(1); // natural regenaration
                    }
                }
            }

            residency.tick();

            // Grow up
            if (getProfession() == ProfessionsMCA.CHILD && getAgeState() == AgeState.ADULT) {
                setProfession(API.randomProfession());
            }

            relations.tick(age);

            // Brain and pregnancy depend on the above states, so we tick them last
            // Every 1 second
            mcaBrain.think();
        }
    }

    protected boolean findAndEquipToMain(Predicate<ItemStack> predicate) {
        int slot = InventoryUtils.getFirstSlotContainingItem(getInventory(), predicate);

        if (slot > -1) {
            ItemStack replacement = getInventory().getStack(slot).split(1);

            if (!replacement.isEmpty()) {
                ItemStack current = getMainHandStack();
                if (!current.isEmpty()) {
                    if (getInventory().canInsert(current)) {
                        getInventory().addStack(current);
                    } else {
                        dropStack(current);
                    }
                }

                setStackInHand(Hand.MAIN_HAND, replacement);
                return true;
            }
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();

        int age = getTrackedValue(GROWTH_AMOUNT);
        if (age != prevGrowthAmount) {
            prevGrowthAmount = age;
            calculateDimensions();
        }

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

        float infection = getInfectionProgress();
        if (infection > 0) {
            if (age % 120 == 0 && infection > FEVER_THRESHOLD && world.random.nextInt(200) > 150) {
                sendChatToAllAround("villager.sickness");
            }

            prevInfectionProgress = infection;
            infection += 0.02F;
            setInfectionProgress(infection);

            if (!world.isClient && infection >= POINT_OF_NO_RETURN && world.random.nextInt(2000) < infection) {
                convertToZombie();
                remove();
            }
        }

        if (this.age % 90 == 0 && mcaBrain.isPanicking()) {
            sendChatToAllAround("villager.scream");
        }
    }

    @Override
    public void calculateDimensions() {
        AgeState current = getAgeState();
        AgeState next = current.getNext();

        dimensions.set(current, 1);
        if (next != current) {
            dimensions.set(next, AgeState.getDelta(getTrackedValue(GROWTH_AMOUNT)));
        }
        super.calculateDimensions();
    }

    @Override
    public ItemStack eatFood(World world, ItemStack stack) {
        if (stack.isFood()) {
            heal(stack.getItem().getFoodComponent().getHunger() / 4F);
        }
        return super.eatFood(world, stack);
    }

    private boolean convertToZombie() {
        ZombieVillagerEntity zombie = method_29243(EntityType.ZOMBIE_VILLAGER, false);
        if (zombie != null) {
            zombie.initialize((ServerWorld) world, world.getLocalDifficulty(zombie.getBlockPos()), SpawnReason.CONVERSION, new ZombieEntity.ZombieData(false, true), null);
            zombie.setVillagerData(getVillagerData());
            zombie.setGossipData(getGossip().serialize(NbtOps.INSTANCE).getValue());
            zombie.setOfferData(getOffers().toNbt());
            zombie.setXp(getExperience());

            world.syncWorldEvent((PlayerEntity) null, 1026, this.getBlockPos(), 0);
            return true;
        }
        return false;
    }

    @Override
    public void tickRiding() {
        super.tickRiding();

        Entity vehicle = getVehicle();

        if (vehicle instanceof PathAwareEntity) {
            bodyYaw = ((PathAwareEntity) vehicle).bodyYaw;
        }

        if (vehicle instanceof PlayerEntity) {
            List<Entity> passengers = vehicle.getPassengerList();

            float yaw = -((PlayerEntity)vehicle).bodyYaw * 0.017453292F;

            boolean left = passengers.get(0) == this;
            boolean head = passengers.size() > 2 && passengers.get(2) == this;

            Vec3d offset = head ? new Vec3d(0, 0.55, 0) : new Vec3d(left ? 0.4F : -0.4F, 0, 0).rotateY(yaw);

            Vec3d pos = this.getPos();
            this.setPos(pos.getX() + offset.getX(), pos.getY() + offset.getY(), pos.getZ() + offset.getZ());

            if (vehicle.isSneaking()) {
                stopRiding();
            }
        }
    }

    @Override
    public double getHeightOffset() {
        Entity vehicle = getVehicle();
        if (vehicle instanceof PlayerEntity) {
            return -0.2;
        }
        return -0.35;
    }

    @Override
    public double getMountedHeightOffset() {
        return super.getMountedHeightOffset();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {

        Entity vehicle = getVehicle();
        if (vehicle instanceof PlayerEntity) {
            return SLEEPING_DIMENSIONS;
        }

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

        if (!(cause.getAttacker() instanceof ZombieEntity) && !(cause.getAttacker() instanceof ZombieVillagerEntity)) {
            if (getInfectionProgress() >= BABBLING_THRESHOLD) {
                boolean wasRemoved = removed;
                removed = false;
                convertToZombie();
                removed = wasRemoved;
                return;
            }
        }

        if (!relations.onDeath(cause)) {
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
    public SoundEvent getDeathSound() {
        return getGenetics().getGender() == Gender.MALE ? SoundsMCA.VILLAGER_MALE_SCREAM : SoundsMCA.VILLAGER_FEMALE_SCREAM;
    }

    public SoundEvent getSurprisedSound() {
        return getGenetics().getGender() == Gender.MALE ? SoundsMCA.VILLAGER_MALE_SURPRISE : SoundsMCA.VILLAGER_FEMALE_SURPRISE;
    }

    @Nullable
    @Override
    protected final SoundEvent getAmbientSound() {
        if (isSleeping()) {
            return null; // TODO: snoring?
        }

        if (getAgeState() == AgeState.BABY) {
            return SoundsMCA.VILLAGER_BABY_LAUGH;
        }

        if (getVillagerBrain().isPanicking()) {
            return getDeathSound();
        }

        if (age % 10 > 5) {
            if (getVillagerBrain().getMood().isAngry()) {
                return getGenetics().getGender() == Gender.MALE ? SoundsMCA.VILLAGER_MALE_ANGRY : SoundsMCA.VILLAGER_FEMALE_ANGRY;
            }
            if (getVillagerBrain().getMood().isSad()) {
                return getGenetics().getGender() == Gender.MALE ? SoundsMCA.VILLAGER_MALE_CRY : SoundsMCA.VILLAGER_FEMALE_CRY;
            }
            if (getVillagerBrain().getMood().isHappy()) {
                return getGenetics().getGender() == Gender.MALE ? SoundsMCA.VILLAGER_MALE_LAUGH : SoundsMCA.VILLAGER_FEMALE_LAUGH;
            }
        }

        if (hasCustomer()) {
            return getSurprisedSound();
        }

        return null;
    }

    @Override
    protected final SoundEvent getHurtSound(DamageSource cause) {
        return getDeathSound();
    }

    public final void playWelcomeSound() {
        if (!getVillagerBrain().isPanicking()) {
            playSound(getGenetics().getGender() == Gender.MALE ? SoundsMCA.VILLAGER_MALE_GREET : SoundsMCA.VILLAGER_FEMALE_GREET, getSoundVolume(), getSoundPitch());
        }
    }

    public final void playSurprisedSound() {
        playSound(getSurprisedSound(), getSoundVolume(), getSoundPitch());
    }

    @Override
    public final Text getDisplayName() {
        Text name = super.getDisplayName();

        MoveState state = getVillagerBrain().getMoveState();
        if (state != MoveState.MOVE) {
            name = name.shallowCopy().append(" (").append(getVillagerBrain().getMoveState().getName()).append(")");
        }

        if (isInfected()) {
            return name.shallowCopy().formatted(Formatting.GREEN);
        } else if (getProfession() == ProfessionsMCA.OUTLAW) {
            return name.shallowCopy().formatted(Formatting.RED);
        }
        return name;
    }

    @Override
    public final Text getDefaultName() {
        return new LiteralText(getTrackedValue(VILLAGER_NAME));
    }

    @Override
    public float getInfectionProgress() {
        return getTrackedValue(INFECTION_PROGRESS);
    }

    @Override
    public float getPrevInfectionProgress() {
        return prevInfectionProgress;
    }

    @Override
    public void setInfectionProgress(float progress) {
        setTrackedValue(INFECTION_PROGRESS, progress);
    }

    @Override
    public void playSpeechEffect() {
        if (isSpeechImpaired()) {
            playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, getSoundVolume(), getSoundPitch());
        } else {
            // playWelcomeSound();
        }
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

    @Override
    public VillagerDimensions getVillagerDimensions() {
        return dimensions;
    }

    @Override
    public boolean setAgeState(AgeState state) {
        if (VillagerLike.super.setAgeState(state)) {
            if (!world.isClient) {
                // trigger grow up advancements
                relations.getParents().filter(e -> e instanceof ServerPlayerEntity).forEach(e -> {
                    CriterionMCA.CHILD_AGE_STATE_CHANGE.trigger((ServerPlayerEntity) e, state.name());
                });

                if (state == AgeState.ADULT) {
                    // Notify player parents of the age up and set correct dialogue type.
                    relations.getParents().filter(e -> e instanceof PlayerEntity).map(e -> (PlayerEntity) e).forEach(p -> {
                        mcaBrain.getMemoriesForPlayer(p).setDialogueType(DialogueType.ADULT);
                        sendEventMessage(new TranslatableText("notify.child.grownup", getName()), p);
                    });
                }
            }

            return true;
        }

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public float getScaleFactor() {
        return genetics == null ? 1 : genetics.getVerticalScaleFactor() * getVillagerDimensions().getHeight();
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions size) {
        return getScaleFactor() * 1.81f;
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

    @Override
    public boolean equip(int slot, ItemStack item) {
        if (slot >= 300 && slot < 300 + getInventory().size()) {
            getInventory().setStack(slot - 300, item);
            return true;
        }
        return super.equip(slot, item);
    }

    public void moveTowards(BlockPos pos, float speed, int closeEnoughDist) {
        this.brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosLookTarget(pos), speed, closeEnoughDist));
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
        SimpleInventory inv = getInventory();

        for (EquipmentSlot type : EquipmentSlot.values()) {
            if (type.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack stack = InventoryUtils.getBestArmorOfType(inv, type);
                if (!stack.isEmpty()) {
                    equipStack(type, stack);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T extends MobEntity> T method_29243/*convertTo*/(EntityType<T> type, boolean keepInventory) {
        if (!removed && type == EntityType.ZOMBIE_VILLAGER) {
            ZombieVillagerEntityMCA mob = super.method_29243(getGenetics().getGender().getZombieType(), keepInventory);
            mob.copyVillagerAttributesFrom(this);
            return (T) mob;
        }

        T mob = super.method_29243(type, keepInventory);

        if (mob instanceof VillagerLike<?>) {
            ((VillagerLike<?>) mob).copyVillagerAttributesFrom(this);
        }

        return mob;
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
}
