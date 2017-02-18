package mca.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.io.Files;

import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PlayerDataCollection extends WorldSavedData 
{	
	private static final String ID = "MinecraftComesAlive";

	/** Map of player UUIDs and the list of quests they've encountered. */
	private Map<UUID, NBTPlayerData> playerDataMap;

	public static PlayerDataCollection get() 
	{
		World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
		PlayerDataCollection data = (PlayerDataCollection) world.loadItemData(PlayerDataCollection.class, ID);

		if (data == null)
		{
			data = new PlayerDataCollection();
			world.setItemData(ID, data);
		}

		return data;
	}

	public PlayerDataCollection() 
	{
		this(ID);
	}

	public PlayerDataCollection(String id) 
	{
		super(id);
		playerDataMap = new HashMap<UUID, NBTPlayerData>();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		for (Map.Entry<UUID, NBTPlayerData> entry : playerDataMap.entrySet())
		{
			UUID uuid = entry.getKey();
			NBTPlayerData playerData = entry.getValue();
			NBTTagCompound playerDataTags = new NBTTagCompound();
			
			try 
			{
				playerData.writeToNBT(playerDataTags);
				nbt.setTag("PlayerData-" + uuid.toString(), playerDataTags);
			}

			catch (Exception e)
			{
				RadixExcept.logErrorCatch(e, "Error writing player data to NBT for UUID " + uuid.toString() + ". Progress may be corrupted or lost.");
				continue;
			}
		}
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		for (Object tagKey : nbt.getKeySet()) //Loop through all keys.
		{
			try
			{
				String tagName = (String)tagKey;
				UUID uuid = UUID.fromString(tagName.replace("PlayerData-", ""));
				NBTTagCompound playerDataTags = nbt.getCompoundTag(tagName);
				NBTPlayerData data = new NBTPlayerData();

				data.readFromNBT(playerDataTags);
				playerDataMap.put(uuid, data);
			}

			catch (Exception e)
			{
				RadixExcept.logErrorCatch(e, "Error reading player data from NBT tag: " + (String)tagKey + ". Progress may be corrupted or lost.");
				continue;
			}
		}

		markDirty();
	}

	public boolean migrateOldPlayerData(EntityPlayer player, PlayerData data)
	{
		return migrateOldPlayerData(player.worldObj, player.getUniqueID(), data);
	}

	public boolean migrateOldPlayerData(World world, UUID uuid, PlayerData data)
	{
		String backupFullPath = data.getDataFile().replace(MCA.getMetadata().modId.toLowerCase(), "mca-backup");
		File backupFolder = new File(world.getSaveHandler().getWorldDirectory().getAbsolutePath() + "/playerdata-mca-backup");
		File originalFullPathFile = new File(data.getDataFile());
		File backupFullPathFile = new File(backupFullPath);
		NBTPlayerData newData = new NBTPlayerData();

		logWarn("*** Detected old player data! UUID: " + uuid.toString() + " ***");
		logWarn("*** MCA will now migrate their data to a new internal format, doing the following: ***");
		logWarn("*** THESE ARE STEPS MCA WILL TAKE ON ITS OWN, NOT INSTRUCTIONS FOR SERVER ADMINISTRATORS ***");
		logWarn("*** - Copy old data to the new format in `[your world]/data/MinecraftComesAlive.dat` ***");
		logWarn("*** - Backup the old data file to `[your world]/playerdata-mca-backup` ***");
		logWarn("*** - Delete the old data file to prevent duplicate migration of bad data. ***");
		logWarn("*** You can turn this message off in MCA's configuration file, but no errors will be shown. ***");
		logWarn("*** You can force -everyone's- player data to be converted by using `/mca cpd`. ***");

		try
		{
			newData.setPermanentId(data.permanentId.getInt());
			newData.setSpousePermanentId(data.spousePermanentId.getInt());
			newData.setGenderPreference(data.genderPreference.getInt());
			newData.setIsMale(data.isMale.getBoolean());
			newData.setShouldHaveBaby(data.shouldHaveBaby.getBoolean());
			newData.setIsEngaged(data.isEngaged.getBoolean());
			newData.setHasChosenDestiny(data.hasChosenDestiny.getBoolean());
			newData.setMcaName(data.mcaName.getString());
			newData.setSpouseName(data.spouseName.getString());
			newData.setIsSuperUser(data.isSuperUser.getBoolean());
			
			//New data entries added in the update requiring migration.
			newData.setIsNobility(false);
			newData.setHappinessThresholdMet(false);
		}

		catch (Exception e)
		{
			logError("****************   FAILED TO MIGRATE PLAYER DATA   ***********************");
			logError("* This player's data may be completely corrupted, or partially migrated. *");
			logError("*                                                                        *");
			logError("* This can indicate irreversible damage to this player's MCA progress.   *");
			logError("* This message will continue to appear until their old data is removed.  *");
			logError("* Their progress in MCA from this point forward can't be saved while the *");
			logError("* old data file remains in the following location:                       *");
			logError("*    /playerdata-mca/" + uuid.toString() + ".dat" + "            *");
			logError("*                                                                        *");
			logError("*      ONLY remove this file when Minecraft is not running!              *");
			logError("**************************************************************************");
			logError("Stacktrace will follow below:");
			logError(ExceptionUtils.getStackTrace(e));
			logFatal("Please check recent logs for important information about this error.");
			return false;
		}

		logInfo("- Backing up old player data file to `playerdata-mca-backup`...");

		try
		{
			backupFolder.mkdirs();
			Files.copy(originalFullPathFile, backupFullPathFile);
		}

		catch (Exception e)
		{
			logError("****************   FAILED TO BACKUP PLAYER DATA   *******************");
			logError("* Migration will not continue for this player unless their old data *");
			logError("* can be backed up. They will not be able to progress in MCA.       *");
			logError("* Try manually creating the folder below:                           *");
			logError("*        /[your world name]/playerdata-mca-backup/                  *");
			logError("*                                                                   *");
			logError("*    Ensure that this folder's permissions allow creating files.    *");
			logError("*********************************************************************");
			logError("Stacktrace will follow below:");
			logError(ExceptionUtils.getStackTrace(e));
			logFatal("Please check recent logs for important information about this error.");

			return false;
		}

		logInfo("- Removing old player data from previous load location...");
		try
		{
			originalFullPathFile.delete();
		}

		catch (Exception e)
		{
			logError("****************   FAILED TO DELETE PLAYER DATA   ****************");
			logError("* The migration will try again when this player logs in again.   *");
			logError("* If their old MCA player data file isn't removed, they may lose *");
			logError("* progress in MCA. You must manually remove the file below:      *");
			logError("*    /playerdata-mca/" + uuid.toString() + ".dat" + "    *");
			logError("*                                                                *");
			logError("*      ONLY remove this file when Minecraft is not running!      *");
			logError("******************************************************************");
			logError("Stacktrace will follow below:");
			logError(ExceptionUtils.getStackTrace(e));
			logFatal("Please check recent logs for important information about this error.");

			return false;
		}

		playerDataMap.put(uuid, newData);
		logInfo("- Migration of this data file succeeded without issue.");
		markDirty();
		return true;
	}

	public NBTPlayerData getPlayerData(UUID uuid)
	{
		return playerDataMap.get(uuid);
	}

	public void putPlayerData(UUID uuid, NBTPlayerData data)
	{
		playerDataMap.put(uuid, data);
		markDirty();
	}

	public UUID getUUIDByReference(NBTPlayerData data)
	{
		for (Map.Entry<UUID, NBTPlayerData> entry : playerDataMap.entrySet())
		{
			if (entry.getValue() == data)
			{
				return entry.getKey();
			}
		}
		
		return null;
	}
	
	private void logWarn(String message)
	{
		if (MCA.getConfig().showPlayerDataMigrationErrors)
		{
			MCA.getLog().warn(message);
		}		
	}

	private void logError(String message)
	{
		if (MCA.getConfig().showPlayerDataMigrationErrors)
		{
			MCA.getLog().error(message);
		}		
	}

	private void logInfo(String message)
	{
		if (MCA.getConfig().showPlayerDataMigrationErrors)
		{
			MCA.getLog().info(message);
		}
	}

	private void logFatal(String message)
	{
		if (MCA.getConfig().showPlayerDataMigrationErrors)
		{
			MCA.getLog().fatal(message);
		}
	}
}
