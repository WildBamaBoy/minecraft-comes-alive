package mca.data;

import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import radixcore.data.AbstractPlayerData;
import radixcore.data.WatchedBoolean;
import radixcore.data.WatchedInt;
import radixcore.data.WatchedString;
import radixcore.util.RadixLogic;
import cpw.mods.fml.common.ModMetadata;

public class PlayerData extends AbstractPlayerData
{
	public static final long serialVersionUID = 1L;

	public WatchedInt permanentId;
	public WatchedInt spousePermanentId;
	public WatchedInt heirPermanentId;
	public WatchedInt genderPreference;
	public WatchedBoolean isMale;
	public WatchedBoolean shouldHaveBaby;
	public WatchedBoolean isEngaged;
	public WatchedBoolean isInLiteMode;
	public WatchedBoolean isMonarch;
	public WatchedBoolean hasChosenDestiny;
	public WatchedString mcaName;
	public WatchedString uuid;
	public WatchedBoolean isSuperUser;

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
		mcaName = new WatchedString("none", WatcherIDsPlayerData.MCA_NAME, dataWatcher);
		hasChosenDestiny = new WatchedBoolean(false, WatcherIDsPlayerData.HAS_CHOSEN_DESTINY, dataWatcher);
		uuid = new WatchedString("none", WatcherIDsPlayerData.UUID, dataWatcher);
		isSuperUser = new WatchedBoolean(false, WatcherIDsPlayerData.IS_SUPER_USER, dataWatcher);
	}
	
	@Override
	public void initializeNewData(EntityPlayer player) 
	{
		permanentId.setValue(RadixLogic.generatePermanentEntityId(player));
		uuid.setValue(player.getUniqueID().toString());
	}

	public void dumpToConsole()
	{
		MCA.getLog().info("--------PLAYER DATA DUMP--------");
		MCA.getLog().info("Permanent ID: " + permanentId.getInt());
		MCA.getLog().info("Spouse Permanent ID: " + spousePermanentId.getInt());
		MCA.getLog().info("Heir Permanent ID: " + heirPermanentId.getInt());
		MCA.getLog().info("Is Male: " + isMale.getBoolean());
		MCA.getLog().info("Should Have Baby: " + shouldHaveBaby.getBoolean());
		MCA.getLog().info("Is Engaged: " + isEngaged.getBoolean());
		MCA.getLog().info("Is In Lite Mode: " + isInLiteMode.getBoolean());
		MCA.getLog().info("Is Monarch: " + isMonarch.getBoolean());
		MCA.getLog().info("MCA Name: " + mcaName.getString());
		MCA.getLog().info("UUID: " + uuid.getString());
	}
}
