package mca.data;

import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.data.AbstractPlayerData;
import radixcore.data.WatchedBoolean;
import radixcore.data.WatchedInt;
import radixcore.data.WatchedString;
import radixcore.helpers.LogicHelper;
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
	}
	
	@Override
	public void initializeNewData(EntityPlayer player) 
	{
		permanentId.setValue(LogicHelper.generatePermanentEntityId(player));
		uuid.setValue(player.getUniqueID().toString());
	}

	public void dumpToConsole()
	{
		System.out.println(permanentId.getInt());
		System.out.println(spousePermanentId.getInt());
		System.out.println(heirPermanentId.getInt());
		System.out.println(isMale.getBoolean());
		System.out.println(shouldHaveBaby.getBoolean());
		System.out.println(isEngaged.getBoolean());
		System.out.println(isInLiteMode.getBoolean());
		System.out.println(isMonarch.getBoolean());
		System.out.println(mcaName.getString());
		System.out.println(uuid.getString());
	}
}
