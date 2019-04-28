package mca.core;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Config implements Serializable {
    private transient final Configuration config;

    public boolean disableWeddingRingRecipe;
    public boolean overwriteOriginalVillagers;
    public boolean allowMobAttacks;
    public boolean shiftClickForPlayerMarriage;
    public boolean enableDiminishingReturns;
    public boolean enableInfection;
    public double infectionChance;
    public int villageTickRate;
    public boolean allowVillagerRevival;
    public int guardSpawnRate;
    public double chanceToHaveTwins;
    public int marriageHeartsRequirement;
    public int roseGoldSpawnWeight;
    public int babyGrowUpTime;
    public int childGrowUpTime;
    public int childLimit;
    public int villagerSpawnerCap;
    public int villagerSpawnerRateMinutes;
    public int goldenAppleGrowthAcceleration;
    public boolean allowTrading;
    public boolean logVillagerDeaths;
    public String villagerChatPrefix;
    public boolean showNameTagOnHover;
    public boolean showModifiedFemaleBody;
    public boolean allowCrashReporting;
    public boolean allowUpdateChecking;

    public Config(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        addConfigValues();
    }

    private void addConfigValues() {
        config.setCategoryComment("Init", "Settings that affect how MCA starts up.");
        disableWeddingRingRecipe = config.get("Init", "Disable wedding ring recipe", false, "True if you want to disable the recipe for the wedding ring. It can confict with a few mods. Rose gold can be used as an alternative. Requires a restart.").getBoolean();

        config.setCategoryComment("Privacy", "Setting pertaining to your privacy while using MCA.");
        allowCrashReporting = config.get("Privacy", "Allow crash reporting", true, "True if MCA can send crash reports to the mod authors. Crash reports may include your Minecraft username, OS version, Java version, and PC username.").getBoolean();
        allowUpdateChecking = config.get("Privacy", "Allow update checking", true, "True if MCA can check for updates. This setting requires a restart in order to take effect.").getBoolean();

        config.setCategoryComment("General", "General mod settings.");

        overwriteOriginalVillagers = config.get("General", "Overwrite original villagers", true).getBoolean();
        villageTickRate = config.get("General", "Village tick rate", 1200, "How often MCA runs village updates, events, etc. in ticks. (spawning guards, raids, and more)").getInt();

        shiftClickForPlayerMarriage = config.get("General", "Shift-click for player marriage menu", false, "True if you must hold shift then right click a player to open the marriage menu. Useful on PvP servers.").getBoolean();
        chanceToHaveTwins = config.get("General", "Chance to have twins", 0.02, "Your percent chance of having twins. 1 = 100%, 0.1 = 10%").getDouble();
        guardSpawnRate = config.get("General", "Guard spawn rate", 3, "One guard per this many villagers. Set to zero or a negative number to disable guards.").getInt();
        enableDiminishingReturns = config.get("General", "Enable diminishing returns?", true, "True if hearts increase decreases after multiple interactions.").getBoolean();
        enableInfection = config.get("General", "Enable infection?", true, "True if villagers and your children have a chance of being infected from zombies.").getBoolean();
        infectionChance = config.get("General", "Infection Chance", 0.1F, "Chance of villageres being infected from zombie attacks. Percentage (1 = 100%, 0.1 = 10%").getDouble();

        allowMobAttacks = config.get("General", "Allow mob attacks", true, "True if regular Minecraft mobs can attack villagers. False to prevent mobs from attacking any villager.").getBoolean();
        marriageHeartsRequirement = config.get("General", "Marriage hearts requirement", 100, "Heart points (1 heart = 10, 1 gold heart = 20) required to marry a villager. -1 if no requirement").getInt();

        config.setCategoryComment("World Generation", "All settings related to MCA's world generation.");
        roseGoldSpawnWeight = config.get("World Generation", "Rose gold spawn weight", 1, "Sets the spawn weight for rose gold. Higher numbers = less common. Set to zero to disable.").getInt();

        config.setCategoryComment("Aging", "All aging-related settings of villagers and children in-game.");
        babyGrowUpTime = config.get("Aging", "Time until babies grow up (in minutes)", 10).getInt();
        childGrowUpTime = config.get("Aging", "Time until children grow up (in minutes)", 180).getInt();
        goldenAppleGrowthAcceleration = config.get("Aging", "Gifting a golden apple to a child decreases their growth time by this many minutes", 30).getInt();

        config.setCategoryComment("Graphics", "All graphics-related settings are located here.");
        showNameTagOnHover = config.get("Graphics", "Show name tag on hover", true, "True if you want a villager's name to appear above their head when you hover over them.").getBoolean();
        showModifiedFemaleBody = config.get("Graphics", "Show modified female body", true, "True if you want a female villager to render with breasts, curves, etc.").getBoolean();

        config.setCategoryComment("Server", "All settings that server administrators may want to configure.");
        childLimit = config.get("Server", "Child limit", -1).getInt();
        villagerSpawnerCap = config.get("Server", "Villager spawner cap", 16, "How many villagers maximum that can be within a 32 block radius of any villager spawner block.").getInt();
        villagerSpawnerCap = config.get("Server", "Villager spawner rate minutes", 5, "How often the villager spawner attempts to spawn new villagers, in minutes.").getInt();
        allowTrading = config.get("Server", "Allow trading", true).getBoolean();
        logVillagerDeaths = config.get("Server", "Log villager deaths", false, "True if you want villager deaths to be logged to the console/server logs. Shows 'RMFS' values in console, R = related, M = mother, F = father, S = spouse. Can be a bit spammy!").getBoolean();
        villagerChatPrefix = config.get("Server", "Villager chat prefix", "").getDefault();
        allowVillagerRevival = config.get("Server", "Allow dead villagers to be revived?", true, "True if players can have the ability to revive villagers they are related to. Creates a file in [world name]/data/ that could become very large on big servers.").getBoolean();

        config.save();
    }

    public Configuration getInstance() {
        return config;
    }

    public List<IConfigElement> getCategories() {
        List<IConfigElement> elements = new ArrayList<IConfigElement>();

        for (String s : config.getCategoryNames()) {
            if (!s.equals("server")) {
                IConfigElement element = new ConfigElement(config.getCategory(s));
                for (IConfigElement e : (List<IConfigElement>) element.getChildElements()) {
                    elements.add(e);
                }
            }
        }

        return elements;
    }
}