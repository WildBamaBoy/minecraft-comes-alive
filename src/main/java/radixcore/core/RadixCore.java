package radixcore.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

import radixcore.network.RadixPacketHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = RadixCore.ID, name = RadixCore.NAME, version = RadixCore.VERSION, acceptedMinecraftVersions = "[1.7.10]")
public class RadixCore 
{
	protected static final String ID = "RadixCore";
	protected static final String NAME = "RadixCore";
	protected static final String VERSION = "2.0.0";

	@Instance(ID)
	private static RadixCore instance;
	private static Configuration config;
	private static Logger logger;
	private static String runningDirectory;
	private static RadixCrashWatcher crashWatcher;
	private static RadixPacketHandler packetHandler;
	protected static final List<ModMetadataEx> registeredMods = new ArrayList<ModMetadataEx>();

	public static boolean allowUpdateChecking;
	public static boolean allowCrashReporting;
	
    @EventHandler 
    public void preInit(FMLPreInitializationEvent event)
    {
    	instance = this;
    	logger = event.getModLog();
    	runningDirectory = System.getProperty("user.dir");
    	
    	config = new Configuration(event.getSuggestedConfigurationFile());
    	config.setCategoryComment("Privacy", "Settings relating to your privacy are located here.");
    	allowUpdateChecking = config.get("Privacy", "Allow update checking", true).getBoolean();
    	allowCrashReporting = config.get("Privacy", "Allow crash reporting", true).getBoolean();
    	
    	config.get("Privacy", "Allow crash reporting", true).comment = 
    			"Mod crashes are sent to a remote server for debugging purposes. \n"
    			+ "Your Minecraft username, OS version, Java version, PC username, and installed mods may be shared with the mod author.";
    	
    	crashWatcher = new RadixCrashWatcher();
    	packetHandler = new RadixPacketHandler("RadixCore");
    	
		FMLCommonHandler.instance().bus().register(new RadixEvents());
		MinecraftForge.EVENT_BUS.register(new RadixEvents());
		
    	logger.info("RadixCore version " + VERSION + " is running from " + runningDirectory);
    }
    
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
    	crashWatcher.checkForCrashReports();
    }
    
    public static String getRunningDirectory()
    {
    	return runningDirectory;
    }
    
    public static RadixCore getInstance()
    {
    	return instance;
    }
    
    public static Logger getLogger()
    {
    	return logger;
    }
    
    public static RadixPacketHandler getPacketHandler()
    {
    	return packetHandler;
    }
    
    public static void registerMod(ModMetadata modMetadata)
    {
    	registerMod(ModMetadataEx.getFromModMetadata(modMetadata));
    }
    
    public static void registerMod(ModMetadataEx modMetadataEx)
    {
    	registeredMods.add(modMetadataEx);
    }
    
    public static List<ModMetadataEx> getRegisteredMods()
    {
    	return registeredMods;
    }
    
    public static ModMetadataEx getModMetadataByID(String modID)
    {
    	for (ModMetadataEx data : registeredMods)
    	{
    		if (data.modId.equals(modID))
    		{
    			return data;
    		}
    	}
    	
    	return null;
    }
}
