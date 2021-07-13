package mca.entity;

import com.mojang.serialization.Dynamic;

import mca.api.API;
import mca.api.types.Hair;
import mca.cobalt.minecraft.network.datasync.*;
import mca.core.MCA;
import mca.core.minecraft.*;
import mca.entity.ai.VillagerNavigation;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.data.*;
import mca.enums.*;
import mca.util.InventoryUtils;
import mca.util.WorldUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Stream;

public class VillagerEntityMCA extends VillagerEntity implements NamedScreenHandlerFactory, Infectable, Messenger {

    private final CDataManager data = new CDataManager(this);

    private final Genetics genetics = Genetics.create(data);
    private final Pregnancy pregnancy = new Pregnancy(this, data);
    private final Residency residency = new Residency(this, data);
    private final VillagerBrain mcaBrain = new VillagerBrain(this, data);
    private final Interactions interactions = new Interactions(this);

    public final SimpleInventory inventory = new SimpleInventory(27);

    public final CStringParameter villagerName = data.newString("villagerName");
    public final CStringParameter clothes = data.newString("clothes");
    public final CStringParameter hair = data.newString("hair");
    public final CStringParameter hairOverlay = data.newString("hairOverlay");

    public final CEnumParameter<AgeState> ageState = data.newEnum("ageState", AgeState.UNASSIGNED);

    public final CStringParameter spouseName = data.newString("spouseName");
    public final CUUIDParameter spouseUUID = data.newUUID("spouseUUID");

    public final CEnumParameter<MarriageState> marriageState = data.newEnum("marriageState", MarriageState.NOT_MARRIED);

    public final CBooleanParameter isInfected = data.newBoolean("isInfected");

    //gift desaturation queue
    final List<String> giftDesaturation = new LinkedList<>();

    public final CBooleanParameter isProcreating = data.newBoolean("isProcreating");
    public int procreateTick = -1;

    public VillagerEntityMCA(EntityType<VillagerEntityMCA> type, World w, Gender gender) {
        super(type, w);
        inventory.addListener(this::onInvChange);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager requires those fields
        data.register();
        this.genetics.setGender(gender);

        // TODO: sounds
        this.setSilent(true);
    }

    public static DefaultAttributeContainer.Builder createVillagerAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new VillagerNavigation(this, world);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return VillagerBrain.initializeTasks(this, VillagerBrain.createProfile().deserialize(dynamic));
    }

    @Override
    public void reinitializeBrain(ServerWorld world) {
        Brain<VillagerEntityMCA> brain = getMCABrain();
        brain.stopAllTasks(world, this);
        //copyWithoutBehaviors will copy the memories of the old brain to the new brain
        this.brain = brain.copy();
        VillagerBrain.initializeTasks(this, getMCABrain());
    }

    @SuppressWarnings("unchecked")
    public Brain<VillagerEntityMCA> getMCABrain() {
        return (Brain<VillagerEntityMCA>) brain;
    }

    public Genetics getGenetics() {
        return genetics;
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

            villagerName.set(API.getVillagePool().pickCitizenName(getGenetics().getGender()));

            initializeSkin();

            mcaBrain.randomize();
        }

        calculateDimensions();

        return data;
    }

    public final VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }

    public final void setProfession(VillagerProfession profession) {
        setVillagerData(getVillagerData().withProfession(profession));
    }

    public boolean isProfessionImportant() {
        return getProfession() == ProfessionsMCA.GUARD;
    }

    @Override
    public void setVillagerData(VillagerData data) {
        if (getProfession() != data.getProfession()) {
            clothes.set(API.getClothingPool().pickOne(this));
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
                        knockback / 2, MathHelper.sin(getYaw() * ((float)Math.PI / 180F)),
                        -MathHelper.cos(this.getYaw() * ((float) Math.PI / 180F))
                );

                setVelocity(getVelocity().multiply(0.6D, 1, 0.6));
            }

            applyDamageEffects(this, target);
            onAttacking(target);
        }

        return damageDealt;
    }

    @Override
    public final ActionResult interactAt(PlayerEntity player, Vec3d pos, @NotNull Hand hand) {
        return interactions.interactAt(player, pos, hand);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        data.load(nbt);

        //load gift desaturation queue
        NbtList res = nbt.getList("giftDesaturation", 8);
        for (int i = 0; i < res.size(); i++) {
            String c = res.getString(i);
            giftDesaturation.add(c);
        }

        //set speed
        float speed = mcaBrain.getPersonality().getSpeedModifier();

        //width and size impact
        speed /= genetics.width.get();
        speed *= genetics.skin.get();

        setMovementSpeed(speed);
        InventoryUtils.readFromNBT(inventory, nbt);
    }

    @Override
    public final void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        data.save(nbt);

        InventoryUtils.saveToNBT(inventory, nbt);

        //save gift desaturation queue
        NbtList giftDesaturationQueue = new NbtList();
        for (int i = 0; i < giftDesaturation.size(); i++) {
            giftDesaturationQueue.addElement(i, NbtString.of(giftDesaturation.get(i)));
        }
        nbt.put("giftDesaturation", giftDesaturationQueue);
    }

    private void initializeSkin() {
        clothes.set(API.getClothingPool().pickOne(this));

        Hair h = API.getHairPool().pickOne(this);
        hair.set(h.texture());
        hairOverlay.set(h.overlay());
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
                sendChatMessage((PlayerEntity)source.getAttacker(), "villager.hurt");
            }

            if (source.getSource() instanceof ZombieEntity
                    && getProfession() != ProfessionsMCA.GUARD
                    && MCA.getConfig().enableInfection
                    && random.nextFloat() < MCA.getConfig().infectionChance / 100.0) {
                isInfected.set(true);
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
    }

    @Override
    public void tick() {
        super.tick();

        this.calculateDimensions();

        if (world.isClient) {
            if (isProcreating.get()) {
                this.headYaw += 50.0F;
            }

            if (age % 20 == 0) {
                mcaBrain.clientTick();
            }
        } else {
            onEachServerUpdate();
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {

        if (pose == EntityPose.SLEEPING) {
            return SLEEPING_DIMENSIONS;
        }

        float height = genetics.getVerticalScaleFactor() * 1.9F;
        float width = genetics.getHorizontalScaleFactor() * 0.65F;

        return EntityDimensions.changing(width, height);
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (world.isClient) {
            return;
        }

        InventoryUtils.dropAllItems(this, inventory);

        //The death of a villager negatively modifies the mood of nearby villagers
        WorldUtils
            .getCloseEntities(world, this, 32, VillagerEntityMCA.class)
            .forEach(villager -> villager.onNeighbourDeath(cause));

        getSpouse().ifPresent(spouse -> {
            // Notify spouse of the death
            if (spouse instanceof VillagerEntityMCA) {
                ((VillagerEntityMCA) spouse).endMarriage();
            } else {
                PlayerSaveData.get(world, spouse.getUuid()).endMarriage();
            }
        });

        // Notify all parents of the death
        // Entity[] parents = getBothParentEntities();
        //TODO, optionally affect parents behavior

        SavedVillagers.get(world).saveVillager(this);
    }

    public void onNeighbourDeath(DamageSource cause) {
        mcaBrain.modifyMoodLevel(-10);
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

    @Override
    public final Text getDisplayName() {
        Text name = super.getDisplayName();

        MoveState state = getVillagerBrain().getMoveState();
        if (state != MoveState.MOVE) {
            return new LiteralText("").append(name).append(" (").append(getVillagerBrain().getMoveState().getName()).append(")").setStyle(name.getStyle());
        }
        return name;
    }

    @Override
    public final Text getDefaultName() {
        return new LiteralText(villagerName.get());
    }

    @Override
    public boolean isInfected() {
        return isInfected.get();
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

    public boolean isMarried() {
        return !spouseUUID.get().orElse(Util.NIL_UUID).equals(Util.NIL_UUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return spouseUUID.get().orElse(Util.NIL_UUID).equals(uuid);
    }

    public void marry(PlayerEntity player) {
        spouseUUID.set(player.getUuid());
        spouseName.set(player.getName().asString());
        marriageState.set(MarriageState.MARRIED_TO_PLAYER);
    }

    public void marry(VillagerEntityMCA spouse) {
        spouseUUID.set(spouse.getUuid());
        spouseName.set(spouse.villagerName.get());
        marriageState.set(MarriageState.MARRIED);
    }

    private void endMarriage() {
        spouseUUID.set(Util.NIL_UUID);
        spouseName.set("");
        marriageState.set(MarriageState.NOT_MARRIED);
    }

    public Optional<Entity> getSpouse() {
        return spouseUUID.get().map(id -> ((ServerWorld) world).getEntity(id));
    }

    public Hair getHair() {
        return new Hair(hair.get(), hairOverlay.get());
    }

    public void setHair(Hair hair) {
        this.hair.set(hair.texture());
        this.hairOverlay.set(hair.overlay());
    }

    public Pregnancy getPregnancy() {
        return pregnancy;
    }

    // we make it public here
    @Override
    public void produceParticles(ParticleEffect parameters) {
        super.produceParticles(parameters);
    }

    private void onEachServerUpdate() {
        // Every second
        if (age % 20 == 0) {
         // villager has a baby
            pregnancy.tick();
            mcaBrain.think();
        }

        residency.updateVillage();

        // Every 10 seconds and when we're not already dead
        if (this.age % 200 == 0 && this.getHealth() > 0.0F) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F); // heal
            }
        }

        //check if another state has been reached
        AgeState last = ageState.get();
        AgeState next = AgeState.byCurrentAge(getBreedingAge());

        if (last != next) {
            ageState.set(next);
            calculateDimensions();

            if (next == AgeState.ADULT) {
                // Notify player parents of the age up and set correct dialogue type.
                getParents().filter(e -> e instanceof PlayerEntity).map(e -> (PlayerEntity) e).forEach(p -> {
                    mcaBrain.getMemoriesForPlayer(p).setDialogueType(DialogueType.ADULT);
                    sendEventMessage(new TranslatableText("notify.child.grownup", villagerName.get()), p);
                });
            }
        }

        if (getProfession() == ProfessionsMCA.CHILD && this.getAgeState() == AgeState.ADULT) {
            setProfession(API.randomProfession());
        }
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, inventory);
    }

    public AgeState getAgeState() {
        return ageState.get();
    }

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
        if (ageState != null && (ageState.getParam().equals(par) || genetics.size.equals(par))) {
            calculateDimensions();
        }

        super.onTrackedDataSet(par);
    }

    @Override
    public SimpleInventory getInventory() {
        return this.inventory;
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
        if (id == 15) {
            this.world.addImportantParticle(ParticleTypesMCA.NEG_INTERACTION, true, this.getX(), this.getEyeY() + 0.5, this.getZ(), 0, 0, 0);
        } else if (id == 16) {
            this.world.addImportantParticle(ParticleTypesMCA.POS_INTERACTION, true, this.getX(), this.getEyeY() + 0.5, this.getZ(), 0, 0, 0);
        } else {
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
        this.setBreedingAge(isBaby ? -AgeState.startingAge : 0);
    }

    @Override
    public boolean canBeTargettedBy(Entity mob) {
        return !this.isInfected.get();
    }

    //TODO: Reputation
    @Override
    public int getReputation(PlayerEntity player) {
        return super.getReputation(player);
    }

    public FamilyTree getFamilyTree() {
        return FamilyTree.get(world);
    }

    public FamilyTreeEntry getFamilyTreeEntry() {
        return getFamilyTree().getEntry(this);
    }

    public Stream<Entity> getParents() {
        ServerWorld serverWorld = (ServerWorld) world;
        FamilyTreeEntry entry = getFamilyTreeEntry();

        return Stream.of(
                serverWorld.getEntity(entry.getFather()),
                serverWorld.getEntity(entry.getMother())
        ).filter(Objects::nonNull);
    }
}