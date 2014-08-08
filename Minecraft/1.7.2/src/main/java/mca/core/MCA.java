/*******************************************************************************
 * MCA.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mca.api.chores.CatchableFish;
import mca.api.chores.CookableFood;
import mca.api.chores.CuttableLog;
import mca.api.chores.FarmableCrop;
import mca.api.chores.FishingReward;
import mca.api.chores.HuntableAnimal;
import mca.api.chores.MineableOre;
import mca.api.enums.EnumFarmType;
import mca.api.registries.ChoreRegistry;
import mca.api.registries.VillagerRegistryMCA;
import mca.api.villagers.VillagerEntryMCA;
import mca.block.BlockTombstone;
import mca.command.CommandBlock;
import mca.command.CommandBlockAll;
import mca.command.CommandDebugMode;
import mca.command.CommandDebugRule;
import mca.command.CommandDevControl;
import mca.command.CommandDivorce;
import mca.command.CommandHaveBaby;
import mca.command.CommandHaveBabyAccept;
import mca.command.CommandHelp;
import mca.command.CommandMarry;
import mca.command.CommandMarryAccept;
import mca.command.CommandMarryDecline;
import mca.command.CommandModProps;
import mca.command.CommandReloadModProperties;
import mca.command.CommandReloadWorldProperties;
import mca.command.CommandSetGender;
import mca.command.CommandSetName;
import mca.command.CommandUnblock;
import mca.command.CommandUnblockAll;
import mca.core.forge.ClientTickHandler;
import mca.core.forge.CommonProxy;
import mca.core.forge.EventHooks;
import mca.core.forge.GuiHandler;
import mca.core.forge.ServerTickHandler;
import mca.core.util.SkinLoader;
import mca.entity.AbstractEntity;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.entity.EntityVillagerChild;
import mca.enums.EnumCrownColor;
import mca.item.ItemArrangersRing;
import mca.item.ItemBabyBoy;
import mca.item.ItemBabyGirl;
import mca.item.ItemCrown;
import mca.item.ItemDecorativeCrown;
import mca.item.ItemEggFemale;
import mca.item.ItemEggMale;
import mca.item.ItemEngagementRing;
import mca.item.ItemHeirCrown;
import mca.item.ItemKingsBoots;
import mca.item.ItemKingsCoat;
import mca.item.ItemKingsPants;
import mca.item.ItemLostRelativeDocument;
import mca.item.ItemTombstone;
import mca.item.ItemVillagerEditor;
import mca.item.ItemWeddingRing;
import mca.item.ItemWhistle;
import mca.lang.LanguageLoaderHook;
import mca.lang.LanguageParser;
import mca.network.PacketRegistry;
import mca.network.packets.PacketSetWorldProperties;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.world.World;
import net.minecraftforge.common.AchievementPage;

import com.radixshock.radixcore.core.ModLogger;
import com.radixshock.radixcore.core.RadixCore;
import com.radixshock.radixcore.core.UnenforcedCore;
import com.radixshock.radixcore.file.ModPropertiesManager;
import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.lang.ILanguageLoaderHook;
import com.radixshock.radixcore.lang.ILanguageParser;
import com.radixshock.radixcore.lang.LanguageLoader;
import com.radixshock.radixcore.network.AbstractPacketHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * Main entry point and core of the Minecraft Comes Alive mod.
 */
@Mod(modid="mca", name="Minecraft Comes Alive", version=Constants.VERSION, dependencies="required-after:radixcore")
public class MCA extends UnenforcedCore
{
	/** An instance of the core MCA class. */
	@Instance("mca")
	private static MCA instance;

	/** An instance of the sided proxy. */
	@SidedProxy(clientSide="mca.core.forge.ClientProxy", serverSide="mca.core.forge.CommonProxy")
	public static CommonProxy proxy;
	public static AbstractPacketHandler packetHandler;	
	public static boolean packetsRegisteredServerSide;
	
	public static ServerTickHandler serverTickHandler;
	public static ClientTickHandler clientTickHandler; 

	private static ModLogger logger;
	private static LanguageLoader languageLoader;
	private static LanguageParser languageParser;
	private static LanguageLoaderHook languageLoaderHook;

	public static Random rand = new Random();

	//Creative tab.
	public CreativeTabs tabMCA;

	//Items and Blocks.
	public Item itemWeddingRing;
	public Item itemEngagementRing;
	public Item itemArrangersRing;
	public Item itemBabyBoy;
	public Item itemBabyGirl;
	public Item itemTombstone;
	public Item itemEggMale;
	public Item itemEggFemale;
	public Item itemWhistle;
	public Item itemVillagerEditor;
	public Item itemLostRelativeDocument;
	public Item itemCrown;
	public Item itemHeirCrown;
	public Item itemKingsCoat;
	public Item itemKingsPants;
	public Item itemKingsBoots;
	public Item itemRedCrown;
	public Item itemGreenCrown;
	public Item itemBlueCrown;
	public Item itemPinkCrown;
	public Item itemPurpleCrown;
	public Block blockTombstone;

	//Achievements
	public Achievement achievementCharmer;
	public Achievement achievementGetMarried;
	public Achievement achievementHaveBabyBoy;
	public Achievement achievementHaveBabyGirl;
	public Achievement achievementCookBaby;
	public Achievement achievementBabyGrowUp;
	public Achievement achievementChildFarm;
	public Achievement achievementChildFish;
	public Achievement achievementChildWoodcut;
	public Achievement achievementChildMine;
	public Achievement achievementChildHuntKill;
	public Achievement achievementChildHuntTame;
	public Achievement achievementChildGrowUp;
	public Achievement achievementAdultFullyEquipped;
	public Achievement achievementAdultKills;
	public Achievement achievementAdultMarried;
	public Achievement achievementHaveGrandchild;
	public Achievement achievementHaveGreatGrandchild;
	public Achievement achievementHaveGreatx2Grandchild;
	public Achievement achievementHaveGreatx10Grandchild;
	public Achievement achievementHardcoreSecret;
	public Achievement achievementCraftCrown;
	public Achievement achievementExecuteVillager;
	public Achievement achievementMakeKnight;
	public Achievement achievementKnightArmy;
	public Achievement achievementMakePeasant;
	public Achievement achievementPeasantArmy;
	public Achievement achievementNameHeir;
	public Achievement achievementMonarchSecret;
	public AchievementPage achievementPageMCA;

	//Various fields for core functions.
	public  boolean languageLoaded 			   = false;
	public	boolean	hasLoadedProperties		   = false;
	public 	boolean hasCompletedMainMenuTick   = false;
	public  boolean hasEmptiedPropertiesFolder = false;
	public  boolean hasCheckedForUpdates	   = false;
	public  boolean isDevelopmentEnvironment   = false;
	public 	boolean hasReceivedClientSetup     = false;

	public  ModPropertiesManager modPropertiesManager = null;
	public  WorldPropertiesManager worldPropertiesManager = null;

	//Debug fields.
	public boolean inDebugMode				   		= false;
	public boolean debugDoSimulateHardcore 			= false;
	public boolean debugDoRapidVillagerBabyGrowth 	= false;
	public boolean debugDoRapidVillagerChildGrowth 	= false;
	public boolean debugDoRapidPlayerChildGrowth 	= false;
	public boolean debugDoLogPackets 				= false;

	//World-specific fields.
	public boolean hasNotifiedOfBabyReadyToGrow = false;

	//Maps of data
	/**Map of marriage requests. Key = request sender, Value = request recipient.**/
	public Map<String, String> marriageRequests = new HashMap<String, String>();

	/**Map of requests to have a baby. Key = request sender, Value = sender spouse name.**/
	public Map<String, String> babyRequests = new HashMap<String, String>();

	/**Map of MCA ids and entity ids. Key = mcaId, Value = entityId.**/
	public Map<Integer, Integer> idsMap = new HashMap<Integer, Integer>();

	/**Map of MCA ids and their associated entity. Key = mcaId, Value = abstractEntity. */
	public Map<Integer, AbstractEntity> entitiesMap = new HashMap<Integer, AbstractEntity>();

	/**Map of all current players and their world properties manager. Server side only.**/
	public Map<String, WorldPropertiesManager> playerWorldManagerMap = new HashMap<String, WorldPropertiesManager>();

	/**Map of the inventory of a player saved just before they died. */
	public Map<String, ArrayList<EntityItem>> deadPlayerInventories = new HashMap<String, ArrayList<EntityItem>>();

	/** List of the male names loaded from MaleNames.txt.*/
	public static List<String> maleNames = new ArrayList<String>();

	/** List of the female names loaded from FemaleNames.txt.*/
	public static List<String> femaleNames = new ArrayList<String>();

	/** Map of the IDs of items and the amount of hearts given to the villager who receives this item.*/
	public static Map<Object, Integer> acceptableGifts = new HashMap<Object, Integer>();

	/**
	 * Constructor. Registers MCA with RadixCore.
	 */
	public MCA()
	{
		RadixCore.registeredMods.add(this);
	}

	/**
	 * @return	An instance of MCA.
	 */
	public static MCA getInstance()
	{
		return instance;
	}

	/**
	 * @return	Gets the mod properties list.
	 */
	public ModPropertiesList getModProperties()
	{
		return (ModPropertiesList)modPropertiesManager.modPropertiesInstance;
	}

	/**
	 * @return	Gets the world properties list.
	 */
	public WorldPropertiesList getWorldProperties()
	{
		return (WorldPropertiesList)worldPropertiesManager.worldPropertiesInstance;
	}

	/**
	 * @return  Gets the world properties list associated with the provided manager.
	 */
	public WorldPropertiesList getWorldProperties(Object obj)
	{
		if (obj instanceof WorldPropertiesList)
		{
			return (WorldPropertiesList)obj;
		}

		else
		{
			final WorldPropertiesManager manager = (WorldPropertiesManager)obj;
			return (WorldPropertiesList)manager.worldPropertiesInstance;
		}
	}

	/**
	 * Gets the appropriate skin list for the entity provided.
	 * 
	 * @param 	entity	The entity that needs a list of valid skins.
	 * 
	 * @return	A list of skins that are valid for the provided entity.
	 */
	public static List<String> getSkinList(AbstractEntity entity)
	{
		VillagerEntryMCA entry = null;

		if (entity instanceof EntityPlayerChild)
		{
			entry = VillagerRegistryMCA.getRegisteredVillagerEntry(-1);
		}

		else
		{
			entry = VillagerRegistryMCA.getRegisteredVillagersMap().get(entity.profession);
		}

		if (entity.isMale)
		{
			return entry.getMaleSkinsList();
		}

		else
		{
			return entry.getFemaleSkinsList();
		}
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) 
	{
		instance = this;
		logger = new ModLogger(this);
		languageLoader = new LanguageLoader(this);

		if (event.getSide() == Side.CLIENT)
		{
			clientTickHandler = new ClientTickHandler();
		}

		serverTickHandler = new ServerTickHandler();

		try
		{
			final String sourcePath = MCA.class.getProtectionDomain().getCodeSource().getLocation().getPath();

			if (sourcePath.contains("bin/mca/core/MCA.class"))
			{
				isDevelopmentEnvironment = true;
			}
		}

		catch (NullPointerException e) { }

		//Load external data and register proxy methods.
		modPropertiesManager = new ModPropertiesManager(this, ModPropertiesList.class);

		//Register GUI handlers.
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		//Register villagers with API.
		VillagerRegistryMCA.registerVillagerType(-1, "Kid", this.getClass());
		VillagerRegistryMCA.registerVillagerType(0, "Farmer", this.getClass());
		VillagerRegistryMCA.registerVillagerType(1, "Librarian", this.getClass());
		VillagerRegistryMCA.registerVillagerType(2, "Priest", this.getClass());
		VillagerRegistryMCA.registerVillagerType(3, "Smith", this.getClass());
		VillagerRegistryMCA.registerVillagerType(4, "Butcher", this.getClass());
		VillagerRegistryMCA.registerVillagerType(5, "Guard", this.getClass());
		VillagerRegistryMCA.registerVillagerType(6, "Baker", this.getClass());
		VillagerRegistryMCA.registerVillagerType(7, "Miner", this.getClass());

		SkinLoader.loadMainSkins();

		//Register chore data with API.
		ChoreRegistry.registerChoreEntry(new CookableFood(Items.porkchop, Items.cooked_porkchop));
		ChoreRegistry.registerChoreEntry(new CookableFood(Items.beef, Items.cooked_beef));
		ChoreRegistry.registerChoreEntry(new CookableFood(Items.chicken, Items.cooked_chicken));
		ChoreRegistry.registerChoreEntry(new CookableFood(Items.fish, Items.cooked_fished));
		ChoreRegistry.registerChoreEntry(new CookableFood(Items.potato, Items.baked_potato));

		ChoreRegistry.registerChoreEntry(new FarmableCrop("gui.button.chore.farming.plant.wheat", Items.wheat_seeds, Blocks.wheat, Items.wheat, 1, 1, EnumFarmType.NORMAL, true));
		ChoreRegistry.registerChoreEntry(new FarmableCrop("gui.button.chore.farming.plant.melon", Items.melon_seeds, Blocks.melon_stem, Blocks.melon_block, Items.melon, 3, 7, EnumFarmType.BLOCK, false));
		ChoreRegistry.registerChoreEntry(new FarmableCrop("gui.button.chore.farming.plant.pumpkin", Items.pumpkin_seeds, Blocks.pumpkin_stem, Blocks.pumpkin, 1, 1, EnumFarmType.BLOCK, false));
		ChoreRegistry.registerChoreEntry(new FarmableCrop("gui.button.chore.farming.plant.carrot", Items.carrot, Blocks.carrots, Items.carrot, 1, 6, EnumFarmType.NORMAL, false));
		ChoreRegistry.registerChoreEntry(new FarmableCrop("gui.button.chore.farming.plant.potato", Items.potato, Blocks.potatoes, Items.potato, 1, 6, EnumFarmType.NORMAL, false));
		ChoreRegistry.registerChoreEntry(new FarmableCrop("gui.button.chore.farming.plant.sugarcane", Items.reeds, Blocks.reeds, Items.reeds, 1, 1, EnumFarmType.SUGARCANE, false));

		ChoreRegistry.registerChoreEntry(new CatchableFish(Items.fish));
		ChoreRegistry.registerChoreEntry(new CatchableFish(Items.fish, ItemFishFood.FishType.CLOWNFISH.func_150976_a()));
		ChoreRegistry.registerChoreEntry(new CatchableFish(Items.fish, ItemFishFood.FishType.COD.func_150976_a()));
		ChoreRegistry.registerChoreEntry(new CatchableFish(Items.fish, ItemFishFood.FishType.PUFFERFISH.func_150976_a()));
		ChoreRegistry.registerChoreEntry(new CatchableFish(Items.fish, ItemFishFood.FishType.SALMON.func_150976_a()));

		ChoreRegistry.registerChoreEntry(new FishingReward(Blocks.torch, false, 1 , 4));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.wheat_seeds, false, 1 , 4));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.bucket, false, 1 , 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.stone_pickaxe, false, 1 , 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.stone_shovel, false, 1 , 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Blocks.rail, false, 1 , 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.book, false, 1 , 3));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.bone, false, 1 , 4));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.flint_and_steel, false, 1 , 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.compass, false, 1 , 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.arrow, false, 3 , 12));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.bow, false, 1 , 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.leather_boots, false, 1 , 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.stick, false, 1 , 5));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.melon_seeds, false, 1 , 3));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.pumpkin_seeds, false, 1 , 3));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.clay_ball, false, 2 , 5));

		ChoreRegistry.registerChoreEntry(new FishingReward(Items.boat, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.diamond, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.emerald, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Blocks.tnt, true, 1, 2));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.fishing_rod, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.diamond_horse_armor, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.golden_horse_armor, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.iron_horse_armor, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.diamond_pickaxe, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.diamond_shovel, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.name_tag, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.diamond_helmet, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.diamond_boots, true, 1, 1));
		ChoreRegistry.registerChoreEntry(new FishingReward(Items.ender_pearl, true, 1, 3));

		ChoreRegistry.registerChoreEntry(new CuttableLog("gui.button.chore.woodcutting.treetype.oak", Blocks.log, 0));
		ChoreRegistry.registerChoreEntry(new CuttableLog("gui.button.chore.woodcutting.treetype.spruce", Blocks.log, 1));
		ChoreRegistry.registerChoreEntry(new CuttableLog("gui.button.chore.woodcutting.treetype.birch", Blocks.log, 2));
		ChoreRegistry.registerChoreEntry(new CuttableLog("gui.button.chore.woodcutting.treetype.jungle", Blocks.log, 3));
		ChoreRegistry.registerChoreEntry(new CuttableLog("gui.button.chore.woodcutting.treetype.acacia", Blocks.log2, 0));
		ChoreRegistry.registerChoreEntry(new CuttableLog("gui.button.chore.woodcutting.treetype.darkoak", Blocks.log2, 1));

		ChoreRegistry.registerChoreEntry(new MineableOre("gui.button.chore.mining.find.coal", Blocks.coal_ore, Items.coal, 0, 1, 1));
		ChoreRegistry.registerChoreEntry(new MineableOre("gui.button.chore.mining.find.iron", Blocks.iron_ore, Blocks.iron_ore, 0, 1, 1));
		ChoreRegistry.registerChoreEntry(new MineableOre("gui.button.chore.mining.find.lapis", Blocks.lapis_ore, Items.dye, 4, 4, 8));
		ChoreRegistry.registerChoreEntry(new MineableOre("gui.button.chore.mining.find.gold", Blocks.gold_ore, Blocks.gold_ore, 0, 1, 1));
		ChoreRegistry.registerChoreEntry(new MineableOre("gui.button.chore.mining.find.diamond", Blocks.diamond_ore, Items.diamond, 0, 1, 1));
		ChoreRegistry.registerChoreEntry(new MineableOre("gui.button.chore.mining.find.redstone", Blocks.redstone_ore, Items.redstone, 0, 4, 5));
		ChoreRegistry.registerChoreEntry(new MineableOre("gui.button.chore.mining.find.emerald", Blocks.emerald_ore, Items.emerald, 0, 1, 1));

		ChoreRegistry.registerChoreEntry(new HuntableAnimal(EntitySheep.class, Blocks.wool, Items.wheat, 50, true, true));
		ChoreRegistry.registerChoreEntry(new HuntableAnimal(EntityCow.class, Items.beef, Items.wheat, 40, true, true));
		ChoreRegistry.registerChoreEntry(new HuntableAnimal(EntityPig.class, Items.porkchop, Items.carrot, 70, true, true));
		ChoreRegistry.registerChoreEntry(new HuntableAnimal(EntityChicken.class, Items.chicken, Items.wheat_seeds, 70, true, true));
		ChoreRegistry.registerChoreEntry(new HuntableAnimal(EntityWolf.class, (Item)null, Items.bone, 33, false, true));

		languageLoader = new LanguageLoader(this);
		languageParser = new LanguageParser();
		languageLoaderHook = new LanguageLoaderHook();
	}

	@Override
	public void init(FMLInitializationEvent event) 
	{
		return;
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) 
	{
		SkinLoader.loadAddonSkins();
		return;
	}

	@Override
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandDebugMode());
		event.registerServerCommand(new CommandHelp());
		event.registerServerCommand(new CommandSetName());
		event.registerServerCommand(new CommandSetGender());
		event.registerServerCommand(new CommandMarry());
		event.registerServerCommand(new CommandMarryAccept());
		event.registerServerCommand(new CommandMarryDecline());
		event.registerServerCommand(new CommandHaveBaby());
		event.registerServerCommand(new CommandHaveBabyAccept());
		event.registerServerCommand(new CommandDivorce());
		event.registerServerCommand(new CommandBlock());
		event.registerServerCommand(new CommandBlockAll());
		event.registerServerCommand(new CommandUnblock());
		event.registerServerCommand(new CommandUnblockAll());
		event.registerServerCommand(new CommandDebugRule());
		event.registerServerCommand(new CommandModProps());
		event.registerServerCommand(new CommandDevControl());
		event.registerServerCommand(new CommandReloadModProperties());
		event.registerServerCommand(new CommandReloadWorldProperties());
		
		if (!packetsRegisteredServerSide)
		{
			packetHandler.registerPackets();
			packetsRegisteredServerSide = true;
		}
		
		MCA.getInstance().getLogger().log("Minecraft Comes Alive is running.");
	}

	@Override
	public void serverStopping(FMLServerStoppingEvent event)
	{
		idsMap.clear();

		for (final WorldPropertiesManager manager : playerWorldManagerMap.values())
		{
			manager.saveWorldProperties();
		}

		playerWorldManagerMap.clear();
		hasLoadedProperties = false;
		hasCompletedMainMenuTick = false;
	}

	@Override
	public String getShortModName() 
	{
		return "MCA";
	}

	@Override
	public String getLongModName() 
	{
		return "Minecraft Comes Alive";
	}

	@Override
	public String getVersion() 
	{
		return Constants.VERSION;
	}

	@Override
	public String getMinimumRadixCoreVersion() 
	{
		return Constants.REQUIRED_RADIX;
	}

	@Override
	public boolean getChecksForUpdates() 
	{
		return true;
	}

	@Override
	public String getUpdateURL() 
	{
		return "http://pastebin.com/raw.php?i=mfenhJaJ";
	}

	@Override
	public String getRedirectURL() 
	{
		return "http://radix-shock.com/update-page.html?userMCA=" + getVersion() + "&currentMCA=%" + 
				"&userMC=" + Loader.instance().getMCVersionString().substring(10) + "&currentMC=%";
	}

	@Override
	public ModLogger getLogger() 
	{
		return logger;
	}

	@Override
	public Class getEventHookClass()
	{
		return EventHooks.class;
	}

	@Override
	public void initializeProxy() 
	{
		proxy.registerTileEntities();
		proxy.registerRenderers();
	}

	@Override
	public void initializeItems() 
	{
		//Creative tab icon
		itemEngagementRing  = new ItemEngagementRing().setUnlocalizedName("ring.engagement");

		tabMCA = new CreativeTabs("tabMCA")
		{
			public Item getTabIconItem()
			{
				return itemEngagementRing;
			}
		};
		LanguageRegistry.instance().addStringLocalization("itemGroup.tabMCA", "Minecraft Comes Alive");

		itemEngagementRing = itemEngagementRing.setCreativeTab(tabMCA);
		GameRegistry.registerItem(itemEngagementRing, "MCA_EngagementRing");

		itemWeddingRing     = new ItemWeddingRing().setUnlocalizedName("ring.wedding").setCreativeTab(tabMCA);
		itemArrangersRing   = new ItemArrangersRing().setUnlocalizedName("ring.arranger").setCreativeTab(tabMCA);
		itemBabyBoy         = new ItemBabyBoy().setUnlocalizedName("baby.boy").setCreativeTab(tabMCA);
		itemBabyGirl        = new ItemBabyGirl().setUnlocalizedName("baby.girl").setCreativeTab(tabMCA);
		itemTombstone       = new ItemTombstone().setUnlocalizedName("tombstone").setCreativeTab(tabMCA);
		itemEggMale		    = new ItemEggMale().setUnlocalizedName("egg.male").setCreativeTab(tabMCA);
		itemEggFemale       = new ItemEggFemale().setUnlocalizedName("egg.female").setCreativeTab(tabMCA);
		itemWhistle		    = new ItemWhistle().setUnlocalizedName("whistle").setCreativeTab(tabMCA);
		itemVillagerEditor  = new ItemVillagerEditor().setUnlocalizedName("editor").setCreativeTab(tabMCA);
		itemLostRelativeDocument = new ItemLostRelativeDocument().setUnlocalizedName("lostrelativedocument").setCreativeTab(tabMCA);
		itemCrown			= new ItemCrown().setUnlocalizedName("crown").setCreativeTab(tabMCA);
		itemHeirCrown		= new ItemHeirCrown().setUnlocalizedName("heircrown").setCreativeTab(tabMCA);
		itemKingsCoat		= new ItemKingsCoat().setUnlocalizedName("kingscoat").setCreativeTab(tabMCA);
		itemKingsPants		= new ItemKingsPants().setUnlocalizedName("kingspants").setCreativeTab(tabMCA);
		itemKingsBoots		= new ItemKingsBoots().setUnlocalizedName("kingsboots").setCreativeTab(tabMCA);
		itemRedCrown = new ItemDecorativeCrown(EnumCrownColor.Red).setUnlocalizedName("redcrown").setCreativeTab(tabMCA);
		itemGreenCrown = new ItemDecorativeCrown(EnumCrownColor.Green).setUnlocalizedName("greencrown").setCreativeTab(tabMCA);
		itemBlueCrown = new ItemDecorativeCrown(EnumCrownColor.Blue).setUnlocalizedName("bluecrown").setCreativeTab(tabMCA);
		itemPinkCrown = new ItemDecorativeCrown(EnumCrownColor.Pink).setUnlocalizedName("pinkcrown").setCreativeTab(tabMCA);
		itemPurpleCrown = new ItemDecorativeCrown(EnumCrownColor.Purple).setUnlocalizedName("purplecrown").setCreativeTab(tabMCA);

		GameRegistry.registerItem(itemWeddingRing, "MCA_WeddingRing");
		GameRegistry.registerItem(itemArrangersRing, "MCA_ArangersRing");
		GameRegistry.registerItem(itemBabyBoy, "MCA_BabyBoy");
		GameRegistry.registerItem(itemBabyGirl, "MCA_BabyGirl");
		GameRegistry.registerItem(itemTombstone, "MCA_Tombstone");
		GameRegistry.registerItem(itemEggMale, "MCA_EggMale");
		GameRegistry.registerItem(itemEggFemale, "MCA_EggFemale");
		GameRegistry.registerItem(itemWhistle, "MCA_Whistle");
		GameRegistry.registerItem(itemVillagerEditor, "MCA_VillagerEditor");
		GameRegistry.registerItem(itemLostRelativeDocument, "MCA_LostRelativeDocument");
		GameRegistry.registerItem(itemCrown, "MCA_Crown");
		GameRegistry.registerItem(itemHeirCrown, "MCA_HeirCrown");
		GameRegistry.registerItem(itemKingsCoat, "MCA_KingsCoat");
		GameRegistry.registerItem(itemKingsPants, "MCA_KingsPants");
		GameRegistry.registerItem(itemKingsBoots, "MCA_KingsBoots");
		GameRegistry.registerItem(itemRedCrown, "MCA_RedCrown");
		GameRegistry.registerItem(itemGreenCrown, "MCA_GreenCrown");
		GameRegistry.registerItem(itemBlueCrown, "MCA_BlueCrown");
		GameRegistry.registerItem(itemPinkCrown, "MCA_PinkCrown");
		GameRegistry.registerItem(itemPurpleCrown, "MCA_PurpleCrown");
	}

	@Override
	public void initializeBlocks() 
	{
		blockTombstone = new BlockTombstone().setBlockName("tombstone");
		GameRegistry.registerBlock(blockTombstone, "MCA_BlockTombstone");
	}

	@Override
	public void initializeRecipes() 
	{
		GameRegistry.addRecipe(new ItemStack(itemEngagementRing, 1), new Object[]
				{
			"#D#", "# #", "###", '#', Items.gold_ingot, 'D', Items.diamond
				});

		GameRegistry.addRecipe(new ItemStack(itemWeddingRing, 1), new Object[]
				{
			"###", '#', Items.gold_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemArrangersRing, 1), new Object[]
				{
			"###", '#', Items.iron_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemTombstone, 1), new Object[]
				{
			" # ", "###", "###", '#', Blocks.cobblestone
				});

		GameRegistry.addRecipe(new ItemStack(itemWhistle, 1), new Object[]
				{
			" W#", "###", '#', Items.iron_ingot, 'W', Blocks.planks
				});

		GameRegistry.addRecipe(new ItemStack(itemLostRelativeDocument, 1), new Object[]
				{
			" IF", " P ", 'I', new ItemStack(Items.dye, 1, 0), 'F', Items.feather, 'P', Items.paper
				});

		GameRegistry.addRecipe(new ItemStack(itemCrown, 1), new Object[]
				{
			"EDE", "G G", "GGG", 'E', Items.emerald, 'D', Items.diamond, 'G', Items.gold_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemHeirCrown, 1), new Object[]
				{
			"GEG", "G G", "GGG", 'E', Items.emerald, 'G', Items.gold_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemKingsCoat, 1), new Object[]
				{
			"GWG", "RWR", "RWR", 'G', Items.gold_ingot, 'W', new ItemStack(Blocks.wool, 1, 0), 'R', new ItemStack(Blocks.wool, 1, 14)
				});

		GameRegistry.addRecipe(new ItemStack(itemKingsPants, 1), new Object[]
				{
			"BBB", "G G", "W W", 'G', Items.gold_ingot, 'W', new ItemStack(Blocks.wool, 1, 0), 'B', new ItemStack(Blocks.wool, 1, 15)
				});

		GameRegistry.addRecipe(new ItemStack(itemKingsBoots, 1), new Object[]
				{
			"G G", "R R", 'G', Items.gold_ingot, 'R', new ItemStack(Blocks.wool, 1, 14)
				});

		GameRegistry.addRecipe(new ItemStack(itemHeirCrown, 1), new Object[]
				{
			"GEG", "G G", "GGG", 'E', Items.emerald, 'G', Items.gold_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemRedCrown, 1), new Object[]
				{
			"GDG", "G G", "GGG", 'D', new ItemStack(Items.dye, 1, 1), 'G', Items.gold_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemGreenCrown, 1), new Object[]
				{
			"GDG", "G G", "GGG", 'D', new ItemStack(Items.dye, 1, 2), 'G', Items.gold_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemBlueCrown, 1), new Object[]
				{
			"GDG", "G G", "GGG", 'D', new ItemStack(Items.dye, 1, 4), 'G', Items.gold_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemPinkCrown, 1), new Object[]
				{
			"GDG", "G G", "GGG", 'D', new ItemStack(Items.dye, 1, 9), 'G', Items.gold_ingot
				});

		GameRegistry.addRecipe(new ItemStack(itemPurpleCrown, 1), new Object[]
				{
			"GDG", "G G", "GGG", 'D', new ItemStack(Items.dye, 1, 5), 'G', Items.gold_ingot
				});
	}

	@Override
	public void initializeSmeltings() 
	{
		GameRegistry.addSmelting(itemBabyBoy, new ItemStack(itemTombstone, 1), 1);
		GameRegistry.addSmelting(itemBabyGirl, new ItemStack(itemTombstone, 1), 1);
	}

	@Override
	public void initializeAchievements() 
	{
		achievementCharmer = new Achievement("MCA_Charmer", "charmer", 0, 13, Blocks.yellow_flower, null).registerStat();
		achievementGetMarried = new Achievement("MCA_GetMarried", "getmarried",0, 12, itemWeddingRing, achievementCharmer).registerStat();
		achievementHaveBabyBoy = new Achievement("MCA_HaveBabyBoy", "havebabyboy",-1, 11, itemBabyBoy, achievementGetMarried).registerStat();
		achievementHaveBabyGirl = new Achievement("MCA_HaveBabyGirl", "havebabygirl",1, 11, itemBabyGirl, achievementGetMarried).registerStat();
		achievementCookBaby = new Achievement("MCA_CookBaby", "cookbaby",3, 12, Blocks.furnace, null).registerStat();
		achievementBabyGrowUp = new Achievement("MCA_BabyGrowUp", "growbaby",0, 10, Items.cake, achievementGetMarried).registerStat();
		achievementChildFarm = new Achievement("MCA_ChildFarm", "farming",-1, 9, Items.wheat, achievementBabyGrowUp).registerStat();
		achievementChildFish = new Achievement("MCA_ChildFish", "fishing",-1, 8, Items.fish, achievementBabyGrowUp).registerStat();
		achievementChildWoodcut = new Achievement("MCA_ChildWoodcut", "woodcutting",-1, 7, Blocks.log, achievementBabyGrowUp).registerStat();
		achievementChildMine = new Achievement("MCA_ChildMine", "mining",-1, 6, Items.diamond, achievementBabyGrowUp).registerStat();
		achievementChildHuntKill = new Achievement("MCA_ChildHuntKill", "huntkill",-2, 5, Items.beef, achievementBabyGrowUp).registerStat();
		achievementChildHuntTame = new Achievement("MCA_ChildHuntTame", "hunttame",-1, 5, Items.carrot_on_a_stick, achievementBabyGrowUp).registerStat();
		achievementChildGrowUp = new Achievement("MCA_ChildGrowUp", "growkid",0, 4, Items.chainmail_chestplate, achievementBabyGrowUp).registerStat();
		achievementAdultFullyEquipped = new Achievement("MCA_AdultFullyEquipped", "equipadult", 1, 3, Items.diamond_helmet, achievementChildGrowUp).registerStat();
		achievementAdultKills = new Achievement("MCA_AdultKills", "mobkills", 1, 2, Items.diamond_sword, achievementAdultFullyEquipped).registerStat();
		achievementAdultMarried = new Achievement("MCA_AdultMarried", "marrychild",0, 1, itemArrangersRing, achievementChildGrowUp).registerStat();
		achievementHaveGrandchild = new Achievement("MCA_HaveGrandchild", "havegrandchild",-1, 0, itemBabyBoy, achievementAdultMarried).registerStat();
		achievementHaveGreatGrandchild = new Achievement("MCA_HaveGreatGrandchild", "havegreatgrandchild",-1, -1, itemBabyBoy, achievementHaveGrandchild).registerStat();
		achievementHaveGreatx2Grandchild = new Achievement("MCA_HaveGreatx2Grandchild", "havegreatx2grandchild",-1, -2, itemBabyBoy, achievementHaveGreatGrandchild).registerStat();
		achievementHaveGreatx10Grandchild = new Achievement("MCA_HaveGreatx10Grandchild", "havegreatx10grandchild",-1, -3, itemBabyBoy, achievementHaveGreatx2Grandchild).setSpecial().registerStat();
		achievementHardcoreSecret = new Achievement("MCA_HardcoreSecret", "hardcoresecret",0, -4, itemTombstone, achievementAdultMarried).setSpecial().registerStat();
		achievementCraftCrown = new Achievement("MCA_CraftCrown", "craftcrown",7, 12, itemCrown, null).setSpecial().registerStat();
		achievementExecuteVillager = new Achievement("MCA_ExecuteVillager", "executevillager",4, 10, Items.skull, achievementCraftCrown).registerStat(); 
		achievementMakeKnight = new Achievement("MCA_MakeKnight", "makeknight",6, 10, Items.iron_sword, achievementCraftCrown).registerStat();
		achievementKnightArmy = new Achievement("MCA_KnightArmy", "knightarmy",6, 8, Items.diamond_sword, achievementMakeKnight).setSpecial().registerStat();
		achievementMakePeasant = new Achievement("MCA_MakePeasant", "makepeasant",8, 10, Items.iron_hoe, achievementCraftCrown).registerStat();
		achievementPeasantArmy = new Achievement("MCA_PeasantArmy", "peasantarmy",8, 8, Items.diamond_hoe, achievementMakePeasant).setSpecial().registerStat();
		achievementNameHeir = new Achievement("MCA_NameHeir", "nameheir",10, 10, itemHeirCrown, achievementCraftCrown).registerStat();
		achievementMonarchSecret = new Achievement("MCA_MonarchSecret", "monarchsecret",7, 5, Items.writable_book, achievementCraftCrown).setSpecial().registerStat();

		//Register achievement page.
		achievementPageMCA = new AchievementPage("Minecraft Comes Alive", 
				achievementCharmer,
				achievementGetMarried,
				achievementHaveBabyBoy,
				achievementHaveBabyGirl,
				achievementCookBaby,
				achievementBabyGrowUp,
				achievementChildFarm,
				achievementChildFish,
				achievementChildWoodcut,
				achievementChildMine,
				achievementChildHuntTame,
				achievementChildHuntKill,
				achievementChildGrowUp,
				achievementAdultFullyEquipped,
				achievementAdultKills,
				achievementAdultMarried,
				achievementHaveGrandchild,
				achievementHaveGreatGrandchild,
				achievementHaveGreatx2Grandchild,
				achievementHaveGreatx10Grandchild,
				achievementHardcoreSecret,
				achievementCraftCrown,
				achievementExecuteVillager,
				achievementMakeKnight,
				achievementKnightArmy,
				achievementMakePeasant,
				achievementPeasantArmy,
				achievementNameHeir,
				achievementMonarchSecret
				);
		AchievementPage.registerAchievementPage(achievementPageMCA);
	}

	@Override
	public void initializeEntities() 
	{	
		EntityRegistry.registerModEntity(EntityVillagerAdult.class, EntityVillagerAdult.class.getSimpleName(), 3, this, 50, 2, true);
		EntityRegistry.registerModEntity(EntityVillagerChild.class, EntityVillagerChild.class.getSimpleName(), 4, this, 50, 2, true);
		EntityRegistry.registerModEntity(EntityPlayerChild.class, EntityPlayerChild.class.getSimpleName(), 5, this, 50, 2, true);
		EntityRegistry.registerModEntity(EntityChoreFishHook.class, EntityChoreFishHook.class.getSimpleName(), 6, this, 50, 2, true);
	}

	@Override
	public void initializeNetwork() 
	{		
		packetHandler = new PacketRegistry(this);
	}

	@Override
	public ModPropertiesManager getModPropertiesManager() 
	{
		return modPropertiesManager;
	}

	@Override
	public WorldPropertiesManager getWorldPropertiesManager()
	{
		return worldPropertiesManager;
	}

	@Override
	public boolean getSetModPropertyCommandEnabled() 
	{
		return true;
	}

	@Override
	public boolean getGetModPropertyCommandEnabled() 
	{
		return true;
	}

	@Override
	public boolean getListModPropertiesCommandEnabled() 
	{
		return true;
	}

	@Override
	public String getPropertyCommandPrefix() 
	{
		return "mca.";
	}

	@Override
	public AbstractPacketHandler getPacketHandler()
	{
		return packetHandler;
	}

	@Override
	public LanguageLoader getLanguageLoader()
	{
		return languageLoader;
	}

	@Override
	public ILanguageParser getLanguageParser() 
	{
		return languageParser;
	}

	@Override
	public ILanguageLoaderHook getLanguageLoaderHook() 
	{
		return languageLoaderHook;
	}

	@Override
	public boolean getLanguageLoaded() 
	{
		return languageLoaded;
	}

	@Override
	public void setLanguageLoaded(boolean value) 
	{
		languageLoaded = value;
	}

	/**
	 * Gets a player in the provided world that has the id provided.
	 * 
	 * @param 	world	The world that the player is in.
	 * @param 	id		The id that the player should have.
	 * 
	 * @return	The player entity that has the ID provided.
	 */
	public EntityPlayer getPlayerByID(World world, int id)
	{
		for (final Map.Entry<String, WorldPropertiesManager> entry : playerWorldManagerMap.entrySet())
		{
			if (getWorldProperties(entry.getValue().worldPropertiesInstance).playerID == id)
			{
				return world.getPlayerEntityByName(entry.getKey());
			}
		}

		return null;
	}

	/**
	 * Gets the ID of a provided player entity.
	 * 
	 * @param 	player	The player whose ID should be gotten.
	 * 
	 * @return	The ID of the provided player. 0 if the player doesn't have an ID, which is invalid
	 * 			since all player IDs are negative.
	 *
	 */
	public int getIdOfPlayer(EntityPlayer player)
	{
		try
		{
			for (final Map.Entry<String, WorldPropertiesManager> entry : playerWorldManagerMap.entrySet())
			{
				if (entry.getKey().equals(player.getCommandSenderName()))
				{
					return getWorldProperties(entry.getValue()).playerID;
				}
			}

			return 0;
		}

		//Happens in a client side GUI.
		catch (NullPointerException e)
		{
			return 0;
		}
	}

	@Override
	public void onCreateNewWorldProperties(WorldPropertiesManager manager)
	{
		WorldPropertiesList list = (WorldPropertiesList)manager.worldPropertiesInstance;
		list.playerID = (int) Math.abs((list.playerName.hashCode() + System.currentTimeMillis() % (1024 * 1024))) * -1;
	}

	@Override
	public void onSaveWorldProperties(WorldPropertiesManager manager)
	{
		this.playerWorldManagerMap.put(manager.getCurrentPlayerName(), manager);
	}

	@Override
	public void onLoadWorldProperties(WorldPropertiesManager manager)
	{
		this.playerWorldManagerMap.put(manager.getCurrentPlayerName(), manager);
	}

	@Override
	public void onUpdateWorldProperties(WorldPropertiesManager manager)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			packetHandler.sendPacketToServer(new PacketSetWorldProperties(manager));
		}

		else if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			packetHandler.sendPacketToPlayer(new PacketSetWorldProperties(manager), (EntityPlayerMP)RadixCore.getPlayerByName(manager.getCurrentPlayerName()));
		}
	}

	static
	{
		acceptableGifts.put(Items.wooden_sword, 3);
		acceptableGifts.put(Items.wooden_axe, 3);
		acceptableGifts.put(Items.wooden_hoe, 3);
		acceptableGifts.put(Items.wooden_shovel, 3);
		acceptableGifts.put(Items.stone_sword, 5);
		acceptableGifts.put(Items.stone_axe, 5);
		acceptableGifts.put(Items.stone_hoe, 5);
		acceptableGifts.put(Items.stone_shovel, 5);
		acceptableGifts.put(Items.wooden_pickaxe, 3);
		acceptableGifts.put(Items.beef, 2);
		acceptableGifts.put(Items.chicken, 2);
		acceptableGifts.put(Items.porkchop, 2);
		acceptableGifts.put(Items.leather, 2);
		acceptableGifts.put(Items.leather_chestplate, 5);
		acceptableGifts.put(Items.leather_helmet, 5);
		acceptableGifts.put(Items.leather_leggings, 5);
		acceptableGifts.put(Items.leather_boots, 5);
		acceptableGifts.put(Items.reeds, 2);
		acceptableGifts.put(Items.wheat_seeds, 2);
		acceptableGifts.put(Items.wheat, 3);
		acceptableGifts.put(Items.bread, 6);
		acceptableGifts.put(Items.coal, 5);
		acceptableGifts.put(Items.sugar, 5);
		acceptableGifts.put(Items.clay_ball, 2);
		acceptableGifts.put(Items.dye, 1);

		acceptableGifts.put(Items.cooked_beef, 7);
		acceptableGifts.put(Items.cooked_chicken, 7);
		acceptableGifts.put(Items.cooked_porkchop, 7);
		acceptableGifts.put(Items.cookie, 10);
		acceptableGifts.put(Items.melon, 10);
		acceptableGifts.put(Items.melon_seeds, 5);
		acceptableGifts.put(Items.iron_helmet, 10);
		acceptableGifts.put(Items.iron_chestplate, 10);
		acceptableGifts.put(Items.iron_leggings, 10);
		acceptableGifts.put(Items.iron_boots, 10);
		acceptableGifts.put(Items.cake, 12);
		acceptableGifts.put(Items.iron_sword, 10);
		acceptableGifts.put(Items.iron_axe, 10);
		acceptableGifts.put(Items.iron_hoe, 10);
		acceptableGifts.put(Items.iron_pickaxe, 10);
		acceptableGifts.put(Items.iron_shovel, 10);
		acceptableGifts.put(Items.fishing_rod, 3);
		acceptableGifts.put(Items.bow, 5);
		acceptableGifts.put(Items.book, 5);
		acceptableGifts.put(Items.bucket, 3);
		acceptableGifts.put(Items.milk_bucket, 5);
		acceptableGifts.put(Items.water_bucket, 2);
		acceptableGifts.put(Items.lava_bucket, 2);
		acceptableGifts.put(Items.mushroom_stew, 5);
		acceptableGifts.put(Items.pumpkin_seeds, 8);
		acceptableGifts.put(Items.flint_and_steel, 4);
		acceptableGifts.put(Items.redstone, 5);
		acceptableGifts.put(Items.boat, 4);
		acceptableGifts.put(Items.wooden_door, 4);
		acceptableGifts.put(Items.iron_door, 6);
		acceptableGifts.put(Items.minecart, 7);
		acceptableGifts.put(Items.flint, 2);
		acceptableGifts.put(Items.gold_nugget, 4);
		acceptableGifts.put(Items.gold_ingot, 20);
		acceptableGifts.put(Items.iron_ingot, 10);

		acceptableGifts.put(Items.diamond, 30);
		acceptableGifts.put(Items.map, 10);
		acceptableGifts.put(Items.clock, 5);
		acceptableGifts.put(Items.compass, 5);
		acceptableGifts.put(Items.blaze_rod, 10);
		acceptableGifts.put(Items.blaze_powder, 5);
		acceptableGifts.put(Items.diamond_sword, 15);
		acceptableGifts.put(Items.diamond_axe, 15);
		acceptableGifts.put(Items.diamond_shovel, 15);
		acceptableGifts.put(Items.diamond_hoe, 15);
		acceptableGifts.put(Items.diamond_leggings, 15);
		acceptableGifts.put(Items.diamond_helmet, 15);
		acceptableGifts.put(Items.diamond_chestplate, 15);
		acceptableGifts.put(Items.diamond_leggings, 15);
		acceptableGifts.put(Items.diamond_boots, 15);
		acceptableGifts.put(Items.painting, 6);
		acceptableGifts.put(Items.ender_pearl, 5);
		acceptableGifts.put(Items.ender_eye, 10);
		acceptableGifts.put(Items.potionitem, 3);
		acceptableGifts.put(Items.slime_ball, 3);
		acceptableGifts.put(Items.saddle, 5);
		acceptableGifts.put(Items.gunpowder, 7);
		acceptableGifts.put(Items.golden_apple, 25);
		acceptableGifts.put(Items.record_11, 15);
		acceptableGifts.put(Items.record_13, 15);
		acceptableGifts.put(Items.record_wait, 15);
		acceptableGifts.put(Items.record_cat, 15);
		acceptableGifts.put(Items.record_chirp, 15);
		acceptableGifts.put(Items.record_far, 15);
		acceptableGifts.put(Items.record_mall, 15);
		acceptableGifts.put(Items.record_mellohi, 15);
		acceptableGifts.put(Items.record_stal, 15);
		acceptableGifts.put(Items.record_strad, 15);
		acceptableGifts.put(Items.record_ward, 15);
		acceptableGifts.put(Items.emerald, 25);

		acceptableGifts.put(Blocks.red_flower, 3);
		acceptableGifts.put(Blocks.yellow_flower, 3);
		acceptableGifts.put(Blocks.planks, 5);
		acceptableGifts.put(Blocks.log, 3);

		acceptableGifts.put(Blocks.pumpkin, 3);
		acceptableGifts.put(Blocks.chest, 5);
		acceptableGifts.put(Blocks.wool, 2);
		acceptableGifts.put(Blocks.iron_ore, 4);
		acceptableGifts.put(Blocks.gold_ore, 7);
		acceptableGifts.put(Blocks.redstone_ore, 3);
		acceptableGifts.put(Blocks.rail, 3);
		acceptableGifts.put(Blocks.detector_rail, 5);
		acceptableGifts.put(Blocks.activator_rail, 5);
		acceptableGifts.put(Blocks.furnace, 5);
		acceptableGifts.put(Blocks.crafting_table, 5);
		acceptableGifts.put(Blocks.lapis_block, 15);

		acceptableGifts.put(Blocks.bookshelf, 7);
		acceptableGifts.put(Blocks.gold_block, 50);
		acceptableGifts.put(Blocks.iron_block, 25);
		acceptableGifts.put(Blocks.diamond_block, 100);
		acceptableGifts.put(Blocks.brewing_stand, 12);
		acceptableGifts.put(Blocks.enchanting_table, 25);
		acceptableGifts.put(Blocks.brick_block, 15);
		acceptableGifts.put(Blocks.obsidian, 15);
		acceptableGifts.put(Blocks.piston, 10);
		acceptableGifts.put(Blocks.glowstone, 10);

		acceptableGifts.put(Blocks.emerald_block, 100);
	}
}
