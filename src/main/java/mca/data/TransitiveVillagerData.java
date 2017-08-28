package mca.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mca.core.Constants;
import mca.entity.VillagerAttributes;
import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMovementState;
import mca.enums.EnumPersonality;
import mca.enums.EnumProfession;
import net.minecraft.nbt.NBTTagCompound;

/*
 * Used to carry around villager attributes without using the data manager, ex. in memorial objects.
 */
public class TransitiveVillagerData implements Serializable
{
	private final UUID uuid;
	private final String name;
	private final String headTexture;
	private final String clothesTexture;
	private final Integer profession;
	private final Integer personality;
	private final Integer gender;
	private final String spouseName;
	private final UUID spouseUUID;
	private final Integer spouseGender;
	private final String motherName;
	private final UUID motherUUID;
	private final Integer motherGender;
	private final String fatherName;
	private final UUID fatherUUID;
	private final Integer fatherGender;
	private final Integer babyState;
	private final Integer movementState;
	private final Boolean isChild;
	private final Integer age;
	private final Float scaleHeight;
	private final Float scaleWidth;
	private final Boolean doDisplay;
	private final Boolean isSwinging;
	private final Integer heldItemSlot;
	private final Boolean isInfected;
	private final Boolean doOpenInventory;
	private final Integer marriageState;
	private final Map<UUID, PlayerMemory> playerMemories;
	
	public TransitiveVillagerData(VillagerAttributes attributes)
	{
		this.uuid = attributes.getVillagerUUID();
		this.name = attributes.getName();
		this.headTexture = attributes.getHeadTexture();
		this.clothesTexture = attributes.getClothesTexture();
		this.profession = attributes.getProfessionEnum().getId();
		this.personality = attributes.getPersonality().getId();
		this.gender = attributes.getGender().getId();
		this.spouseUUID = attributes.getSpouseUUID();
		this.spouseGender = attributes.getSpouseGender().getId();
		this.spouseName = attributes.getSpouseName();
		this.motherName = attributes.getMotherName();
		this.motherUUID = attributes.getMotherUUID();
		this.motherGender = attributes.getMotherGender().getId();
		this.fatherName = attributes.getFatherName();
		this.fatherUUID = attributes.getFatherUUID();
		this.fatherGender = attributes.getFatherGender().getId();
		this.babyState = attributes.getBabyState().getId();
		this.movementState = attributes.getMovementState().getId();
		this.isChild = attributes.getIsChild();
		this.age = attributes.getAge();
		this.scaleHeight = attributes.getScaleHeight();
		this.scaleWidth = attributes.getScaleWidth();
		this.doDisplay = attributes.getDoDisplay();
		this.isSwinging = attributes.getIsSwinging();
		this.heldItemSlot = attributes.getHeldItemSlot();
		this.isInfected = attributes.getIsInfected();
		this.doOpenInventory = attributes.getDoOpenInventory();
		this.marriageState = attributes.getMarriageState().getId();
		this.playerMemories = attributes.getPlayerMemories();
	}

	public TransitiveVillagerData(NBTTagCompound nbt)
	{
		this.uuid = nbt.getUniqueId("uuid");
		this.name = nbt.getString("name");
		this.headTexture = nbt.getString("headTexture");
		this.clothesTexture = nbt.getString("clothesTexture");
		this.profession = nbt.getInteger("profession");
		this.personality = nbt.getInteger("personality");
		this.gender = nbt.getInteger("gender");
		this.spouseUUID = nbt.getUniqueId("spouseUUID");
		this.spouseGender = nbt.getInteger("spouseGender");
		this.spouseName = nbt.getString("spouseName");
		this.motherUUID = nbt.getUniqueId("motherUUID");
		this.motherGender = nbt.getInteger("motherGender");
		this.motherName = nbt.getString("motherName");
		this.fatherUUID = nbt.getUniqueId("fatherUUID");
		this.fatherGender = nbt.getInteger("fatherGender");
		this.fatherName = nbt.getString("fatherName");
		this.babyState = nbt.getInteger("babyState");
		this.movementState = nbt.getInteger("movementState");
		this.isChild = nbt.getBoolean("isChild");
		this.age = nbt.getInteger("age");
		this.scaleHeight = nbt.getFloat("scaleHeight");
		this.scaleWidth = nbt.getFloat("scaleWidth");
		this.doDisplay = nbt.getBoolean("doDisplay");
		this.isSwinging = nbt.getBoolean("isSwinging");
		this.heldItemSlot = nbt.getInteger("heldItemSlot");
		this.isInfected = nbt.getBoolean("isInfected");
		this.doOpenInventory = nbt.getBoolean("doOpenInventory");
		this.marriageState = nbt.getInteger("marriageState");
		this.playerMemories = new HashMap<UUID, PlayerMemory>();
		
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
				final PlayerMemory playerMemory = new PlayerMemory(null, playerUUID);
				playerMemory.readPlayerMemoryFromNBT(nbt);
				playerMemories.put(playerUUID, playerMemory);
				counter++;
			}
		}
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
	
	public String getName() 
	{
		return name;
	}

	public String getHeadTexture() 
	{
		return headTexture;
	}

	public String getClothesTexture() 
	{
		return clothesTexture;
	}

	public EnumProfession getProfession() 
	{
		return EnumProfession.getProfessionById(profession);
	}

	public EnumPersonality getPersonality() 
	{
		return EnumPersonality.getById(personality);
	}

	public EnumGender getGender() 
	{
		return EnumGender.byId(gender);
	}

	public String getSpouseName()
	{
		return spouseName;
	}
	
	public UUID getSpouseUUID() 
	{
		return spouseUUID;
	}

	public EnumGender getSpouseGender() 
	{
		return EnumGender.byId(spouseGender);
	}

	public String getMotherName() 
	{
		return motherName;
	}

	public UUID getMotherUUID() 
	{
		return motherUUID;
	}

	public EnumGender getMotherGender() 
	{
		return EnumGender.byId(motherGender);
	}

	public String getFatherName() 
	{
		return fatherName;
	}

	public UUID getFatherUUID() 
	{
		return fatherUUID;
	}

	public EnumGender getFatherGender() 
	{
		return EnumGender.byId(fatherGender);
	}

	public EnumBabyState getBabyState() 
	{
		return EnumBabyState.fromId(babyState);
	}

	public EnumMovementState getMovementState() 
	{
		return EnumMovementState.fromId(movementState);
	}

	public Boolean getIsChild() 
	{
		return isChild;
	}

	public Integer getAge() 
	{
		return age;
	}

	public Float getScaleHeight() 
	{
		return scaleHeight;
	}

	public Float getScaleWidth() 
	{
		return scaleWidth;
	}

	public Boolean getDoDisplay() 
	{
		return doDisplay;
	}

	public Boolean getIsSwinging() 
	{
		return isSwinging;
	}

	public Integer getHeldItemSlot() 
	{
		return heldItemSlot;
	}

	public Boolean getIsInfected() 
	{
		return isInfected;
	}

	public Boolean getDoOpenInventory() 
	{
		return doOpenInventory;
	}

	public EnumMarriageState getMarriageState() 
	{
		return EnumMarriageState.byId(marriageState);
	}
	
	public Map<UUID, PlayerMemory> getPlayerMemories()
	{
		return playerMemories;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setUniqueId("uuid", uuid);
		nbt.setString("name", name);
		nbt.setString("headTexture", headTexture);
		nbt.setString("clothesTexture", clothesTexture);
		nbt.setInteger("profession", profession);
		nbt.setInteger("personality", personality);
		nbt.setInteger("gender", gender);
		nbt.setUniqueId("spouseUUID", spouseUUID);
		nbt.setInteger("spouseGender", spouseGender);
		nbt.setString("spouseName", spouseName);
		nbt.setUniqueId("motherUUID", motherUUID);
		nbt.setInteger("motherGender", motherGender);
		nbt.setString("motherName", motherName);
		nbt.setUniqueId("fatherUUID", fatherUUID);
		nbt.setInteger("fatherGender", fatherGender);
		nbt.setString("fatherName", fatherName);
		nbt.setInteger("babyState", babyState);
		nbt.setInteger("movementState", movementState);
		nbt.setBoolean("isChild", isChild);
		nbt.setInteger("age", age);
		nbt.setFloat("scaleHeight", scaleHeight);
		nbt.setFloat("scaleWidth", scaleWidth);
		nbt.setBoolean("doDisplay", doDisplay);
		nbt.setBoolean("isSwinging", isSwinging);
		nbt.setInteger("heldItemSlot", heldItemSlot);
		nbt.setBoolean("isInfected", isInfected);
		nbt.setBoolean("doOpenInventory", doOpenInventory);
		nbt.setInteger("marriageState", marriageState);
		
		int counter = 0;
		for (Map.Entry<UUID, PlayerMemory> pair : playerMemories.entrySet())
		{
			nbt.setUniqueId("playerMemoryKey" + counter, pair.getKey());
			pair.getValue().writePlayerMemoryToNBT(nbt);
			counter++;
		}
	}
}
