package com.minecraftcomesalive.mca.entity;

import cobalt.core.CConstants;
import cobalt.enums.CEnumHand;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.inventory.CEquipmentSlotType;
import cobalt.minecraft.inventory.CInventory;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.pathfinding.CPathNavigator;
import cobalt.minecraft.util.CDamageSource;
import cobalt.minecraft.util.CText;
import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.util.math.CVec3f;
import com.minecraftcomesalive.mca.api.API;
import com.minecraftcomesalive.mca.core.Config;
import com.minecraftcomesalive.mca.core.MCA;
import com.minecraftcomesalive.mca.entity.data.MCAPlayerData;
import com.minecraftcomesalive.mca.entity.data.Memories;
import com.minecraftcomesalive.mca.entity.data.ParentPair;
import com.minecraftcomesalive.mca.entity.data.SavedVillagers;
import com.minecraftcomesalive.mca.enums.EnumAgeState;
import com.minecraftcomesalive.mca.enums.EnumDialogueType;
import com.minecraftcomesalive.mca.enums.EnumGender;
import com.minecraftcomesalive.mca.enums.EnumMoveState;
import com.minecraftcomesalive.mca.wrappers.VillagerWrapper;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;
import java.util.*;

public class EntityVillagerMCA extends VillagerWrapper implements IEntityAdditionalSpawnData {
    public static final DataParameter<CompoundNBT> VILLAGER_PARAMS = EntityDataManager.createKey(EntityVillagerMCA.class, DataSerializers.COMPOUND_NBT);
    public final CInventory inventory;

    // Non-persistent
    private float swingProgressTicks;
    private int startingAge;

    // Client-side fields
    private CVec3f renderOffset;

    public EntityVillagerMCA(EntityType<? extends EntityVillagerMCA> type, World world) {
        super(type, world);
        inventory = new CInventory(CEntity.fromMC(this), 27);
    }

    @Override
    public void load(CNBT nbt) {
        setVillagerParams(nbt.getCompoundTag("params"));

        this.startingAge = nbt.getInteger("startingAge");
        setGrowingAge(nbt.getInteger("Age"));
        inventory.load(nbt.getList("inventory"));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setTag("params", getVillagerParams());
        nbt.setInteger("startingAge", this.startingAge);
        nbt.setList("inventory", inventory.save());
    }

    @Override
    public CPos getHomePos() {
        return getVillagerParams().getCPos("homePos");
    }

    public void setHomePos(CPos value) {
        getVillagerParams().setCPos("homePos", value);
    }

    @Override
    public float beforeDamaged(CDamageSource source, float amount) {
        if (this.getProfession() == MCA.PROFESSION_GUARD.get()) {
            amount /= 2; // Guards take half damage
        }
        return amount;
    }

    @Override
    public void afterDamaged(CDamageSource source, float amount) {
        source.getPlayer().ifPresent(e -> {
            e.sendMessage(MCA.localize("villager.hurt"));
        });

        if (source.isZombie() && Config.enableInfection && getRNG().nextFloat() < Config.infectionChance / 100) {
            setIsInfected(true);
        }
    }

    @Override
    public CItemStack getEquipmentOfType(CEquipmentSlotType type) {
        //TODO clean this up
        return CItemStack.fromMC(inventory.getBestArmorOfType(type.getMcType()));
    }

    @Override
    protected void initialize() {
        this.dataManager.register(VILLAGER_PARAMS, new CompoundNBT());
        this.setTexture("");
        this.setSilent(true);
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void swing(CEnumHand hand) {
        if (!getIsSwinging() || swingProgressTicks >= 4 || swingProgressTicks < 0) {
            swingProgressTicks = -1;
            setIsSwinging(true);
        }
    }

    @Override
    protected void initializeAI() {

    }

    @Override
    protected void onGrowingAdult() {
        setAgeState(EnumAgeState.ADULT);

        // Notify player parents of the age up and set correct dialogue type.
        CEntity[] parents = getParents().getBothParentEntities(this.world);
        Arrays.stream(parents).forEach(e -> e.asPlayer().ifPresent(p -> {
            getMemoriesForPlayer(p).setDialogueType(EnumDialogueType.ADULT);
            p.sendMessage(MCA.localize("notify.child.grownup", this.getVillagerName()));
        }));

        // Change profession away from child for villager children.
        if (getProfession() == MCA.PROFESSION_CHILD.get()) {
            setProfession(API.randomProfession().getMcProfession());
        }
    }

    @Override
    public void handleDeath(CDamageSource cause) {
        // Play zombie death sound when infected.
        if (getIsInfected()) {
            this.playSound(SoundEvents.ENTITY_ZOMBIE_DEATH, this.getSoundVolume(), this.getSoundPitch());
        }

        // Notify spouse and reset marriage
        if (isMarried()) {
            Optional<EntityVillagerMCA> spouse = getSpouseEntity();
            MCAPlayerData playerData = MCAPlayerData.get(world, getSpouseUUID());

            if (spouse.isPresent()) {
                this.setSpouse(null);
            } else if (playerData != null) {
                playerData.endMarriage();
                world.getPlayerEntityByUUID(getSpouseUUID()).ifPresent(p -> p.sendMessage(CText.Color.RED + MCA.localize("notify.spousedied", getVillagerName(), cause.getCauseName(this))));
            }
        }

        // Notify parents
        ParentPair parents = getParents();
        parents.sendMessage(world, CText.Color.RED + MCA.localize("notify.childdied", getVillagerName(), cause.getCauseName(this)));

        // Save so that the villager persists
        SavedVillagers.get(world).saveVillager(this);

        // Write to log if configured
        if (Config.logVillagerDeaths) {
            MCA.log(String.format("Villager death: %s. Caused by: %s, UUID: %s", this.getVillagerName(), cause.getCauseName(this), this.getUniqueID().toString()));
        }

        // Drop everything.
        inventory.dropAllItems();
    }

    @Override
    public void onRightClick(CPlayer player, CEnumHand hand) {
        // open gui -> display buttons -> on click button, send click event to server
        // -> trigger interaction func with IInteraction on server.
    }

    @Override
    public String getNameForDisplay() {
        String professionName = getAgeState() == EnumAgeState.ADULT ? getCareerName().getString() : getAgeState().localizedName();
        String color = this.getProfession() == MCA.PROFESSION_GUARD.get() ? CText.Color.GREEN : "";
        return String.format("%1$s%2$s%3$s (%4$s)", color, Config.villagerChatPrefix, getVillagerName(), professionName);
    }

    @Override
    public boolean attack(CEntity e) {
        return false;
    }

    public void setStartingAge(int value) {
        this.startingAge = value;
        setGrowingAge(value);
    }

    public void reset() {
        setMemories(CNBT.createNew());
        setHealth(20.0F);
        setSpouse(null);
        setBabyGender(EnumGender.UNASSIGNED);
    }

    public void goHome(CPlayer player) {
        if (getHomePos().equals(CPos.ORIGIN)) {
            say(player, "interaction.gohome.fail");
        } else {
            CPathNavigator nav = CPathNavigator.fromMC(this.getNavigator());
            if (!nav.tryGoTo(getHomePos())) {
                attemptTeleport(getHomePos().getX(), getHomePos().getY(), getHomePos().getZ(), false);
            }
            say(player, "interaction.gohome.success");
        }
    }

    public void setHomeSafely(CPlayer player) {
        if (attemptTeleport(player.getPosX(), player.getPosY(), player.getPosZ(), true)) {
            say(player, "interaction.sethome.success");
            setHomePos(player.getPosition());
            setHomePosAndDistance(player.getPosition().getMcPos(), 32);

            //TODO sleeping/find bed
        } else {
            say(player, "interaction.sethome.fail");
        }
    }

    public void say(CPlayer target, String phraseId, String... params) {
        ArrayList<String> paramList = new ArrayList<>();
        if (params != null) Collections.addAll(paramList, params);

        // Player is always first in params passed to localizer for say().
        paramList.add(0, target.getName());

        String chatPrefix = Config.villagerChatPrefix + getDisplayName().getFormattedText() + ": ";
        if (getIsInfected()) { // Infected villagers do not speak
            target.sendMessage(chatPrefix + "???");
            playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, this.getSoundVolume(), this.getSoundPitch());
        } else {
            EnumDialogueType dialogueType = getMemoriesForPlayer(target).getDialogueType();
            target.sendMessage(chatPrefix + MCA.localize(dialogueType + "." + phraseId, params));
        }
    }

    /*******************************************************************************************************************
     *                                         Attribute getters and setters
     ******************************************************************************************************************/
    public CNBT getVillagerParams() {
        return CNBT.fromMC(dataManager.get(VILLAGER_PARAMS));
    }

    public void setVillagerParams(CNBT params) {
        dataManager.set(VILLAGER_PARAMS, params.getMcCompound());
    }

    public void updateAttribute(String key, Object value) {
        CNBT attrs = getVillagerParams();
        attrs.set(key, value);
        dataManager.set(VILLAGER_PARAMS, attrs.getMcCompound());
    }

    public boolean hasBaby() { return getBabyGender() != EnumGender.UNASSIGNED; }
    public boolean isMarried() { return !getSpouseUUID().equals(CConstants.ZERO_UUID); }
    public boolean isMarriedTo(UUID uniqueID) { return getSpouseUUID().equals(uniqueID); }
    public boolean playerIsParent(CPlayer player) { return getParents().isParent(player); }
    public boolean playerIsSpouse(CPlayer player) { return getSpouseUUID().equals(player.getUniqueID()); }

    public String getVillagerName() { return getVillagerParams().getString("name"); }
    public void setVillagerName(String value) {
        updateAttribute("name", value);
    }

    public String getTexture() { return getVillagerParams().getString("texture"); }
    public void setTexture(String value) {
        updateAttribute("texture", value);
    }

    public EnumGender getGender() { return EnumGender.byId(getVillagerParams().getInteger("genderId")); }
    public void setGender(EnumGender value) {
        updateAttribute("genderId", value.getId());
    }

    public float getGirth() { return getVillagerParams().getFloat("girth"); }
    public void setGirth(float value) {
        updateAttribute("girth", value);
    }

    public float getTallness() { return getVillagerParams().getFloat("tallness"); }
    public void setTallness(float value) {
        updateAttribute("tallness", value);
    }

    public EnumMoveState getMoveState() { return EnumMoveState.byId(getVillagerParams().getInteger("moveStateId")); }
    public void setMoveState(EnumMoveState value) {
        updateAttribute("moveStateId", value.getId());
    }

    public EnumAgeState getAgeState() { return EnumAgeState.byId(getVillagerParams().getInteger("ageStateId")); }
    public void setAgeState(EnumAgeState value) {
        updateAttribute("ageStateId", value.getId());
    }

    public String getSpouseName() { return getVillagerParams().getString("spouseName"); }
    public UUID getSpouseUUID() { return getVillagerParams().getUUID("spouseUUID"); }
    public void setSpouse(@Nullable CEntity entity) {
        if (entity == null) {
            updateAttribute("spouseName", "");
            updateAttribute("spouseUUID", CConstants.ZERO_UUID);
        } else {
            updateAttribute("spouseName", entity.getName());
            updateAttribute("spouseUUID", entity.getUniqueID());
        }
    }

    public boolean getIsProcreating() { return getVillagerParams().getBoolean("isProcreating"); }
    public void setIsProcreating(boolean value) {
        updateAttribute("isProcreating", value);
    }

    public boolean getIsInfected() { return getVillagerParams().getBoolean("isInfected"); }
    public void setIsInfected(boolean value) {
        updateAttribute("isInfected", value);
    }

    public int getActiveChore() { return getVillagerParams().getInteger("activeChore"); }
    public void setActiveChore(int value) {
        updateAttribute("activeChore", value);
    }

    public boolean getIsSwinging() { return getVillagerParams().getBoolean("isSwinging"); }
    public void setIsSwinging(boolean value) {
        updateAttribute("isSwinging", value);
    }

    public EnumGender getBabyGender() { return EnumGender.byId(getVillagerParams().getInteger("babyGenderId")); }
    public void setBabyGender(EnumGender value) {
        updateAttribute("babyGenderId", value.getId());
    }

    public int getBabyAge() { return getVillagerParams().getInteger("babyAge"); }
    public void setBabyAge(int value) {
        updateAttribute("babyAge", value);
    }

    public UUID getChoreAssigningPlayer() { return getVillagerParams().getUUID("choreAssigningPlayer"); }
    public void setChoreAssigningPlayer(UUID uuid) {
        updateAttribute("choreAssigningPlayer", uuid);
    }

    public CPos getBedPos() { return getVillagerParams().getCPos("bedPos"); }
    public void setBedPos(CPos pos) {
        updateAttribute("bedPos", pos);
    }

    public CPos getWorkplacePos() { return getVillagerParams().getCPos("workPos"); }
    public void setWorkplacePos(CPos pos) {
        updateAttribute("workPos", pos);
    }

    public CPos getHangoutPos() { return getVillagerParams().getCPos("hangoutPos"); }
    public void setHangoutPos(CPos pos) {
        updateAttribute("hangoutPos", pos);
    }

    public boolean getIsSleeping() { return getVillagerParams().getBoolean("isSleeping"); }
    public void setSleeping(boolean value) {
        updateAttribute("isSleeping", value);
    }

    public ParentPair getParents() { return ParentPair.fromNBT(getVillagerParams().getCompoundTag("parents")); }
    public void setParents(ParentPair value) {
        updateAttribute("parents", value.toNBT());
    }

    public UUID getFollowingPlayer() {
        return getVillagerParams().getUUID("followingPlayer");
    }

    public void setFollowingPlayer(CPlayer uuid) {
        updateAttribute("followingPlayer", uuid);
    }

    public CNBT getMemories() {
        return getVillagerParams().getCompoundTag("memories");
    }

    public void setMemories(CNBT cnbt) {
        updateAttribute("memories", cnbt);
    }

    public Memories getMemoriesForPlayer(CPlayer player) {
        Memories returnMemories = Memories.fromCNBT(this, getMemories().getCompoundTag(player.getUniqueID().toString()));
        if (returnMemories == null) {
            returnMemories = Memories.getNew(this, player.getUniqueID());
            setMemories(getMemories().setTag(player.getUniqueID().toString(), returnMemories.toCNBT()));
        }
        return returnMemories;
    }

    public void updateMemories(Memories memories) {
        CNBT nbt = getMemories();
        nbt.setTag(memories.getPlayerUUID().toString(), memories.toCNBT());
        setMemories(nbt);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        System.out.println("Write spawn data");
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        System.out.println("Read spawn data");
    }

    private Optional<EntityVillagerMCA> getSpouseEntity() {
        Optional<CEntity> entity = this.world.getEntityByUUID(getSpouseUUID());
        if (entity.isPresent() && entity.get().getMcEntity() instanceof EntityVillagerMCA) {
            return Optional.of((EntityVillagerMCA)entity.get().getMcEntity());
        }
        return Optional.empty();
    }
}
