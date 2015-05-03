package mca.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public final class Config 
{
	private final Configuration config;

	public int baseItemId;
	public int baseBlockId;
	public int baseEntityId;
	
	public boolean overwriteOriginalVillagers;
	public int guardSpawnRate;
	public int chanceToHaveTwins;
	public int villagerMaxHealth;
	public int guardMaxHealth;
	public boolean storyProgression;
	public int storyProgressionThreshold;
	public int storyProgressionRate;

	public int roseGoldSpawnWeight;
	
	public int babyGrowUpTime;
	public int childGrowUpTime;
	public boolean isAgingEnabled;

	public int childLimit;
	public boolean allowFarmingChore;
	public boolean allowFishingChore;
	public boolean allowWoodcuttingChore;
	public boolean allowMiningChore;
	public boolean allowHuntingChore;
	public boolean allowGiftDemands;
	public boolean allowTrading;
	public String villagerChatPrefix;

	public boolean showMoodParticles;
	public boolean showNameTagOnHover;
	public boolean showVillagerConversations = false; //TODO
	public boolean modifyFemaleBody;
	public boolean allowBlinking;
	
	public boolean inTutorialMode;
	
	public boolean allowCrashReporting;
	public boolean allowUpdateChecking;
	
	public Config(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());
		addConfigValues();
	}

	public void addConfigValues()
	{
		config.setCategoryComment("Init", "Settings that affect how MCA starts up.");
		baseItemId = config.get("Init", "Base Item ID", 35277, "The base ID to use for items in MCA. Only applicable in 1.6.4.").getInt();
		baseBlockId = config.get("Init", "Base Block ID", 3344, "The base ID to use for blocks in MCA. Only applicable in 1.6.4.").getInt();
		baseEntityId = config.get("Init", "Base Entity ID", 227, "The base ID to use for entities in MCA. Only change if you know what you are doing!").getInt();

		config.setCategoryComment("Privacy", "Setting pertaining to your privacy while using MCA.");
		allowCrashReporting = config.get("Privacy", "Allow crash reporting", true, "True if MCA can send crash reports to the mod authors. Crash reports may include your Minecraft username, OS version, Java version, and PC username.").getBoolean();
		allowUpdateChecking = config.get("Privacy", "Allow update checking", true, "True if MCA can check for updates. This setting requires a restart in order to take effect.").getBoolean();
		
		config.setCategoryComment("General", "General mod settings.");

		overwriteOriginalVillagers = config.get("General", "Overwrite original villagers", true).getBoolean();
		chanceToHaveTwins = config.get("General", "Chance to have twins", 2, "Your percent chance of having twins.").getInt();
		guardSpawnRate = config.get("General", "Guard spawn rate", 3, 
				"One guard per this many villagers. Set to zero or a negative number to disable guards.").getInt();

		villagerMaxHealth = config.get("General", "Villager max health", 20).getInt();
		guardMaxHealth = config.get("General", "Guard max health", 40).getInt();

		storyProgression = config.get("General", "Story progression", true, 
				"Villagers automatically get married, have children, etc.").getBoolean();

		storyProgressionThreshold = config.get("General", "Story progression threshold", 120, 
				"Amount of time a villager has to be alive before story progression begins to affect them. This value is in MINUTES, default is 120. Range (1 and above)").getInt();

		storyProgressionRate = config.get("General", "Story progression rate", 20, 
				"How often story progression tries to make changes. Changes may not always be made. This value is in MINUTES, default is 20. Range (1 and above)").getInt();
		inTutorialMode = config.get("General", "Tutorial mode", true,
				"Displays various tips while you play. ").getBoolean();
		
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
		allowFarmingChore = config.get("Server", "Allow farming chore", true).getBoolean();
		allowFishingChore = config.get("Server", "Allow fishing chore", true).getBoolean();
		allowWoodcuttingChore = config.get("Server", "Allow woodcutting chore", true).getBoolean();
		allowMiningChore = config.get("Server", "Allow mining chore", true).getBoolean();
		allowHuntingChore = config.get("Server", "Allow hunting chore", true).getBoolean();
		allowGiftDemands = config.get("Server", "Allow gift demands", true).getBoolean();
		allowTrading = config.get("Server", "Allow trading", true).getBoolean();
		
		villagerChatPrefix = config.get("Server", "Villager chat prefix", "").getDefault();
		
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
}
