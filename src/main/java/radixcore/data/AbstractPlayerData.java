package radixcore.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import radixcore.core.RadixCore;
import radixcore.util.RadixExcept;
import cpw.mods.fml.common.ModMetadata;

public abstract class AbstractPlayerData implements Serializable, IWatchable
{
	public static final long serialVersionUID = 1L;
	protected static final transient String playerDataPath = "%WorldDir%/playerdata-%ModID%/";

	protected transient EntityPlayer owner;
	protected String ownerIdentifier;
	protected DataWatcherEx dataWatcher;
	
	/**
	 * Class constructor. 
	 * <b>Do not</b> assign default data values here, only instantiate objects if necessary.</b> 
	 * <p>
	 * Use <code>initializeNewData()</code> to set default values.
	 */
	public AbstractPlayerData(EntityPlayer player, String modId)
	{
		owner = player;
		ownerIdentifier = player.getUniqueID().toString();
		dataWatcher = new DataWatcherEx(this, modId);
		
		instantiateData();
		
		final File dataFolder = new File(playerDataPath
				.replace("%WorldDir%", player.worldObj.getSaveHandler().getWorldDirectory().getAbsolutePath())
				.replace("%ModID%", getModMetadata().modId.toLowerCase()));
		
		dataFolder.mkdirs();
	}

	public AbstractPlayerData(String ownerIdentifier, String modId, World world)
	{
		this.ownerIdentifier = ownerIdentifier;
		dataWatcher = new DataWatcherEx(this, modId);
		
		instantiateData();
		
		final File dataFolder = new File(playerDataPath
				.replace("%WorldDir%", world.getSaveHandler().getWorldDirectory().getAbsolutePath())
				.replace("%ModID%", getModMetadata().modId.toLowerCase()));
	}
	
	public abstract void instantiateData();
	
	public abstract void initializeNewData(EntityPlayer player);

	public abstract ModMetadata getModMetadata();

	@Override
	public DataWatcherEx getDataWatcherEx() 
	{
		return dataWatcher;
	}
	
	public String getDataFile()
	{
		World baseWorld = MinecraftServer.getServer().worldServerForDimension(0);
		String fileName = owner != null ? owner.getUniqueID().toString() + ".dat" : ownerIdentifier + ".dat";
		
		return playerDataPath
				.replace("%WorldDir%", baseWorld.getSaveHandler().getWorldDirectory().getAbsolutePath())
				.replace("%ModID%", getModMetadata().modId.toLowerCase())
				+ fileName;
	}
	
	public void saveDataToFile()
	{
		try
		{
			FileOutputStream fileOut = new FileOutputStream(getDataFile());
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(this);

			objOut.close();
		}

		catch (IOException e)
		{
			RadixExcept.logErrorCatch(e, "IOException while saving player data to file! Data may be lost or corrupted.");
		}
	}

	public <generic extends AbstractPlayerData> generic readDataFromFile(EntityPlayer player, Class<generic> type, File file)
	{
		generic returnData = null;
		
		try
		{
			FileInputStream fileIn = new FileInputStream(file != null ? file.getAbsolutePath() : getDataFile());
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			returnData = (generic) objIn.readObject();
			returnData.owner = player;
			
			objIn.close();
		}

		catch (IOException e)
		{
			RadixExcept.logErrorCatch(e, "IOException while saving player data to file! Data may be lost or corrupted.");
		} 
		
		catch (ClassNotFoundException e) 
		{
			RadixExcept.logErrorCatch(e, "ClassNotFoundException while saving player data to file!");
		}

		return returnData;
	}

	
	public boolean dataExists()
	{
		File dataFile = new File(getDataFile());
		return dataFile.exists();
	}
	
	public static String getPlayerDataPath(World world, String modID)
	{
		return playerDataPath
				.replace("%WorldDir%", world.getSaveHandler().getWorldDirectory().getAbsolutePath())
				.replace("%ModID%", RadixCore.getModMetadataByID(modID).modId.toLowerCase());
	}
}
