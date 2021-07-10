package mca.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import mca.api.API;
import mca.api.types.APIButton;
import mca.api.types.Hair;
import mca.client.gui.GuiInteract;
import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.minecraft.network.datasync.*;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.*;
import mca.entity.ai.brain.VillagerTasksMCA;
import mca.entity.data.*;
import mca.enums.*;
import mca.items.SpecialCaseGift;
import mca.util.InventoryUtils;
import mca.util.Util;
import mca.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerGossips;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Stream;

public class VillagerEntityMCA extends VillagerEntity implements NamedScreenHandlerFactory {
    public static final String[] GENES_NAMES = new String[]{
            "gene_size", "gene_width", "gene_breast", "gene_melanin", "gene_hemoglobin", "gene_eumelanin", "gene_pheomelanin", "gene_skin", "gene_face"};
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.HOME, MemoryModuleType.JOB_SITE,
            MemoryModuleType.POTENTIAL_JOB_SITE,
            MemoryModuleType.MEETING_POINT,
            MemoryModuleType.MOBS,
            MemoryModuleType.VISIBLE_MOBS,
            MemoryModuleType.VISIBLE_VILLAGER_BABIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.PATH,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.NEAREST_BED,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.NEAREST_HOSTILE,
            MemoryModuleType.SECONDARY_JOB_SITE,
            MemoryModuleType.HIDING_PLACE,
            MemoryModuleType.HEARD_BELL_TIME,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.LAST_SLEPT,
            MemoryModuleType.LAST_WOKEN,
            MemoryModuleType.LAST_WORKED_AT_POI,
            MemoryModuleType.GOLEM_DETECTED_RECENTLY,
            MemoryModuleTypeMCA.PLAYER_FOLLOWING,
            MemoryModuleTypeMCA.STAYING
    );

    private static final ImmutableList<SensorType<? extends Sensor<? super VillagerEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_BED,
            SensorType.HURT_BY,
            SensorType.VILLAGER_HOSTILES,
            SensorType.VILLAGER_BABIES,
            SensorType.SECONDARY_POIS,
            SensorType.GOLEM_DETECTED
    );

    public final CDataManager data = new CDataManager(this);
    public final SimpleInventory inventory;
    public final CStringParameter villagerName = data.newString("villagerName");
    public final CStringParameter clothes = data.newString("clothes");
    public final CStringParameter hair = data.newString("hair");
    public final CStringParameter hairOverlay = data.newString("hairOverlay");
    public final CIntegerParameter gender = data.newInteger("gender");
    public final CTagParameter memories = data.newTag("memories");
    public final CIntegerParameter moveState = data.newInteger("moveState");
    public final CIntegerParameter ageState = data.newInteger("ageState");
    public final CStringParameter spouseName = data.newString("spouseName");
    public final CUUIDParameter spouseUUID = data.newUUID("spouseUUID");
    public final CIntegerParameter marriageState = data.newInteger("marriageState");
    public final CBooleanParameter isProcreating = data.newBoolean("isProcreating");
    public final CBooleanParameter isInfected = data.newBoolean("isInfected");
    public final CIntegerParameter activeChore = data.newInteger("activeChore");
    public final CBooleanParameter hasBaby = data.newBoolean("hasBaby");
    public final CBooleanParameter isBabyMale = data.newBoolean("isBabyMale");
    public final CIntegerParameter babyAge = data.newInteger("babyAge");
    public final CUUIDParameter choreAssigningPlayer = data.newUUID("choreAssigningPlayer");
    public final BlockPosParameter hangoutPos = data.newPos("hangoutPos");
    public final CBooleanParameter importantProfession = data.newBoolean("importantProfession", false);

    // genes
    public final CFloatParameter gene_size = data.newFloat("gene_size");
    public final CFloatParameter gene_width = data.newFloat("gene_width");
    public final CFloatParameter gene_breast = data.newFloat("gene_breast");
    public final CFloatParameter gene_melanin = data.newFloat("gene_melanin");
    public final CFloatParameter gene_hemoglobin = data.newFloat("gene_hemoglobin");
    public final CFloatParameter gene_eumelanin = data.newFloat("gene_eumelanin");
    public final CFloatParameter gene_pheomelanin = data.newFloat("gene_pheomelanin");
    public final CFloatParameter gene_skin = data.newFloat("gene_skin");
    public final CFloatParameter gene_face = data.newFloat("gene_face");

    // genes list
    public final CFloatParameter[] GENES = new CFloatParameter[]{
            gene_size, gene_width, gene_breast, gene_melanin, gene_hemoglobin, gene_eumelanin, gene_pheomelanin, gene_skin, gene_face};

    //personality and mood
    public final CIntegerParameter personality = data.newInteger("personality");
    public final CIntegerParameter mood = data.newInteger("mood");

    public final CIntegerParameter village = data.newInteger("village", -1);
    public final CIntegerParameter building = data.newInteger("buildings", -1);
    private final VillagerGossips gossips = new VillagerGossips();
    //gift desaturation queue
    private final List<String> giftDesaturation = new LinkedList<>();
    public int procreateTick = -1;
    @Nullable
    private PlayerEntity interactingPlayer;
    private long lastGossipTime;
    private long lastGossipDecayTime;

    public VillagerEntityMCA(World w) {
        super(EntitiesMCA.VILLAGER, w);

        inventory = new SimpleInventory(27);
        inventory.addListener(this::onInvChange);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager required those fields
        data.register();

        this.setSilent(true);

        if (!world.isClient) {
            Gender eGender = Gender.getRandom();
            gender.set(eGender.getId());

            villagerName.set(API.getRandomName(eGender));
        }
    }

    public static DefaultAttributeContainer.Builder createVillagerAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D);
    }

    protected Brain.Profile<VillagerEntityMCA> mcaBrainProvider() {
        return Brain.createProfile(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        Brain<VillagerEntityMCA> brain = this.mcaBrainProvider().deserialize(dynamic);
        this.initBrain(brain);
        return brain;
    }

    @Override
    public void reinitializeBrain(ServerWorld world) {
        Brain<VillagerEntityMCA> brain = this.getMCABrain();
        brain.stopAllTasks(world, this);
        //copyWithoutBehaviors will copy the memories of the old brain to the new brain
        this.brain = brain.copy();
        this.initBrain(this.getMCABrain());
    }

    public Brain<VillagerEntityMCA> getMCABrain() {
        //generics amirite
        return (Brain<VillagerEntityMCA>) this.brain;
    }

    private void initBrain(Brain<VillagerEntityMCA> brain) {
        VillagerProfession villagerprofession = this.getVillagerData().getProfession();
        if (this.isBaby()) {
            brain.setSchedule(Schedule.VILLAGER_BABY);
            brain.setTaskList(Activity.PLAY, VillagerTasksMCA.getPlayPackage(0.5F));
        } else {
            brain.setSchedule(Schedule.VILLAGER_DEFAULT);
            brain.setTaskList(Activity.WORK, VillagerTasksMCA.getWorkPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)));
        }

        brain.setTaskList(Activity.CORE, VillagerTasksMCA.getCorePackage(villagerprofession, 0.5F));
        brain.setTaskList(Activity.MEET, VillagerTasksMCA.getMeetPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT)));
        brain.setTaskList(Activity.REST, VillagerTasksMCA.getRestPackage(villagerprofession, 0.5F));
        brain.setTaskList(Activity.IDLE, VillagerTasksMCA.getIdlePackage(villagerprofession, 0.5F));
        brain.setTaskList(Activity.PANIC, VillagerTasksMCA.getPanicPackage(villagerprofession, 0.5F));
        brain.setTaskList(Activity.PRE_RAID, VillagerTasksMCA.getPreRaidPackage(villagerprofession, 0.5F));
        brain.setTaskList(Activity.RAID, VillagerTasksMCA.getRaidPackage(villagerprofession, 0.5F));
        brain.setTaskList(Activity.HIDE, VillagerTasksMCA.getHidePackage(villagerprofession, 0.5F));
        brain.setTaskList(ActivityMCA.CHORE, VillagerTasksMCA.getChorePackage(villagerprofession, 0.5F));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.doExclusively(Activity.IDLE);
        brain.refreshActivities(this.world.getTimeOfDay(), this.world.getTime());
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public EntityData initialize(ServerWorldAccess p_213386_1_, LocalDifficulty p_213386_2_, SpawnReason p_213386_3_, @Nullable EntityData p_213386_4_, @Nullable NbtCompound p_213386_5_) {
        EntityData iLivingEntityData = super.initialize(p_213386_1_, p_213386_2_, p_213386_3_, p_213386_4_, p_213386_5_);

        initializeGenes();
        initializeSkin();
        initializePersonality();

        return iLivingEntityData;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    public final VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }

    public final void setProfession(VillagerProfession profession) {
        this.setVillagerData(this.getVillagerData().withProfession(profession));
        reinitializeBrain((ServerWorld) world);
        clothes.set(API.getRandomClothing(this));
    }

    @Override
    public boolean tryAttack(Entity target) {
        //villager is peaceful and wont hurt as long as not necessary
        if (getPersonality() == Personality.PEACEFUL && getHealth() == getMaxHealth()) {
            return false;
        }

        //we don't use attributes
        float damage = getProfession() == ProfessionsMCA.GUARD ? 9.0F : 3.0F;
        float knockback = 3.0F;

        //personality bonus
        if (getPersonality() == Personality.WEAK) {
            damage *= 0.75;
        } else if (getPersonality() == Personality.CONFIDENT) {
            damage *= 1.25;
        } else if (getPersonality() == Personality.STRONG) {
            damage *= 1.5;
        }

        //enchantment
        if (target instanceof LivingEntity) {
            damage += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity) target).getGroup());
            knockback += (float) EnchantmentHelper.getKnockback(this);
        }

        //fire aspect
        int i = EnchantmentHelper.getFireAspect(this);
        if (i > 0) {
            target.setOnFireFor(i * 4);
        }

        boolean damageDealt = target.damage(DamageSource.mob(this), damage);

        //knockback and post damage stuff
        if (damageDealt) {
            if (knockback > 0.0F && target instanceof LivingEntity) {
                ((LivingEntity) target).takeKnockback(knockback * 0.5F, MathHelper.sin(this.yaw * ((float) Math.PI / 180F)), -MathHelper.cos(this.yaw * ((float) Math.PI / 180F)));
                this.setVelocity(this.getVelocity().multiply(0.6D, 1.0D, 0.6D));
            }

            this.applyDamageEffects(this, target);
            this.onAttacking(target);
        }

        return damageDealt;
    }

    private void openScreen(PlayerEntity player) {
        MinecraftClient.getInstance().openScreen(new GuiInteract(this, player));
    }

    @Override
    public final ActionResult interactAt(PlayerEntity player, Vec3d pos, @Nonnull Hand hand) {
        if (world.isClient) {
            openScreen(player);
            return ActionResult.SUCCESS;
        } else {
            this.setInteractingPlayer(player);
            return ActionResult.PASS;
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        data.load(CNBT.fromMC(nbt));

        //load gift desaturation queue
        NbtList res = nbt.getList("giftDesaturation", 8);
        for (int i = 0; i < res.size(); i++) {
            String c = res.getString(i);
            giftDesaturation.add(c);
        }

        //set speed
        float speed = 1.0f;

        //personality bonuses
        if (getPersonality() == Personality.ATHLETIC) speed *= 1.15;
        if (getPersonality() == Personality.SLEEPY) speed *= 0.8;

        //width and size impact
        speed /= gene_width.get();
        speed *= gene_skin.get();

        setMovementSpeed(speed);
        InventoryUtils.readFromNBT(inventory, nbt);

        NbtList listnbt = nbt.getList("Gossips", 10);
        this.lastGossipDecayTime = nbt.getLong("LastGossipDecay");

        this.gossips.deserialize(new Dynamic<>(NbtOps.INSTANCE, listnbt));
    }

    @Override
    public final void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        data.save(CNBT.fromMC(nbt));

        InventoryUtils.saveToNBT(inventory, nbt);

        //save gift desaturation queue
        NbtList giftDesaturationQueue = new NbtList();
        for (int i = 0; i < giftDesaturation.size(); i++) {
            giftDesaturationQueue.addElement(i, NbtString.of(giftDesaturation.get(i)));
        }
        nbt.put("giftDesaturation", giftDesaturationQueue);

        nbt.put("Gossips", this.gossips.serialize(NbtOps.INSTANCE).getValue());
        nbt.putLong("LastGossipDecay", this.lastGossipDecayTime);
    }

    private void initializeSkin() {
        clothes.set(API.getRandomClothing(this));

        Hair h = API.getRandomHair(this);
        hair.set(h.getTexture());
        hairOverlay.set(h.getOverlay());
    }

    private void initializePersonality() {
        personality.set(Personality.getRandom().getId());
        //since minLevel is -100 and it makes no
        mood.set(Mood.getLevel(random.nextInt(Mood.maxLevel - Mood.normalMinLevel + 1) + Mood.normalMinLevel));
    }

    //returns a float between 0 and 1, weighted at 0.5
    private float centeredRandom() {
        return (float) Math.min(1.0, Math.max(0.0, (random.nextFloat() - 0.5f) * (random.nextFloat() - 0.5f) + 0.5f));
    }

    //initializes the genes with random numbers
    private void initializeGenes() {
        for (CFloatParameter dp : GENES) {
            dp.set(random.nextFloat());
        }

        // size is more centered
        gene_size.set(centeredRandom());
        gene_width.set(centeredRandom());

        // temperature
        float temp = world.getBiome(getLandingPos()).getTemperature();

        // immigrants
        if (random.nextInt(100) < MCA.getConfig().immigrantChance) {
            temp = random.nextFloat() * 2.0f - 0.5f;
        }

        // melanin
        gene_melanin.set(Util.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.4f + 0.1f));
        gene_hemoglobin.set(Util.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.4f + 0.1f));

        // TODO hair tend to have similar values than hair, but the used LUT is a little bit random
        gene_eumelanin.set(random.nextFloat());
        gene_pheomelanin.set(random.nextFloat());
    }

    //interpolates and mutates the genes from two parent villager
    public void inheritGenes(VillagerEntityMCA mother, VillagerEntityMCA father) {
        for (int i = 0; i < GENES.length; i++) {
            float m = mother.GENES[i].get();
            float f = father.GENES[i].get();
            float interpolation = random.nextFloat();
            float mutation = (random.nextFloat() - 0.5f) * 0.2f;
            float g = m * interpolation + f * (1.0f - interpolation) + mutation;
            GENES[i].set((float) Math.min(1.0, Math.max(0.0, g)));
        }
    }

    @Override
    public final boolean damage(DamageSource source, float damageAmount) {
        // Guards take 50% less damage
        if (getProfession() == ProfessionsMCA.GUARD) {
            damageAmount *= 0.5;
        }

        //personality bonus
        if (getPersonality() == Personality.TOUGH) damageAmount *= 0.5;
        if (getPersonality() == Personality.FRAGILE) damageAmount *= 1.25;

        if (!world.isClient) {
            if (source.getAttacker() instanceof PlayerEntity) {
                PlayerEntity p = (PlayerEntity) source.getAttacker();
                sendMessageTo(MCA.localize("villager.hurt"), p);
            }

            if (source.getSource() instanceof ZombieEntity && getProfession() != ProfessionsMCA.GUARD && MCA.getConfig().enableInfection && random.nextFloat() < MCA.getConfig().infectionChance / 100.0) {
                isInfected.set(true);
            }
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

        if (world.isClient) {
            onEachClientUpdate();
        } else {
            onEachServerUpdate();
        }

        this.decayGossip();
    }

    public void sendMessageTo(String message, Entity receiver) {
        receiver.sendSystemMessage(new LiteralText(message), receiver.getUuid());
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (!world.isClient) {
            //The death of a villager negatively modifies the mood of nearby villagers
            for (VillagerEntityMCA villager : WorldUtils.getCloseEntities(world, this, 32.0D, VillagerEntityMCA.class)) {
                villager.modifyMoodLevel(-10);
            }

            InventoryUtils.dropAllItems(this, inventory);

            if (isMarried()) {
                UUID spouse = spouseUUID.get().orElse(Constants.ZERO_UUID);
                Entity sp = ((ServerWorld) world).getEntity(spouse);
                PlayerSaveData playerSaveData = PlayerSaveData.get(world, spouse);

                // Notify spouse of the death
                if (sp instanceof VillagerEntityMCA) {
                    ((VillagerEntityMCA) sp).endMarriage();
                } else {
                    playerSaveData.endMarriage();
                }
            }

            // Notify all parents of the death
            // Entity[] parents = getBothParentEntities();
            //TODO, optionally affect parents behavior

            SavedVillagers.get(world).saveVillager(this);
        }
    }

    @Override
    public final SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Override
    protected final SoundEvent getAmbientSound() {
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
        BaseText name = new LiteralText(villagerName.get());
        if (this.brain.getOptionalMemory(MemoryModuleTypeMCA.STAYING).isPresent()) {
            name.append(new LiteralText("(Staying)"));
        }
        return name;
    }

    @Override
    public final Text getCustomName() {
        return new LiteralText(villagerName.get());
    }

    public Memories getMemoriesForPlayer(PlayerEntity player) {
        CNBT cnbt = memories.get();
        CNBT compoundTag = cnbt.getCompoundTag(player.getUuid().toString());
        Memories returnMemories = Memories.fromCNBT(this, compoundTag);
        if (returnMemories == null) {
            returnMemories = Memories.getNew(this, player.getUuid());
            memories.set(memories.get().setTag(player.getUuid().toString(), returnMemories.toCNBT()));
        }
        return returnMemories;
    }

    public Personality getPersonality() {
        return Personality.getById(personality.get());
    }

    public Mood getMood() {
        return getPersonality().getMoodGroup().getMood(mood.get());
    }

    public void modifyMoodLevel(int mood) {
        this.mood.set(Mood.getLevel(this.getMoodLevel() + mood));
    }

    public int getMoodLevel() {
        return mood.get();
    }

    private void goHome(PlayerEntity player) {
        if (getHome().equals(BlockPos.ORIGIN)) {
            say(player, "interaction.gohome.fail");
        } else {
            BlockPos home = getHome();
            this.moveTowards(home);
            say(player, "interaction.gohome.success");
        }
    }

    public BlockPos getWorkplace() {
        Optional<GlobalPos> home = this.brain.getOptionalMemory(MemoryModuleType.JOB_SITE);
        return home.map(GlobalPos::getPos).orElse(BlockPos.ORIGIN);
    }

    public void setWorkplace(PlayerEntity player) {
        say(player, "interaction.setworkplace.success");
        this.brain.remember(MemoryModuleType.JOB_SITE, GlobalPos.create(player.world.getRegistryKey(), player.getBlockPos()));
    }

    public BlockPos getHangout() {
        return hangoutPos.get();
    }

    public void setHangout(PlayerEntity player) {
        say(player, "interaction.sethangout.success");
        hangoutPos.set(player.getBlockPos());
    }

    public BlockPos getHome() {
        Optional<GlobalPos> home = this.brain.getOptionalMemory(MemoryModuleType.HOME);
        return home.map(GlobalPos::getPos).orElse(BlockPos.ORIGIN);
    }

    private void setHome(PlayerEntity player) {
        //check if it is a bed
        if (setHome(player.getBlockPos(), player.world)) {
            say(player, "interaction.sethome.success");
        } else {
            say(player, "interaction.sethome.fail");
        }
    }

    private void clearHome() {
        ServerWorld serverWorld = ((ServerWorld) world);
        PointOfInterestStorage poiManager = serverWorld.getPointOfInterestStorage();
        Optional<GlobalPos> bed = this.brain.getOptionalMemory(MemoryModuleType.HOME);
        bed.ifPresent(globalPos -> {
            if (poiManager.hasTypeAt(PointOfInterestType.HOME, globalPos.getPos())) {
                poiManager.releaseTicket(globalPos.getPos());
            }
        });
    }

    private boolean setHome(BlockPos pos, World world) {
        clearHome();

        ServerWorld serverWorld = ((ServerWorld) world);
        PointOfInterestStorage poiManager = serverWorld.getPointOfInterestStorage();

        //check if it is a bed
        if (world.getBlockState(pos).isOf(BlockTags.BEDS)) {
            brain.remember(MemoryModuleType.HOME, GlobalPos.create(world.getRegistryKey(), pos));
            poiManager.getPosition(PointOfInterestType.HOME.getCompletionCondition(), (p) -> p.equals(pos), pos, 1);
            serverWorld.sendEntityStatus(this, (byte) 14);

            return true;
        } else {
            return false;
        }
    }

    public void say(PlayerEntity target, String phraseId, String... params) {
        ArrayList<String> paramList = new ArrayList<>();
        Collections.addAll(paramList, params);

        // Player is always first in params passed to localizer for say().
        paramList.add(0, target.getName().getString());

        String chatPrefix = MCA.getConfig().villagerChatPrefix + getDisplayName().getString() + ": ";
        if (isInfected.get()) { // Infected villagers do not speak
            sendMessageTo(chatPrefix + "???", target);
            playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, this.getSoundVolume(), this.getSoundPitch());
        } else {
            DialogueType dialogueType = getMemoriesForPlayer(target).getDialogueType();
            String localizedText = MCA.getLocalizer().localize(dialogueType.getName() + "." + phraseId, "generic." + phraseId, paramList);
            sendMessageTo(chatPrefix + localizedText, target);
        }
    }

    public boolean isMarried() {
        return !spouseUUID.get().orElse(Constants.ZERO_UUID).equals(Constants.ZERO_UUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return spouseUUID.get().orElse(Constants.ZERO_UUID).equals(uuid);
    }

    public void marry(PlayerEntity player) {
        spouseUUID.set(player.getUuid());
        spouseName.set(player.getName().asString());
        marriageState.set(MarriageState.MARRIED_TO_PLAYER.getId());
    }

    public void marry(VillagerEntityMCA spouse) {
        spouseUUID.set(spouse.getUuid());
        spouseName.set(spouse.villagerName.get());
        marriageState.set(MarriageState.MARRIED.getId());
    }

    private void endMarriage() {
        spouseUUID.set(Constants.ZERO_UUID);
        spouseName.set("");
        marriageState.set(MarriageState.NOT_MARRIED.getId());
    }

    private void handleInteraction(PlayerEntity player, Memories memory, APIButton button) {
        //interaction
        String interactionName = button.getIdentifier().replace("gui.button.", "");
        Interaction interaction = Interaction.fromName(interactionName);

        //success chance and hearts
        float successChance = 0.85F;
        int heartsBoost = 5;
        if (interaction != null) {
            heartsBoost = interaction.getHearts(this);
            successChance = interaction.getSuccessChance(this, memory) / 100.0f;
        }

        boolean succeeded = random.nextFloat() < successChance;

        //spawn particles
        if (succeeded) {
            this.world.sendEntityStatus(this, (byte) 16);
        } else {
            this.world.sendEntityStatus(this, (byte) 15);

            //sensitive people doubles the loss
            if (getPersonality() == Personality.SENSITIVE) {
                heartsBoost *= 2;
            }
        }

        memory.modInteractionFatigue(1);
        memory.modHearts(succeeded ? heartsBoost : -heartsBoost);
        modifyMoodLevel(succeeded ? heartsBoost : -heartsBoost);

        String responseId = String.format("%s.%s", interactionName, succeeded ? "success" : "fail");
        say(player, responseId);
        closeGUIIfOpen();
    }

    public void handleInteraction(PlayerEntity player, String guiKey, String buttonId) {
        Memories memory = getMemoriesForPlayer(player);
        java.util.Optional<APIButton> button = API.getButtonById(guiKey, buttonId);
        if (!button.isPresent()) {
            MCA.log("Button not found for key and ID: " + guiKey + ", " + buttonId);
        } else if (button.get().isInteraction()) handleInteraction(player, memory, button.get());

        Hair h;
        switch (buttonId) {
            case "gui.button.move":
                this.brain.forget(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
                this.brain.forget(MemoryModuleTypeMCA.STAYING);
                updateMoveState();
                closeGUIIfOpen();
                break;
            case "gui.button.stay":
                this.brain.forget(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
                this.brain.remember(MemoryModuleTypeMCA.STAYING, true);
                updateMoveState();
                closeGUIIfOpen();

                break;
            case "gui.button.follow":
                this.brain.remember(MemoryModuleTypeMCA.PLAYER_FOLLOWING, player);
                this.brain.forget(MemoryModuleTypeMCA.STAYING);
                stopChore();
                updateMoveState();
                closeGUIIfOpen();
                break;
            case "gui.button.ridehorse":
//                toggleMount(player);
                closeGUIIfOpen();
                break;
            case "gui.button.sethome":
                setHome(player);
                closeGUIIfOpen();
                break;
            case "gui.button.gohome":
                goHome(player);
                closeGUIIfOpen();
                break;
            case "gui.button.setworkplace":
                setWorkplace(player);
                closeGUIIfOpen();
                break;
            case "gui.button.sethangout":
                setHangout(player);
                closeGUIIfOpen();
                break;
            case "gui.button.trade":
                this.sendOffers(player, this.getDisplayName(), this.getVillagerData().getLevel());
                this.prepareOffersFor(player);
                this.setCurrentCustomer(player);

                break;
            case "gui.button.inventory":
                player.openHandledScreen(this);
                break;
            case "gui.button.gift":
                ItemStack stack = player.getMainHandStack();
                if (!stack.isEmpty()) {
                    int giftValue = API.getGiftValueFromStack(stack);
                    if (!handleSpecialCaseGift(player, stack)) {
                        if (stack.getItem() == Items.GOLDEN_APPLE) {
                            //TODO special
                            isInfected.set(false);
                        } else {
                            String id = stack.getTranslationKey();
                            long occurrences = giftDesaturation.stream().filter(e -> e.equals(id)).count();

                            //check if desaturation fail happen
                            if (random.nextInt(100) < occurrences * MCA.getConfig().giftDesaturationPenalty) {
                                giftValue = -giftValue / 2;
                                say(player, API.getResponseForSaturatedGift(stack));
                            } else {
                                say(player, API.getResponseForGift(stack));
                            }

                            //modify mood and hearts
                            modifyMoodLevel(giftValue / 2 + 2 * MathHelper.sign(giftValue));
                            memory.modHearts(giftValue);
                        }
                    }

                    //add to desaturation queue
                    giftDesaturation.add(stack.getTranslationKey());
                    while (giftDesaturation.size() > MCA.getConfig().giftDesaturationQueueLength) {
                        giftDesaturation.remove(0);
                    }

                    //particles
                    if (giftValue > 0) {
                        player.getMainHandStack().decrement(1);
                        this.world.sendEntityStatus(this, (byte) 16);
                    } else {
                        this.world.sendEntityStatus(this, (byte) 15);
                    }
                }
                closeGUIIfOpen();
                break;
            case "gui.button.procreate":
                if (PlayerSaveData.get(world, player.getUuid()).isBabyPresent()) {
                    say(player, "interaction.procreate.fail.hasbaby");
                } else if (memory.getHearts() < 100) {
                    say(player, "interaction.procreate.fail.lowhearts");
                } else {
                    procreateTick = 60;
                    isProcreating.set(true);
                }
                closeGUIIfOpen();
                break;
            case "gui.button.infected":
                isInfected.set(!isInfected.get());
                break;
            case "gui.button.clothing.randClothing":
                clothes.set(API.getRandomClothing(this));
                break;
            case "gui.button.clothing.prevClothing":
                clothes.set(API.getNextClothing(this, clothes.get(), -1));
                break;
            case "gui.button.clothing.nextClothing":
                clothes.set(API.getNextClothing(this, clothes.get()));
                break;
            case "gui.button.clothing.randHair":
                h = API.getRandomHair(this);
                hair.set(h.getTexture());
                hairOverlay.set(h.getOverlay());
                break;
            case "gui.button.clothing.prevHair":
                h = API.getNextHair(this, new Hair(hair.get(), hairOverlay.get()), -1);
                hair.set(h.getTexture());
                hairOverlay.set(h.getOverlay());
                break;
            case "gui.button.clothing.nextHair":
                h = API.getNextHair(this, new Hair(hair.get(), hairOverlay.get()));
                hair.set(h.getTexture());
                hairOverlay.set(h.getOverlay());
                break;
            case "gui.button.profession":
                setProfession(ProfessionsMCA.randomProfession());
                break;
            case "gui.button.prospecting":
                startChore(Chore.PROSPECT, player);
                closeGUIIfOpen();
                break;
            case "gui.button.hunting":
                startChore(Chore.HUNT, player);
                closeGUIIfOpen();
                break;
            case "gui.button.fishing":
                startChore(Chore.FISH, player);
                closeGUIIfOpen();
                break;
            case "gui.button.chopping":
                startChore(Chore.CHOP, player);
                closeGUIIfOpen();
                break;
            case "gui.button.harvesting":
                startChore(Chore.HARVEST, player);
                closeGUIIfOpen();
                break;
            case "gui.button.stopworking":
                closeGUIIfOpen();
                stopChore();
                break;
        }
    }

    private boolean handleSpecialCaseGift(PlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof SpecialCaseGift) {
            if (((SpecialCaseGift) item).handle(player, this)) {
                player.getMainHandStack().decrement(1);
            }
            return true;
        } else if (item == Items.CAKE) {
            if (isMarried() && !isBaby()) {
                Entity spouse = ((ServerWorld) world).getEntity(spouseUUID.get().orElse(Constants.ZERO_UUID));
                if (spouse instanceof VillagerEntityMCA) {
                    VillagerEntityMCA progressor = gender.get() == Gender.FEMALE.getId() ? this : (VillagerEntityMCA) spouse;
                    progressor.hasBaby.set(true);
                    progressor.isBabyMale.set(random.nextBoolean());
                    produceParticles(ParticleTypes.HEART);
                    say(player, "gift.cake.success");
                } else {
                    say(player, "gift.cake.fail");
                }
                return true;
            }
        } else if (item == Items.GOLDEN_APPLE && this.isBaby()) {
            // increase age by 5 minutes
            this.growUp(1200 * 5);
            return true;
        }

        return false;
    }

    private void onEachClientUpdate() {
        if (isProcreating.get()) {
            this.headYaw += 50.0F;
        }

        if (this.age % 20 == 0) {
            onEachClientSecond();
        }
    }

    private void onEachClientSecond() {
        if (random.nextBoolean()) {
            if (getMoodLevel() <= -15) {
                switch (getPersonality().getMoodGroup()) {
                    case GENERAL:
                        this.produceParticles(ParticleTypes.SPLASH);

                        break;
                    case PLAYFUL:
                        this.produceParticles(ParticleTypes.SMOKE);

                        break;
                    case SERIOUS:
                        this.produceParticles(ParticleTypes.ANGRY_VILLAGER);

                        break;
                }

            } else if (getMoodLevel() >= 15) {
                this.produceParticles(ParticleTypes.HAPPY_VILLAGER);
            }
        }
    }

    //report potential buildings within this villagers reach
    private void reportBuildings() {
        VillageManagerData manager = VillageManagerData.get(world);

        //fetch all near POIs
        Stream<BlockPos> stream = ((ServerWorld) world).getPointOfInterestStorage().getPositions(
                PointOfInterestType.ALWAYS_TRUE,
                (p) -> !manager.cache.contains(p),
                getLandingPos(),
                48,
                PointOfInterestStorage.OccupationStatus.ANY);

        //check if it is a building
        stream.forEach(manager::reportBuilding);
    }

    private void updateVillage() {
        if (age % 600 == 0) {
            reportBuildings();

            //poor villager has no village
            if (village.get() == -1) {
                Village v = VillageHelper.getNearestVillage(this);
                if (v != null) {
                    village.set(v.getId());
                }
            }

            //and no house
            if (village.get() >= 0 && building.get() == -1) {
                Village v = VillageManagerData.get(world).villages.get(this.village.get());
                if (v == null) {
                    village.set(-1);
                } else {
                    //choose the first building available, shuffled
                    ArrayList<Building> buildings = new ArrayList<>(v.getBuildings().values());
                    Collections.shuffle(buildings);
                    for (Building b : buildings) {
                        if (b.getBeds() > b.getResidents().size()) {
                            //find a free bed within the building
                            Optional<PointOfInterest> bed = ((ServerWorld) world).getPointOfInterestStorage().getInSquare(
                                    PointOfInterestType.HOME.getCompletionCondition(),
                                    b.getCenter(),
                                    b.getPos0().getManhattanDistance(b.getPos1()),
                                    PointOfInterestStorage.OccupationStatus.HAS_SPACE).filter((poi) -> b.containsPos(poi.getPos())).findAny();

                            //sometimes the bed is blocked by someone
                            if (bed.isPresent()) {
                                //get a bed
                                setHome(bed.get().getPos(), world);

                                //add to residents
                                building.set(b.getId());
                                v.addResident(this, b.getId());
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (age % 6000 == 0) {
            //check if village still exists
            Village v = VillageManagerData.get(world).villages.get(this.village.get());
            if (v == null) {
                village.set(-1);
                building.set(-1);
                clearHome();
            } else {
                //check if building still exists
                if (v.getBuildings().containsKey(building.get())) {
                    //check if still resident
                    //this is a rare case and is in most cases a save corruptionption
                    if (v.getBuildings().get(building.get()).getResidents().keySet().stream().noneMatch((uuid) -> uuid.equals(this.uuid))) {
                        building.set(-1);
                        clearHome();
                    }
                } else {
                    building.set(-1);
                    clearHome();
                }
            }
        }
    }

    private void onEachServerUpdate() {
        // Every second
        if (this.age % 20 == 0) {
            onEachServerSecond();
        }

        updateVillage();

        // Every 10 seconds and when we're not already dead
        if (this.age % 200 == 0 && this.getHealth() > 0.0F) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F); // heal
            }
        }

        //check if another state has been reached
        AgeState last = AgeState.byId(ageState.get());
        AgeState next = AgeState.byCurrentAge(getBreedingAge());
        if (last != next) {
            ageState.set(next.getId());
            calculateDimensions();

            if (next == AgeState.ADULT) {
                // Notify player parents of the age up and set correct dialogue type.
                Entity[] parents = getBothParentEntities();
                Arrays.stream(parents).filter(e -> e instanceof PlayerEntity).map(e -> (PlayerEntity) e).forEach(p -> {
                    getMemoriesForPlayer(p).setDialogueType(DialogueType.ADULT);
                    sendMessageTo(MCA.localize("notify.child.grownup", villagerName.get()), p);
                });
            }
        }

        if (getProfession() == ProfessionsMCA.CHILD && this.getAgeState() == AgeState.ADULT) {
            setProfession(API.randomProfession());
        }
    }

    private void onEachServerSecond() {
        // villager has a baby
        if (hasBaby.get()) {
            babyAge.set(babyAge.get() + 1);

            // grow up time is in minutes and we measure age in seconds
            if (babyAge.get() >= MCA.getConfig().babyGrowUpTime * 60) {
                //get the father
                UUID fatherUUID = spouseUUID.get().orElse(Constants.ZERO_UUID);
                Entity fatherEntity = ((ServerWorld) world).getEntity(fatherUUID);
                VillagerEntityMCA father;
                if (fatherEntity instanceof VillagerEntityMCA) {
                    father = (VillagerEntityMCA) fatherEntity;
                } else {
                    //the father died, is out of range, etc
                    //fallback to mother-gene only
                    father = this;
                }

                //create child
                Gender gender = isBabyMale.get() ? Gender.MALE : Gender.FEMALE;
                VillagerEntityMCA child = new VillagerEntityMCA(world);
                child.gender.set(gender.getId());
                child.setPosition(this.offsetX(), this.getBodyY(), this.offsetZ());
                child.inheritGenes(father, this);

                //add all 3 to the family tree
                FamilyTree tree = getFamilyTree();
                tree.addEntry(father);
                tree.addEntry(this);
                tree.addEntry(child, fatherUUID, getUuid());

                //and yeet it into the world
                WorldUtils.spawnEntity(world, child);

                hasBaby.set(false);
                babyAge.set(0);
            }
        }

        //When you relog, it should continue doing the chores. Chore save but Activity doesn't, so this checks if the activity is not on there and puts it on there.
        Optional<Activity> possiblyChore = this.brain.getFirstPossibleNonCoreActivity();
        if (possiblyChore.isPresent() && !possiblyChore.get().equals(ActivityMCA.CHORE) && activeChore.get() != Chore.NONE.getId()) {
            this.brain.doExclusively(ActivityMCA.CHORE);
        }

        if (MoveState.byId(this.moveState.get()) == MoveState.FOLLOW && !this.brain.getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
            this.updateMoveState();
        }

    }

    public void stopChore() {
        this.brain.doExclusively(Activity.IDLE);
        activeChore.set(Chore.NONE.getId());
        choreAssigningPlayer.set(Constants.ZERO_UUID);
    }

    public void startChore(Chore chore, PlayerEntity player) {
        this.brain.doExclusively(ActivityMCA.CHORE);
        activeChore.set(chore.getId());
        choreAssigningPlayer.set(player.getUuid());
        this.brain.forget(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
        this.brain.forget(MemoryModuleTypeMCA.STAYING);
    }

    public void updateMemories(Memories memories) {
        CNBT nbt = this.memories.get().copy();
        nbt.setTag(memories.getPlayerUUID().toString(), memories.toCNBT());
        this.memories.set(nbt);
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, inventory);
    }

    public AgeState getAgeState() {
        return AgeState.byId(ageState.get());
    }

    @Override
    public float getScaleFactor() {
        if (gene_size == null) {
            return super.getScaleFactor();
        } else {
            float height = gene_size.get() * 0.5f + 0.75f;
            return height * getAgeState().getHeight();
        }
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions size) {
        if (gene_size == null) {
            return super.getActiveEyeHeight(pose, size);
        } else {
            float height = gene_size.get() * 0.5f + 0.75f;
            return height * getAgeState().getHeight() * 1.6f;
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> par) {
        if (ageState != null && ageState.getParam().equals(par)) {
            calculateDimensions();
        } else if (gene_size != null && gene_size.getParam().equals(par)) {
            calculateDimensions();
        }

        super.onTrackedDataSet(par);
    }

    @Override
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public void updateMoveState() {
        if (this.brain.getOptionalMemory(MemoryModuleTypeMCA.STAYING).isPresent()) {
            this.moveState.set(MoveState.STAY.getId());
        } else if (this.brain.getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
            this.moveState.set(MoveState.FOLLOW.getId());
        } else {
            this.moveState.set(MoveState.MOVE.getId());
        }
    }

    public void moveTowards(BlockPos pos, float speed, int closeEnoughDist) {
        BlockPosLookTarget blockposwrapper = new BlockPosLookTarget(pos);
        this.brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(blockposwrapper, speed, closeEnoughDist));
        this.lookAt(pos);
    }

    public void moveTowards(BlockPos pos) {
        this.moveTowards(pos, 0.5F, 1);
    }

    public void lookAt(BlockPos pos) {
        BlockPosLookTarget blockposwrapper = new BlockPosLookTarget(pos);
        this.brain.remember(MemoryModuleType.LOOK_TARGET, blockposwrapper);

    }

    public void closeGUIIfOpen() {
        if (!this.world.isClient) {
            ServerPlayerEntity entity = (ServerPlayerEntity) this.getInteractingPlayer();
            if (entity != null) {
                entity.closeHandledScreen();
            }
            this.setInteractingPlayer(null);
        }

    }

    public PlayerEntity getInteractingPlayer() {
        return this.interactingPlayer;
    }

    public void setInteractingPlayer(PlayerEntity player) {
        this.interactingPlayer = player;
    }

    private void prepareOffersFor(PlayerEntity player) {
        int i = this.getReputation(player);
        if (i != 0) {
            for (TradeOffer merchantoffer : this.getOffers()) {
                merchantoffer.increaseSpecialPrice(-MathHelper.floor((float) i * merchantoffer.getPriceMultiplier()));
            }
        }

        if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            StatusEffectInstance effectinstance = player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
            if (effectinstance != null) {
                int k = effectinstance.getAmplifier();

                for (TradeOffer merchantOffer : this.getOffers()) {
                    double d0 = 0.3D + 0.0625D * (double) k;
                    int j = (int) Math.floor(d0 * (double) merchantOffer.getOriginalFirstBuyItem().getCount());
                    merchantOffer.increaseSpecialPrice(-Math.max(j, 1));
                }
            }
        }

    }

    @Override
    public void handleStatus(byte id) {
        if (id == 15) {
            this.world.addImportantParticle(ParticleTypesMCA.NEG_INTERACTION.get(), true, this.offsetX(), this.getEyeY() + 0.5, this.offsetZ(), 0, 0, 0);
        } else if (id == 16) {
            this.world.addImportantParticle(ParticleTypesMCA.POS_INTERACTION.get(), true, this.offsetX(), this.getEyeY() + 0.5, this.offsetZ(), 0, 0, 0);
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
                    this.equipStack(type, stack);
                }
            }
        }
    }

    @Override
    public void setBaby(boolean isBaby) {
        this.setBreedingAge(isBaby ? -AgeState.startingAge : 0);
    }

    //TODO
    @Override
    public int getReputation(PlayerEntity p_223107_1_) {
        return super.getReputation(p_223107_1_);
    }

    public void gossip(ServerWorld p_242368_1_, VillagerEntityMCA p_242368_2_, long p_242368_3_) {
        if ((p_242368_3_ < this.lastGossipTime || p_242368_3_ >= this.lastGossipTime + 1200L) && (p_242368_3_ < p_242368_2_.lastGossipTime || p_242368_3_ >= p_242368_2_.lastGossipTime + 1200L)) {
            this.gossips.shareGossipFrom(p_242368_2_.gossips, this.random, 10);
            this.lastGossipTime = p_242368_3_;
            p_242368_2_.lastGossipTime = p_242368_3_;
            this.summonGolem(p_242368_1_, p_242368_3_, 5);
        }
    }

    private void decayGossip() {
        long i = this.world.getTime();
        if (this.lastGossipDecayTime == 0L) {
            this.lastGossipDecayTime = i;
        } else if (i >= this.lastGossipDecayTime + 24000L) {
            this.gossips.decay();
            this.lastGossipDecayTime = i;
        }
    }

    public FamilyTree getFamilyTree() {
        return FamilyTree.get(world);
    }

    public FamilyTreeEntry getFamilyTreeEntry() {
        return getFamilyTree().getEntry(this);
    }

    public Entity[] getBothParentEntities() {
        ServerWorld serverWorld = (ServerWorld) world;
        FamilyTreeEntry entry = getFamilyTreeEntry();
        return new Entity[]{
                serverWorld.getEntity(entry.getFather()),
                serverWorld.getEntity(entry.getMother())
        };
    }

    @Override
    public void onInteractionWith(EntityInteraction p_213739_1_, Entity p_213739_2_) {
        super.onInteractionWith(p_213739_1_, p_213739_2_);
    }

    public VillagerGossips getGossip() {
        return this.gossips;
    }

    public void readGossipDataNbt(NbtElement p_223716_1_) {
        this.gossips.deserialize(new Dynamic<>(NbtOps.INSTANCE, p_223716_1_));
    }

    public Gender getGender() {
        return Gender.byId(gender.get());
    }
}