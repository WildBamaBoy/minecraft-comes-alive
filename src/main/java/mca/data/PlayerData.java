package mca.data;

import cpw.mods.fml.common.ModMetadata;
import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import radixcore.data.AbstractPlayerData;
import radixcore.data.WatchedBoolean;
import radixcore.data.WatchedInt;
import radixcore.data.WatchedString;
import radixcore.util.RadixLogic;

public class PlayerData extends AbstractPlayerData
{
	public static final long serialVersionUID = 1L;

	protected WatchedInt permanentId;
	protected WatchedInt spousePermanentId;
	protected WatchedInt heirPermanentId;
	protected WatchedInt genderPreference;
	protected WatchedBoolean isMale;
	protected WatchedBoolean shouldHaveBaby;
	protected WatchedBoolean isEngaged;
	protected WatchedBoolean isInLiteMode;
	protected WatchedBoolean isMonarch;
	protected WatchedBoolean hasChosenDestiny;
	protected WatchedString mcaName;
	protected WatchedString spouseName;
	protected WatchedBoolean isSuperUser;

	public PlayerData(String playerUUID, World world)
	{
		super(playerUUID, MCA.ID, world);
	}

	public PlayerData(EntityPlayer player)
	{
		super(player, MCA.ID);
	}

	@Override
	public ModMetadata getModMetadata() 
	{
		return MCA.getMetadata();
	}

	@Override
	public void instantiateData()
	{
		permanentId = new WatchedInt(0, WatcherIDsPlayerData.PERMANENT_ID, dataWatcher);
		spousePermanentId = new WatchedInt(0, WatcherIDsPlayerData.SPOUSE_PERMANENT_ID, dataWatcher);
		heirPermanentId = new WatchedInt(0, WatcherIDsPlayerData.HEIR_PERMANENT_ID, dataWatcher);
		genderPreference = new WatchedInt(1, WatcherIDsPlayerData.GENDER_PREFERENCE, dataWatcher);
		isMale = new WatchedBoolean(true, WatcherIDsPlayerData.IS_MALE, dataWatcher);
		shouldHaveBaby = new WatchedBoolean(false, WatcherIDsPlayerData.SHOULD_HAVE_BABY, dataWatcher);
		isEngaged = new WatchedBoolean(false, WatcherIDsPlayerData.IS_ENGAGED, dataWatcher);
		isInLiteMode = new WatchedBoolean(false, WatcherIDsPlayerData.IS_IN_LITE_MODE, dataWatcher);
		isMonarch = new WatchedBoolean(false, WatcherIDsPlayerData.IS_MONARCH, dataWatcher);
		mcaName = new WatchedString(owner != null ? owner.getCommandSenderName() : "none", WatcherIDsPlayerData.MCA_NAME, dataWatcher);
		hasChosenDestiny = new WatchedBoolean(false, WatcherIDsPlayerData.HAS_CHOSEN_DESTINY, dataWatcher);
		isSuperUser = new WatchedBoolean(false, WatcherIDsPlayerData.IS_SUPER_USER, dataWatcher);
		spouseName = new WatchedString("none", WatcherIDsPlayerData.SPOUSE_NAME, dataWatcher);
	}

	@Override
	public void initializeNewData(EntityPlayer player) 
	{
		permanentId.setValue(RadixLogic.generatePermanentEntityId(player));
	}

	public void dumpToConsole()
	{
		MCA.getLog().info("--------PLAYER DATA DUMP--------");
		MCA.getLog().info("Owner: " + owner);
		MCA.getLog().info("Owner's Identity: " + ownerIdentifier);
		MCA.getLog().info("Permanent ID: " + getPermanentId());
		MCA.getLog().info("Spouse Permanent ID: " + getSpousePermanentId());
		MCA.getLog().info("Heir Permanent ID: " + getHeirPermanentId());
		MCA.getLog().info("Is Male: " + isMale.getBoolean());
		MCA.getLog().info("Should Have Baby: " + getShouldHaveBaby());
		MCA.getLog().info("Is Engaged: " + getIsEngaged());
		MCA.getLog().info("Is In Lite Mode: " + isInLiteMode.getBoolean());
		MCA.getLog().info("Is Monarch: " + isMonarch.getBoolean());
		MCA.getLog().info("MCA Name: " + mcaName.getString());
	}
	
	public int getPermanentId() 
	{
		return permanentId.getInt();
	}

	public int getSpousePermanentId() 
	{
		return spousePermanentId.getInt();
	}

	public int getHeirPermanentId() 
	{
		return heirPermanentId.getInt();
	}

	public void setHeir(EntityHuman heirInstance) 
	{
		this.heirPermanentId.setValue(heirInstance.getPermanentId());
	}

	public int getGenderPreference() 
	{
		return genderPreference.getInt();
	}

	public void setGenderPreference(int value) 
	{
		this.genderPreference.setValue(value);
	}

	public boolean getIsMale() 
	{
		return isMale.getBoolean();
	}

	public void setIsMale(boolean value)
	{
		this.setIsMale(value);
	}

	public boolean getShouldHaveBaby() 
	{
		return shouldHaveBaby.getBoolean();
	}

	public void setShouldHaveBaby(boolean value) 
	{
		this.shouldHaveBaby.setValue(value);
	}

	public void setSpousePermanentId(int value)
	{
		this.spousePermanentId.setValue(value);
	}
	
	public boolean getIsEngaged() 
	{
		return isEngaged.getBoolean();
	}
	
	public boolean getIsInLiteMode() 
	{
		return isInLiteMode.getBoolean();
	}

	public void setIsInLiteMode(boolean value) 
	{
		this.isInLiteMode.setValue(value);
	}

	public boolean getIsMonarch() 
	{
		return isMonarch.getBoolean();
	}

	public void setIsMonarch(boolean value) 
	{
		this.isMonarch.setValue(value);
	}

	public boolean getHasChosenDestiny() 
	{
		return hasChosenDestiny.getBoolean();
	}

	public void setHasChosenDestiny(boolean value) 
	{
		this.hasChosenDestiny.setValue(value);
	}

	public String getMcaName() 
	{
		return mcaName.getString();
	}

	public void setMcaName(String value) 
	{
		this.mcaName.setValue(value);
	}

	public String getSpouseName() 
	{
		return spouseName.getString();
	}

	public void setSpouseName(String value)
	{
		this.spouseName.setValue(value);
	}

	public boolean getIsSuperUser() 
	{
		return isSuperUser.getBoolean();
	}

	public void setIsSuperUser(boolean value) 
	{
		this.isSuperUser.setValue(value);
	}
	
	public boolean getIsMarried()
	{
		return spousePermanentId.getInt() != 0 && (!getIsEngaged());
	}

	public void setIsEngaged(boolean value) 
	{
		this.isEngaged.setValue(value);
	}
}
