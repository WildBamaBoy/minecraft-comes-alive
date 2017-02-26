package mca.data;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Nullable;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.packets.PacketPlayerDataC;
import mca.packets.PacketPlayerDataS;
import mca.util.Either;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class NBTPlayerData implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum FieldID
	{
		UUID(1),
		MCA_NAME(2),
		GENDER(3),
		GENDER_PREFERENCE(4),
		SPOUSE_NAME(5),
		SPOUSE_UUID(6),
		SPOUSE_GENDER(7),
		MARRIAGE_STATE(8),
		OWNS_BABY(9),
		IS_NOBILITY(10),
		HAS_CHOSEN_DESTINY(11),
		IS_SUPER_USER(12),
		HAPPINESS_THRESHOLD_MET(13);
		
		private int id;
		
		FieldID(int id)
		{
			this.id = id;
		}
		
		public int getId()
		{
			return id;
		}
		
		public static FieldID fromId(int id)
		{
			for (FieldID value : values())
			{
				if (value.id == id)
				{
					return value;
				}
			}
			
			return null;
		}
	}
	
	public enum TypeID
	{
		STRING(1),
		BOOLEAN(2),
		INT(3),
		UUID(4);
		
		private int id;
		
		TypeID(int id)
		{
			this.id = id;
		}
		
		public int getId()
		{
			return id;
		}
		
		public static TypeID fromId(int id)
		{
			for (TypeID value : values())
			{
				if (value.id == id)
				{
					return value;
				}
			}
			
			return null;
		}
	}
	
	public static class FieldUpdateObj
	{
		private static FieldUpdateObj instance;
		public TypeID typeId;
		public FieldID fieldId;
		public Object value;
		
		private FieldUpdateObj()
		{
		}
		
		public static FieldUpdateObj get(FieldID fieldId, TypeID typeId, Object value)
		{
			if (instance == null)
			{
				instance = new FieldUpdateObj();
			}
			
			instance.fieldId = fieldId;
			instance.typeId = typeId;
			instance.value = value;
			
			return instance;
		}
	}
	
	private UUID uuid;
	private String mcaName;
	private int gender;
	private int genderPreference;
	private String spouseName;
	private UUID spouseUUID;
	private int spouseGender;
	private int marriageState;
	private boolean ownsBaby;
	private boolean isNobility;
	private boolean hasChosenDestiny;
	private boolean isSuperUser;
	private boolean happinessThresholdMet;
	
	public boolean ignoreBroadcast;
	
	public NBTPlayerData()
	{
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setUniqueId("uuid", uuid);
		nbt.setString("mcaName", (mcaName == null || mcaName.isEmpty()) ? "none" : mcaName);
		nbt.setInteger("gender", gender);
		nbt.setInteger("genderPreference", genderPreference);
		nbt.setString("spouseName", (spouseName == null || spouseName.isEmpty()) ? "none" : spouseName);
		nbt.setUniqueId("spouseUUID", spouseUUID == null ? Constants.EMPTY_UUID : spouseUUID);
		nbt.setInteger("marriageState", marriageState);
		nbt.setBoolean("ownsBaby", ownsBaby);
		nbt.setBoolean("isNobility", isNobility);
		nbt.setBoolean("hasChosenDestiny", hasChosenDestiny);
		nbt.setBoolean("isSuperUser", isSuperUser);
		nbt.setBoolean("happinessThresholdMet", happinessThresholdMet);
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		uuid = nbt.getUniqueId("uuid");
		mcaName = nbt.getString("mcaName");
		gender = nbt.getInteger("gender");
		genderPreference = nbt.getInteger("genderPreference");
		spouseName = nbt.getString("spouseName");
		spouseUUID = nbt.getUniqueId("spouseUUID");
		marriageState = nbt.getInteger("marriageState");
		ownsBaby = nbt.getBoolean("ownsBaby");
		isNobility = nbt.getBoolean("isNobility");
		hasChosenDestiny = nbt.getBoolean("hasChosenDestiny");
		isSuperUser = nbt.getBoolean("isSuperUser");
		happinessThresholdMet = nbt.getBoolean("happinessThresholdMet");
	}
	
	public UUID getUUID() 
	{
		return uuid;
	}

	public void setUUID(UUID value)
	{
		this.uuid = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.UUID, TypeID.UUID, value));
	}
	
	public UUID getSpouseUUID() 
	{
		return spouseUUID;
	}

	public EnumGender getGenderPreference() 
	{
		return EnumGender.byId(genderPreference);
	}

	public void setGenderPreference(EnumGender value) 
	{
		this.genderPreference = value.getId();
		broadcastValueChange(FieldUpdateObj.get(FieldID.GENDER_PREFERENCE, TypeID.INT, value.getId()));
	}

	public EnumGender getGender() 
	{
		return EnumGender.byId(gender);
	}

	public EnumGender getSpouseGender()
	{
		return EnumGender.byId(spouseGender);
	}
	
	public void setGender(EnumGender value)
	{
		this.gender = value.getId();
		broadcastValueChange(FieldUpdateObj.get(FieldID.GENDER, TypeID.INT, value.getId()));
	}

	public void setSpouseGender(EnumGender value)
	{
		this.spouseGender = value.getId();
		broadcastValueChange(FieldUpdateObj.get(FieldID.SPOUSE_GENDER, TypeID.INT, value.getId()));
	}
	
	public boolean getOwnsBaby() 
	{
		return ownsBaby;
	}

	public void setOwnsBaby(boolean value) 
	{
		this.ownsBaby = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.OWNS_BABY, TypeID.BOOLEAN, value));
	}

	public void setSpouseUUID(UUID value)
	{
		this.spouseUUID = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.SPOUSE_UUID, TypeID.UUID, value));
	}
	
	public EnumMarriageState getMarriageState()
	{
		return EnumMarriageState.byId(marriageState);
	}
	
	public void setMarriageState(EnumMarriageState value)
	{
		this.marriageState = value.getId();
		broadcastValueChange(FieldUpdateObj.get(FieldID.MARRIAGE_STATE, TypeID.INT, value.getId()));
	}
	
	/**
	 * Sets the given entity to be the spouse of the current player. This is symmetric against the provided entity.
	 * If null is provided, this player's spouse information will be reset. This is **NOT** symmetric.
	 * 
	 * @param 	either	Either object containing an MCA villager or a player. May be null.
	 */
	public void setSpouse(@Nullable Either<EntityVillagerMCA, EntityPlayer> either)
	{
		if (either == null)
		{
			//Reset spouse information back to default
			setSpouseName("");
			setSpouseUUID(Constants.EMPTY_UUID);
			setSpouseGender(EnumGender.UNASSIGNED);
			setMarriageState(EnumMarriageState.NOT_MARRIED);
		}
		
		else if (either.getLeft() != null) //Marry to a villager, symmetric
		{
			EntityVillagerMCA spouse = either.getLeft();
			spouse.startMarriage(either); //Call to method already in villager
		}
		
		else if (either.getRight() != null) //Marry to another player
		{
			EntityPlayer targetPlayer = either.getRight();
			EntityPlayer thisPlayer = targetPlayer.world.getPlayerEntityByUUID(this.getUUID());
			NBTPlayerData targetPlayerData = MCA.getPlayerData(targetPlayer);
			
			this.setSpouseName(targetPlayer.getName());
			this.setSpouseUUID(targetPlayerData.getUUID());
			this.setSpouseGender(targetPlayerData.getGender());
			setMarriageState(EnumMarriageState.MARRIED_TO_PLAYER);
			
			targetPlayerData.setSpouseName(thisPlayer.getName());
			targetPlayerData.setSpouseUUID(this.getUUID());
			targetPlayerData.setSpouseGender(this.getGender());
			targetPlayerData.setMarriageState(EnumMarriageState.MARRIED_TO_PLAYER);
		}
	}
	
	public boolean getHasChosenDestiny() 
	{
		return hasChosenDestiny;
	}

	public void setHasChosenDestiny(boolean value) 
	{
		this.hasChosenDestiny = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.HAS_CHOSEN_DESTINY, TypeID.BOOLEAN, value));
	}

	public String getMcaName() 
	{
		return mcaName;
	}

	public void setMcaName(String value) 
	{
		this.mcaName = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.MCA_NAME, TypeID.STRING, value));
	}

	public String getSpouseName() 
	{
		return spouseName;
	}

	public void setSpouseName(String value)
	{
		this.spouseName = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.SPOUSE_NAME, TypeID.STRING, value));
	}

	public boolean getIsSuperUser() 
	{
		return isSuperUser;
	}

	public void setIsSuperUser(boolean value) 
	{
		this.isSuperUser = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.IS_SUPER_USER, TypeID.BOOLEAN, value));
	}
	
	public boolean getIsNobility()
	{
		return isNobility;
	}
	
	public void setIsNobility(boolean value)
	{
		this.isNobility = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.IS_NOBILITY, TypeID.BOOLEAN, value));
	}
	
	public boolean getHappinessThresholdMet()
	{
		return this.happinessThresholdMet;
	}
	
	public void setHappinessThresholdMet(boolean value)
	{
		this.happinessThresholdMet = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.HAPPINESS_THRESHOLD_MET, TypeID.BOOLEAN, value));
	}
	
	private void broadcastValueChange(FieldUpdateObj fieldUpdate)
	{		
		if (ignoreBroadcast)
		{
			//Switch off to prevent any kind of permanent setting.
			ignoreBroadcast = false;
			return;
		}
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketPlayerDataS(fieldUpdate));
		}
		
		else //Server
		{
			//Always trigger a save on value change.
			PlayerDataCollection.get().markDirty();
			
			//Lookup by reference to find UUID.
			UUID ownerUUID = PlayerDataCollection.get().getUUIDByReference(this);
			
			if (ownerUUID != null) //Make sure it was found.
			{
				//Find the player reference in the world.
				EntityPlayer player = null;
				
				for (WorldServer server : FMLCommonHandler.instance().getMinecraftServerInstance().worlds)
				{
					EntityPlayer foundPlayer = server.getPlayerEntityByUUID(ownerUUID); 
					
					if (foundPlayer != null)
					{
						player = foundPlayer;
						break;
					}
				}
				
				if (player != null)
				{
					MCA.getPacketHandler().sendPacketToPlayer(new PacketPlayerDataC(fieldUpdate), (EntityPlayerMP) player);
				}
				
				else
				{
					MCA.getLog().error("Error getting player instance by UUID");
				}
			}
			
			else
			{
				MCA.getLog().error("Error looking up player by UUID");
			}
		}
	}
	
	public void setByFieldUpdateObj(FieldUpdateObj obj)
	{
		ignoreBroadcast = true;
		
		switch (obj.fieldId)
		{
		case UUID: setUUID((UUID) obj.value); break;
		case SPOUSE_UUID: setSpouseUUID((UUID) obj.value); break;
		case GENDER: setGender(EnumGender.byId((Integer)obj.value)); break;
		case SPOUSE_GENDER: setSpouseGender(EnumGender.byId((Integer)obj.value)); break;
		case OWNS_BABY: setOwnsBaby((Boolean)obj.value); break;
		case MARRIAGE_STATE: setMarriageState(EnumMarriageState.byId((Integer)obj.value)); break;
		case IS_NOBILITY: setIsNobility((Boolean)obj.value); break;
		case MCA_NAME: setMcaName((String)obj.value); break;
		case HAS_CHOSEN_DESTINY: setHasChosenDestiny((Boolean)obj.value); break;
		case GENDER_PREFERENCE: setGenderPreference(EnumGender.byId((Integer)obj.value)); break;
		case IS_SUPER_USER: setIsSuperUser((Boolean)obj.value); break;
		case SPOUSE_NAME: setSpouseName((String)obj.value); break;
		case HAPPINESS_THRESHOLD_MET: setHappinessThresholdMet((Boolean)obj.value); break;
		}
		
		ignoreBroadcast = false;
	}
}
