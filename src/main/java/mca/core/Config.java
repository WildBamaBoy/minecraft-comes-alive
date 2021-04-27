package mca.core;

import java.io.Serializable;

public final class Config implements Serializable {
    public boolean overwriteOriginalVillagers = true;
    public boolean enableDiminishingReturns = true;
    public boolean enableInfection = true;
    public int infectionChance = 5;
    public boolean allowGrimReaper = true;
    public int guardSpawnRate = 6;
    public int chanceToHaveTwins = 2;
    public int marriageHeartsRequirement = 100;
    public int roseGoldSpawnWeight = 6;
    public int babyGrowUpTime = 30;
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

    public Config() {
    }

//    private void addConfigValues() {
//        overwriteOriginalVillagers = this.get("General", "Overwrite Original Villagers?", true, "Should original villagers be overwritten by MCA villagers?").getBoolean();
//        enableDiminishingReturns = this.get("General", "Enable Interaction Fatigue?", true, "Should interactions yield diminishing returns over time?").getBoolean();
//        enableInfection = this.get("General", "Enable Zombie Infection?", true, "Should zombies be able to infect villagers?").getBoolean();
//        infectionChance = this.get("General", "Chance of Infection", 5, "Chance that a villager will be infected on hit from a zombie. Default is 5 for 5%.").getInt();
//        allowGrimReaper = this.get("General", "Allow Grim Reaper?", true, "Should the Grim Reaper boss be enabled?").getBoolean();
//        guardSpawnRate = this.get("General", "Guard Spawn Rate", 6, "How many villagers that should be in a village before a guard spawns.").getInt();
//        chanceToHaveTwins = this.get("General", "Chance to Have Twins", 2, "Chance that you will have twins. Default is 2 for 2%.").getInt();
//        marriageHeartsRequirement = this.get("General", "Marriage Hearts Requirement", 100, "Number of hearts required to get married.").getInt();
//        roseGoldSpawnWeight = this.get("General", "Rose Gold Spawn Weight", 6, "Spawn weights for Rose Gold").getInt();
//        babyGrowUpTime = this.get("General", "Baby Grow Up Time (Minutes)", 30, "Minutes it takes for a baby to be ready to grow up.").getInt();
//        childGrowUpTime = this.get("General", "Child Grow Up Time (Minutes)", 60, "Minutes it takes for a child to grow into an adult.").getInt();
//        villagerSpawnerCap = this.get("General", "Villager Spawner Cap", 5, "Maximum number of villagers that a spawner will create in the area before it stops.").getInt();
//        villagerSpawnerRateMinutes = this.get("General", "Villager Spawner Rate", 30, "The spawner will spawn 1 villager per this many minutes.").getInt();
//        allowTrading = this.get("General", "Enable Trading?", true, "Is trading with villagers enabled?").getBoolean();
//        logVillagerDeaths = this.get("General", "Log Villager Deaths?", true, "Should villager deaths be logged?").getBoolean();
//        enableRevivals = this.get("General", "Enable Revivals?", true, "Should reviving dead villagers be enabled?").getBoolean();
//        villagerChatPrefix = this.get("General", "Villager Chat Prefix", "", "Formatting prefix used for all chat with villagers.").getString();
//        allowPlayerMarriage = this.get("General", "Allow Player Marriage?", true, "Enables or disables player marriage.").getBoolean();
//        enableAdminCommands = this.get("General", "Enable Admin Commands?", true, "Enables or disables MCA admin commands for ops.").getBoolean();
//        allowRoseGoldGeneration = this.get("General", "Allow Rose Gold World Generation", true, "If enabled, generates rose gold in your world. If disabled, generates stone instead.").getBoolean();
//        villagerMaxHealth = this.get("General", "Villager Max Health", 20, "Each villager's maximum health. 1 point equals 1 heart.").getInt();
//        marriageChance = this.get("General", "Villager Marriage Chance", 5, "Chance that two villagers get married every 10 minutes.").getInt();
//        marriageLimit = this.get("General", "Villager Marriage Limit", 50, "Percentage of Villagers who will get married over time.").getInt();
//        childrenChance = this.get("General", "Villager Children Chance", 5, "Chance that a married villager get children every 10 minutes.").getInt();
//        childrenLimit = this.get("General", "Villager Children Limit", 50, "Percentage of Villagers in the village, until they stop procreating.").getInt();
//        this.save();
//    }
}