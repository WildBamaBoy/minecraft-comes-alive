/*******************************************************************************
 * MCA.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.stats.Achievement;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Main entry point and core of the Minecraft Comes Alive mod.
 */
@Mod(modid="mca", name="Minecraft Comes Alive", version="3.4.3")
@NetworkMod(clientSideRequired=true, serverSideRequired=false, 
channels={"MCA_F_REQ", "MCA_F_VAL", "MCA_TARGET", "MCA_REMOVEITEM", "MCA_ACHIEV", "MCA_SYNC", 
		"MCA_SYNC_REQ", "MCA_ENGAGE", "MCA_ADDITEM", "MCA_DROPITEM", "MCA_FAMTREE", "MCA_INVENTORY", 
		"MCA_CHORE", "MCA_TOMB", "MCA_TOMB_REQ", "MCA_POSITION", "MCA_KILL", "MCA_LOGIN", "MCA_WORLDPROP",
		"MCA_SAYLOCAL", "MCA_PLMARRY", "MCA_HAVEBABY", "MCA_BABYINFO", "MCA_TRADE", "MCA_RESPAWN", "MCA_VPPROC"},
		packetHandler = PacketHandler.class)
public class MCA
{
	/** An instance of the core MCA class. */
	@Instance("mca")
	public static MCA instance;

	/** An instance of the sided proxy. */
	@SidedProxy(clientSide="mca.ClientProxy", serverSide="mca.CommonProxy")
	public static CommonProxy proxy;

	//Creative tab.
	public CreativeTabs tabMCA;

	//Items and Blocks
	public Item itemWeddingRing;
	public Item itemEngagementRing;
	public Item itemArrangersRing;
	public Item itemBabyBoy;
	public Item itemBabyGirl;
	public Item itemTombstone;
	public Item itemEggMale;
	public Item itemEggFemale;
	public Item itemWhistle;
	public Item itemFertilityPotion;
	public Item itemVillagerEditor;
	public Item itemLostRelativeDocument;
	public Item itemCrown;
	public Item itemHeirCrown;
	public Item itemKingsCoat;
	public Item itemKingsPants;
	public Item itemKingsBoots;
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
	public Achievement achievementKillHeir;
	public Achievement achievementMonarchSecret;
	public Achievement achievementAdoptOrphan;
	public Achievement achievementMakeFertilityPotion;
	public Achievement achievementUseFertilityPotion;
	public AchievementPage achievementPageMCA;

	//Gui IDs
	public int guiInventoryID = 0;
	public int guiGameOverID = 1;
	public int guiInteractionPlayerChildID = 2;
	public int guiInteractionSpouseID = 3;
	public int guiInteractionVillagerAdultID = 4;
	public int guiInteractionVillagerChildID = 5;
	public int guiNameChildID = 7;
	public int guiSetupID = 8;
	public int guiSpecialDivorceCoupleID = 9;
	public int guiTombstoneID = 10;
	public int guiVillagerEditorID = 11;
	public int guiLostRelativeDocumentID = 12;

	//Various fields for core functions.
	public  String 	runningDirectory           = "";
	public  boolean languageLoaded 			   = false;
	public  boolean inDebugMode				   = false;
	public	boolean	hasLoadedProperties		   = false;
	public 	boolean hasCompletedMainMenuTick   = false;
	public  boolean hasEmptiedPropertiesFolder = false;
	public 	int 	prevMinutes				   = Calendar.getInstance().get(Calendar.MINUTE);
	public 	int 	currentMinutes			   = Calendar.getInstance().get(Calendar.MINUTE);
	private Logger	logger 					   = FMLLog.getLogger();
	public  ModPropertiesManager modPropertiesManager = null;
	public  Random  rand = new Random();

	//Side related fields.
	public boolean isDedicatedServer 			= false;
	public boolean isIntegratedServer			= false;
	public boolean isIntegratedClient			= false;
	public boolean isDedicatedClient			= false;

	//World-specific fields.
	public boolean hasNotifiedOfBabyReadyToGrow = false;

	/**Map of marriage requests. Key = request sender, Value = request recipient.**/
	public Map<String, String> marriageRequests = new HashMap<String, String>();

	/**Map of requests to have a baby. Key = request sender, Value = sender spouse name.**/
	public Map<String, String> babyRequests = new HashMap<String, String>();

	/**Map of MCA ids and entity ids. Key = mcaId, Value = entityId.**/
	public Map<Integer, Integer> idsMap = new HashMap<Integer, Integer>();

	/**Map of all current players and their world properties manager. Server side only.**/
	public Map<String, WorldPropertiesManager> playerWorldManagerMap = new HashMap<String, WorldPropertiesManager>();

	/**
	 * Runs code as soon as possible. Specifically before a player's stats are checked for invalidity (Achievement bug).
	 * So just for the heck of it, let's init EVERYTHING here along with achievements.
	 * 
	 * @param 	event	An instance of the FMLPreInitializationEvent.
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		//Set instance.
		instance = this;
		MinecraftForge.EVENT_BUS.register(new EventHooks());

		//Set running directory.
		if (event.getSide() == Side.CLIENT)
		{
			runningDirectory = System.getProperty("user.dir");
		}

		else if (event.getSide() == Side.SERVER)
		{
			runningDirectory = System.getProperty("user.dir");
		}

		//Load external data and register things.
		modPropertiesManager = new ModPropertiesManager();
		proxy.loadSkinsFromArchive();
		proxy.registerTileEntities();
		proxy.registerRenderers();
		proxy.registerTickHandlers();

		//Register the items and blocks.
		itemEngagementRing  = new ItemEngagementRing(modPropertiesManager.modProperties.itemID_EngagementRing).setUnlocalizedName("EngagementRing");
		itemWeddingRing     = new ItemWeddingRing(modPropertiesManager.modProperties.itemID_WeddingRing).setUnlocalizedName("WeddingRing");
		itemArrangersRing   = new ItemArrangersRing(modPropertiesManager.modProperties.itemID_ArrangersRing).setUnlocalizedName("ArrangersRing");
		itemBabyBoy         = new ItemBabyBoy(modPropertiesManager.modProperties.itemID_BabyBoy).setUnlocalizedName("BabyBoy");
		itemBabyGirl        = new ItemBabyGirl(modPropertiesManager.modProperties.itemID_BabyGirl).setUnlocalizedName("BabyGirl");
		itemTombstone       = new ItemTombstone(modPropertiesManager.modProperties.itemID_Tombstone).setUnlocalizedName("Tombstone");
		itemEggMale		    = new ItemEggMale(modPropertiesManager.modProperties.itemID_EggMale).setUnlocalizedName("MCAEggMale");
		itemEggFemale       = new ItemEggFemale(modPropertiesManager.modProperties.itemID_EggFemale).setUnlocalizedName("MCAEggFemale");
		itemWhistle		    = new ItemWhistle(modPropertiesManager.modProperties.itemID_Whistle).setUnlocalizedName("Whistle");
		itemFertilityPotion = new ItemFertilityPotion(modPropertiesManager.modProperties.itemID_FertilityPotion).setUnlocalizedName("FertilityPotion");
		itemVillagerEditor  = new ItemVillagerEditor(modPropertiesManager.modProperties.itemID_VillagerEditor).setUnlocalizedName("VillagerEditor");
		itemLostRelativeDocument = new ItemLostRelativeDocument(modPropertiesManager.modProperties.itemID_LostRelativeDocument).setUnlocalizedName("LostRelativeDocument");
		itemCrown			= new ItemCrown(modPropertiesManager.modProperties.itemID_Crown).setUnlocalizedName("Crown");
		itemHeirCrown		= new ItemHeirCrown(modPropertiesManager.modProperties.itemID_HeirCrown).setUnlocalizedName("HeirCrown");
		itemKingsCoat		= new ItemKingsCoat(modPropertiesManager.modProperties.itemID_KingsCoat).setUnlocalizedName("KingsCoat");
		itemKingsPants		= new ItemKingsPants(modPropertiesManager.modProperties.itemID_KingsPants).setUnlocalizedName("KingsPants");
		itemKingsBoots		= new ItemKingsBoots(modPropertiesManager.modProperties.itemID_KingsBoots).setUnlocalizedName("KingsBoots");
		blockTombstone      = new BlockTombstone(modPropertiesManager.modProperties.blockID_Tombstone, TileEntityTombstone.class);

		//Register creative tab.
		tabMCA = new CreativeTabs("tabMCA")
		{
			public ItemStack getIconItemStack()
			{
				return new ItemStack(itemEngagementRing, 1, 0);
			}
		};
	    LanguageRegistry.instance().addStringLocalization("itemGroup.tabMCA", "Minecraft Comes Alive");
	    
	    //Set creative tabs.
		itemWeddingRing = itemWeddingRing.setCreativeTab(tabMCA);
		itemEngagementRing = itemEngagementRing.setCreativeTab(tabMCA);
		itemArrangersRing = itemArrangersRing.setCreativeTab(tabMCA);
		itemBabyBoy = itemBabyBoy.setCreativeTab(tabMCA);
		itemBabyGirl = itemBabyGirl.setCreativeTab(tabMCA);
		itemTombstone = itemTombstone.setCreativeTab(tabMCA);
		itemEggMale = itemEggMale.setCreativeTab(tabMCA);
		itemEggFemale = itemEggFemale.setCreativeTab(tabMCA);
		itemWhistle = itemWhistle.setCreativeTab(tabMCA);
		itemFertilityPotion = itemFertilityPotion.setCreativeTab(tabMCA);
		itemVillagerEditor = itemVillagerEditor.setCreativeTab(tabMCA);
		itemLostRelativeDocument = itemLostRelativeDocument.setCreativeTab(tabMCA);
		itemCrown = itemCrown.setCreativeTab(tabMCA);
		itemHeirCrown = itemHeirCrown.setCreativeTab(tabMCA);
		itemKingsCoat = itemKingsCoat.setCreativeTab(tabMCA);
		itemKingsPants = itemKingsPants.setCreativeTab(tabMCA);
		itemKingsBoots = itemKingsBoots.setCreativeTab(tabMCA);
		blockTombstone = blockTombstone.setCreativeTab(tabMCA);
		
		//Register recipes.
		GameRegistry.addRecipe(new ItemStack(itemEngagementRing, 1), new Object[]
				{
			"#D#", "# #", "###", '#', Item.ingotGold, 'D', Item.diamond
				});

		GameRegistry.addRecipe(new ItemStack(itemWeddingRing, 1), new Object[]
				{
			"###", '#', Item.ingotGold
				});

		GameRegistry.addRecipe(new ItemStack(itemArrangersRing, 1), new Object[]
				{
			"###", '#', Item.ingotIron
				});

		GameRegistry.addRecipe(new ItemStack(itemTombstone, 1), new Object[]
				{
			" # ", "###", "###", '#', Block.cobblestone
				});

		GameRegistry.addRecipe(new ItemStack(itemWhistle, 1), new Object[]
				{
			" W#", "###", '#', Item.ingotIron, 'W', Block.planks
				});

		GameRegistry.addRecipe(new ItemStack(itemLostRelativeDocument, 1), new Object[]
				{
			" IF", " P ", 'I', new ItemStack(Item.dyePowder, 1, 0), 'F', Item.feather, 'P', Item.paper
				});

		GameRegistry.addRecipe(new ItemStack(itemCrown, 1), new Object[]
				{
			"EDE", "G G", "GGG", 'E', Item.emerald, 'D', Item.diamond, 'G', Item.ingotGold
				});

		GameRegistry.addRecipe(new ItemStack(itemHeirCrown, 1), new Object[]
				{
			"GEG", "G G", "GGG", 'E', Item.emerald, 'G', Item.ingotGold
				});

		GameRegistry.addRecipe(new ItemStack(itemKingsCoat, 1), new Object[]
				{
			"GWG", "RWR", "RWR", 'G', Item.ingotGold, 'W', new ItemStack(Block.cloth, 1, 0), 'R', new ItemStack(Block.cloth, 1, 14)
				});

		GameRegistry.addRecipe(new ItemStack(itemKingsPants, 1), new Object[]
				{
			"BBB", "G G", "W W", 'G', Item.ingotGold, 'W', new ItemStack(Block.cloth, 1, 0), 'B', new ItemStack(Block.cloth, 1, 15)
				});

		GameRegistry.addRecipe(new ItemStack(itemKingsBoots, 1), new Object[]
				{
			"G G", "R R", 'G', Item.ingotGold, 'R', new ItemStack(Block.cloth, 1, 14)
				});

		//Register GUI handlers.
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
		NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());

		//Register entities.
		EntityRegistry.registerModEntity(EntityVillagerAdult.class, EntityVillagerAdult.class.getSimpleName(), 3, this, 50, 2, true);
		EntityRegistry.registerModEntity(EntityVillagerChild.class, EntityVillagerChild.class.getSimpleName(), 4, this, 50, 2, true);
		EntityRegistry.registerModEntity(EntityPlayerChild.class, EntityPlayerChild.class.getSimpleName(), 5, this, 50, 2, true);
		EntityRegistry.registerModEntity(EntityChoreFishHook.class, EntityChoreFishHook.class.getSimpleName(), 6, this, 50, 2, true);

		//Add smeltings and crafting handler.
		GameRegistry.addSmelting(itemBabyBoy.itemID, new ItemStack(itemTombstone, 1), 1);
		GameRegistry.addSmelting(itemBabyGirl.itemID, new ItemStack(itemTombstone, 1), 1);
		GameRegistry.registerCraftingHandler(new CraftingHandler());

		//Create achievements
		achievementCharmer = new Achievement(1534, "MCA_Charmer", 0, 13, Block.plantYellow, null).registerAchievement();
		achievementGetMarried = new Achievement(1535, "MCA_GetMarried", 0, 12, itemWeddingRing, achievementCharmer).registerAchievement();
		achievementHaveBabyBoy = new Achievement(1536, "MCA_HaveBabyBoy", -1, 11, itemBabyBoy, achievementGetMarried).registerAchievement();
		achievementHaveBabyGirl = new Achievement(1537, "MCA_HaveBabyGirl", 1, 11, itemBabyGirl, achievementGetMarried).registerAchievement();
		achievementCookBaby = new Achievement(1538, "MCA_CookBaby", 3, 12, Block.furnaceBurning, null).registerAchievement();
		achievementBabyGrowUp = new Achievement(1539, "MCA_BabyGrowUp", 0, 10, Block.cake, achievementGetMarried).registerAchievement();
		achievementChildFarm = new Achievement(1540, "MCA_ChildFarm", -1, 9, Item.wheat, achievementBabyGrowUp).registerAchievement();
		achievementChildFish = new Achievement(1541, "MCA_ChildFish", -1, 8, Item.fishRaw, achievementBabyGrowUp).registerAchievement();
		achievementChildWoodcut = new Achievement(1543, "MCA_ChildWoodcut", -1, 7, Block.wood, achievementBabyGrowUp).registerAchievement();
		achievementChildMine = new Achievement(1553, "MCA_ChildMine", -1, 6, Item.diamond, achievementBabyGrowUp).registerAchievement();
		achievementChildHuntKill = new Achievement(1554, "MCA_ChildHuntKill", -2, 5, Item.beefRaw, achievementBabyGrowUp).registerAchievement();
		achievementChildHuntTame = new Achievement(1555, "MCA_ChildHuntTame", -1, 5, Item.carrotOnAStick, achievementBabyGrowUp).registerAchievement();
		achievementChildGrowUp = new Achievement(1544, "MCA_ChildGrowUp", 0, 4, Item.plateChain, achievementBabyGrowUp).registerAchievement();
		achievementAdultFullyEquipped = new Achievement(1545, "MCA_AdultFullyEquipped", 1, 3, Item.helmetDiamond, achievementChildGrowUp).registerAchievement();
		achievementAdultKills = new Achievement(1546, "MCA_AdultKills", 1, 2, Item.swordDiamond, achievementAdultFullyEquipped).registerAchievement();
		achievementAdultMarried = new Achievement(1547, "MCA_AdultMarried", 0, 1, itemArrangersRing, achievementChildGrowUp).registerAchievement();
		achievementHaveGrandchild = new Achievement(1548, "MCA_HaveGrandchild", -1, 0, itemBabyBoy, achievementAdultMarried).registerAchievement();
		achievementHaveGreatGrandchild = new Achievement(1549, "MCA_HaveGreatGrandchild", -1, -1, itemBabyBoy, achievementHaveGrandchild).registerAchievement();
		achievementHaveGreatx2Grandchild = new Achievement(1550, "MCA_HaveGreatx2Grandchild", -1, -2, itemBabyBoy, achievementHaveGreatGrandchild).registerAchievement();
		achievementHaveGreatx10Grandchild = new Achievement(1551, "MCA_HaveGreatx10Grandchild", -1, -3, itemBabyBoy, achievementHaveGreatx2Grandchild).setSpecial().registerAchievement();
		achievementHardcoreSecret = new Achievement(1556, "MCA_HardcoreSecret", 0, -4, itemTombstone, achievementAdultMarried).setSpecial().registerAchievement();
		achievementCraftCrown = new Achievement(1557, "MCA_CraftCrown", 7, 12, itemCrown, null).setSpecial().registerAchievement();
		achievementExecuteVillager = new Achievement(1558, "MCA_ExecuteVillager", 4, 10, Item.skull, achievementCraftCrown).registerAchievement(); 
		achievementMakeKnight = new Achievement(1559, "MCA_MakeKnight", 6, 10, Item.swordIron, achievementCraftCrown).registerAchievement();
		achievementKnightArmy = new Achievement(1560, "MCA_KnightArmy", 6, 8, Item.swordDiamond, achievementMakeKnight).setSpecial().registerAchievement();
		achievementMakePeasant = new Achievement(1561, "MCA_MakePeasant", 8, 10, Item.hoeIron, achievementCraftCrown).registerAchievement();
		achievementPeasantArmy = new Achievement(1562, "MCA_PeasantArmy", 8, 8, Item.hoeDiamond, achievementMakePeasant).setSpecial().registerAchievement();
		//		achievementNameHeir = new Achievement(1563, "MCA_NameHeir", 10, 10, itemHeirCrown, achievementCraftCrown).registerAchievement();
		//		achievementKillHeir = new Achievement(1564, "MCA_KillHeir", 10, 8, Item.axeDiamond, achievementNameHeir).setSpecial().registerAchievement();
		achievementMonarchSecret = new Achievement(1565, "MCA_MonarchSecret", 7, 5, Item.writableBook, achievementCraftCrown).setSpecial().registerAchievement();
		//		achievementAdoptOrphan = new Achievement(1566, "MCA_AdoptOrphan", 7, 2, Item.goldNugget, null).registerAchievement();
		//		achievementMakeFertilityPotion = new Achievement(1567, "MCA_MakeFertilityPotion", 7, 0, Item.potion, null).registerAchievement();
		//		achievementUseFertilityPotion = new Achievement(1568, "MCA_UseFertilityPotion", 7, -2, itemFertilityPotion, achievementMakeFertilityPotion).setSpecial().registerAchievement();

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
				//				achievementNameHeir,
				//				achievementKillHeir,
				achievementMonarchSecret);
		//				achievementAdoptOrphan,
		//				achievementMakeFertilityPotion,
		//				achievementUseFertilityPotion);
		AchievementPage.registerAchievementPage(achievementPageMCA);

		Localization.loadLanguage();
	}

	/**
	 * Writes the specified object's string representation to System.out.
	 * 
	 * @param 	obj	The object to write to System.out.
	 */
	public void log(Object obj)
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();

		if (obj instanceof Throwable)
		{
			((Throwable)obj).printStackTrace();
		}

		try
		{
			logger.log(Level.FINER, "Minecraft Comes Alive " + side.toString() + ": " + obj.toString());
			System.out.println("Minecraft Comes Alive " + side.toString() + ": " + obj.toString());

			MinecraftServer server = MinecraftServer.getServer();

			if (server != null)
			{
				if (server.isDedicatedServer())
				{
					MinecraftServer.getServer().logInfo("MCA: " + obj.toString());
				}
			}
		}

		catch (NullPointerException e)
		{
			logger.log(Level.FINER, "Minecraft Comes Alive " + side.toString() + ": null");
			System.out.println("MCA: null");

			MinecraftServer server = MinecraftServer.getServer();

			if (server != null)
			{
				if (server.isDedicatedServer())
				{
					MinecraftServer.getServer().logDebug("Minecraft Comes Alive " + side.toString() + ": null");
				}
			}
		}
	}

	/**
	 * Writes the specified object's string representation to System.out.
	 * 
	 * @param 	obj	The object to write to System.out.
	 */
	public void logDebug(Object obj)
	{
		if (inDebugMode)
		{
			Side side = FMLCommonHandler.instance().getEffectiveSide();

			if (obj instanceof Throwable)
			{
				((Throwable)obj).printStackTrace();
			}
			try
			{
				logger.log(Level.FINER, "Minecraft Comes Alive [DEBUG] " + side.toString() + ": " + obj.toString());
				System.out.println("Minecraft Comes Alive [DEBUG] " + side.toString() + ": " + obj.toString());

				if (isDedicatedServer)
				{
					MinecraftServer.getServer().logInfo("MCA [DEBUG]: " + obj.toString());
				}
			}

			catch (NullPointerException e)
			{
				logger.log(Level.FINER, "Minecraft Comes Alive [DEBUG] " + side.toString() + ": null");
				System.out.println("MCA [DEBUG]: null");

				if (isDedicatedServer)
				{
					MinecraftServer.getServer().logDebug("Minecraft Comes Alive [DEBUG] " + side.toString() + ": null");
				}
			}
		}
	}

	/**
	 * Stops the game and writes the error to the Forge crash log.
	 * 
	 * @param 	description	A string providing a short description of the problem.
	 * @param 	e			The exception that caused this method to be called.
	 */
	@SideOnly(Side.CLIENT)
	public void quitWithError(String description, Throwable e)
	{
		Writer stackTrace = new StringWriter();

		PrintWriter stackTraceWriter = new PrintWriter(stackTrace);
		e.printStackTrace(stackTraceWriter);

		logger.log(Level.FINER, "Minecraft Comes Alive: An exception occurred.\n" + stackTrace.toString());
		System.out.println("Minecraft Comes Alive: An exception occurred.\n" + stackTrace.toString());

		CrashReport crashReport = new CrashReport("MCA: " + description, e);
		net.minecraft.client.Minecraft.getMinecraft().crashed(crashReport);
	}

	/**
	 * Handles the FMLServerStartingEvent.
	 * 
	 * @param 	event	An instance of the FMLServerStarting event.
	 */
	@EventHandler
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

		if (event.getServer() instanceof DedicatedServer)
		{
			isDedicatedServer = true;
			isIntegratedServer = false;
		}

		else
		{
			isDedicatedServer = false;
			isIntegratedServer = true;
		}

		MCA.instance.log("Minecraft Comes Alive is running.");
	}

	/**
	 * Handles the FMLServerStoppingEvent.
	 * 
	 * @param 	event	An instance of the FMLServerStopping event.
	 */
	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{
		idsMap.clear();

		if (isDedicatedServer)
		{
			for (WorldPropertiesManager manager : playerWorldManagerMap.values())
			{
				manager.saveWorldProperties();
			}
		}

		playerWorldManagerMap.clear();
		hasLoadedProperties = false;
		hasCompletedMainMenuTick = false;
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
		for (Map.Entry<String, WorldPropertiesManager> entry : playerWorldManagerMap.entrySet())
		{
			if (entry.getValue().worldProperties.playerID == id)
			{
				return world.getPlayerEntityByName(entry.getKey());
			}
		}

		return null;
	}

	/**
	 * Gets a player with the name provided.
	 * 
	 * @param 	username	The username of the player.
	 * 
	 * @return	The player entity with the specified username.
	 */
	public EntityPlayer getPlayerByName(String username)
	{
		for (WorldServer world : MinecraftServer.getServer().worldServers)
		{
			EntityPlayer player = world.getPlayerEntityByName(username);

			if (player != null)
			{
				return player;
			}

			else
			{
				continue;
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
			for (Map.Entry<String, WorldPropertiesManager> entry : playerWorldManagerMap.entrySet())
			{
				if (entry.getKey().equals(player.username))
				{
					return entry.getValue().worldProperties.playerID;
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
}
