package mca.entity;

import cobalt.minecraft.inventory.CInventory;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.network.datasync.*;
import cobalt.minecraft.world.CWorld;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import mca.api.API;
import mca.api.types.APIButton;
import mca.api.types.Hair;
import mca.client.gui.GuiInteract;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.*;
import mca.entity.ai.brain.MCAVillagerTasks;
import mca.entity.data.*;
import mca.enums.*;
import mca.items.ItemSpecialCaseGift;
import mca.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Stream;

public class EntityVillagerMCA extends VillagerEntity implements INamedContainerProvider {
    public static final String[] GENES_NAMES = new String[]{
            "gene_size", "gene_width", "gene_breast", "gene_melanin", "gene_hemoglobin", "gene_eumelanin", "gene_pheomelanin", "gene_skin", "gene_face"};
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.HOME, MemoryModuleType.JOB_SITE,
            MemoryModuleType.POTENTIAL_JOB_SITE,
            MemoryModuleType.MEETING_POINT,
            MemoryModuleType.LIVING_ENTITIES,
            MemoryModuleType.VISIBLE_LIVING_ENTITIES,
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
    public final CInventory inventory;
    public final CWorld world;
    public CStringParameter villagerName = data.newString("villagerName");
    public CStringParameter clothes = data.newString("clothes");
    public CStringParameter hair = data.newString("hair");
    public CStringParameter hairOverlay = data.newString("hairOverlay");
    public CIntegerParameter gender = data.newInteger("gender");
    public CTagParameter memories = data.newTag("memories");
    public CIntegerParameter moveState = data.newInteger("moveState");
    public CIntegerParameter ageState = data.newInteger("ageState");
    public CStringParameter spouseName = data.newString("spouseName");
    public CUUIDParameter spouseUUID = data.newUUID("spouseUUID");
    public CIntegerParameter marriageState = data.newInteger("marriageState");
    public CBooleanParameter isProcreating = data.newBoolean("isProcreating");
    public CTagParameter parents = data.newTag("parents");
    public CBooleanParameter isInfected = data.newBoolean("isInfected");
    public CIntegerParameter activeChore = data.newInteger("activeChore");
    public CBooleanParameter hasBaby = data.newBoolean("hasBaby");
    public CBooleanParameter isBabyMale = data.newBoolean("isBabyMale");
    public CIntegerParameter babyAge = data.newInteger("babyAge");
    public CUUIDParameter choreAssigningPlayer = data.newUUID("choreAssigningPlayer");
    public BlockPosParameter hangoutPos = data.newPos("hangoutPos");

    // genes
    public CFloatParameter gene_size = data.newFloat("gene_size");
    public CFloatParameter gene_width = data.newFloat("gene_width");
    public CFloatParameter gene_breast = data.newFloat("gene_breast");
    public CFloatParameter gene_melanin = data.newFloat("gene_melanin");
    public CFloatParameter gene_hemoglobin = data.newFloat("gene_hemoglobin");
    public CFloatParameter gene_eumelanin = data.newFloat("gene_eumelanin");
    public CFloatParameter gene_pheomelanin = data.newFloat("gene_pheomelanin");
    public CFloatParameter gene_skin = data.newFloat("gene_skin");
    public CFloatParameter gene_face = data.newFloat("gene_face");

    // genes list
    public CFloatParameter[] GENES = new CFloatParameter[]{
            gene_size, gene_width, gene_breast, gene_melanin, gene_hemoglobin, gene_eumelanin, gene_pheomelanin, gene_skin, gene_face};

    //personality and mood
    public CIntegerParameter personality = data.newInteger("personality");
    public CIntegerParameter mood = data.newInteger("mood");

    public CIntegerParameter village = data.newInteger("village", -1);
    public CIntegerParameter building = data.newInteger("buildings", -1);

    @Nullable
    private PlayerEntity interactingPlayer;
    public int procreateTick = -1;

    public EntityVillagerMCA(EntityType<? extends EntityVillagerMCA> type, World w) {
        super(type, w);
        inventory = new CInventory(this, 27);
        inventory.addListener(this::onInvChange);

        world = CWorld.fromMC(w);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager required those fields
        data.register();

        this.setSilent(true);

        if (!world.isClientSide) {
            EnumGender eGender = EnumGender.getRandom();
            gender.set(eGender.getId());

            villagerName.set(API.getRandomName(eGender));
        }
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    protected Brain.BrainCodec<EntityVillagerMCA> mcaBrainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<EntityVillagerMCA> brain = this.mcaBrainProvider().makeBrain(dynamic);
        this.registerBrainGoals(brain);
        return brain;
    }

    @Override
    public void refreshBrain(ServerWorld world) {
        Brain<EntityVillagerMCA> brain = this.getMCABrain();
        brain.stopAll(world, this);
        //copyWithoutBehaviors will copy the memories of the old brain to the new brain
        this.brain = brain.copyWithoutBehaviors();
        this.registerBrainGoals(this.getMCABrain());
    }

    public Brain<EntityVillagerMCA> getMCABrain() {
        //generics amirite
        return (Brain<EntityVillagerMCA>) this.brain;
    }

    @Override
    protected void ageBoundaryReached() {

        //sus method
        super.ageBoundaryReached();
    }

    private void registerBrainGoals(Brain<EntityVillagerMCA> brain) {
        VillagerProfession villagerprofession = this.getVillagerData().getProfession();
        if (this.isBaby()) {
            brain.setSchedule(Schedule.VILLAGER_BABY);
            brain.addActivity(Activity.PLAY, MCAVillagerTasks.getPlayPackage(0.5F));
        } else {
            brain.setSchedule(this.random.nextBoolean() ? Schedule.VILLAGER_DEFAULT : SchedulesMCA.VILLAGER_DEFAULT_FLIPPED);
            brain.addActivityWithConditions(Activity.WORK, MCAVillagerTasks.getWorkPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_PRESENT)));
        }

        brain.addActivity(Activity.CORE, MCAVillagerTasks.getCorePackage(villagerprofession, 0.5F));
        brain.addActivityWithConditions(Activity.MEET, MCAVillagerTasks.getMeetPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.VALUE_PRESENT)));
        brain.addActivity(Activity.REST, MCAVillagerTasks.getRestPackage(villagerprofession, 0.5F));
        brain.addActivity(Activity.IDLE, MCAVillagerTasks.getIdlePackage(villagerprofession, 0.5F));
        brain.addActivity(Activity.PANIC, MCAVillagerTasks.getPanicPackage(villagerprofession, 0.5F));
        brain.addActivity(Activity.PRE_RAID, MCAVillagerTasks.getPreRaidPackage(villagerprofession, 0.5F));
        brain.addActivity(Activity.RAID, MCAVillagerTasks.getRaidPackage(villagerprofession, 0.5F));
        brain.addActivity(Activity.HIDE, MCAVillagerTasks.getHidePackage(villagerprofession, 0.5F));
        brain.addActivity(ActivityMCA.CHORE, MCAVillagerTasks.getChorePackage(villagerprofession, 0.5F));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
        brain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());

    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public ILivingEntityData finalizeSpawn(IServerWorld p_213386_1_, DifficultyInstance p_213386_2_, SpawnReason p_213386_3_, @Nullable ILivingEntityData p_213386_4_, @Nullable CompoundNBT p_213386_5_) {
        ILivingEntityData iLivingEntityData = super.finalizeSpawn(p_213386_1_, p_213386_2_, p_213386_3_, p_213386_4_, p_213386_5_);

        initializeGenes();
        initializeSkin();
        initializePersonality();

        //TODO big problem here, the profession changing AI...
        setProfession(ProfessionsMCA.randomProfession());

        return iLivingEntityData;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    public final VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }

    public final void setProfession(VillagerProfession profession) {
        this.setVillagerData(this.getVillagerData().setProfession(profession));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        //villager is peaceful and wont hurt as long as not necessary
        if (getPersonality() == EnumPersonality.PEACEFUL && getHealth() == getMaxHealth()) {
            return false;
        }

        //we don't use attributes
        float damage = getProfession() == MCA.PROFESSION_GUARD.get() ? 9.0F : 3.0F;
        float knockback = 3.0F;

        //personality bonus
        if (getPersonality() == EnumPersonality.WEAK) damage *= 0.75;
        if (getPersonality() == EnumPersonality.CONFIDENT) damage *= 1.25;
        if (getPersonality() == EnumPersonality.STRONG) damage *= 1.5;

        //enchantment
        if (target instanceof LivingEntity) {
            damage += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity) target).getMobType());
            knockback += (float) EnchantmentHelper.getKnockbackBonus(this);
        }

        //fire aspect
        int i = EnchantmentHelper.getFireAspect(this);
        if (i > 0) {
            target.setSecondsOnFire(i * 4);
        }

        boolean damageDealt = target.hurt(DamageSource.mobAttack(this), damage);

        //knockback and post damage stuff
        if (damageDealt) {
            if (knockback > 0.0F && target instanceof LivingEntity) {
                ((LivingEntity) target).knockback(knockback * 0.5F, MathHelper.sin(this.yRot * ((float) Math.PI / 180F)), -MathHelper.cos(this.yRot * ((float) Math.PI / 180F)));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            }

            this.doEnchantDamageEffects(this, target);
            this.setLastHurtMob(target);
        }

        return damageDealt;
    }


    @OnlyIn(Dist.CLIENT)
    private void openScreen(PlayerEntity player) {
        Minecraft.getInstance().setScreen(new GuiInteract(this, player));
    }

    @Override
    public final ActionResultType interactAt(PlayerEntity player, Vector3d pos, @Nonnull Hand hand) {
        if (world.isClientSide) {
            openScreen(player);
            return ActionResultType.SUCCESS;
        } else {
            this.setInteractingPlayer(player);
            return ActionResultType.PASS;
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);

        data.load(CNBT.fromMC(nbt));

        //verify clothes and hair
        clothes.set(API.getNextClothing(this, nbt.getString("clothes"), 0));
        Hair h = API.getNextHair(this, new Hair(
                hair.get(),
                hairOverlay.get()
        ), 0);
        hair.set(h.getTexture());
        hairOverlay.set(h.getOverlay());

        //set speed
        float speed = 1.0f;

        //personality bonuses
        if (getPersonality() == EnumPersonality.ATHLETIC) speed *= 1.15;
        if (getPersonality() == EnumPersonality.SLEEPY) speed *= 0.8;

        //width and size impact
        speed /= gene_width.get();
        speed *= gene_skin.get();

        setSpeed(speed);
        inventory.readFromNBT(nbt);
    }

    @Override
    public final void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        data.save(CNBT.fromMC(nbt));
        inventory.saveToNBT(nbt);
    }

    private void initializeSkin() {
        clothes.set(API.getRandomClothing(this));

        Hair h = API.getRandomHair(this);
        hair.set(h.getTexture());
        hairOverlay.set(h.getOverlay());
    }

    private void initializePersonality() {
        personality.set(EnumPersonality.getRandom().getId());
        mood.set(random.nextInt((EnumMood.maxLevel - EnumMood.minLevel) * EnumMood.levelsPerMood + 1) + EnumMood.minLevel * EnumMood.levelsPerMood);
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

        //temperature
        float temp = world.getBiome(getOnPos()).getBaseTemperature();

        // melanin
        gene_melanin.set(Util.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.5f));
        gene_hemoglobin.set(Util.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.5f));

        // TODO hair tend to have similar values than hair, but the used LUT is a little bit random
        gene_eumelanin.set(random.nextFloat());
        gene_pheomelanin.set(random.nextFloat());
    }

    //interpolates and mutates the genes from two parent villager
    public void inheritGenes(EntityVillagerMCA mother, EntityVillagerMCA father) {
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
    public final boolean hurt(DamageSource source, float damageAmount) {
        // Guards take 50% less damage
        if (getProfession() == MCA.PROFESSION_GUARD.get()) {
            damageAmount *= 0.5;
        }

        //personality bonus
        if (getPersonality() == EnumPersonality.TOUGH) damageAmount *= 0.5;
        if (getPersonality() == EnumPersonality.FRAGILE) damageAmount *= 1.25;

        if (!world.isClientSide) {
            if (source.getEntity() instanceof PlayerEntity) {
                PlayerEntity p = (PlayerEntity) source.getEntity();
                sendMessageTo(MCA.localize("villager.hurt"), p);
            }

            if (source.getDirectEntity() instanceof ZombieEntity && getProfession() != MCA.PROFESSION_GUARD.get() && MCA.getConfig().enableInfection && random.nextFloat() < MCA.getConfig().infectionChance / 100.0) {
                isInfected.set(true);
            }
        }

        return super.hurt(source, damageAmount);
    }

    @Override
    public void aiStep() {
        updateSwingTime();
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClientSide) {
            onEachClientUpdate();
        } else {
            onEachServerUpdate();
        }
    }

    public void sendMessageTo(String message, Entity receiver) {
        receiver.sendMessage(new StringTextComponent(message), receiver.getUUID());
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);

        if (!world.isClientSide) {
            //The death of a villager negatively modifies the mood of nearby villagers
            for (EntityVillagerMCA villager : world.getCloseEntities(this, 32.0D, EntityVillagerMCA.class)) {
                villager.modifyMoodLevel(-10);
            }

            //TODO: player memory gets lost on revive
            //TODO: childp becomes to child on revive (needs verification)

            inventory.dropAllItems();

            if (isMarried()) {
                UUID spouse = spouseUUID.get().orElse(Constants.ZERO_UUID);
                Entity sp = world.getEntityByUUID(spouse);
                PlayerSaveData playerSaveData = PlayerSaveData.get(world, spouse);

                // Notify spouse of the death
                if (sp instanceof EntityVillagerMCA) {
                    ((EntityVillagerMCA) sp).endMarriage();
                } else if (playerSaveData != null) {
                    playerSaveData.endMarriage();
                    PlayerEntity player = world.getPlayerEntityByUUID(spouse);
                    if (player != null) {
                        //TODO store message in case player was offline
                    }
                }
            }

            // Notify all parents of the death
            ParentPair parents = getParents();
            Arrays.stream(parents.getBothParentEntities(world))
                    .filter(e -> e instanceof PlayerEntity)
                    .forEach(e -> {
                        //TODO store message in case player was offline
                    });

            SavedVillagers.get(world).saveVillager(this);
        }
    }

    @Override
    public final SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    protected final SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected final SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    public final ITextComponent getDisplayName() {
        TextComponent name = new StringTextComponent(villagerName.get());
        if (this.brain.getMemory(MemoryModuleTypeMCA.STAYING).isPresent()) {
            name.append(new StringTextComponent("(Staying)"));
        }
        return name;
    }

    @Override
    public final ITextComponent getCustomName() {
        return new StringTextComponent(villagerName.get());
    }

    public Memories getMemoriesForPlayer(PlayerEntity player) {
        CNBT cnbt = memories.get();
        CNBT compoundTag = cnbt.getCompoundTag(player.getUUID().toString());
        Memories returnMemories = Memories.fromCNBT(this, compoundTag);
        if (returnMemories == null) {
            returnMemories = Memories.getNew(this, player.getUUID());
            memories.set(memories.get().setTag(player.getUUID().toString(), returnMemories.toCNBT()));
        }
        return returnMemories;
    }

    public EnumPersonality getPersonality() {
        return EnumPersonality.getById(personality.get());
    }

    public EnumMood getMood() {
        return getPersonality().getMoodGroup().getMood(mood.get());
    }

    public void modifyMoodLevel(int mood) {
        this.mood.set(this.mood.get() + mood);
    }

    public int getMoodLevel() {
        return mood.get();
    }

    private void goHome(PlayerEntity player) {
        if (getHome().equals(BlockPos.ZERO)) {
            say(player, "interaction.gohome.fail");
        } else {
            BlockPos home = getHome();
            this.moveTowards(home);
            say(player, "interaction.gohome.success");
        }
    }

    public BlockPos getWorkplace() {
        Optional<GlobalPos> home = this.brain.getMemory(MemoryModuleType.JOB_SITE);
        return home.map(GlobalPos::pos).orElse(BlockPos.ZERO);
    }

    public void setWorkplace(PlayerEntity player) {
        say(player, "interaction.setworkplace.success");
        this.brain.setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(player.level.dimension(), player.blockPosition()));
    }

    public BlockPos getHangout() {
        return hangoutPos.get();
    }

    public void setHangout(PlayerEntity player) {
        say(player, "interaction.sethangout.success");
        hangoutPos.set(player.blockPosition());
    }

    public BlockPos getHome() {
        Optional<GlobalPos> home = this.brain.getMemory(MemoryModuleType.HOME);
        return home.map(GlobalPos::pos).orElse(BlockPos.ZERO);
    }

    private boolean setHome(BlockPos pos, World world) {
        ServerWorld serverWorld = ((ServerWorld) level);
        PointOfInterestManager poiManager = serverWorld.getPoiManager();
        Optional<GlobalPos> bed = this.brain.getMemory(MemoryModuleType.HOME);
        bed.ifPresent(globalPos -> poiManager.release(globalPos.pos()));

        //check if it is a bed
        if (level.getBlockState(pos).is(BlockTags.BEDS)) {
            brain.setMemory(MemoryModuleType.HOME, GlobalPos.of(world.dimension(), pos));
            poiManager.take(PointOfInterestType.HOME.getPredicate(), (p) -> p.equals(pos), pos, 1);
            serverWorld.broadcastEntityEvent(this, (byte) 14);

            return true;
        } else {
            return false;
        }
    }

    private void setHome(PlayerEntity player) {
        //check if it is a bed
        if (setHome(player.blockPosition(), player.level)) {
            say(player, "interaction.sethome.success");
        } else {
            say(player, "interaction.sethome.fail");
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
            playSound(SoundEvents.ZOMBIE_AMBIENT, this.getSoundVolume(), this.getVoicePitch());
        } else {
            EnumDialogueType dialogueType = getMemoriesForPlayer(target).getDialogueType();
            sendMessageTo(chatPrefix + MCA.localize(dialogueType.getName() + "." + phraseId, paramList.toArray(new String[0])), target);
        }
    }

    public boolean isMarried() {
        return !spouseUUID.get().orElse(Constants.ZERO_UUID).equals(Constants.ZERO_UUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return spouseUUID.get().orElse(Constants.ZERO_UUID).equals(uuid);
    }

    public void marry(PlayerEntity player) {
        spouseUUID.set(player.getUUID());
        spouseName.set(player.getName().getContents());
        marriageState.set(EnumMarriageState.MARRIED_TO_PLAYER.getId());
    }

    public void marry(EntityVillagerMCA spouse) {
        spouseUUID.set(spouse.getUUID());
        spouseName.set(spouse.villagerName.get());
        marriageState.set(EnumMarriageState.MARRIED.getId());
    }

    private void endMarriage() {
        spouseUUID.set(Constants.ZERO_UUID);
        spouseName.set("");
        marriageState.set(EnumMarriageState.NOT_MARRIED.getId());
    }

    private void handleInteraction(PlayerEntity player, Memories memory, APIButton button) {
        //interaction
        String interactionName = button.getIdentifier().replace("gui.button.", "");
        EnumInteraction interaction = EnumInteraction.fromName(interactionName);

        //success chance and hearts
        float successChance = 0.85F;
        int heartsBoost = 5;
        if (interaction != null) {
            heartsBoost = interaction.getHearts(this, memory);
            successChance = interaction.getSuccessChance(this, memory) / 100.0f;
        }

        boolean succeeded = random.nextFloat() < successChance;

        //spawn particles
        if (succeeded) {
            this.level.broadcastEntityEvent(this, (byte) 16);
        } else {
            this.level.broadcastEntityEvent(this, (byte) 15);

            //sensitive people doubles the loss
            if (getPersonality() == EnumPersonality.SENSITIVE) {
                heartsBoost *= 2;
            }
        }

        memory.modInteractionFatigue(1);
        memory.modHearts(succeeded ? heartsBoost : (heartsBoost * -1));
        modifyMoodLevel(succeeded ? heartsBoost : (heartsBoost * -1));

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
                this.brain.eraseMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
                this.brain.eraseMemory(MemoryModuleTypeMCA.STAYING);
                updateMoveState();
                closeGUIIfOpen();
                break;
            case "gui.button.stay":
                this.brain.eraseMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
                this.brain.setMemory(MemoryModuleTypeMCA.STAYING, true);
                updateMoveState();
                closeGUIIfOpen();

                break;
            case "gui.button.follow":
                this.brain.setMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING, player);
                this.brain.eraseMemory(MemoryModuleTypeMCA.STAYING);
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
                this.openTradingScreen(player, this.getDisplayName(), this.getVillagerData().getLevel());
                this.updateSpecialPrices(player);
                this.setTradingPlayer(player);

                break;
            case "gui.button.inventory":
                player.openMenu(this);
                break;
            case "gui.button.gift":
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty()) {
                    int giftValue = API.getGiftValueFromStack(stack);
                    if (!handleSpecialCaseGift(player, stack)) {
                        if (stack.getItem() == Items.GOLDEN_APPLE) isInfected.set(false);
                        else {
                            modifyMoodLevel(giftValue / 4 + 2);
                            memory.modHearts(giftValue);
                            say(player, API.getResponseForGift(stack));
                        }
                    }
                    if (giftValue > 0) {
                        player.getMainHandItem().shrink(1);
                        this.level.broadcastEntityEvent(this, (byte) 16);
                    } else {
                        this.level.broadcastEntityEvent(this, (byte) 15);
                    }
                }
                closeGUIIfOpen();
                break;
            case "gui.button.procreate":
                if (PlayerSaveData.get(world, player.getUUID()).isBabyPresent()) {
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
//                applySpecialAI();
                break;
            case "gui.button.prospecting":
                startChore(EnumChore.PROSPECT, player);
                closeGUIIfOpen();
                break;
            case "gui.button.hunting":
                startChore(EnumChore.HUNT, player);
                closeGUIIfOpen();
                break;
            case "gui.button.fishing":
                startChore(EnumChore.FISH, player);
                closeGUIIfOpen();
                break;
            case "gui.button.chopping":
                startChore(EnumChore.CHOP, player);
                closeGUIIfOpen();
                break;
            case "gui.button.harvesting":
                startChore(EnumChore.HARVEST, player);
                closeGUIIfOpen();
                break;
            case "gui.button.stopworking":
                closeGUIIfOpen();
                stopChore();
                break;
            case "gui.button.village":
                //Village village = VillageHelper.findClosestVillage(world, this.getPos());
                //TODO somebody decided to remove villages....
//                if (village != null) {
//                    String phrase = MCA.localize("events.village",
//                            String.valueOf(village.getVillageRadius()),
//                            String.valueOf(village.getNumVillagers()),
//                            String.valueOf(village.getNumVillageDoors())
//                    );
//                    player.sendMessage(phrase);
//                } else {
                sendMessageTo("I wasn't able to find a village.", player);
//                }
        }
    }

    private boolean handleSpecialCaseGift(PlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ItemSpecialCaseGift) {
            if (((ItemSpecialCaseGift) item).handle(player, this)) {
                player.getMainHandItem().shrink(1);
            }
            return true;
        } else if (item == Items.CAKE) {
            if (isMarried() && !isBaby()) {
                Entity spouse = world.getEntityByUUID(spouseUUID.get().orElse(Constants.ZERO_UUID));
                if (spouse instanceof EntityVillagerMCA) {
                    EntityVillagerMCA progressor = gender.get() == EnumGender.FEMALE.getId() ? this : (EntityVillagerMCA) spouse;
                    progressor.hasBaby.set(true);
                    progressor.isBabyMale.set(random.nextBoolean());
                    addParticlesAroundSelf(ParticleTypes.HEART);
                    say(player, "gift.cake.success");
                } else {
                    say(player, "gift.cake.fail");
                }
                return true;
            }
        } else if (item == Items.GOLDEN_APPLE && this.isBaby()) {
            // increase age by 5 minutes
            this.ageUp(1200 * 5);
            return true;
        }

        return false;
    }

    private void onEachClientUpdate() {
        if (isProcreating.get()) {
            this.yHeadRot += 50.0F;
        }

        if (this.tickCount % 20 == 0) {
            onEachClientSecond();
        }
    }

    private void onEachClientSecond() {
        if (random.nextBoolean()) {
            if (getMoodLevel() <= -10) {
                switch (getPersonality().getMoodGroup()) {
                    case GENERAL:
                        this.addParticlesAroundSelf(ParticleTypes.SPLASH);

                        break;
                    case PLAYFUL:
                        this.addParticlesAroundSelf(ParticleTypes.SMOKE);

                        break;
                    case SERIOUS:
                        this.addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);

                        break;
                }

            } else if (getMoodLevel() >= 10) {
                this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
            }
        }
    }

    //report potential buildings within this villagers reach
    private void reportBuildings() {
        VillageManagerData manager = VillageManagerData.get(world);

        //fetch all near POIs
        Stream<BlockPos> stream = ((ServerWorld) level).getPoiManager().findAll(
                PointOfInterestType.ALL,
                (p) -> !manager.cache.contains(p),
                getOnPos(),
                48,
                PointOfInterestManager.Status.ANY);

        //check if it is a building
        stream.forEach((pos) -> manager.reportBuilding(level, pos));
    }

    private void updateVillage() {
        //extremely high scan rate, this is debug, don't worry
        if (tickCount % 100 == 0) {
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
                            Optional<PointOfInterest> bed = ((ServerWorld) level).getPoiManager().getInSquare(
                                    PointOfInterestType.HOME.getPredicate(),
                                    b.getCenter(),
                                    b.getPos0().distManhattan(b.getPos1()),
                                    PointOfInterestManager.Status.HAS_SPACE).filter((poi) -> b.containsPos(poi.getPos())).findAny();

                            //sometimes the bed is blocked by someone
                            if (bed.isPresent()) {
                                //get a bed
                                setHome(bed.get().getPos(), level);

                                //add to residents
                                building.set(b.getId());
                                b.addResident(this);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (tickCount % 1000 == 0) {
            //check if village still exists
            Village v = VillageManagerData.get(world).villages.get(this.village.get());
            if (v == null) {
                village.set(-1);
                building.set(-1);
            } else {
                //check if building still exists
                if (v.getBuildings().containsKey(building.get())) {
                    //check if still resident
                    //this is a rare case and is in most cases a save corruptionption
                    if (v.getBuildings().get(building.get()).getResidents().stream().noneMatch((uuid) -> uuid.equals(this.uuid))) {
                        building.set(-1);
                    }
                } else {
                    building.set(-1);
                }
            }
        }
    }

    private void onEachServerUpdate() {
        if (this.tickCount % 20 == 0) { // Every second
            onEachServerSecond();
        }

        updateVillage();

        if (this.tickCount % 200 == 0 && this.getHealth() > 0.0F) { // Every 10 seconds and when we're not already dead
            if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F); // heal
            }
        }

        //check if another state has been reached
        EnumAgeState last = EnumAgeState.byId(ageState.get());
        EnumAgeState next = EnumAgeState.byCurrentAge(getAge());
        if (last != next) {
            ageState.set(next.getId());

            if (next == EnumAgeState.ADULT) {
                // Notify player parents of the age up and set correct dialogue type.
                Entity[] parents = getParents().getBothParentEntities(world);
                Arrays.stream(parents).filter(e -> e instanceof PlayerEntity).map(e -> (PlayerEntity) e).forEach(p -> {
                    getMemoriesForPlayer(p).setDialogueType(EnumDialogueType.ADULT);
                    sendMessageTo(MCA.localize("notify.child.grownup", villagerName.get()), p);
                });

                // Change profession away from child for villager children.
                if (getProfession() == MCA.PROFESSION_CHILD.get()) {
                    setProfession(API.randomProfession().getMcProfession());
                }
            }
        }

        //When you relog, it should continue doing the chores. Chore save but Activity doesn't, so this checks if the activity is not on there and puts it on there.
        Optional<Activity> possiblyChore = this.brain.getActiveNonCoreActivity();
        if (possiblyChore.isPresent() && !possiblyChore.get().equals(ActivityMCA.CHORE) && activeChore.get() != EnumChore.NONE.getId()) {
            this.brain.setActiveActivityIfPossible(ActivityMCA.CHORE);
        }
    }

    private void onEachServerSecond() {
        // villager has a baby
        if (hasBaby.get()) {
            babyAge.set(babyAge.get() + 1);

            // grow up time is in minutes and we measure age in seconds
            if (babyAge.get() >= MCA.getConfig().babyGrowUpTime * 60) {
                EnumGender gender = isBabyMale.get() ? EnumGender.MALE : EnumGender.FEMALE;
                EntityVillagerMCA child = new EntityVillagerMCA(MCA.ENTITYTYPE_VILLAGER.get(), level);
                child.gender.set(gender.getId());
                child.setPos(this.getX(), this.getY(), this.getZ());
                //noinspection OptionalGetWithoutIsPresent
                child.parents.set(ParentPair.create(this.getUUID(), this.spouseUUID.get().get(), villagerName.get(), spouseName.get()).toNBT());
                world.spawnEntity(child);

                hasBaby.set(false);
                babyAge.set(0);
            }
        }

        //this.setItemSlot(EquipmentSlotType.CHEST, stack-from-the-inventory);


    }

    public void stopChore() {
        this.brain.setActiveActivityIfPossible(Activity.IDLE);
        activeChore.set(EnumChore.NONE.getId());
        choreAssigningPlayer.set(Constants.ZERO_UUID);
    }

    public void startChore(EnumChore chore, PlayerEntity player) {
        this.brain.setActiveActivityIfPossible(ActivityMCA.CHORE);
        activeChore.set(chore.getId());
        choreAssigningPlayer.set(player.getUUID());
        this.brain.eraseMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING);
        this.brain.eraseMemory(MemoryModuleTypeMCA.STAYING);
    }

    public boolean playerIsParent(PlayerEntity player) {
        ParentPair data = ParentPair.fromNBT(parents.get());
        return data.getParent1UUID().equals(player.getUUID()) || data.getParent2UUID().equals(player.getUUID());
    }

    public ParentPair getParents() {
        return ParentPair.fromNBT(parents.get());
    }

    public void updateMemories(Memories memories) {
        CNBT nbt = this.memories.get().copy();
        nbt.setTag(memories.getPlayerUUID().toString(), memories.toCNBT());
        this.memories.set(nbt);
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return ChestContainer.threeRows(i, playerInventory, inventory);
    }

    public EnumAgeState getAgeState() {
        return EnumAgeState.byId(ageState.get());
    }

    @Override
    public CInventory getInventory() {
        return this.inventory;
    }

    public void updateMoveState() {
        if (this.brain.getMemory(MemoryModuleTypeMCA.STAYING).isPresent()) {
            this.moveState.set(EnumMoveState.STAY.getId());
        } else if (this.brain.getMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
            this.moveState.set(EnumMoveState.FOLLOW.getId());
        } else {
            this.moveState.set(EnumMoveState.MOVE.getId());
        }
    }

    public void moveTowards(BlockPos pos, float speed, int closeEnoughDist) {
        BlockPosWrapper blockposwrapper = new BlockPosWrapper(pos);
        this.brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockposwrapper, speed, closeEnoughDist));
        this.lookAt(pos);
    }

    public void moveTowards(BlockPos pos) {
        this.moveTowards(pos, 0.5F, 1);
    }

    public void lookAt(BlockPos pos) {
        BlockPosWrapper blockposwrapper = new BlockPosWrapper(pos);
        this.brain.setMemory(MemoryModuleType.LOOK_TARGET, blockposwrapper);

    }

    public void closeGUIIfOpen() {
        if (!this.world.isClientSide) {
            ServerPlayerEntity entity = (ServerPlayerEntity) this.getInteractingPlayer();
            if (entity != null) {
                entity.closeContainer();
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

    private void updateSpecialPrices(PlayerEntity p_213762_1_) {
        int i = this.getPlayerReputation(p_213762_1_);
        if (i != 0) {
            for (MerchantOffer merchantoffer : this.getOffers()) {
                merchantoffer.addToSpecialPriceDiff(-MathHelper.floor((float) i * merchantoffer.getPriceMultiplier()));
            }
        }

        if (p_213762_1_.hasEffect(Effects.HERO_OF_THE_VILLAGE)) {
            EffectInstance effectinstance = p_213762_1_.getEffect(Effects.HERO_OF_THE_VILLAGE);
            int k = effectinstance.getAmplifier();

            for (MerchantOffer merchantoffer1 : this.getOffers()) {
                double d0 = 0.3D + 0.0625D * (double) k;
                int j = (int) Math.floor(d0 * (double) merchantoffer1.getBaseCostA().getCount());
                merchantoffer1.addToSpecialPriceDiff(-Math.max(j, 1));
            }
        }

    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 15) {
            this.level.addAlwaysVisibleParticle(ParticleTypesMCA.NEG_INTERACTION.get(), true, this.getX(), this.getY() + 2.1, this.getZ(), 0, 0, 0);
        } else if (id == 16) {
            this.level.addAlwaysVisibleParticle(ParticleTypesMCA.POS_INTERACTION.get(), true, this.getX(), this.getY() + 2.1, this.getZ(), 0, 0, 0);
        } else {
            super.handleEntityEvent(id);
        }
    }

    public void onInvChange(IInventory inventoryFromListener) {
        CInventory inv = this.getInventory();

        for (EquipmentSlotType type : EquipmentSlotType.values()) {
            if (type.getType() == EquipmentSlotType.Group.ARMOR) {
                ItemStack stack = inv.getBestArmorOfType(type);
                if (!stack.isEmpty()) {
                    this.setItemSlot(type, stack);
                }
            }
        }
    }
}