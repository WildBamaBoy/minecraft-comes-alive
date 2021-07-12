package mca.entity;

import com.mojang.serialization.Dynamic;

import mca.api.API;
import mca.api.types.Button;
import mca.api.types.Hair;
import mca.client.gui.GuiInteract;
import mca.cobalt.localizer.Localizer;
import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.minecraft.network.datasync.*;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.*;
import mca.core.minecraft.entity.village.VillageHelper;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.data.*;
import mca.enums.*;
import mca.items.SpecialCaseGift;
import mca.util.InventoryUtils;
import mca.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Stream;

public class VillagerEntityMCA extends VillagerEntity implements NamedScreenHandlerFactory, Infectable {

    public final CDataManager data = new CDataManager(this);

    private final Genetics genetics = Genetics.create(data);
    private final Pregnancy pregnancy = new Pregnancy(this, data);
    private final VillagerBrain mcaBrain = new VillagerBrain(data);

    public final SimpleInventory inventory = new SimpleInventory(27);

    public final CStringParameter villagerName = data.newString("villagerName");

    public final CStringParameter clothes = data.newString("clothes");

    public final CStringParameter hair = data.newString("hair");

    public final CStringParameter hairOverlay = data.newString("hairOverlay");

    public final CTagParameter memories = data.newTag("memories");

    public final CIntegerParameter moveState = data.newInteger("moveState");

    public final CIntegerParameter ageState = data.newInteger("ageState");

    public final CStringParameter spouseName = data.newString("spouseName");
    public final CUUIDParameter spouseUUID = data.newUUID("spouseUUID");

    public final CIntegerParameter marriageState = data.newInteger("marriageState");
    public final CBooleanParameter isProcreating = data.newBoolean("isProcreating");
    public final CBooleanParameter isInfected = data.newBoolean("isInfected");
    public final CIntegerParameter activeChore = data.newInteger("activeChore");

    public final CUUIDParameter choreAssigningPlayer = data.newUUID("choreAssigningPlayer");
    public final BlockPosParameter hangoutPos = data.newPos("hangoutPos");

    public final CBooleanParameter importantProfession = data.newBoolean("importantProfession", false);

    //personality and mood
    public final CIntegerParameter personality = data.newInteger("personality");
    public final CIntegerParameter mood = data.newInteger("mood");

    public final CIntegerParameter village = data.newInteger("village", -1);
    public final CIntegerParameter building = data.newInteger("buildings", -1);

    //gift desaturation queue
    private final List<String> giftDesaturation = new LinkedList<>();
    public int procreateTick = -1;

    @Nullable
    private PlayerEntity interactingPlayer;
    private long lastGossipTime;
    private long lastGossipDecayTime;

    public VillagerEntityMCA(World w) {
        this(EntitiesMCA.VILLAGER, w);
    }

    public VillagerEntityMCA(EntityType<VillagerEntityMCA> type, World w) {
        super(type, w);
        inventory.addListener(this::onInvChange);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager requires those fields
        data.register();

        this.setSilent(true);
    }

    public static DefaultAttributeContainer.Builder createVillagerAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48);
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

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);

        genetics.randomize(this);
        villagerName.set(API.getRandomName(getGenetics().getGender()));

        initializeSkin();
        initializePersonality();

        return data;
    }

    public final VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }

    public final void setProfession(VillagerProfession profession) {
        this.setVillagerData(this.getVillagerData().withProfession(profession));
        reinitializeBrain((ServerWorld) world);
        clothes.set(API.getRandomClothing(this));
    }

    public Genetics getGenetics() {
        return genetics;
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
            if (knockback > 0.0F && target instanceof LivingEntity) {
                ((LivingEntity) target).takeKnockback(knockback * 0.5F, MathHelper.sin(this.getYaw() * ((float) Math.PI / 180F)), -MathHelper.cos(this.getYaw() * ((float) Math.PI / 180F)));
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
    public final ActionResult interactAt(PlayerEntity player, Vec3d pos, @NotNull Hand hand) {
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
        speed /= genetics.width.get();
        speed *= genetics.skin.get();

        setMovementSpeed(speed);
        InventoryUtils.readFromNBT(inventory, nbt);

        NbtList listnbt = nbt.getList("Gossips", 10);
        this.lastGossipDecayTime = nbt.getLong("LastGossipDecay");

        this.getGossip().deserialize(new Dynamic<>(NbtOps.INSTANCE, listnbt));
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

        nbt.put("Gossips", this.getGossip().serialize(NbtOps.INSTANCE).getValue());
        nbt.putLong("LastGossipDecay", this.lastGossipDecayTime);
    }

    private void initializeSkin() {
        clothes.set(API.getRandomClothing(this));

        Hair h = API.getRandomHair(this);
        hair.set(h.texture());
        hairOverlay.set(h.overlay());
    }

    private void initializePersonality() {
        personality.set(Personality.getRandom().getId());
        //since minLevel is -100 and it makes no
        mood.set(Mood.getLevel(random.nextInt(Mood.maxLevel - Mood.normalMinLevel + 1) + Mood.normalMinLevel));
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
                sendMessageTo(Localizer.localize("villager.hurt"), p);
            }

            if (source.getSource() instanceof ZombieEntity && getProfession() != ProfessionsMCA.GUARD && MCA.getConfig().enableInfection && random.nextFloat() < MCA.getConfig().infectionChance / 100.0) {
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

        if (world.isClient) {
            onEachClientUpdate();
        } else {
            onEachServerUpdate();
        }

        this.decayGossip();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        AgeState ageState = getAgeState();

        float height = genetics.size.get() + 1;
        float width = (genetics.width.get() * 0.5f + 0.75f) * ageState.getWidth();

        return EntityDimensions.changing(width, height);
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
        if (world.getBlockState(pos).isIn(BlockTags.BEDS)) {
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
            String localizedText = Localizer.localize(dialogueType.getName() + "." + phraseId, "generic." + phraseId, paramList);
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

    public Optional<Entity> getSpouse() {
        return spouseUUID.get().map(id -> ((ServerWorld) world).getEntity(id));
    }

    private void handleInteraction(PlayerEntity player, Memories memory, Button button) {
        //interaction
        String interactionName = button.identifier().replace("gui.button.", "");
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
        Optional<Button> button = API.getButtonById(guiKey, buttonId);
        if (!button.isPresent()) {
            MCA.logger.info("Button not found for key and ID: " + guiKey + ", " + buttonId);
        } else if (button.get().isInteraction()) {
            handleInteraction(player, memory, button.get());
        }

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
                setHair(API.getRandomHair(this));
                break;
            case "gui.button.clothing.prevHair":
                setHair(API.getNextHair(this, getHair(), -1));
                break;
            case "gui.button.clothing.nextHair":
                setHair(API.getNextHair(this, getHair()));
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

    private boolean handleSpecialCaseGift(PlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof SpecialCaseGift) {
            if (((SpecialCaseGift) item).handle(player, this)) {
                player.getMainHandStack().decrement(1);
            }
            return true;
        } else if (item == Items.CAKE) {
            if (isMarried() && !isBaby()) {
                if (pregnancy.tryStartGestation()) {
                    produceParticles(ParticleTypes.HEART);
                    say(player, "gift.cake.success");
                } else {
                    say(player, "gift.cake.fail");
                }
                return true;
            }
        } else if (item == Items.GOLDEN_APPLE && isBaby()) {
            // increase age by 5 minutes
            growUp(1200 * 5);
            return true;
        }

        return false;
    }

    private void onEachClientUpdate() {
        if (isProcreating.get()) {
            this.headYaw += 50.0F;
        }

        if (this.age % 20 != 0) {
            return;
        }

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
                    default:
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
                VillageHelper.getNearestVillage(this).map(Village::getId).ifPresent(village::set);
            }

            //and no house
            if (village.get() >= 0 && building.get() == -1) {
                VillageManagerData.get(world).getOrEmpty(this.village.get()).ifPresentOrElse(village -> {
                  //choose the first building available, shuffled
                    ArrayList<Building> buildings = new ArrayList<>(village.getBuildings().values());
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
                                village.addResident(this, b.getId());
                                break;
                            }
                        }
                    }
                }, () -> village.set(-1));
            }
        }

        if (age % 6000 == 0) {
            //check if village still exists
            VillageManagerData.get(world).getOrEmpty(this.village.get()).ifPresentOrElse(village -> {
              //check if building still exists
                if (village.getBuildings().containsKey(building.get())) {
                    //check if still resident
                    //this is a rare case and is in most cases a save corruptionption
                    if (village.getBuildings().get(building.get()).getResidents().keySet().stream().noneMatch((uuid) -> uuid.equals(this.uuid))) {
                        building.set(-1);
                        clearHome();
                    }
                } else {
                    building.set(-1);
                    clearHome();
                }
            }, () -> {
                village.set(-1);
                building.set(-1);
                clearHome();
            });
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
                    sendMessageTo(Localizer.localize("notify.child.grownup", villagerName.get()), p);
                });
            }
        }

        if (getProfession() == ProfessionsMCA.CHILD && this.getAgeState() == AgeState.ADULT) {
            setProfession(API.randomProfession());
        }
    }

    private void onEachServerSecond() {
        // villager has a baby
        pregnancy.tick();

        //When you relog, it should continue doing the chores. Chore save but Activity doesn't, so this checks if the activity is not on there and puts it on there.
        Optional<Activity> possiblyChore = this.brain.getFirstPossibleNonCoreActivity();
        if (possiblyChore.isPresent() && !possiblyChore.get().equals(ActivityMCA.CHORE) && activeChore.get() != Chore.NONE.ordinal()) {
            this.brain.doExclusively(ActivityMCA.CHORE);
        }

        if (MoveState.byId(this.moveState.get()) == MoveState.FOLLOW && !this.brain.getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
            this.updateMoveState();
        }
    }

    public void stopChore() {
        this.brain.doExclusively(Activity.IDLE);
        activeChore.set(Chore.NONE.ordinal());
        choreAssigningPlayer.set(Constants.ZERO_UUID);
    }

    public void startChore(Chore chore, PlayerEntity player) {
        this.brain.doExclusively(ActivityMCA.CHORE);
        activeChore.set(chore.ordinal());
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
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, inventory);
    }

    public AgeState getAgeState() {
        return AgeState.byId(ageState.get());
    }

    @Override
    public float getScaleFactor() {
        return genetics.getVerticalScaleFactor() * getAgeState().getHeight();
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
        if (ageState.getParam().equals(par) || genetics.size.equals(par)) {
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
            this.moveState.set(MoveState.STAY.ordinal());
        } else if (this.brain.getOptionalMemory(MemoryModuleTypeMCA.PLAYER_FOLLOWING).isPresent()) {
            this.moveState.set(MoveState.FOLLOW.ordinal());
        } else {
            this.moveState.set(MoveState.MOVE.ordinal());
        }
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

    public void closeGUIIfOpen() {
        if (!this.world.isClient) {
            ServerPlayerEntity entity = (ServerPlayerEntity) getInteractingPlayer();
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
                merchantoffer.increaseSpecialPrice(-MathHelper.floor(i * merchantoffer.getPriceMultiplier()));
            }
        }

        if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            StatusEffectInstance effectinstance = player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
            if (effectinstance != null) {
                int k = effectinstance.getAmplifier();

                for (TradeOffer merchantOffer : this.getOffers()) {
                    double d0 = 0.3D + 0.0625D * k;
                    int j = (int) Math.floor(d0 * merchantOffer.getOriginalFirstBuyItem().getCount());
                    merchantOffer.increaseSpecialPrice(-Math.max(j, 1));
                }
            }
        }

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
                    this.equipStack(type, stack);
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

    //TODO
    @Override
    public int getReputation(PlayerEntity p_223107_1_) {
        return super.getReputation(p_223107_1_);
    }

    public void gossip(ServerWorld p_242368_1_, VillagerEntityMCA p_242368_2_, long p_242368_3_) {
        if ((p_242368_3_ < this.lastGossipTime || p_242368_3_ >= this.lastGossipTime + 1200L) && (p_242368_3_ < p_242368_2_.lastGossipTime || p_242368_3_ >= p_242368_2_.lastGossipTime + 1200L)) {
            this.getGossip().shareGossipFrom(p_242368_2_.getGossip(), this.random, 10);
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
            this.getGossip().decay();
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
}