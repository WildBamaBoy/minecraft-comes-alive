package mca.data;

import java.io.Serializable;
import java.util.UUID;

import cpw.mods.fml.common.FMLCommonHandler;
import mca.core.MCA;
import mca.packets.PacketPlayerDataC;
import mca.packets.PacketPlayerDataS;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public final class NBTPlayerData implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum FieldID
	{
		PERMANENT_ID(1),
		SPOUSE_PERMANENT_ID(2),
		HEIR_PERMANENT_ID(3),
		IS_MALE(4),
		SHOULD_HAVE_BABY(5),
		IS_ENGAGED(6),
		IS_IN_LITE_MODE(7),
		IS_NOBILITY(8),
		MCA_NAME(9),
		UNUSED_10(10),
		HAS_CHOSEN_DESTINY(11),
		GENDER_PREFERENCE(12),
		IS_SUPER_USER(13),
		SPOUSE_NAME(14),
		HAPPINESS_THRESHOLD_MET(15);
		
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
		INT(3);
		
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
	
	private int permanentId;
	private int spousePermanentId;
	private int genderPreference;
	private boolean isMale;
	private boolean shouldHaveBaby;
	private boolean isEngaged;
	private boolean hasChosenDestiny;
	private String mcaName;
	private String spouseName;
	private boolean isSuperUser;
	private boolean isNobility;
	private boolean happinessThresholdMet;
	
	public boolean ignoreBroadcast;
	
	public NBTPlayerData()
	{
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("permanentId", permanentId);
		nbt.setInteger("spousePermanentId", spousePermanentId);
		nbt.setInteger("genderPreference", genderPreference);
		nbt.setBoolean("isMale", isMale);
		nbt.setBoolean("shouldHaveBaby", shouldHaveBaby);
		nbt.setBoolean("isEngaged", isEngaged);
		nbt.setBoolean("hasChosenDestiny", hasChosenDestiny);
		nbt.setString("mcaName", (mcaName == null || mcaName.isEmpty()) ? "none" : mcaName);
		nbt.setString("spouseName", (spouseName == null || spouseName.isEmpty()) ? "none" : spouseName);
		nbt.setBoolean("isSuperUser", isSuperUser);
		nbt.setBoolean("isNobility", isNobility);
		nbt.setBoolean("happinessThresholdMet", happinessThresholdMet);
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		permanentId = nbt.getInteger("permanentId");
		spousePermanentId = nbt.getInteger("spousePermanentId");
		genderPreference = nbt.getInteger("genderPreference");
		isMale = nbt.getBoolean("isMale");
		shouldHaveBaby = nbt.getBoolean("shouldHaveBaby");
		isEngaged = nbt.getBoolean("isEngaged");
		hasChosenDestiny = nbt.getBoolean("hasChosenDestiny");
		mcaName = nbt.getString("mcaName");
		spouseName = nbt.getString("spouseName");
		isSuperUser = nbt.getBoolean("isSuperUser");
		isNobility = nbt.getBoolean("isNobility");
		happinessThresholdMet = nbt.getBoolean("happinessThresholdMet");
	}
	
	public int getPermanentId() 
	{
		return permanentId;
	}

	public void setPermanentId(int value)
	{
		this.permanentId = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.PERMANENT_ID, TypeID.INT, value));
	}
	
	public int getSpousePermanentId() 
	{
		return spousePermanentId;
	}

	public int getGenderPreference() 
	{
		return genderPreference;
	}

	public void setGenderPreference(int value) 
	{
		this.genderPreference = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.GENDER_PREFERENCE, TypeID.INT, value));
	}

	public boolean getIsMale() 
	{
		return isMale;
	}

	public void setIsMale(boolean value)
	{
		this.isMale = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.IS_MALE, TypeID.BOOLEAN, value));
	}

	public boolean getShouldHaveBaby() 
	{
		return shouldHaveBaby;
	}

	public void setShouldHaveBaby(boolean value) 
	{
		this.shouldHaveBaby = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.SHOULD_HAVE_BABY, TypeID.BOOLEAN, value));
	}

	public void setSpousePermanentId(int value)
	{
		this.spousePermanentId = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.SPOUSE_PERMANENT_ID, TypeID.INT, value));
	}
	
	public boolean getIsEngaged() 
	{
		return isEngaged;
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
	
	public boolean getIsMarried()
	{
		return spousePermanentId != 0 && (!getIsEngaged());
	}

	public void setIsEngaged(boolean value) 
	{
		this.isEngaged = value;
		broadcastValueChange(FieldUpdateObj.get(FieldID.IS_ENGAGED, TypeID.BOOLEAN, value));
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
				
				for (WorldServer server : MinecraftServer.getServer().worldServers)
				{
					EntityPlayer foundPlayer = server.func_152378_a(ownerUUID); 
					
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
		case PERMANENT_ID: setPermanentId((Integer) obj.value); break;
		case SPOUSE_PERMANENT_ID: setSpousePermanentId((Integer) obj.value); break;
		case IS_MALE: setIsMale((Boolean)obj.value); break;
		case SHOULD_HAVE_BABY: setShouldHaveBaby((Boolean)obj.value); break;
		case IS_ENGAGED: setIsEngaged((Boolean)obj.value); break;
		case IS_NOBILITY: setIsNobility((Boolean)obj.value); break;
		case MCA_NAME: setMcaName((String)obj.value); break;
		case HAS_CHOSEN_DESTINY: setHasChosenDestiny((Boolean)obj.value); break;
		case GENDER_PREFERENCE: setGenderPreference((Integer)obj.value); break;
		case IS_SUPER_USER: setIsSuperUser((Boolean)obj.value); break;
		case SPOUSE_NAME: setSpouseName((String)obj.value); break;
		case HAPPINESS_THRESHOLD_MET: setHappinessThresholdMet((Boolean)obj.value); break;
		}
		
		ignoreBroadcast = false;
	}
	
	public void dumpToConsole()
	{
		MCA.getLog().info("--------PLAYER DATA DUMP--------");
		MCA.getLog().info("Permanent ID: " + getPermanentId());
		MCA.getLog().info("Spouse Permanent ID: " + getSpousePermanentId());
		MCA.getLog().info("Is Male: " + getIsMale());
		MCA.getLog().info("Should Have Baby: " + getShouldHaveBaby());
		MCA.getLog().info("Is Engaged: " + getIsEngaged());
		MCA.getLog().info("Is Nobility: " + getIsNobility());
		MCA.getLog().info("MCA Name: " + getMcaName());
		MCA.getLog().info("Has Chosen Destiny: " + getHasChosenDestiny());
	}
}
