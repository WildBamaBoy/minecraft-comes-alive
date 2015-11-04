package mca.core;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import radixcore.util.RadixExcept; 

public final class Config implements Serializable
{
	private transient final Configuration config;

	public int baseItemId;
	public int baseBlockId;
	public int baseEntityId;
	public boolean disableWeddingRingRecipe;
	
	public boolean overwriteOriginalVillagers;
	public boolean allowMobAttacks;
	public boolean shiftClickForPlayerMarriage;
	public boolean giveCrystalBall;
	public boolean disablePatreonButton;
	public int guardSpawnRate;
	public int chanceToHaveTwins;
	public int villagerMaxHealth;
	public int villagerAttackDamage;
	public int guardMaxHealth;
	public int guardAttackDamage;
	public boolean storyProgression;
	public int storyProgressionThreshold;
	public int storyProgressionRate;
	
	public int roseGoldSpawnWeight;
	
	public int babyGrowUpTime;
	public int childGrowUpTime;
	public boolean isAgingEnabled;

	public int childLimit;
	public int villagerSpawnerCap;
	public int storyProgressionCap;
	public boolean allowFarmingChore;
	public boolean allowFishingChore;
	public boolean allowWoodcuttingChore;
	public boolean allowMiningChore;
	public boolean allowHuntingChore;
	public boolean allowGiftDemands;
	public boolean allowTrading;
	public boolean logVillagerDeaths;
	public String villagerChatPrefix;

	public boolean showMoodParticles;
	public boolean showNameTagOnHover;
	public boolean showVillagerConversations = false;
	public boolean modifyFemaleBody;
	public boolean allowBlinking;
	
	public boolean inTutorialMode;
	
	public boolean allowCrashReporting;
	public boolean allowUpdateChecking;
        
        public boolean allowBuildingChore;
        public int ticksPerBuildStep;
        public int cyclesPerBuildStep;
        public int skipsPerBuildCycle;
        
        // build menu
        public char[][] buildables = null; // client/server
        public String buildablePath = null; // server only
	
	public Config(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());
		addConfigValues(event);
	}

	public void addConfigValues()
	{
		config.setCategoryComment("Init", "Settings that affect how MCA starts up.");
		baseItemId = config.get("Init", "Base Item ID", 35277, "The base ID to use for items in MCA. Only applicable in 1.6.4.").getInt();
		baseBlockId = config.get("Init", "Base Block ID", 3344, "The base ID to use for blocks in MCA. Only applicable in 1.6.4.").getInt();
		baseEntityId = config.get("Init", "Base Entity ID", 227, "The base ID to use for entities in MCA. Only change if you know what you are doing!").getInt();
		disableWeddingRingRecipe = config.get("Init", "Disable wedding ring recipe", false, "True if you want to disable the recipe for the wedding ring. It can confict with a few mods. Rose gold can be used as an alternative.").getBoolean();
		
		config.setCategoryComment("Privacy", "Setting pertaining to your privacy while using MCA.");
		allowCrashReporting = config.get("Privacy", "Allow crash reporting", true, "True if MCA can send crash reports to the mod authors. Crash reports may include your Minecraft username, OS version, Java version, and PC username.").getBoolean();
		allowUpdateChecking = config.get("Privacy", "Allow update checking", true, "True if MCA can check for updates. This setting requires a restart in order to take effect.").getBoolean();
		
		config.setCategoryComment("General", "General mod settings.");

		giveCrystalBall = config.get("General", "Give crystal ball", true, "Toggles giving the crystal ball to new players on join. WARNING: If this is false, you must spawn the crystal ball in later manually!").getBoolean();
		disablePatreonButton = config.get("General", "Disable patreon button", false, "Allows you to toggle the Patreon button on and off.").getBoolean();
		overwriteOriginalVillagers = config.get("General", "Overwrite original villagers", true).getBoolean();
		shiftClickForPlayerMarriage = config.get("General", "Shift-click for player marriage menu", false, "True if you must hold shift then right click a player to open the marriage menu. Useful on PvP servers.").getBoolean();
		chanceToHaveTwins = config.get("General", "Chance to have twins", 2, "Your percent chance of having twins.").getInt();
		guardSpawnRate = config.get("General", "Guard spawn rate", 3, 
				"One guard per this many villagers. Set to zero or a negative number to disable guards.").getInt();

		villagerMaxHealth = config.get("General", "Villager max health", 20).getInt();
		villagerAttackDamage = config.get("General", "Villager attack damage", 2, "How many half-hearts of damage a villager can deal without a weapon. Does not affect players.").getInt();
		guardMaxHealth = config.get("General", "Guard max health", 40).getInt();
		guardAttackDamage = config.get("General", "Guard attack damage", 8, "How many half-hearts of damage a guard can deal. Does not affect players.").getInt();
		storyProgression = config.get("General", "Story progression", true, 
				"Villagers automatically get married, have children, etc.").getBoolean();

		storyProgressionThreshold = config.get("General", "Story progression threshold", 120, 
				"Amount of time a villager has to be alive before story progression begins to affect them. This value is in MINUTES, default is 120. Range (1 and above)").getInt();

		storyProgressionRate = config.get("General", "Story progression rate", 20, 
				"How often story progression tries to make changes. Changes may not always be made. This value is in MINUTES, default is 20. Range (1 and above)").getInt();
		storyProgressionCap = config.get("General", "Story progression spawn cap", -1, 
				"Determines whether or not story progression will occur based on this number of villagers within a 32 block radius. Set to -1 to disable. 16 is recommended.").getInt();
		inTutorialMode = config.get("General", "Tutorial mode", true,
				"Displays various tips while you play. ").getBoolean();
		allowMobAttacks = config.get("General", "Allow mob attacks", true, "True if regular Minecraft mobs can attack villagers. False to prevent mobs from attacking any villager.").getBoolean();
		
		config.setCategoryComment("World Generation", "All settings related to MCA's world generation.");
		roseGoldSpawnWeight = config.get("World Generation", "Rose gold spawn weight", 1, "Sets the spawn weight for rose gold. Higher numbers = less common. Set to zero to disable.").getInt();
		
		config.setCategoryComment("Aging", "All aging-related settings of villagers and children in-game.");
		babyGrowUpTime = config.get("Aging", "Time until babies grow up (in minutes)", 10).getInt();
		childGrowUpTime = config.get("Aging", "Time until children grow up (in minutes)", 180).getInt();
		isAgingEnabled = config.get("Aging", "Enable aging", true).getBoolean();

		config.setCategoryComment("Graphics", "All graphics-related settings are located here.");
		showMoodParticles = config.get("Graphics", "Show mood particles", true, "True if you want for particles to appear around villagers if they are in a certain mood").getBoolean();
		showNameTagOnHover = config.get("Graphics", "Show name tag on hover", true, "True if you want a villager's name to appear above their head when you hover over them.").getBoolean();
		//showVillagerConversations = config.get("Graphics", "Show villager conversations", true, "True if you want to see any conversations a villager may have with another villager.").getBoolean();
		modifyFemaleBody = config.get("Graphics", "Modify female body", true, "True if you want a female villager to render with breasts, curves, etc.").getBoolean();
		allowBlinking = config.get("Graphics", "Allow blinking", true, "True if you want to see villagers blink their eyes at random.").getBoolean();
		
		config.setCategoryComment("Server", "All settings that server administrators may want to configure.");
		childLimit = config.get("Server", "Child limit", -1).getInt();
		villagerSpawnerCap = config.get("Server", "Villager spawner cap", 16, "How many villagers maximum that can be within a 32 block radius of any villager spawner block.").getInt();
		allowFarmingChore = config.get("Server", "Allow farming chore", true).getBoolean();
		allowFishingChore = config.get("Server", "Allow fishing chore", true).getBoolean();
		allowWoodcuttingChore = config.get("Server", "Allow woodcutting chore", true).getBoolean();
		allowMiningChore = config.get("Server", "Allow mining chore", true).getBoolean();
		allowHuntingChore = config.get("Server", "Allow hunting chore", true).getBoolean();
		allowGiftDemands = config.get("Server", "Allow gift demands", true).getBoolean();
		allowTrading = config.get("Server", "Allow trading", true).getBoolean();
		logVillagerDeaths = config.get("Server", "Log villager deaths", false, "True if you want villager deaths to be logged to the console/server logs. Shows 'RMFS' values in console, R = related, M = mother, F = father, S = spouse. Can be a bit spammy!").getBoolean();
		villagerChatPrefix = config.get("Server", "Villager chat prefix", "").getDefault();
                
                // build menu
                allowBuildingChore = config.get("Server", "Allow building chore", true).getBoolean();
                ticksPerBuildStep = config.get("Server", "Ticks per Build Step", 10, "Remember there are 20 ticks per second!").getInt();
                cyclesPerBuildStep = config.get("Server", "Cycles per Build Step", 2, "Too many of these too infrequently can cause lag spikes. Too many too often can bog down the game completely").getInt();
                skipsPerBuildCycle = config.get("Server", "Skips per Build Cycle", 64, "Able to skip multiples of the same block that don't need to be replaced.").getInt();
		
		config.save();
	}
	
	public void syncConfiguration()
	{
		config.load();
		addConfigValues();
		config.save();
	}
	
	public Configuration getConfigInstance()
	{
		return config;
	}

	public List<IConfigElement> getConfigCategories()
	{
		List<IConfigElement> elements = new ArrayList<IConfigElement>();

		for (String s : config.getCategoryNames())
		{
			if (!s.equals("server"))
			{	
				IConfigElement element = new ConfigElement(config.getCategory(s));
				for (IConfigElement e : (List<IConfigElement>)element.getChildElements())
				{
					elements.add(e);
				}
			}
		}

		return elements;
	}
        
        public void addConfigValues(FMLPreInitializationEvent event)
        {
            addConfigValues();
            if (allowBuildingChore) findBuildables(event);
        }
        
        public void findBuildables(FMLPreInitializationEvent event)
        {
            //search for buildable folder
            File dir = event.getModConfigurationDirectory();
            File buildableDir = new File(dir.getPath() + File.separator + "MCA" + File.separator + "buildable");
            if (buildableDir.isFile() || buildableDir.isHidden())
            {
                RadixExcept.logErrorCatch(new IOException("Unable to get MCA buildable directory!"),
                        "Is a file or hidden file taking \"config/MCA/buildable\"?");
                return;
            }
            //if it doesn't exist, create it and fill it with internal buildable schematics
            if (!buildableDir.exists())
            {
                if (!extractBuildables(event,buildableDir))
                {
                    RadixExcept.logErrorCatch(new IOException("Unable to extract internal schematics!"),
                        "Can I not write to \"" + buildableDir.getPath() + "\" ?");
                }
            }
            //open buildable folder
            File[] buildableFiles = buildableDir.listFiles();
            String str;
            String ext = ".schematic";
            String name;
            ArrayList<String> matches = new ArrayList<String>();
            //count schematic files
            if (buildableFiles != null) for (File f : buildableFiles)
            {
                name = f.getName();
                str = name.toLowerCase();
                if (str.endsWith(ext))
                {
                    matches.add(name);
                }
            }
            //create a large enough array of character arrays
            if (!matches.isEmpty())
            {
                buildablePath = buildableDir.getPath() + File.separator;
                buildables = new char[matches.size()][];
                //populate strings with filenames sans directory and extension
                int i = 0;
                for (String s : matches)
                {
                    buildables[i++] = s.toCharArray(); // quite convenient
                }
            }
            //now you either have a list of names of buildable schematics or you have nothing
            if (buildables != null)
            {
                File log = new File(buildablePath + "buildable.log");
                try {
                    Files.write(Arrays.deepToString(buildables).getBytes(), log);
                } catch (IOException ex) {
                    RadixExcept.logErrorCatch(ex,  "Can't write buildables log.");
                }
            }
            else RadixExcept.logErrorCatch(new IOException("No buildables!"),"No buildable schematics.");
        } 
        
        public boolean extractBuildables(FMLPreInitializationEvent event, File buildableDir)
        {
            boolean ret = true;
            if (!buildableDir.mkdirs())
            {
                RadixExcept.logErrorCatch(new IOException("Unable to make MCA buildable directory!"),
                        "Am I unable to write to \"config/MCA/buildable\"?");
                return false;
            }
            //load internal buildable folder to new external one
            ZipFile modJar;
            try {
                modJar = new ZipFile(event.getSourceFile());
            } catch (IOException ex) {
                RadixExcept.logErrorCatch(ex, "Unable to get MCA jar!");
                return false;
            }

            if (modJar != null)
            {
                String inDir = "assets/mca/schematic/buildable/";
                File newFile;
                ZipEntry entry;
                ZipInputStream zis;
                FileOutputStream writeable;
                byte[] buffer = new byte[4096];
                try {
                    zis = new ZipInputStream(new FileInputStream(modJar.getName()));
                } catch (FileNotFoundException ex) {
                    RadixExcept.logErrorCatch(ex,"Could not get ZipInputStream");
                    return false;
                }
                try {
                    //single deep copy
                    while ((entry = zis.getNextEntry()) != null)
                    {
                        if (!entry.isDirectory() && entry.getName().startsWith(inDir))
                        {
                            newFile = new File(buildableDir.getPath() + File.separator + entry.getName().substring(inDir.length()-1));
                            writeable = new FileOutputStream(newFile);
                            
                            // now copy out of the zip archive until all bytes are copied
                            int len;
                            while ((len = zis.read(buffer)) > 0)
                            {
                                writeable.write(buffer, 0, len);
                            }
                            writeable.close();
                        }
                    }
                } catch (IOException ex) {
                    RadixExcept.logErrorCatch(ex,"Failed to read the mod jar.");
                    return false;
                }
            }
            else 
            {
                RadixExcept.logErrorCatch(new IOException("modJar = null"),"Unable to get mod jar.");
                return false;
            }
            return ret;
        }
}

       