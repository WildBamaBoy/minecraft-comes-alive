package mca.entity;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.inventory.CEquipmentSlotType;
import cobalt.minecraft.inventory.CInventory;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.network.datasync.*;
import cobalt.minecraft.pathfinding.CPathNavigator;
import cobalt.minecraft.util.CDamageSource;
import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.world.CWorld;
import cobalt.util.ResourceLocationCache;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import mca.api.API;
import mca.api.types.APIButton;
import mca.api.types.Hair;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.data.Memories;
import mca.entity.data.ParentPair;
import mca.entity.data.PlayerSaveData;
import mca.entity.data.SavedVillagers;
import mca.enums.*;
import mca.items.ItemSpecialCaseGift;
import mca.util.Util;
import mca.wrappers.VillagerWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class EntityVillagerMCA extends VillagerWrapper {
    public final CDataManager data = new CDataManager(this);

    public CStringParameter villagerName = data.newString("villagerName");
    public CStringParameter clothes = data.newString("clothes");
    public CStringParameter hair = data.newString("hair");
    public CStringParameter hairOverlay = data.newString("hairOverlay");
    public CIntegerParameter gender = data.newInteger("gender");
    public CTagParameter memories = data.newTag("memories");
    public CIntegerParameter moveState = data.newInteger("moveState");
    public CStringParameter spouseName = data.newString("spouseName");
    public CUUIDParameter spouseUUID = data.newUUID("spouseUUID");
    public CIntegerParameter marriageState = data.newInteger("marriageState");
    public CBooleanParameter isProcreating = data.newBoolean("isProcreating");
    public CTagParameter parents = data.newTag("parents");
    public CBooleanParameter isInfected = data.newBoolean("isInfected");
    public CIntegerParameter ageState = data.newInteger("ageState");
    public CIntegerParameter activeChore = data.newInteger("activeChore");
    public CBooleanParameter isSwinging = data.newBoolean("isSwinging");
    public CBooleanParameter hasBaby = data.newBoolean("hasBaby");
    public CBooleanParameter isBabyMale = data.newBoolean("isBabyMale");
    public CIntegerParameter babyAge = data.newInteger("babyAge");
    public CUUIDParameter choreAssigningPlayer = data.newUUID("choreAssigningPlayer");
    public CPosParameter bedPos = data.newPos("bedPos");
    public CPosParameter workplacePos = data.newPos("workplacePos");
    public CPosParameter hangoutPos = data.newPos("hangoutPos");
    public CBooleanParameter isSleeping = data.newBoolean("isSleeping");

    // genes
    // TODO move into own class
    public CFloatParameter GENE_SIZE = data.newFloat("gene_size");
    public CFloatParameter GENE_WIDTH = data.newFloat("gene_width");
    public CFloatParameter GENE_BREAST = data.newFloat("gene_breast");
    public CFloatParameter GENE_MELANIN = data.newFloat("gene_melanin");
    public CFloatParameter GENE_HEMOGLOBIN = data.newFloat("gene_hemoglobin");
    public CFloatParameter GENE_EUMELANIN = data.newFloat("gene_eumelanin");
    public CFloatParameter GENE_PHEOMELANIN = data.newFloat("gene_pheomelanin");
    public CFloatParameter GENE_SKIN = data.newFloat("gene_skin");
    public CFloatParameter GENE_FACE = data.newFloat("gene_face");

    //personality and mood
    public CIntegerParameter PERSONALITY = data.newInteger("personality");
    public CIntegerParameter MOOD = data.newInteger("mood");

    // genes list
    public CFloatParameter[] GENES = new CFloatParameter[]{
            GENE_SIZE, GENE_WIDTH, GENE_BREAST, GENE_MELANIN, GENE_HEMOGLOBIN, GENE_EUMELANIN, GENE_PHEOMELANIN, GENE_SKIN, GENE_FACE};
    public static final String[] GENES_NAMES = new String[]{
            "gene_size", "gene_width", "gene_breast", "gene_melanin", "gene_hemoglobin", "gene_eumelanin", "gene_pheomelanin", "gene_skin", "gene_face"};

    private static final Predicate<EntityVillagerMCA> BANDIT_TARGET_SELECTOR = (v) -> v.getProfession() != ProfessionsMCA.bandit && v.getProfession() != ProfessionsMCA.child;
    private static final Predicate<EntityVillagerMCA> GUARD_TARGET_SELECTOR = (v) -> v.getProfession() == ProfessionsMCA.bandit;

    public final CInventory inventory;
    public UUID playerToFollowUUID = Constants.ZERO_UUID;

    private CPos home = CPos.ORIGIN;
    private int startingAge = 0;
    private float swingProgressTicks;

    public EntityVillagerMCA(EntityType<? extends EntityVillagerMCA> type, CWorld world) {
        super(type, world);
        inventory = new CInventory(CEntity.fromMC(this), 27);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager required those fields
        data.register();

        if (world.isRemote) {
            EnumGender eGender = EnumGender.getRandom();
            gender.set(eGender.getId());

            villagerName.set(API.getRandomName(eGender));

            setProfession(ProfessionsMCA.randomProfession());
        }
    }

    public EntityVillagerMCA(EntityType<EntityVillagerMCA> entityVillagerMCAEntityType, World world) {
        this(entityVillagerMCAEntityType, CWorld.fromMC(world));
    }

//    @Override
//    public ILivingEntityData onInitialSpawn(DifficultyInstance difficulty, @Nullable ILivingEntityData entity) {
//        //finalizeMobSpawn is a method from the villager, false means it wont overwrite our profession
//        entity = super.finalizeMobSpawn(difficulty, entity, false);
//
//        applySpecialAI();
//
//        initializeGenes();
//        initializeSkin();
//        initializePersonality();
//
//        return entity;
//    }

    @Override
    protected void initialize() {
        this.setSilent(true);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.FOLLOW_RANGE, 48.0D);
    }

//    @Override
//    protected void applyEntityAttributes() {
//        super.applyEntityAttributes();
//        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(MCA.getConfig().villagerMaxHealth);
//        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
//
//        if (this.getHealth() <= MCA.getConfig().villagerMaxHealth) {
//            this.setHealth(MCA.getConfig().villagerMaxHealth);
//        }
//    }

    @Override
    public boolean attack(CEntity entityIn) {
        //villager is peaceful and wont hurt as long as not necessary
        if (getPersonality() == EnumPersonality.PEACEFUL && getHealth() == getMaxHealth()) {
            return false;
        }

        float damage = getProfession() == ProfessionsMCA.guard ? 9.0F : 3.0F;

        //personality bonus
        if (getPersonality() == EnumPersonality.WEAK) damage *= 0.75;
        if (getPersonality() == EnumPersonality.CONFIDENT) damage *= 1.25;
        if (getPersonality() == EnumPersonality.STRONG) damage *= 1.5;

        return false;//entityIn.getMcEntity().attackEntityFrom(DamageSource.causeMobDamage(this), damage);
    }

    @Override
    public void load(CNBT nbt) {
        data.load(nbt);

        //verify clothes and hair
        clothes.set(API.getNextClothing(this, nbt.getString("clothes"), 0));
        Hair h = API.getNextHair(this, new Hair(
                hair.get(),
                hairOverlay.get()
        ), 0);
        hair.set(h.getTexture());
        hairOverlay.set(h.getOverlay());

        //also supports older versions
        if (GENE_SIZE.get() == 0) {
            initializeGenes();
            initializeSkin();
            initializePersonality();
        }

        // Vanilla Age doesn't apply from the superclass call. Causes children to revert to the starting age on world reload.
//        setStartingAge(nbt.getInteger("Age"));

        this.home = new CPos(nbt.getDouble("homePositionX"), nbt.getDouble("homePositionY"), nbt.getDouble("homePositionZ"));
        this.playerToFollowUUID = nbt.getUUID("playerToFollowUUID");

//        applySpecialAI();
    }

    @Override
    public void save(CNBT nbt) {

    }

    @Override
    public CPos getHomePos() {
        return null;
    }

    private void initializeSkin() {
        clothes.set(API.getRandomClothing(this));

        Hair h = API.getRandomHair(this);
        hair.set(h.getTexture());
        hairOverlay.set(h.getOverlay());
    }

    private void initializePersonality() {
        PERSONALITY.set(EnumPersonality.getRandom().getId());
        MOOD.set(random.nextInt((EnumMood.maxLevel - EnumMood.minLevel) * EnumMood.levelsPerMood + 1) + EnumMood.minLevel * EnumMood.levelsPerMood);
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
        GENE_SIZE.set(centeredRandom());
        GENE_WIDTH.set(centeredRandom());

        //temperature
        float temp = world.getBiome(getPos()).getBaseTemperature();

        // melanin
        GENE_MELANIN.set(Util.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.5f));
        GENE_HEMOGLOBIN.set(Util.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.5f));

        // TODO hair tend to have similar values than hair, but the used LUT is a little bit random
        GENE_EUMELANIN.set(random.nextFloat());
        GENE_PHEOMELANIN.set(random.nextFloat());
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
    public float beforeDamaged(CDamageSource damageSource, float damageAmount) {
        // Guards take 50% less damage
        if (getProfession() == ProfessionsMCA.guard) {
            damageAmount *= 0.5;
        }

        //personality bonus
        if (getPersonality() == EnumPersonality.TOUGH) damageAmount *= 0.5;
        if (getPersonality() == EnumPersonality.FRAGILE) damageAmount *= 1.25;

        return damageAmount;
    }

    public boolean afterDamaged(CDamageSource source, float amount) {
        source.getPlayer().ifPresent(e -> {
            e.sendMessage(MCA.localize("villager.hurt"));
        });

        if (source.isZombie() && getProfession() != ProfessionsMCA.guard && MCA.getConfig().enableInfection && random.nextFloat() < MCA.getConfig().infectionChance / 100.0) {
            isInfected.set(true);
        }
        return false;
    }

    @Override
    public CItemStack getEquipmentOfType(CEquipmentSlotType type) {
        return null;
    }

    @Override
    public void onUpdate() {
        updateSwinging();
        updateSleeping();

        if (world.isRemote) {
            onEachServerUpdate();
        } else {
            onEachClientUpdate();
        }
    }

    @Override
    public void swing(CEnumHand hand) {

    }

    @Override
    public void handleDeath(CDamageSource cause) {
        if (!world.isRemote) {
            if (MCA.getConfig().logVillagerDeaths) {
                //MCA.getLog().info("Villager death: " + villagerName.get() + ". Caused by: " + cause.getCauseName() + ". UUID: " + this.getUUID().toString());
            }

            //The death of a villager negatively modifies the mood of nearby villagers
            for (EntityVillagerMCA villager : Util.getEntitiesWithinDistance(world, getPos(), 24, EntityVillagerMCA.class)) {
                villager.modifyMoodLevel(-10);
            }

            //TODO: player memory gets lost on revive
            //TODO: childp becomes to child on revive (needs verification)

            inventory.dropAllItems();

            if (isMarried()) {
                UUID spouse = spouseUUID.get().orElse(Constants.ZERO_UUID);
                Optional<EntityVillagerMCA> sp = Util.getEntityByUUID(world, spouse, EntityVillagerMCA.class);
                PlayerSaveData playerSaveData = PlayerSaveData.get(world, spouse);

                // Notify spouse of the death
                if (sp.isPresent()) {
                    sp.get().endMarriage();
                } else if (playerSaveData != null) {
                    playerSaveData.endMarriage();
//                    world.getPlayerEntityByUUID(spouse).ifPresent(player -> player.sendMessage(new StringTextComponent(Constants.Color.RED + MCA.localize("notify.spousedied", villagerName.get(), cause.getCauseName()))));
                }
            }

            // Notify all parents of the death
//            ParentPair parents = ParentPair.fromNBT(get(this.parents));
//            Arrays.stream(parents.getParentEntities(world))
//                    .filter(e -> e instanceof CPlayer)
//                    .forEach(e -> {
//                        CPlayer player = (CPlayer) e;
//                        player.sendMessage(new StringTextComponent(Constants.Color.RED + MCA.localize("notify.childdied", villagerName.get(), cause.getCauseName())));
//                    }
//                    });

            SavedVillagers.get(world).saveVillager(this);
        }
    }

    @Override
    public void onRightClick(CPlayer player, CEnumHand hand) {

    }

    @Override
    protected void onGrowingAdult() {
        ageState.set(EnumAgeState.ADULT.getId());

        // Notify player parents of the age up and set correct dialogue type.
        CEntity[] parents = getParents().getBothParentEntities(this.world);
        Arrays.stream(parents).forEach(e -> e.asPlayer().ifPresent(p -> {
            getMemoriesForPlayer(p).setDialogueType(EnumDialogueType.ADULT);
            p.sendMessage(MCA.localize("notify.child.grownup", this.getVillagerName()));
        }));

        // Change profession away from child for villager children.
        if (getProfession() == ProfessionsMCA.child) {
            setProfession(API.randomProfession().getMcProfession());
        }
    }

    @Override
    public String getNameForDisplay() {
        return "display name";
    }

    @Override
    public String getVillagerName() {
        return "villager name";
    }

    private void updateSwinging() {
        if (isSwinging.get()) {
            swingProgressTicks++;

            if (swingProgressTicks >= 8) {
                swingProgressTicks = 0;
                isSwinging.set(false);
            }
        } else {
            swingProgressTicks = 0;
        }
    }

    public Memories getMemoriesForPlayer(CPlayer player) {
        Memories returnMemories = Memories.fromCNBT(this, memories.get().getCompoundTag(player.getUUID().toString()));
        if (returnMemories == null) {
            returnMemories = Memories.getNew(this, player.getUUID());
            memories.set(memories.get().setTag(player.getUUID().toString(), returnMemories.toCNBT()));
        }
        return returnMemories;
    }

    public EnumPersonality getPersonality() {
        return EnumPersonality.getById(PERSONALITY.get());
    }

    public EnumMood getMood() {
        return getPersonality().getMoodGroup().getMood(MOOD.get());
    }

    public void modifyMoodLevel(int mood) {
        MOOD.set(MOOD.get() + mood);
    }

    public int getMoodLevel() {
        return MOOD.get();
    }

    public void reset() {
        memories.set(CNBT.createNew());
        setHealth(20.0F);
        spouseUUID.set(null);
        isBabyMale.set(false);
    }

    private void goHome(CPlayer player) {
        if (getHomePos().equals(CPos.ORIGIN)) {
            say(player, "interaction.gohome.fail");
        } else {
            CPathNavigator nav = CPathNavigator.fromMC(this.getNavigation());
            if (!nav.tryGoTo(getHomePos())) {
                teleportTo(getHomePos().getX(), getHomePos().getY(), getHomePos().getZ());
            }
            say(player, "interaction.gohome.success");
        }
    }

    public CPos getWorkplace() {
        return workplacePos.get();
    }

    public CPos getHangout() {
        return hangoutPos.get();
    }

    /**
     * Forces the villager's home to be set to their position. No checks for safety are made.
     * This is used on overwriting the original villager.
     */
    public void forcePositionAsHome() {
        this.home = this.getPos();
    }

    private void setHome(CPlayer player) {
        say(player, "interaction.sethome.success");
        bedPos.set(player.getPosition());
    }

    public void setWorkplace(CPlayer player) {
        say(player, "interaction.setworkplace.success");
        workplacePos.set(player.getPosition());
    }

    public void setHangout(CPlayer player) {
        say(player, "interaction.sethangout.success");
        hangoutPos.set(player.getPosition());
    }

    public void say(CPlayer target, String phraseId, String... params) {
        ArrayList<String> paramList = new ArrayList<>();
        if (params != null) Collections.addAll(paramList, params);

        // Player is always first in params passed to localizer for say().
        paramList.add(0, target.getName());

        String chatPrefix = MCA.getConfig().villagerChatPrefix + getDisplayName().getString() + ": ";
        if (isInfected.get()) { // Infected villagers do not speak
            target.sendMessage(chatPrefix + "???");
            playSound(SoundEvents.ZOMBIE_AMBIENT, this.getSoundVolume(), this.getVoicePitch());
        } else {
            EnumDialogueType dialogueType = getMemoriesForPlayer(target).getDialogueType();
            target.sendMessage(chatPrefix + MCA.localize(dialogueType + "." + phraseId, params));
        }
    }

    public boolean isMarried() {
        return !spouseUUID.get().orElse(Constants.ZERO_UUID).equals(Constants.ZERO_UUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return spouseUUID.get().orElse(Constants.ZERO_UUID).equals(uuid);
    }

    public void marry(CPlayer player) {
        spouseUUID.set(player.getUUID());
        spouseName.set(player.getName());
        marriageState.set(EnumMarriageState.MARRIED.getId());
    }

    public void marry(EntityVillagerMCA spouse) {
        spouseUUID.set(spouse.getUUID());
        spouseName.set(spouse.getVillagerName());
        marriageState.set(EnumMarriageState.MARRIED.getId());
    }

    private void endMarriage() {
        spouseUUID.set(Constants.ZERO_UUID);
        spouseName.set("");
        marriageState.set(EnumMarriageState.NOT_MARRIED.getId());
    }

    private void handleInteraction(CPlayer player, Memories memory, APIButton button) {
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

        //sensitive people doubles the loss
        if (!succeeded && getPersonality() == EnumPersonality.SENSITIVE) {
            heartsBoost *= 2;
        }

        memory.modInteractionFatigue(1);
        memory.modHearts(succeeded ? heartsBoost : (heartsBoost * -1));
        modifyMoodLevel(succeeded ? heartsBoost : (heartsBoost * -1));
        String responseId = String.format("%s.%s", interactionName, succeeded ? "success" : "fail");
        say(player, responseId);
    }

    public void handleButtonClick(CPlayer player, String guiKey, String buttonId) {
        Memories memory = getMemoriesForPlayer(player);
        java.util.Optional<APIButton> button = API.getButtonById(guiKey, buttonId);
        if (!button.isPresent()) {
            MCA.log("Button not found for key and ID: " + guiKey + ", " + buttonId);
        } else if (button.get().isInteraction()) handleInteraction(player, memory, button.get());

        Hair h;
        switch (buttonId) {
            case "gui.button.move":
                moveState.set(EnumMoveState.MOVE.getId());
                this.playerToFollowUUID = Constants.ZERO_UUID;
                break;
            case "gui.button.stay":
                moveState.set(EnumMoveState.STAY.getId());
                break;
            case "gui.button.follow":
                moveState.set(EnumMoveState.FOLLOW.getId());
                this.playerToFollowUUID = player.getUUID();
                stopChore();
                break;
            case "gui.button.ridehorse":
//                toggleMount(player);
                break;
            case "gui.button.sethome":
                setHome(player);
                break;
            case "gui.button.gohome":
                goHome(player);
                break;
            case "gui.button.setworkplace":
                setWorkplace(player);
                break;
            case "gui.button.sethangout":
                setHangout(player);
                break;
            case "gui.button.trade":
                if (MCA.getConfig().allowTrading) {
//                    setCustomer(player);
//                    player.displayVillagerTradeGui(this);
                } else {
                    player.sendMessage(MCA.localize("info.trading.disabled"));
                }
                break;
            case "gui.button.inventory":
//                player.openGui(MCA.getInstance(), Constants.GUI_ID_INVENTORY, player.world, this.getEntityId(), 0, 0);
                break;
            case "gui.button.gift":
                CItemStack stack = player.getHeldItem(CEnumHand.MAIN_HAND);
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
                    player.getHeldItem(CEnumHand.MAIN_HAND).decrStackSize();
                }
                break;
            case "gui.button.procreate":
//                if (PlayerSaveData.get(player).isBabyPresent())
                if (true)
                    say(player, "interaction.procreate.fail.hasbaby");
                else if (memory.getHearts() < 100) say(player, "interaction.procreate.fail.lowhearts");
                else {
//                    EntityAITasks.EntityAITaskEntry task = tasks.taskEntries.stream().filter((ai) -> ai.action instanceof EntityAIProcreate).findFirst().orElse(null);
//                    if (task != null) {
//                        ((EntityAIProcreate) task.action).procreateTimer = 20 * 3; // 3 seconds
//                        isProcreating.set(true);
//                    }
                }
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
                break;
            case "gui.button.hunting":
                startChore(EnumChore.HUNT, player);
                break;
            case "gui.button.fishing":
                startChore(EnumChore.FISH, player);
                break;
            case "gui.button.chopping":
                startChore(EnumChore.CHOP, player);
                break;
            case "gui.button.harvesting":
                startChore(EnumChore.HARVEST, player);
                break;
            case "gui.button.stopworking":
                stopChore();
                break;
            case "gui.button.village":
//                Village village = VillageHelper.findClosestVillage(world, this.getPos());
//                if (village != null) {
//                    String phrase = MCA.localize("events.village",
//                            String.valueOf(village.getVillageRadius()),
//                            String.valueOf(village.getNumVillagers()),
//                            String.valueOf(village.getNumVillageDoors())
//                    );
//                    player.sendMessage(phrase);
//                } else {
//                    player.sendMessage("I wasn't able to find a village.");
//                }
        }
    }

    private boolean handleSpecialCaseGift(CPlayer player, CItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ItemSpecialCaseGift && !isBaby()) { // special case gifts are rings so far so prevent giving them to children
//            boolean decStackSize = ((ItemSpecialCaseGift) item).handle(player, this);
            player.getHeldItem(CEnumHand.MAIN_HAND).decrStackSize();
            return true;
        } else if (item == Items.CAKE) {
            if (isMarried() && !isBaby()) {
                Optional<Entity> spouse = Util.getEntityByUUID(world, spouseUUID.get().orElse(Constants.ZERO_UUID));
                if (spouse.isPresent()) {
                    EntityVillagerMCA progressor = gender.get() == EnumGender.FEMALE.getId() ? this : (EntityVillagerMCA) spouse.get();
                    progressor.hasBaby.set(true);
                    progressor.isBabyMale.set(random.nextBoolean());
//                    progressor.spawnParticles(EnumParticleTypes.HEART);
                    say(player, "gift.cake.success");
                } else {
                    say(player, "gift.cake.fail");
                }
                return true;
            }
        } else if (item == Items.GOLDEN_APPLE && this.isBaby()) {
//            this.addGrowth(((startingAge / 4) / 20 * -1));
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

    }

    private void onEachServerUpdate() {
        if (this.tickCount % 20 == 0) { // Every second
            onEachServerSecond();
        }

        if (this.tickCount % 200 == 0 && this.getHealth() > 0.0F) { // Every 10 seconds and when we're not already dead
            if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F); // heal
            }
        }

        if (isBaby()) {
            // get older
//            EnumAgeState current = EnumAgeState.byId(ageState.get());
//            EnumAgeState target = EnumAgeState.byCurrentAge(startingAge, getGrowingAge());
//            if (current != target) {
//                ageState(target.getId());
//            }
        }
    }

    private void onEachServerSecond() {
//        CNBT mem = memories.get();
//        mem.getKeySet().forEach((key) -> Memories.fromNBT(this, UUID.fromString(key), mem.getCompoundTag(key)).update());
//
//        if (get(hasBaby)) {
//            babyAge(get(babyAge) + 1);
//
//            if (get(babyAge) >= MCA.getConfig().babyGrowUpTime * 60) { // grow up time is in minutes and we measure age in seconds
//                EntityVillagerMCA child = new EntityVillagerMCA(world, Optional.absent(), Optional.of(get(isBabyMale) ? EnumGender.MALE : EnumGender.FEMALE));
//                child.ageState(EnumAgeState.BABY.getId());
//                child.setStartingAge(MCA.getConfig().childGrowUpTime * 60 * 20 * -1);
//                child.setScaleForAge(true);
//                child.setPosition(this.posX, this.posY, this.posZ);
//                child.parents(ParentPair.create(this.getUUID(), this.spouseUUID.get().get(), this.get(villagerName), this.get(spouseName)).toNBT());
//                world.spawnEntity(child);
//
//                hasBaby(false);
//                babyAge(0);
//            }
//        }
    }

    public ResourceLocation getTextureResourceLocation() {
        if (isInfected.get()) {
            return ResourceLocationCache.get(String.format("mca:skins/%s/zombievillager.png", gender.get() == EnumGender.MALE.getId() ? "male" : "female"));
        } else {
            return ResourceLocationCache.get(clothes.get());
        }
    }

//    @Override
//    protected void initializeAI() {
//        super.initEntityAI();
//
//        this.tasks.addTask(0, new EntityAIProspecting(this));
//        this.tasks.addTask(0, new EntityAIHunting(this));
//        this.tasks.addTask(0, new EntityAIChopping(this));
//        this.tasks.addTask(0, new EntityAIHarvesting(this));
//        this.tasks.addTask(0, new EntityAIFishing(this));
//        this.tasks.addTask(0, new EntityAIMoveState(this));
//        this.tasks.addTask(0, new EntityAIAgeBaby(this));
//        this.tasks.addTask(0, new EntityAIProcreate(this));
//        this.tasks.addTask(5, new EntityAIGoWorkplace(this));
//        this.tasks.addTask(6, new EntityAIWork(this));
//        this.tasks.addTask(5, new EntityAIGoHangout(this));
//        this.tasks.addTask(1, new EntityAISleeping(this));
//        this.tasks.addTask(10, new EntityAIWatchClosest(this, CPlayer.class, 8.0F));
//        this.tasks.addTask(10, new EntityAILookIdle(this));
//    }

//    private void applySpecialAI() {
//        if (getProfession() == ProfessionsMCA.bandit) {
//            this.tasks.taskEntries.clear();
//            this.tasks.addTask(1, new EntityAIAttackMelee(this, 0.8D, false));
//            this.tasks.addTask(2, new EntityAIMoveThroughVillage(this, 0.6D, false));
//
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVillagerMCA.class, 100, false, false, BANDIT_TARGET_SELECTOR));
//            this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, CPlayer.class, true));
//        } else if (getProfession() == ProfessionsMCA.guard) {
//            removeCertainTasks(EntityAIAvoidEntity.class);
//
//            this.tasks.addTask(1, new EntityAIAttackMelee(this, 0.8D, false));
//            this.tasks.addTask(2, new EntityAIMoveThroughVillage(this, 0.6D, false));
//
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVillagerMCA.class, 100, false, false, GUARD_TARGET_SELECTOR));
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityZombie.class, 100, false, false, null));
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVex.class, 100, false, false, null));
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVindicator.class, 100, false, false, null));
//        } else {
//            //every other villager is allowed to defend itself from zombies while fleeing
//            this.tasks.addTask(0, new EntityAIDefendFromTarget(this));
//
//            this.targetTasks.taskEntries.clear();
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityZombie.class, 100, false, false, null));
//        }
//    }

    //guards should not run away from zombies
    //TODO: should only avoid zombies when low on health
//    private void removeCertainTasks(Class typ) {
//        Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.tasks.taskEntries.iterator();
//
//        while (iterator.hasNext()) {
//            EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = iterator.next();
//            EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;
//
//            if (entityaibase.getClass().equals(typ)) {
//                iterator.remove();
//            }
//        }
//    }

    public void stopChore() {
        activeChore.set(EnumChore.NONE.getId());
        choreAssigningPlayer.set(Constants.ZERO_UUID);
    }

    public void startChore(EnumChore chore, CPlayer player) {
        activeChore.set(chore.getId());
        choreAssigningPlayer.set(player.getUUID());
    }

    public boolean playerIsParent(CPlayer player) {
        ParentPair data = ParentPair.fromNBT(parents.get());
        return data.getParent1UUID().equals(player.getUUID()) || data.getParent2UUID().equals(player.getUUID());
    }

    public String getCurrentActivity() {
        EnumMoveState ms = EnumMoveState.byId(moveState.get());
        if (ms != EnumMoveState.MOVE) {
            return ms.getFriendlyName();
        }

        EnumChore chore = EnumChore.byId(activeChore.get());
        if (chore != EnumChore.NONE) {
            return chore.getFriendlyName();
        }

        return null;
    }

    public void moveTowardsBlock(CPos target) {
        moveTowardsBlock(target, 0.5D);
    }

    public void moveTowardsBlock(CPos target, double speed) {
        int range = 32;

        //personality bonuses
        if (getPersonality() == EnumPersonality.ATHLETIC) speed *= 1.15;
        if (getPersonality() == EnumPersonality.SLEEPY) speed *= 0.8;

        //width and size impact
        speed /= GENE_WIDTH.get();
        speed *= GENE_SKIN.get();

        if (distanceToSqr(target.getX(), target.getY(), target.getZ()) > Math.pow(range, 2.0)) {
            Vector3d vec3d = RandomPositionGenerator.getPosTowards(this, range, 8, new Vector3d(target.getX(), target.getY(), target.getZ()));
//            if (vec3d != null && !getNavigation().setPath(getNavigation().createPath(vec3d.x, vec3d.y, vec3d.z, range), speed)) {
//                teleportTo(vec3d.x, vec3d.y, vec3d.z);
//            }
        } else {
//            getNavigation().createPath(target.getX(), target.getY() ,target.getZ(), range);
        }
    }

    public boolean isSleeping() {
        return isSleeping.get();
    }

    private void updateSleeping() {
        if (isSleeping()) {
//            CPos bedLocation = get(EntityVillagerMCA.bedPos);
//
//            final IBlockState state = this.world.isBlockLoaded(bedLocation) ? this.world.getBlockState(bedLocation) : null;
//            final boolean isBed = state != null && state.getBlock().isBed(state, this.world, bedLocation, this);
//
//            if (isBed) {
//                final EnumFacing enumfacing = state.getBlock() instanceof BlockHorizontal ? state.getValue(BlockHorizontal.FACING) : null;
//
//                if (enumfacing != null) {
//                    float f1 = 0.5F + (float) enumfacing.getFrontOffsetX() * 0.4F;
//                    float f = 0.5F + (float) enumfacing.getFrontOffsetZ() * 0.4F;
//                    this.setRenderOffsetForSleep(enumfacing);
//                    this.setPosition((float) bedLocation.getX() + f1, (float) bedLocation.getY() + 0.6875F, (float) bedLocation.getZ() + f);
//                } else {
//                    this.setPosition((float) bedLocation.getX() + 0.5F, (float) bedLocation.getY() + 0.6875F, (float) bedLocation.getZ() + 0.5F);
//                }
//
//                this.setSize(0.2F, 0.2F);
//
//                this.motionX = 0.0D;
//                this.motionY = 0.0D;
//                this.motionZ = 0.0D;
//            } else {
//                bedPos(CPos.ORIGIN);
//                stopSleeping();
//            }
        } else {
//            this.setSize(0.6F, 1.8F);
        }
    }

    public void startSleeping() {
        isSleeping.set(true);

//        CPos bedLocation = get(EntityVillagerMCA.bedPos);
//        IBlockState blockstate = this.world.getBlockState(bedLocation);
//        if (blockstate.getBlock() == Blocks.BED) {
//            blockstate.getBlock().setBedOccupied(world, bedLocation, null, true);
//        }
    }

    public void stopSleeping() {
        CPos bedLocation = bedPos.get();
        if (bedLocation != CPos.ORIGIN) {
//            IBlockState blockState = this.world.getBlockState(bedLocation);
//
//            if (blockState.getBlock().isBed(blockState, world, bedLocation, this)) {
//                blockState.getBlock().setBedOccupied(world, bedLocation, null, false);
//                CPos blockpos = blockState.getBlock().getBedSpawnPosition(blockState, world, bedLocation, null);
//
//                if (blockpos == null) {
//                    blockpos = bedLocation.up();
//                }
//
//                this.setPosition((float) blockpos.getX() + 0.5F, (float) blockpos.getY() + 0.1F, (float) blockpos.getZ() + 0.5F);
//            }
        }

        isSleeping.set(false);
    }

    public ParentPair getParents() {
        return ParentPair.fromNBT(parents.get());
    }

    public void updateMemories(Memories memories) {
        CNBT nbt = this.memories.get();
        nbt.setTag(memories.getPlayerUUID().toString(), memories.toCNBT());
        this.memories.set(nbt);
    }
}