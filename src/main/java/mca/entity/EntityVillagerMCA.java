package mca.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.UUID;

import java.util.Optional;
import com.google.common.base.Predicate;

import lombok.Getter;
import lombok.Setter;
import mca.api.API;
import mca.api.objects.*;
import mca.api.wrappers.DataManagerWrapper;
import mca.api.wrappers.NBTWrapper;
import mca.api.platforms.VillagerPlatform;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.ai.EntityAIAgeBaby;
import mca.entity.ai.EntityAIChopping;
import mca.entity.ai.EntityAIDefendFromTarget;
import mca.entity.ai.EntityAIFishing;
import mca.entity.ai.EntityAIGoHangout;
import mca.entity.ai.EntityAIGoWorkplace;
import mca.entity.ai.EntityAIHarvesting;
import mca.entity.ai.EntityAIHunting;
import mca.entity.ai.EntityAIMoveState;
import mca.entity.ai.EntityAIProcreate;
import mca.entity.ai.EntityAIProspecting;
import mca.entity.ai.EntityAISleeping;
import mca.entity.data.ParentData;
import mca.entity.data.PlayerHistory;
import mca.entity.data.PlayerSaveData;
import mca.entity.data.SavedVillagers;
import mca.entity.inventory.InventoryMCA;
import mca.enums.EnumAgeState;
import mca.enums.EnumChore;
import mca.enums.EnumConstraint;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMoveState;
import mca.items.ItemSpecialCaseGift;
import mca.util.ItemStackCache;
import mca.util.ResourceLocationCache;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public class EntityVillagerMCA extends VillagerPlatform {
    public static final DataParameter<String> VILLAGER_NAME = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.STRING);
    public static final DataParameter<String> TEXTURE = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.STRING);
    public static final DataParameter<Integer> GENDER = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Float> GIRTH = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> TALLNESS = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.FLOAT);
    public static final DataParameter<NBTTagCompound> PLAYER_HISTORY_MAP = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.COMPOUND_TAG);
    public static final DataParameter<Integer> MOVE_STATE = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<String> SPOUSE_NAME = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.STRING);
    public static final DataParameter<com.google.common.base.Optional<UUID>> SPOUSE_UUID = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<Integer> MARRIAGE_STATE = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean> IS_PROCREATING = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
    public static final DataParameter<NBTTagCompound> PARENTS = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.COMPOUND_TAG);
    public static final DataParameter<Boolean> IS_INFECTED = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> AGE_STATE = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> ACTIVE_CHORE = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean> IS_SWINGING = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> HAS_BABY = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> BABY_IS_MALE = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> BABY_AGE = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<com.google.common.base.Optional<UUID>> CHORE_ASSIGNING_PLAYER = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<BlockPos> BED_POS = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BLOCK_POS);
    public static final DataParameter<BlockPos> WORKPLACE_POS = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BLOCK_POS);
    public static final DataParameter<BlockPos> HANGOUT_POS = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BLOCK_POS);
    public static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
    public final InventoryMCA inventory;
    private Pos home = Pos.ORIGIN;
    
    private static final Predicate<EntityVillagerMCA> BANDIT_TARGET_SELECTOR = (v) -> v.getProfessionForge() != ProfessionsMCA.bandit && v.getProfessionForge() != ProfessionsMCA.child;
    private static final Predicate<EntityVillagerMCA> GUARD_TARGET_SELECTOR = (v) -> v.getProfessionForge() == ProfessionsMCA.bandit;

    public UUID playerToFollowUUID = Constants.ZERO_UUID;

    // Non persisting fields
    private float swingProgressTicks;
    private int startingAge;
    
    // Client side fields
    @Getter @Setter private float renderOffsetX;
    @Getter @Setter private float renderOffsetY;
    @Getter @Setter private float renderOffsetZ;

    public EntityVillagerMCA() {
        super(null);
        inventory = null;
    }

    public EntityVillagerMCA(World worldIn) {
        super(worldIn);
        inventory = new InventoryMCA(this);
    }

    @Override
    protected void initialize() {
        this.dataManager = new DataManagerWrapper(this.getDataManager());
        this.dataManager.register(VILLAGER_NAME, "", "villagerName");
        this.dataManager.register(TEXTURE, "", "texture");
        this.dataManager.register(GENDER, EnumGender.MALE.getId(), "gender");
        this.dataManager.register(GIRTH, 0.0F, "girth");
        this.dataManager.register(TALLNESS, 0.0F, "tallness");
        this.dataManager.register(PLAYER_HISTORY_MAP, new NBTWrapper().getVanillaCompound(), "playerHistoryMap");
        this.dataManager.register(MOVE_STATE, EnumMoveState.MOVE.getId(), "moveState");
        this.dataManager.register(SPOUSE_NAME, "", "spouseName");
        this.dataManager.register(SPOUSE_UUID, com.google.common.base.Optional.of(Constants.ZERO_UUID), "spouseUUID");
        this.dataManager.register(MARRIAGE_STATE, EnumMarriageState.NOT_MARRIED.getId(), "marriageState");
        this.dataManager.register(IS_PROCREATING, false, "isProcreating");
        this.dataManager.register(PARENTS, new NBTWrapper().getVanillaCompound(), "parents");
        this.dataManager.register(IS_INFECTED, false, "isInfected");
        this.dataManager.register(AGE_STATE, EnumAgeState.ADULT.getId(), "ageState");
        this.dataManager.register(ACTIVE_CHORE, EnumChore.NONE.getId(), "activeChore");
        this.dataManager.register(IS_SWINGING, false, "isSwinging");
        this.dataManager.register(HAS_BABY, false, "hasBaby");
        this.dataManager.register(BABY_IS_MALE, false, "babyIsMale");
        this.dataManager.register(BABY_AGE, 0, "babyAge");
        this.dataManager.register(CHORE_ASSIGNING_PLAYER, com.google.common.base.Optional.of(Constants.ZERO_UUID), "choreAssigningPlayer");
        this.dataManager.register(BED_POS, BlockPos.ORIGIN, "bedPos");
        this.dataManager.register(WORKPLACE_POS, BlockPos.ORIGIN, "workplacePos");
        this.dataManager.register(HANGOUT_POS, BlockPos.ORIGIN, "hangoutPos");
        this.dataManager.register(SLEEPING, false, "sleeping");
        this.setSilent(false);
    }

    @Override
    protected void applyAttributes() {
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(MCA.getConfig().villagerMaxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);

        if (this.getHealth() <= MCA.getConfig().villagerMaxHealth) {
            this.setHealth(MCA.getConfig().villagerMaxHealth);
        }
    }

    @Override
    public boolean attackNPC(NPC npc) {
        return npc.attackFrom(DamageSource.causeMobDamage(this), this.getProfessionForge() == ProfessionsMCA.guard ? 9.0F : 2.0F);
    }

    @Override
    public void load(NBTWrapper nbt) {
    	this.dataManager.loadAll(nbt);
        inventory.readInventoryFromNBT(nbt.getTagList("inventory", 10));

        // Vanilla Age doesn't apply from the superclass call. Causes children to revert to the starting age on world reload.
        this.startingAge = nbt.getInteger("startingAge");
        setGrowingAge(nbt.getInteger("Age"));

        this.home = new Pos(nbt.getDouble("homePositionX"), nbt.getDouble("homePositionY"), nbt.getDouble("homePositionZ"));
        this.playerToFollowUUID = nbt.getUUID("playerToFollowUUID");

        applySpecialAI();
    }

    @Override
    public void save(NBTWrapper nbt) {
    	this.dataManager.saveAll(nbt);
    	nbt.setTag("inventory", inventory.writeInventoryToNBT());
    }

    @Override 
    protected float applyDamageModifications(DamageSource source, float amount) {
        if (getProfessionForge() == ProfessionsMCA.guard) {
            amount *= 0.50;
        }
        return amount;
    }
    
    @Override
    protected void afterApplyingDamage(DamageSource source, float amount) {
        if (MCA.getConfig().enableInfection && getProfessionForge() != ProfessionsMCA.guard && source.getImmediateSource() instanceof EntityZombie && getRNG().nextFloat() < MCA.getConfig().infectionChance / 100) {
            set(IS_INFECTED, true);
        }
    }

    @Override
    public void update() {
        updateSwinging();

        if (this.isServerWorld()) {
            onEachServerUpdate();
        } else {
            onEachClientUpdate();
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return get(IS_INFECTED) ? SoundEvents.ENTITY_ZOMBIE_DEATH : null;
    }
    
    @Override
    public void onDeath(DamageSource cause) {
        if (!world.isRemote) {
            String causeName = cause.getImmediateSource() == null ? cause.getDamageType() : cause.getImmediateSource().getName();
            if (MCA.getConfig().logVillagerDeaths) {
                MCA.getLog().info("Villager death: " + get(VILLAGER_NAME) + ". Caused by: " + causeName + ". UUID: " + this.getUniqueID().toString());
            }

            inventory.dropAllItems();

            if (isMarried()) {
                UUID spouseUUID = get(SPOUSE_UUID).or(Constants.ZERO_UUID);
                Optional<EntityVillagerMCA> spouse = world.getVillagerByUUID(spouseUUID);
                PlayerSaveData playerSaveData = PlayerSaveData.getExisting(world, spouseUUID);


                // Notify spouse of the death
                if (spouse.isPresent()) {
                    spouse.get().endMarriage();
                } else if (playerSaveData != null) {
                    playerSaveData.endMarriage();
                    Optional<Player> player = world.getPlayerEntityByUUID(spouseUUID);
                    player.ifPresent(p -> p.sendMessage(Constants.Color.RED + MCA.getLocalizer().localize("notify.spousedied", get(VILLAGER_NAME), causeName)));
                }
            }

            // Notify all parents of the death
            ParentData parents = ParentData.fromNBT(get(PARENTS));
            parents.sendMessage(world, Constants.Color.RED + MCA.getLocalizer().localize("notify.childdied", get(VILLAGER_NAME), causeName));
            SavedVillagers.get(world).save(this);
        }
    }

    @Override
    protected void onGrowingAdult() {
        set(AGE_STATE, EnumAgeState.ADULT.getId());
        
        NPC[] parents = ParentData.fromNBT(get(PARENTS)).getBothParentNPCs(world);
        Arrays.stream(parents).filter((e) -> e instanceof Player).forEach((e) -> {
            PlayerHistory history = getPlayerHistoryFor(e.getUniqueID());
            history.setDialogueType(EnumDialogueType.ADULT);
            e.sendMessage(MCA.getLocalizer().localize("notify.child.grownup", this.get(VILLAGER_NAME)));
        });

        // set profession away from child for villager children
        if (getProfessionForge() == ProfessionsMCA.child) {
            setProfession(ProfessionsMCA.randomProfession());
            setVanillaCareer(getProfessionForge().getRandomCareer(world.rand));
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        // translate profession name
        ITextComponent careerName = new TextComponentTranslation("entity.Villager." + getVanillaCareer().getName());
        EnumAgeState age = EnumAgeState.byId(get(AGE_STATE));
        String professionName = age != EnumAgeState.ADULT ? age.localizedName() : careerName.getUnformattedText();
        String color = this.getProfessionForge() == ProfessionsMCA.bandit ? Constants.Color.RED : this.getProfessionForge() == ProfessionsMCA.guard ? Constants.Color.GREEN : "";

        return new TextComponentString(String.format("%1$s%2$s%3$s (%4$s)", color, MCA.getConfig().villagerChatPrefix, get(VILLAGER_NAME), professionName));
    }

    @Override
    public String getCustomNameTag() {
        return get(VILLAGER_NAME);
    }

    @Override
    public void swing(EnumHand hand) {
        if (!get(IS_SWINGING) || swingProgressTicks >= 8 / 2 || swingProgressTicks < 0) {
            swingProgressTicks = -1;
            set(IS_SWINGING, true);
        }
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
            VillagerRegistry.VillagerProfession profession = getProfessionForge();
            EnumChore chore = EnumChore.byId(get(ACTIVE_CHORE));
            if (get(HAS_BABY)) {
                return ItemStackCache.get(get(BABY_IS_MALE) ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
            } else if (chore != EnumChore.NONE) {
                return inventory.getBestItemOfType(chore.getToolType());
            } else {
                return ProfessionsMCA.getDefaultHeldItem(profession, getVanillaCareer());
            }
        } else {
            return inventory.getBestArmorOfType(slotIn);
        }
    }

    @Override
    protected void initializeAI() {
        this.tasks.addTask(0, new EntityAIProspecting(this));
        this.tasks.addTask(0, new EntityAIHunting(this));
        this.tasks.addTask(0, new EntityAIChopping(this));
        this.tasks.addTask(0, new EntityAIHarvesting(this));
        this.tasks.addTask(0, new EntityAIFishing(this));
        this.tasks.addTask(0, new EntityAIMoveState(this));
        this.tasks.addTask(0, new EntityAIAgeBaby(this));
        this.tasks.addTask(0, new EntityAIProcreate(this));
        this.tasks.addTask(5, new EntityAIGoWorkplace(this));
        this.tasks.addTask(5, new EntityAIGoHangout(this));
        this.tasks.addTask(1, new EntityAISleeping(this));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));
    }
    
    @Override
	public Pos getHomePos() {
		return home;
	}

    private void updateSwinging() {
        if (get(IS_SWINGING)) {
            swingProgressTicks++;

            if (swingProgressTicks >= 8) {
                swingProgressTicks = 0;
                set(IS_SWINGING, false);
            }
        } else {
            swingProgressTicks = 0;
        }
        swingProgress = swingProgressTicks / (float) 8;
    }

    public void setStartingAge(int value) {
        this.startingAge = value;
        setGrowingAge(value);
    }

    public PlayerHistory getPlayerHistoryFor(UUID uuid) {
        if (!get(PLAYER_HISTORY_MAP).hasKey(uuid.toString())) {
            updatePlayerHistoryMap(PlayerHistory.getNew(this, uuid));
        }
        return PlayerHistory.fromNBT(this, uuid, get(PLAYER_HISTORY_MAP).getCompoundTag(uuid.toString()));
    }

    public void updatePlayerHistoryMap(PlayerHistory history) {
        NBTTagCompound nbt = get(PLAYER_HISTORY_MAP);
        nbt.setTag(history.getPlayerUUID().toString(), history.toNBT());
        set(PLAYER_HISTORY_MAP, nbt);
        this.dataManager.setDirty(PLAYER_HISTORY_MAP);
    }

    public void reset() {
        set(PLAYER_HISTORY_MAP, new NBTTagCompound());
        dataManager.setDirty(PLAYER_HISTORY_MAP);

        setHealth(20.0F);

        set(SPOUSE_NAME, "");
        set(SPOUSE_UUID, com.google.common.base.Optional.of(Constants.ZERO_UUID));
        set(MARRIAGE_STATE, EnumMarriageState.NOT_MARRIED.getId());
        set(HAS_BABY, false);
    }

    private void setSizeForAge() {
        EnumAgeState age = EnumAgeState.byId(get(AGE_STATE));
        this.setSize(age.getWidth(), age.getHeight());
        this.setScale(1.0F); // trigger rebuild of the bounding box
    }

    private void toggleMount(Player player) {
        if (getRidingEntity() != null) {
            dismountRidingEntity();
        } else {
            try {
            	startRiding(world.getClosestSaddledHorseTo(this));
                getNavigator().clearPath();
            } catch (NoSuchElementException e) {
                say(Optional.of(player), "interaction.ridehorse.fail.notnearby");
            }
        }
    }

    private void goHome(Player player) {
        if (home.equals(Pos.ORIGIN)) {
            say(Optional.of(player), "interaction.gohome.fail");
        } else {
            say(Optional.of(player), "interaction.gohome.success");
            if (!getNavigator().setPath(getNavigator().getPathToXYZ(home.getX(), home.getY(), home.getZ()), 1.0D)) {
                attemptTeleport(home.getX(), home.getY(), home.getZ());
            }
        }
    }

    /**
     * Forces the villager's home to be set to their position. No checks for safety are made.
     * This is used on overwriting the original villager.
     */
    public void forcePositionAsHome() {
        this.home = new Pos(this.getPosition());
    }

    private void setHome(Player player) {
        if (attemptTeleport(player.getPosX(), player.getPosY(), player.getPosZ())) {
            say(Optional.of(player), "interaction.sethome.success");
            this.home = player.getPosition();
            this.setHomePosAndDistance(this.home.getBlockPos(), 32);
            
            Pos bed = EntityAISleeping.findAnyBed(this);
            if (bed != null) {
                set(BED_POS, bed.getBlockPos());
            }
        } else {
            say(Optional.of(player), "interaction.sethome.fail");
        }
    }

    public void setWorkplace(Player player) {
        say(Optional.of(player), "interaction.setworkplace.success");
        set(WORKPLACE_POS, player.getPosition().getBlockPos());
    }

    public void setHangout(Player player) {
        say(Optional.of(player), "interaction.sethangout.success");
        set(HANGOUT_POS, player.getPosition().getBlockPos());
    }

    public Pos getWorkplace() {
        return new Pos(get(WORKPLACE_POS));
    }

    public Pos getHangout() {
        return new Pos(get(HANGOUT_POS));
    }

    public void say(Optional<Player> player, String phraseId, String... params) {
        ArrayList<String> paramsList = new ArrayList<>();
        if (params != null) Collections.addAll(paramsList, params);

        if (player.isPresent()) {
            Player thePlayer = player.get();

            // Provide player as the first param, always
            paramsList.add(0, thePlayer.getName());

            // Infected villagers do not speak.
            if (get(IS_INFECTED)) {
                thePlayer.sendMessage(getDisplayName().getFormattedText() + ": " + "???");
                this.playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, 0.5F, rand.nextFloat() + 0.5F);
            } else {
                String dialogueType = getPlayerHistoryFor(player.get().getUniqueID()).getDialogueType().getId();
                String phrase = MCA.getLocalizer().localize(dialogueType + "." + phraseId, paramsList);
                thePlayer.sendMessage(String.format("%1$s: %2$s", getDisplayName().getFormattedText(), phrase));
            }
        } else {
            MCA.getLog().warn(new Throwable("Say called on player that is not present!"));
        }
    }

    public boolean isMarried() {
        return !get(SPOUSE_UUID).or(Constants.ZERO_UUID).equals(Constants.ZERO_UUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return get(SPOUSE_UUID).or(Constants.ZERO_UUID).equals(uuid);
    }

    public void marry(Player player) {
        set(SPOUSE_UUID, com.google.common.base.Optional.of(player.getUniqueID()));
        set(SPOUSE_NAME, player.getName());
        set(MARRIAGE_STATE, EnumMarriageState.MARRIED.getId());
    }

    private void endMarriage() {
        set(SPOUSE_UUID, com.google.common.base.Optional.of(Constants.ZERO_UUID));
        set(SPOUSE_NAME, "");
        set(MARRIAGE_STATE, EnumMarriageState.NOT_MARRIED.getId());
    }

    private void handleInteraction(Player player, PlayerHistory history, APIButton button) {
        float successChance = 0.85F;
        int heartsBoost = button.getConstraints().contains(EnumConstraint.ADULTS) ? 15 : 5;

        String interactionName = button.getIdentifier().replace("gui.button.", "");

        successChance -= button.getConstraints().contains(EnumConstraint.ADULTS) ? 0.25F : 0.0F;
        successChance += (history.getHearts() / 10.0D) * 0.025F;

        if (MCA.getConfig().enableDiminishingReturns) successChance -= history.getInteractionFatigue() * 0.05F;

        boolean succeeded = rand.nextFloat() < successChance;
        if (MCA.getConfig().enableDiminishingReturns && succeeded)
            heartsBoost -= history.getInteractionFatigue() * 0.05F;

        history.changeInteractionFatigue(1);
        history.changeHearts(succeeded ? heartsBoost : (heartsBoost * -1));
        String responseId = String.format("%s.%s", interactionName, succeeded ? "success" : "fail");
        say(Optional.of(player), responseId);
    }

    public void handleButtonClick(Player player, String guiKey, String buttonId) {
        PlayerHistory history = getPlayerHistoryFor(player.getUniqueID());
        java.util.Optional<APIButton> button = API.getButtonById(guiKey, buttonId);
        if (!button.isPresent()) {
            MCA.getLog().warn("Button not found for key and ID: " + guiKey + ", " + buttonId);
        } else if (button.get().isInteraction()) handleInteraction(player, history, button.get());

        switch (buttonId) {
            case "gui.button.move":
                set(MOVE_STATE, EnumMoveState.MOVE.getId());
                this.playerToFollowUUID = Constants.ZERO_UUID;
                break;
            case "gui.button.stay":
                set(MOVE_STATE, EnumMoveState.STAY.getId());
                break;
            case "gui.button.follow":
                set(MOVE_STATE, EnumMoveState.FOLLOW.getId());
                this.playerToFollowUUID = player.getUniqueID();
                stopChore();
                break;
            case "gui.button.ridehorse":
                toggleMount(player);
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
                    setCustomer(player.getPlayer());
                    player.displayVillagerTradeGui(this);
                } else {
                    player.sendMessage(MCA.getLocalizer().localize("info.trading.disabled"));
                }
                break;
            case "gui.button.inventory":
                player.openGui(MCA.getInstance(), Constants.GUI_ID_INVENTORY, player.world.getVanillaWorld(), this.getEntityId(), 0, 0);
                break;
            case "gui.button.gift":
                ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
                int giftValue = API.getGiftValueFromStack(stack);
                if (!handleSpecialCaseGift(player, stack)) {
                    if (stack.getItem() == Items.GOLDEN_APPLE) set(IS_INFECTED, false);
                    else {
                        history.changeHearts(giftValue);
                        say(Optional.of(player), API.getResponseForGift(stack));
                    }
                }
                if (giftValue > 0) {
                    player.inventory.decrStackSize(player.inventory.currentItem, 1);
                }
                break;
            case "gui.button.procreate":
                if (PlayerSaveData.get(player).isBabyPresent())
                    say(Optional.of(player), "interaction.procreate.fail.hasbaby");
                else if (history.getHearts() < 100) say(Optional.of(player), "interaction.procreate.fail.lowhearts");
                else {
                    EntityAITasks.EntityAITaskEntry task = tasks.taskEntries.stream().filter((ai) -> ai.action instanceof EntityAIProcreate).findFirst().orElse(null);
                    if (task != null) {
                        ((EntityAIProcreate) task.action).procreateTimer = 20 * 3; // 3 seconds
                        set(IS_PROCREATING, true);
                    }
                }
                break;
            case "gui.button.infected":
                set(IS_INFECTED, !get(IS_INFECTED));
                break;
            case "gui.button.texture.randomize":
                set(TEXTURE, API.getRandomSkin(this));
                break;
            case "gui.button.profession.randomize":
                setProfession(ProfessionsMCA.randomProfession());
                setVanillaCareer(getProfessionForge().getRandomCareer(world.rand));
                break;
            case "gui.button.gender":
                EnumGender gender = EnumGender.byId(get(GENDER));
                if (gender == EnumGender.MALE) {
                    set(GENDER, EnumGender.FEMALE.getId());
                } else {
                    set(GENDER, EnumGender.MALE.getId());
                }
                // intentional fall-through here
            case "gui.button.texture":
                set(TEXTURE, API.getRandomSkin(this));
                break;
            case "gui.button.random":
                set(VILLAGER_NAME, API.getRandomName(EnumGender.byId(get(GENDER))));
                break;
            case "gui.button.profession":
                setProfession(ProfessionsMCA.randomProfession());
                setVanillaCareer(getProfessionForge().getRandomCareer(world.rand));
                applySpecialAI();
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
        }
    }

    private boolean handleSpecialCaseGift(Player player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ItemSpecialCaseGift && !this.isChild()) { // special case gifts are rings so far so prevent giving them to children
            boolean decStackSize = ((ItemSpecialCaseGift) item).handle(player, this);
            if (decStackSize) player.inventory.decrStackSize(player.inventory.currentItem, -1);
            return true;
        } else if (item == Items.CAKE) {
            Optional<NPC> spouse = world.getNPCByUUID(get(SPOUSE_UUID).or(Constants.ZERO_UUID));
            if (spouse.isPresent()) {
                EntityVillagerMCA progressor = this.get(GENDER) == EnumGender.FEMALE.getId() ? this : (EntityVillagerMCA) spouse.get().getEntity();
                progressor.set(HAS_BABY, true);
                progressor.set(BABY_IS_MALE, rand.nextBoolean());
                progressor.spawnParticles(EnumParticleTypes.HEART);
            } else say(Optional.of(player), "gift.cake.fail");
        } else if (item == Items.GOLDEN_APPLE && this.isChild()) {
            this.addGrowth(((startingAge / 4) / 20 * -1));
            return true;
        }

        return false;
    }

    private void onEachClientUpdate() {
        if (get(IS_PROCREATING)) {
            this.rotationYawHead += 50.0F;
        }

        if (this.ticksExisted % 20 == 0) {
            onEachClientSecond();
        }
    }

    private void onEachClientSecond() {
        this.setSizeForAge();
    }

    private void onEachServerUpdate() {
        if (this.ticksExisted % 20 == 0) { // Every second
            onEachServerSecond();
        }

        if (this.ticksExisted % 200 == 0 && this.getHealth() > 0.0F) { // Every 10 seconds and when we're not already dead
            if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F); // heal
            }
        }

        if (isChild()) {
            EnumAgeState current = EnumAgeState.byId(get(AGE_STATE));
            EnumAgeState target = EnumAgeState.byCurrentAge(startingAge, getGrowingAge());
            if (current != target) {
                set(AGE_STATE, target.getId());
            }
        }
    }

    private void onEachServerSecond() {
        NBTTagCompound memories = get(PLAYER_HISTORY_MAP);
        memories.getKeySet().forEach((key) -> PlayerHistory.fromNBT(this, UUID.fromString(key), memories.getCompoundTag(key)).update());
    }

    public ResourceLocation getTextureResourceLocation() {
        if (get(IS_INFECTED)) {
            return ResourceLocationCache.getResourceLocationFor(String.format("mca:skins/%s/zombievillager.png", get(GENDER) == EnumGender.MALE.getId() ? "male" : "female"));
        } else {
            return ResourceLocationCache.getResourceLocationFor(get(TEXTURE));
        }
    }

    private void applySpecialAI() {
        if (getProfessionForge() == ProfessionsMCA.bandit) {
            this.tasks.taskEntries.clear();
            this.tasks.addTask(1, new EntityAIAttackMelee(this, 0.8D, false));
            this.tasks.addTask(2, new EntityAIMoveThroughVillage(this, 0.6D, false));

            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVillagerMCA.class, 100, false, false, BANDIT_TARGET_SELECTOR));
            this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        } else if (getProfessionForge() == ProfessionsMCA.guard) {
        	removeAITask(EntityAIAvoidEntity.class);

            this.tasks.addTask(1, new EntityAIAttackMelee(this, 0.8D, false));
            this.tasks.addTask(2, new EntityAIMoveThroughVillage(this, 0.6D, false));

            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVillagerMCA.class, 100, false, false, GUARD_TARGET_SELECTOR));
            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityZombie.class, 100, false, false, null));
            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVex.class, 100, false, false, null));
            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVindicator.class, 100, false, false, null));
        } else {
            //every other villager is allowed to defend itself from zombies while fleeing
            this.tasks.addTask(0, new EntityAIDefendFromTarget(this));

            this.targetTasks.taskEntries.clear();
            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityZombie.class, 100, false, false, null));
        }
    }

    private void removeAITask(Class<? extends EntityAIBase> clazz) {
        this.tasks.taskEntries.removeIf(ai -> ai.getClass().equals(clazz));
    }

    public void spawnParticles(EnumParticleTypes particleType) {
        if (this.world.isRemote) {
            for (int i = 0; i < 5; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(particleType, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + 1.0D + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2);
            }
        } else {
            NetMCA.INSTANCE.sendToAll(new NetMCA.SpawnParticles(this.getUniqueID(), particleType));
        }
    }

    public void stopChore() {
        set(ACTIVE_CHORE, EnumChore.NONE.getId());
        set(CHORE_ASSIGNING_PLAYER, com.google.common.base.Optional.of(Constants.ZERO_UUID));
    }

    public void startChore(EnumChore chore, Player player) {
        set(ACTIVE_CHORE, chore.getId());
        set(CHORE_ASSIGNING_PLAYER, com.google.common.base.Optional.of(player.getUniqueID()));
    }

    public boolean playerIsParent(Player player) {
        ParentData data = ParentData.fromNBT(get(PARENTS));
        return data.getParent1UUID().equals(player.getUniqueID()) || data.getParent2UUID().equals(player.getUniqueID());
    }

    public String getCurrentActivity() {
        EnumMoveState moveState = EnumMoveState.byId(get(MOVE_STATE));
        if (moveState != EnumMoveState.MOVE) {
            return moveState.getFriendlyName();
        }

        EnumChore chore = EnumChore.byId(get(ACTIVE_CHORE));
        if (chore != EnumChore.NONE) {
            return chore.getFriendlyName();
        }

        return null;
    }

    public void moveTowardsBlock(Pos target) {
        moveTowardsBlock(target, 0.5D);
    }

    public void moveTowardsBlock(Pos target, double speed) {
        double range = getNavigator().getPathSearchRange() - 6.0D;

        if (getDistanceSq(target.getBlockPos()) > Math.pow(range, 2.0)) {
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this, (int) range, 8, new Vec3d(target.getX(), target.getY(), target.getZ()));
            if (vec3d != null && !getNavigator().setPath(getNavigator().getPathToXYZ(vec3d.x, vec3d.y, vec3d.z), speed)) {
                attemptTeleport(vec3d.x, vec3d.y, vec3d.z);
            }
        } else {
            if (!getNavigator().setPath(getNavigator().getPathToPos(target.getBlockPos()), speed)) {
                attemptTeleport(target.getX(), target.getY(), target.getZ());
            }
        }
    }
}