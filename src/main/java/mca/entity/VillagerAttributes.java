package mca.entity;

import static mca.core.Constants.EMPTY_UUID;
import static mca.core.Constants.EMPTY_UUID_OPT;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Optional;

import io.netty.buffer.ByteBuf;
import mca.actions.ActionStoryProgression;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.data.TransitiveVillagerData;
import mca.enums.EnumBabyState;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMovementState;
import mca.enums.EnumPersonality;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumProgressionStep;
import mca.inventory.VillagerInventory;
import mca.packets.PacketSetSize;
import mca.util.Either;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import radixcore.modules.RadixNettyIO;

public class VillagerAttributes
{
	private final EntityVillagerMCA villager;
	private final EntityDataManager dataManager;

	private static final DataParameter<String> NAME = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<String> HEAD_TEXTURE = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<String> CLOTHES_TEXTURE = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<Integer> PROFESSION = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> PERSONALITY = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> GENDER = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<String> SPOUSE_NAME = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<Optional<UUID>> SPOUSE_UUID = EntityDataManager.<Optional<UUID>>createKey(EntityVillagerMCA.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Integer> SPOUSE_GENDER = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<String> MOTHER_NAME = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<Optional<UUID>> MOTHER_UUID = EntityDataManager.<Optional<UUID>>createKey(EntityVillagerMCA.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Integer> MOTHER_GENDER = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<String> FATHER_NAME = EntityDataManager.<String>createKey(EntityVillagerMCA.class, DataSerializers.STRING);
	private static final DataParameter<Optional<UUID>> FATHER_UUID = EntityDataManager.<Optional<UUID>>createKey(EntityVillagerMCA.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Integer> FATHER_GENDER = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> BABY_STATE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> MOVEMENT_STATE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Float> SCALE_HEIGHT = EntityDataManager.<Float>createKey(EntityVillagerMCA.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> SCALE_WIDTH = EntityDataManager.<Float>createKey(EntityVillagerMCA.class, DataSerializers.FLOAT);
	private static final DataParameter<Boolean> DO_DISPLAY = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IS_SWINGING = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> HELD_ITEM_SLOT = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> IS_INFECTED = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DO_OPEN_INVENTORY = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> MARRIAGE_STATE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);

	private int timesWarnedForLowHearts;
	private int ticksAlive;
	private Map<UUID, PlayerMemory> playerMemories;
	private final VillagerInventory inventory;
	
	public VillagerAttributes(EntityVillagerMCA villager)
	{
		this.villager = villager;
		this.dataManager = villager.getDataManager();
		playerMemories = new HashMap<UUID, PlayerMemory>();
		inventory = new VillagerInventory();
	}

	public VillagerAttributes(NBTTagCompound nbt) 
	{
		this.villager = null;
		this.dataManager = null;
		this.inventory = null;
		playerMemories = new HashMap<UUID, PlayerMemory>();
		readFromNBT(nbt);
	}

	public void initialize()
	{
		dataManager.register(NAME, "Steve");
		dataManager.register(HEAD_TEXTURE, "");
		dataManager.register(CLOTHES_TEXTURE, "");
		dataManager.register(PROFESSION, EnumProfession.Farmer.getId());
		dataManager.register(PERSONALITY, EnumPersonality.FRIENDLY.getId());
		dataManager.register(GENDER, EnumGender.MALE.getId());
		dataManager.register(SPOUSE_NAME, "N/A");
		dataManager.register(SPOUSE_UUID, Constants.EMPTY_UUID_OPT);
		dataManager.register(SPOUSE_GENDER, EnumGender.UNASSIGNED.getId());
		dataManager.register(MOTHER_NAME, "N/A");
		dataManager.register(MOTHER_UUID, Constants.EMPTY_UUID_OPT);
		dataManager.register(MOTHER_GENDER, EnumGender.UNASSIGNED.getId());
		dataManager.register(FATHER_NAME, "N/A");
		dataManager.register(FATHER_UUID, Constants.EMPTY_UUID_OPT);
		dataManager.register(FATHER_GENDER, EnumGender.UNASSIGNED.getId());
		dataManager.register(BABY_STATE, EnumBabyState.NONE.getId());
		dataManager.register(MOVEMENT_STATE, EnumMovementState.MOVE.getId());
		dataManager.register(IS_CHILD, Boolean.valueOf(false));
		dataManager.register(AGE, Integer.valueOf(0));
		dataManager.register(SCALE_HEIGHT, Float.valueOf(0));
		dataManager.register(SCALE_WIDTH, Float.valueOf(0));
		dataManager.register(DO_DISPLAY, Boolean.valueOf(false));
		dataManager.register(IS_SWINGING, Boolean.valueOf(false));
		dataManager.register(HELD_ITEM_SLOT, Integer.valueOf(0));
		dataManager.register(IS_INFECTED, Boolean.valueOf(false));
		dataManager.register(DO_OPEN_INVENTORY, Boolean.valueOf(false));
		dataManager.register(MARRIAGE_STATE, Integer.valueOf(0));
	}

	/*
	 * Copies all data from a given transitive villager data object.
	 */
	public void copyFrom(TransitiveVillagerData data)
	{
		setName(data.getName());
		setHeadTexture(data.getHeadTexture());
		setClothesTexture(data.getClothesTexture());
		setProfession(data.getProfession());
		setPersonality(data.getPersonality());
		setGender(data.getGender());
		setSpouseUUID(data.getSpouseUUID());
		setSpouseGender(data.getSpouseGender());
		setSpouseName(data.getSpouseName());
		setMotherUUID(data.getMotherUUID());
		setMotherGender(data.getMotherGender());
		setMotherName(data.getMotherName());
		setFatherUUID(data.getFatherUUID());
		setFatherGender(data.getFatherGender());
		setFatherName(data.getFatherName());
		setBabyState(data.getBabyState());
		setMovementState(data.getMovementState());
		setIsChild(data.getIsChild());
		setAge(data.getAge());
		setScaleHeight(data.getScaleHeight());
		setScaleWidth(data.getScaleWidth());
		setDoDisplay(data.getDoDisplay());
		setIsSwinging(data.getIsSwinging());
		setHeldItemSlot(data.getHeldItemSlot());
		setIsInfected(data.getIsInfected());
		setDoOpenInventory(data.getDoOpenInventory());
		setMarriageState(data.getMarriageState());
	}
	
	public String getName()
	{
		return dataManager.get(NAME);
	}

	public void setName(String name)
	{
		dataManager.set(NAME, name);
	}

	public String getHeadTexture()
	{
		return dataManager.get(HEAD_TEXTURE);
	}

	public void setHeadTexture(String texture)
	{
		dataManager.set(HEAD_TEXTURE, texture);
	}

	public String getClothesTexture()
	{
		return dataManager.get(CLOTHES_TEXTURE);
	}

	public void setClothesTexture(String texture)
	{
		dataManager.set(CLOTHES_TEXTURE, texture);
	}

	public void assignRandomSkin()
	{
		if (this.getGender() == EnumGender.UNASSIGNED)
		{
			Throwable t = new Throwable();
			MCA.getLog().error("Attempted to randomize skin on unassigned gender villager.");
			MCA.getLog().error(t);
		}

		else
		{
			EnumProfessionSkinGroup skinGroup = this.getProfessionSkinGroup();
			String skin = this.getGender() == EnumGender.MALE ? skinGroup.getRandomMaleSkin() : skinGroup.getRandomFemaleSkin();
			setHeadTexture(skin);
			setClothesTexture(skin);
		}
	}

	public void assignRandomScale()
	{

	}

	public EnumProfession getProfessionEnum()
	{
		return EnumProfession.getProfessionById(dataManager.get(PROFESSION));
	}

	public EnumProfessionSkinGroup getProfessionSkinGroup()
	{
		return EnumProfession.getProfessionById(dataManager.get(PROFESSION).intValue()).getSkinGroup();
	}

	public void setProfession(EnumProfession profession)
	{
		dataManager.set(PROFESSION, profession.getId());
	}

	public EnumPersonality getPersonality()
	{
		return EnumPersonality.getById(dataManager.get(PERSONALITY));
	}

	public void setPersonality(EnumPersonality personality)
	{
		dataManager.set(PERSONALITY, personality.getId());
	}

	public EnumGender getGender()
	{
		return EnumGender.byId(dataManager.get(GENDER));
	}

	public void setGender(EnumGender gender)
	{
		dataManager.set(GENDER, gender.getId());
	}

	public String getSpouseName()
	{
		return dataManager.get(SPOUSE_NAME);
	}

	public UUID getSpouseUUID()
	{
		return dataManager.get(SPOUSE_UUID).or(EMPTY_UUID);
	}

	public EnumGender getSpouseGender()
	{
		return EnumGender.byId(dataManager.get(SPOUSE_GENDER));
	}


	/* Performs an engagement between this villager and provided player. 
	 * DOES NOT handle nulls. To end an engagement, call setSpouse with null.
	 * */
	public void setFiancee(EntityPlayer player) 
	{
		if (player == null) throw new Error("Engagement player cannot be null");

		NBTPlayerData playerData = MCA.getPlayerData(player);

		dataManager.set(SPOUSE_NAME, player.getName());
		dataManager.set(SPOUSE_UUID, Optional.of(player.getUniqueID()));
		dataManager.set(SPOUSE_GENDER, playerData.getGender().getId());

		setMarriageState(EnumMarriageState.ENGAGED);

		playerData.setSpouseName(this.getName());
		playerData.setSpouseGender(this.getGender());
		playerData.setSpouseUUID(villager.getUniqueID());
		playerData.setMarriageState(EnumMarriageState.ENGAGED);

		//Prevent story progression when engaged to a player
		villager.getBehavior(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.FINISHED);
	}

	public EntityVillagerMCA getVillagerSpouseInstance()
	{
		for (Object obj : villager.world.loadedEntityList)
		{
			if (obj instanceof EntityVillagerMCA)
			{
				EntityVillagerMCA villager = (EntityVillagerMCA)obj;

				if (villager.getUniqueID().equals(getSpouseUUID()))
				{
					return villager;
				}
			}
		}

		return null;
	}

	public EntityPlayer getPlayerSpouseInstance()
	{
		for (Object obj : villager.world.playerEntities)
		{
			final EntityPlayer player = (EntityPlayer)obj;

			if (player.getUniqueID().equals(this.getSpouseUUID()))
			{
				return player;
			}
		}

		return null;
	}

	public String getMotherName()
	{
		return dataManager.get(MOTHER_NAME);
	}

	public UUID getMotherUUID()
	{
		return dataManager.get(MOTHER_UUID).or(EMPTY_UUID);
	}

	public EnumGender getMotherGender()
	{
		return EnumGender.byId(dataManager.get(MOTHER_GENDER));
	}

	public void setMotherName(String name)
	{
		if (name == null) {
			name = "N/A";
		}
		
		dataManager.set(MOTHER_NAME, name);
	}
	
	public void setMotherUUID(UUID uuid)
	{
		dataManager.set(MOTHER_UUID, Optional.of(uuid));
	}
	
	public void setMotherGender(EnumGender gender)
	{
		dataManager.set(MOTHER_GENDER, gender.getId());
	}
	
	public void setMother(Either<EntityVillagerMCA, EntityPlayer> either)
	{
		if (either == null)
		{
			dataManager.set(MOTHER_NAME, "");
			dataManager.set(MOTHER_UUID, EMPTY_UUID_OPT);
			dataManager.set(MOTHER_GENDER, EnumGender.UNASSIGNED.getId());
		}

		else if (either.getLeft() != null)
		{
			EntityVillagerMCA mother = either.getLeft();
			dataManager.set(MOTHER_NAME, mother.attributes.getName());
			dataManager.set(MOTHER_UUID, Optional.of(mother.getUniqueID()));
			dataManager.set(MOTHER_GENDER, mother.attributes.getGender().getId());
		}

		else if (either.getRight() != null)
		{
			EntityPlayer player = either.getRight();
			NBTPlayerData data = MCA.getPlayerData(player);

			dataManager.set(MOTHER_NAME, player.getName());
			dataManager.set(MOTHER_UUID, Optional.of(player.getUniqueID()));
			dataManager.set(MOTHER_GENDER, data.getGender().getId());
		}
	}

	public String getFatherName()
	{
		return dataManager.get(FATHER_NAME);
	}

	public UUID getFatherUUID()
	{
		return dataManager.get(FATHER_UUID).or(EMPTY_UUID);
	}

	public EnumGender getFatherGender()
	{
		return EnumGender.byId(dataManager.get(FATHER_GENDER));
	}

	public void setFatherName(String name)
	{
		if (name == null) {
			name = "N/A";
		}
		
		dataManager.set(FATHER_NAME, name);
	}
	
	public void setFatherUUID(UUID uuid)
	{
		dataManager.set(FATHER_UUID, Optional.of(uuid));
	}
	
	public void setFatherGender(EnumGender gender)
	{
		dataManager.set(FATHER_GENDER, gender.getId());
	}
	
	public void setFather(Either<EntityVillagerMCA, EntityPlayer> either)
	{
		if (either == null)
		{
			dataManager.set(FATHER_NAME, "");
			dataManager.set(FATHER_UUID, EMPTY_UUID_OPT);
			dataManager.set(FATHER_GENDER, EnumGender.UNASSIGNED.getId());
		}

		else if (either.getLeft() != null)
		{
			EntityVillagerMCA father = either.getLeft();
			dataManager.set(FATHER_NAME, father.attributes.getName());
			dataManager.set(FATHER_UUID, Optional.of(father.getUniqueID()));
			dataManager.set(FATHER_GENDER, father.attributes.getGender().getId());
		}

		else if (either.getRight() != null)
		{
			EntityPlayer player = either.getRight();
			NBTPlayerData data = MCA.getPlayerData(player);

			dataManager.set(FATHER_NAME, player.getName());
			dataManager.set(FATHER_UUID, Optional.of(player.getUniqueID()));
			dataManager.set(FATHER_GENDER, data.getGender().getId());
		}
	}

	public EnumBabyState getBabyState()
	{
		return EnumBabyState.fromId(dataManager.get(BABY_STATE));
	}

	public void setBabyState(EnumBabyState state)
	{
		dataManager.set(BABY_STATE, state.getId());
	}

	public EnumMovementState getMovementState()
	{
		return EnumMovementState.fromId(dataManager.get(MOVEMENT_STATE));
	}

	public void setMovementState(EnumMovementState state)
	{
		dataManager.set(MOVEMENT_STATE, state.getId());
	}

	public boolean getIsChild()
	{
		return dataManager.get(IS_CHILD);
	}

	public void setIsChild(boolean isChild)
	{
		dataManager.set(IS_CHILD, isChild);

		EnumDialogueType newDialogueType = isChild ? EnumDialogueType.CHILD : EnumDialogueType.ADULT;
		EnumDialogueType targetReplacementType = isChild ? EnumDialogueType.ADULT : EnumDialogueType.CHILD;

		for (PlayerMemory memory : playerMemories.values())
		{
			if (memory.getDialogueType() == targetReplacementType)
			{
				memory.setDialogueType(newDialogueType);
			}
		}
	}

	public boolean isChildOfAVillager() 
	{
		// If we can't find data for the mother and father, the child's parents
		// must be other villagers.
		NBTPlayerData motherData = MCA.getPlayerData(villager.world, getMotherUUID());
		NBTPlayerData fatherData = MCA.getPlayerData(villager.world, getFatherUUID());

		return motherData == null && fatherData == null;
	}

	public int getAge()
	{
		return dataManager.get(AGE).intValue();
	}

	public void setAge(int age)
	{
		dataManager.set(AGE, age);
	}

	public float getScaleHeight()
	{
		return dataManager.get(SCALE_HEIGHT);
	}

	public void setScaleHeight(float value)
	{
		dataManager.set(SCALE_HEIGHT, value);
	}

	public float getScaleWidth()
	{
		return dataManager.get(SCALE_WIDTH);
	}

	public void setScaleWidth(float value)
	{
		dataManager.set(SCALE_WIDTH, value);
	}

	public boolean getDoDisplay()
	{
		return dataManager.get(DO_DISPLAY);
	}

	public void setDoDisplay(boolean value)
	{
		dataManager.set(DO_DISPLAY, value);
	}

	public boolean getIsSwinging()
	{
		return dataManager.get(IS_SWINGING);
	}

	public void setIsSwinging(boolean value)
	{
		dataManager.set(IS_SWINGING, value);
	}

	@Deprecated
	public boolean getIsMale()
	{
		return getGender() == EnumGender.MALE;
	}

	@Deprecated
	public String getParentNames() 
	{
		return this.getMotherName() + "|" + this.getFatherName();
	}

	@Deprecated
	public boolean getIsMarried()
	{
		return getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER || getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER;
	}

	public boolean getCanBeHired(EntityPlayer player) 
	{
		return getPlayerSpouseInstance() != player && (getProfessionSkinGroup() == EnumProfessionSkinGroup.Farmer || 
				getProfessionSkinGroup() == EnumProfessionSkinGroup.Miner || 
				getProfessionSkinGroup() == EnumProfessionSkinGroup.Warrior);
	}


	public boolean getDoOpenInventory()
	{
		return dataManager.get(DO_OPEN_INVENTORY);
	}

	public void setDoOpenInventory(boolean value)
	{
		dataManager.set(DO_OPEN_INVENTORY, value);
	}

	public EnumMarriageState getMarriageState()
	{
		return EnumMarriageState.byId(dataManager.get(MARRIAGE_STATE));
	}

	/*package-private*/ void setSpouseUUID(UUID uuid)
	{
		dataManager.set(SPOUSE_UUID, Optional.of(uuid));
	}

	/*package-private*/ void setSpouseName(String value)
	{
		dataManager.set(SPOUSE_NAME, value);
	}

	/*package-private*/ void setSpouseGender(EnumGender gender)
	{
		dataManager.set(SPOUSE_GENDER, gender.getId());
	}

	/*package-private*/ void setParentName(boolean mother, String value)
	{
		DataParameter field = mother ? MOTHER_NAME : FATHER_NAME;
		dataManager.set(field, value);
	}

	/*package-private*/ void setParentUUID(boolean mother, UUID uuid)
	{
		DataParameter field = mother ? MOTHER_UUID: FATHER_UUID;
		dataManager.set(field, Optional.of(uuid));
	}

	/*package-private*/ void setParentGender(boolean mother, EnumGender gender)
	{
		DataParameter field = mother ? MOTHER_GENDER : FATHER_GENDER;
		dataManager.set(field, gender.getId());
	}

	/*package-private*/ void setMarriageState(EnumMarriageState state)
	{
		dataManager.set(MARRIAGE_STATE, state.getId());
	}


	public int getHeldItemSlot()
	{
		return dataManager.get(HELD_ITEM_SLOT);
	}

	public void setHeldItemSlot(int value)
	{
		dataManager.set(HELD_ITEM_SLOT, value);
	}

	public boolean getIsInfected()
	{
		return dataManager.get(IS_INFECTED);
	}

	public void setIsInfected(boolean value)
	{
		dataManager.set(IS_INFECTED, value);
	}


	public double getBaseAttackDamage() 
	{
		switch (getPersonality())
		{
		case STRONG: return 2.0D;
		case CONFIDENT: return 1.0D;
		default: 
			if (getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard)
			{
				return 5.0D;
			}

			else
			{
				return 0.5D;
			}
		}
	}

	public void assignRandomName()
	{
		if (getGender() == EnumGender.MALE)
		{
			setName(MCA.getLocalizer().getString("name.male"));
		}

		else
		{
			setName(MCA.getLocalizer().getString("name.female"));
		}
	}

	public void assignRandomGender()
	{
		setGender(villager.world.rand.nextBoolean() ? EnumGender.MALE : EnumGender.FEMALE);
	}

	public void assignRandomProfession()
	{
		setProfession(EnumProfession.getAtRandom());
	}

	public void assignRandomPersonality() 
	{
		setPersonality(EnumPersonality.getAtRandom());
	}

	public boolean isMarriedToAPlayer()
	{
		return getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER;
	}

	public boolean isMarriedToAVillager()
	{
		return getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER;
	}

	public boolean getIsEngaged()
	{
		return getMarriageState() == EnumMarriageState.ENGAGED;
	}



	public boolean isPlayerAParent(EntityPlayer player)
	{
		final NBTPlayerData data = MCA.getPlayerData(player);

		if (data != null)
		{
			boolean result = getMotherUUID().equals(data.getUUID()) || getFatherUUID().equals(data.getUUID());
			return result;
		}

		else
		{
			return false;
		}
	}

	public boolean isPlayerAParent(UUID uuid)
	{
		final NBTPlayerData data = MCA.getPlayerData(villager.world, uuid);

		if (data != null)
		{
			return getMotherUUID() == data.getUUID() || getFatherUUID() == data.getUUID();
		}

		else
		{
			return false;
		}
	}

	public float getSpeed()
	{
		return getPersonality() == EnumPersonality.ATHLETIC ? Constants.SPEED_RUN : Constants.SPEED_WALK;
	}

	public boolean allowsHiring(EntityPlayer player) 
	{
		return getPlayerSpouseInstance() != player && (getProfessionSkinGroup() == EnumProfessionSkinGroup.Farmer || 
				getProfessionSkinGroup() == EnumProfessionSkinGroup.Miner || 
				getProfessionSkinGroup() == EnumProfessionSkinGroup.Warrior);
	}

	public boolean allowsWorkInteractions(EntityPlayer player)
	{
		final NBTPlayerData data = MCA.getPlayerData(player);
		final PlayerMemory memory = getPlayerMemory(player);

		if (data.getIsSuperUser())
		{
			return true;
		}

		else if (getIsInfected()) //Infected villagers can't use an inventory or do chores.
		{
			return false;
		}

		else if (memory.getIsHiredBy())
		{
			return true;
		}

		else if (isPlayerAParent(player))
		{
			return true;
		}

		return false;
	}

	public boolean allowsControllingInteractions(EntityPlayer player)
	{
		final NBTPlayerData data = MCA.getPlayerData(player);

		if (data.getIsSuperUser())
		{
			return true;
		}

		//Married to a player, and this player is not their spouse.
		else if (isMarriedToAPlayer() && !getSpouseUUID().equals(data.getUUID()))
		{
			return false;
		}

		else if (getIsChild())
		{
			if (isPlayerAParent(player))
			{
				return true;
			}

			else if (isChildOfAVillager())
			{
				return true;
			}

			else
			{
				return false;
			}
		}

		return true;
	}

	public boolean allowsIntimateInteractions(EntityPlayer player)
	{
		return !getIsChild() && !isPlayerAParent(player);
	}

	public int getTicksAlive()
	{
		return ticksAlive;
	}

	public void setTicksAlive(int value)
	{
		this.ticksAlive = value;
	}

	public int getLowHeartWarnings()
	{
		return timesWarnedForLowHearts;
	}

	public void incrementLowHeartWarnings()
	{
		timesWarnedForLowHearts++;
	}

	public void resetLowHeartWarnings()
	{
		timesWarnedForLowHearts = 0;
	}

	public void setPlayerMemory(EntityPlayer player, PlayerMemory memory)
	{
		playerMemories.put(player.getPersistentID(), memory);
	}

	public PlayerMemory getPlayerMemory(EntityPlayer player)
	{
		UUID playerUUID = player.getPersistentID();
		PlayerMemory returnMemory = playerMemories.get(playerUUID);

		if (returnMemory == null)
		{
			returnMemory = new PlayerMemory(villager, player);
			playerMemories.put(playerUUID, returnMemory);
		}

		return returnMemory;
	}

	public PlayerMemory getPlayerMemoryWithoutCreating(EntityPlayer player) 
	{
		return getPlayerMemoryWithoutCreating(player.getUniqueID());
	}

	public PlayerMemory getPlayerMemoryWithoutCreating(UUID playerUUID)
	{
		return playerMemories.get(playerUUID);
	}
	
	public Map<UUID, PlayerMemory> getPlayerMemories()
	{
		return playerMemories;
	}
	
	public boolean hasMemoryOfPlayer(EntityPlayer player)
	{
		return playerMemories.containsKey(player.getName());
	}

	public String getTitle(EntityPlayer player)
	{
		PlayerMemory memory = getPlayerMemory(player);

		if (memory.isRelatedToPlayer())
		{
			return MCA.getLocalizer().getString(getGender() == EnumGender.MALE ? "title.relative.male" : "title.relative.female", villager, player);
		}

		else
		{
			return MCA.getLocalizer().getString(getGender() == EnumGender.MALE ? "title.nonrelative.male" : "title.nonrelative.female", villager, player);
		}
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		//Auto save data manager values to NBT by reflection
		for (Field f : this.getClass().getDeclaredFields())
		{
			try
			{
				if (f.getType() == DataParameter.class)
				{
					Type genericType = f.getGenericType();
					String typeName = genericType.getTypeName();
					DataParameter param = (DataParameter) f.get(this);
					String paramName = f.getName();

					if (typeName.contains("Boolean"))
					{
						DataParameter<Boolean> bParam = (DataParameter<Boolean>)param;
						nbt.setBoolean(paramName, dataManager.get(bParam).booleanValue());
					}

					else if (typeName.contains("Integer"))
					{
						DataParameter<Integer> iParam = (DataParameter<Integer>)param;
						nbt.setInteger(paramName, dataManager.get(iParam).intValue());
					}

					else if (typeName.contains("String"))
					{
						DataParameter<String> sParam = (DataParameter<String>)param;
						nbt.setString(paramName, dataManager.get(sParam));
					}

					else if (typeName.contains("Float"))
					{
						DataParameter<Float> fParam = (DataParameter<Float>)param;
						nbt.setFloat(paramName, dataManager.get(fParam).floatValue());
					}

					else if (typeName.contains("Optional<java.util.UUID>"))
					{
						DataParameter<Optional<UUID>> uuParam = (DataParameter<Optional<UUID>>)param;
						nbt.setUniqueId(paramName, dataManager.get(uuParam).get());
					}

					else
					{
						throw new RuntimeException("Field type not handled while saving to NBT: " + f.getName());
					}
				}
			}

			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		nbt.setInteger("ticksAlive", ticksAlive);
		nbt.setInteger("timesWarnedForLowHearts", timesWarnedForLowHearts);
		nbt.setTag("inventory", inventory.writeInventoryToNBT());
		
		int counter = 0;
		for (Map.Entry<UUID, PlayerMemory> pair : playerMemories.entrySet())
		{
			nbt.setUniqueId("playerMemoryKey" + counter, pair.getKey());
			pair.getValue().writePlayerMemoryToNBT(nbt);
			counter++;
		}
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		//Auto read data manager values
		for (Field f : this.getClass().getDeclaredFields())
		{
			try
			{
				if (f.getType() == DataParameter.class)
				{
					Type genericType = f.getGenericType();
					String typeName = genericType.getTypeName();
					DataParameter param = (DataParameter) f.get(this);
					String paramName = f.getName();

					if (typeName.contains("Boolean"))
					{
						DataParameter<Boolean> bParam = (DataParameter<Boolean>)param;
						dataManager.set(bParam, nbt.getBoolean(paramName));
					}

					else if (typeName.contains("Integer"))
					{
						DataParameter<Integer> iParam = (DataParameter<Integer>)param;
						dataManager.set(iParam, nbt.getInteger(paramName));
					}

					else if (typeName.contains("String"))
					{
						DataParameter<String> sParam = (DataParameter<String>)param;
						dataManager.set(sParam, nbt.getString(paramName));
					}

					else if (typeName.contains("Float"))
					{
						DataParameter<Float> fParam = (DataParameter<Float>)param;
						dataManager.set(fParam, nbt.getFloat(paramName));
					}

					else if (typeName.contains("Optional<java.util.UUID>"))
					{
						DataParameter<Optional<UUID>> uuParam = (DataParameter<Optional<UUID>>)param;
						dataManager.set(uuParam, Optional.of(nbt.getUniqueId(paramName)));
					}

					else
					{
						throw new RuntimeException("Field type not handled while saving to NBT: " + f.getName());
					}
				}
			}

			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		ticksAlive = nbt.getInteger("ticksAlive");
		timesWarnedForLowHearts = nbt.getInteger("timesWarnedForLowHearts");
		inventory.readInventoryFromNBT(nbt.getTagList("inventory", 10));
		
		int counter = 0;
		
		while (true)
		{
			final UUID playerUUID = nbt.getUniqueId("playerMemoryKey" + counter);

			if (playerUUID == null || playerUUID.equals(Constants.EMPTY_UUID))
			{
				break;
			}

			else
			{
				final PlayerMemory playerMemory = new PlayerMemory(villager, playerUUID);
				playerMemory.readPlayerMemoryFromNBT(nbt);
				playerMemories.put(playerUUID, playerMemory);
				counter++;
			}
		}
	}

	public VillagerInventory getInventory() 
	{
		return inventory;
	}

	public void incrementTicksAlive() 
	{
		ticksAlive++;
	}

	public void writeSpawnData(ByteBuf buffer) 
	{
		RadixNettyIO.writeObject(buffer, playerMemories);
	}

	public void readSpawnData(ByteBuf buffer) 
	{
		Map<UUID, PlayerMemory> recvMemories = (Map<UUID, PlayerMemory>) RadixNettyIO.readObject(buffer);
		playerMemories = recvMemories;
		setDoDisplay(true);
	}

	public void setSize(float width, float height) 
	{
		villager.setHitboxSize(width, height);
		
		if (!villager.world.isRemote)
		{
			MCA.getPacketHandler().sendPacketToAllPlayers(new PacketSetSize(villager, width, height));
		}
	}

	public UUID getVillagerUUID() 
	{
		return villager.getPersistentID();
	}
}
