package mca;

import java.io.Serializable;

public final class Config implements Serializable {
    private static final long serialVersionUID = 956221997003825933L;

    private static Config INSTANCE = new Config();

    public static Config getInstance() {
        return INSTANCE;
    }

    public boolean overwriteOriginalVillagers = true;
    public boolean enableDiminishingReturns = true;
    public boolean enableInfection = true;
    public int infectionChance = 5;
    public boolean allowGrimReaper = true;
    public int guardSpawnRate = 6;
    public int chanceToHaveTwins = 2;
    public int marriageHeartsRequirement = 100;
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
}