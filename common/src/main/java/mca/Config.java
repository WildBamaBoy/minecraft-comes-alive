package mca;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import net.fabricmc.loader.game.MinecraftGameProvider;

public final class Config implements Serializable {
    private static final long serialVersionUID = 956221997003825933L;

    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    public boolean overwriteOriginalVillagers = true;
    public boolean overwriteOriginalZombieVillagers = true;
    public boolean enableDiminishingReturns = true;
    public boolean enableInfection = true;
    public int infectionChance = 5;
    public boolean allowGrimReaper = true;
    public int guardSpawnRate = 6;
    public int chanceToHaveTwins = 2;
    public float marriageHeartsRequirement = 100;
    public int roseGoldSpawnWeight = 6;
    public int babyGrowUpTime = 20;
    public int childGrowUpTime = 60;
    public int villagerMaxHealth = 20;
    public boolean allowTrading = true;
    public boolean logVillagerDeaths = true;
    public boolean enableRevivals = true;
    public String villagerChatPrefix = "";
    public boolean allowPlayerMarriage = true;
    public boolean enableAdminCommands = true;
    public boolean allowRoseGoldGeneration = true;
    public int marriageChance = 5;
    public int marriageLimit = 50;
    public int childrenChance = 5;
    public int childrenLimit = 50;
    public int giftDesaturationQueueLength = 16;
    public int giftDesaturationPenalty = 25;
    public int greetHeartsThreshold = 75;
    public int greetAfterDays = 1;
    public int childInitialHearts = 100;
    public int immigrantChance = 20;
    public double giftSatisfactionFactor = 0.25;
    public int bountyHunterInterval = 24000;
    public int bountyHunterThreshold = -5;
    public float traitChance = 0.01f;
    public float traitInheritChance = 0.5f;

    public static File getConfigFile() {
        MinecraftGameProvider provider = new MinecraftGameProvider();
        return provider.getLaunchDirectory().resolve("config").resolve("mca.json").toFile();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config loadOrCreate() {
        try (FileReader reader = new FileReader(getConfigFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Config config = gson.fromJson(reader, Config.class);
            config.save();
            return config;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Config config = new Config();
        config.save();
        return config;
    }
}
