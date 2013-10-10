/*******************************************************************************
 * MCA.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import mca.block.BlockTombstone;
import mca.command.CommandBlock;
import mca.command.CommandBlockAll;
import mca.command.CommandCheckUpdates;
import mca.command.CommandDebugMode;
import mca.command.CommandDebugRule;
import mca.command.CommandDivorce;
import mca.command.CommandHaveBaby;
import mca.command.CommandHaveBabyAccept;
import mca.command.CommandHelp;
import mca.command.CommandMarry;
import mca.command.CommandMarryAccept;
import mca.command.CommandMarryDecline;
import mca.command.CommandSetGender;
import mca.command.CommandSetName;
import mca.command.CommandUnblock;
import mca.command.CommandUnblockAll;
import mca.core.forge.CommonProxy;
import mca.core.forge.ConnectionHandler;
import mca.core.forge.CraftingHandler;
import mca.core.forge.EventHooks;
import mca.core.forge.GuiHandler;
import mca.core.forge.PacketHandler;
import mca.core.io.ModPropertiesManager;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.object.UpdateHandler;
import mca.entity.AbstractEntity;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.entity.EntityVillagerChild;
import mca.item.ItemArrangersRing;
import mca.item.ItemBabyBoy;
import mca.item.ItemBabyGirl;
import mca.item.ItemCrown;
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
import mca.tileentity.TileEntityTombstone;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
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
@Mod(modid="mca", name="Minecraft Comes Alive", version=UpdateHandler.VERSION)
@NetworkMod(clientSideRequired=true, serverSideRequired=false, 
channels={"MCA_F_REQ", "MCA_F_VAL", "MCA_TARGET", "MCA_REMOVEITEM", "MCA_ACHIEV", "MCA_SYNC", 
		"MCA_SYNC_REQ", "MCA_ENGAGE", "MCA_ADDITEM", "MCA_DROPITEM", "MCA_FAMTREE", "MCA_INVENTORY", 
		"MCA_CHORE", "MCA_TOMB", "MCA_TOMB_REQ", "MCA_POSITION", "MCA_KILL", "MCA_LOGIN", "MCA_WORLDPROP",
		"MCA_SAYLOCAL", "MCA_PLMARRY", "MCA_HAVEBABY", "MCA_BABYINFO", "MCA_TRADE", "MCA_RESPAWN", "MCA_VPPROC",
		"MCA_ADDAI", "MCA_RETURNINV"},
		packetHandler = PacketHandler.class)
public class MCA
{
	/** An instance of the core MCA class. */
	@Instance("mca")
	public static MCA instance;

	/** An instance of the sided proxy. */
	@SidedProxy(clientSide="mca.core.forge.ClientProxy", serverSide="mca.core.forge.CommonProxy")
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
	public Achievement achievementMonarchSecret;
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
	public	boolean	hasLoadedProperties		   = false;
	public 	boolean hasCompletedMainMenuTick   = false;
	public  boolean hasEmptiedPropertiesFolder = false;
	public  boolean hasCheckedForUpdates	   = false;
	public 	int 	playerBabyCalendarPrevMinutes	   = Calendar.getInstance().get(Calendar.MINUTE);
	public 	int 	playerBabyCalendarCurrentMinutes   = Calendar.getInstance().get(Calendar.MINUTE);
	private Logger	logger 					   = FMLLog.getLogger();
	public  ModPropertiesManager modPropertiesManager = null;
	public  Random  rand = new Random();

	//Debug fields.
	public boolean inDebugMode				   		= false;
	public boolean debugDoSimulateHardcore 			= false;
	public boolean debugDoRapidVillagerBabyGrowth 	= false;
	public boolean debugDoRapidVillagerChildGrowth 	= false;
	public boolean debugDoRapidPlayerChildGrowth 	= false;
	public boolean debugDoLogPackets 				= false;
	
	//Side related fields.
	public boolean isDedicatedServer 	= false;
	public boolean isIntegratedServer	= false;
	public boolean isIntegratedClient	= false;
	public boolean isDedicatedClient	= false;

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

	/** List of the locations of male farmer skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> farmerSkinsMale = new ArrayList<String>();

	/** List of the locations of male librarian skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> librarianSkinsMale = new ArrayList<String>();

	/** List of the locations of male priest skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> priestSkinsMale = new ArrayList<String>();

	/** List of the locations of male smith skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> smithSkinsMale  = new ArrayList<String>();

	/** List of the locations of male butcher skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> butcherSkinsMale = new ArrayList<String>();

	/** List of the locations of male guard skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> guardSkinsMale = new ArrayList<String>();

	/** List of the locations of male kid skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> kidSkinsMale = new ArrayList<String>();

	/** List of the locations of male baker skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> bakerSkinsMale = new ArrayList<String>();

	/** List of the locations of male miner skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> minerSkinsMale = new ArrayList<String>();

	/** List of the locations of female farmer skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> farmerSkinsFemale = new ArrayList<String>();

	/** List of the locations of female librarian skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> librarianSkinsFemale = new ArrayList<String>();

	/** List of the locations of female priest skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> priestSkinsFemale = new ArrayList<String>();

	/** List of the locations of female smith skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> smithSkinsFemale = new ArrayList<String>();

	/** List of the locations of female butcher skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> butcherSkinsFemale = new ArrayList<String>();

	/** List of the locations of female guard skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> guardSkinsFemale = new ArrayList<String>();

	/** List of the locations of female kid skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> kidSkinsFemale = new ArrayList<String>();

	/** List of the locations of female baker skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> bakerSkinsFemale	= new ArrayList<String>();

	/** List of the locations of female miner skins in the Minecraft JAR/MCA Folder.*/
	public static List<String> minerSkinsFemale	= new ArrayList<String>();

	public static String[] normalFarmFiveByFive = 
		{
		"S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S",
		"S", "S", "W", "S", "S",
		"S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S"
		};

	public static String[] sugarcaneFarmFiveByFive =
		{
		"W", "W", "W", "W", "W",
		"S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W",
		"S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W"
		};

	public static String[] blockFarmFiveByFive =
		{
		"W", "P", "P", "P", "W",
		"P", "S", "S", "S", "P",
		"P", "S", "W", "S", "P",
		"P", "S", "S", "S", "P",
		"W", "P", "P", "P", "W",
		};

	public static String[] normalFarmTenByTen = 
		{
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "W", "S", "S", "S", "S", "W", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "W", "S", "S", "S", "S", "W", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"
		};

	public static String[] sugarcaneFarmTenByTen =
		{
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W"
		};

	public static String[] normalFarmFifteenByFifteen =
		{
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "W", "S", "S", "S", "S", "W", "S", "S", "S", "S", "W", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "W", "S", "S", "S", "S", "W", "S", "S", "S", "S", "W", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "W", "S", "S", "S", "S", "W", "S", "S", "S", "S", "W", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		};

	public static String[] sugarcaneFarmFifteenByFifteen =
		{
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", 
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W",		
		"S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S",
		"W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W",
		};

	/** Map of the IDs of items and the amount of hearts given to the villager who receives this item.*/
	public static Map<Integer, Integer> acceptableGifts = new HashMap<Integer, Integer>();

	/** 2D array containing the item IDs of wedding gifts considered junk gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingJunkGiftIDs = 
		{
		{Block.dirt.blockID, 1, 6},
		{Block.deadBush.blockID, 1, 1},
		{Block.cactus.blockID, 1, 3},
		{Item.stick.itemID, 1, 4},
		{Item.rottenFlesh.itemID, 1, 4},
		};

	/** 2D array containing the item IDs of wedding gifts considered small gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingSmallGiftIDs =
		{
		{Item.clay.itemID, 4, 16},
		{Item.axeStone.itemID, 1, 1},
		{Item.swordStone.itemID, 1, 1},
		{Item.shovelStone.itemID, 1, 1},
		{Item.appleRed.itemID, 1, 4},
		{Item.arrow.itemID, 8, 16},
		{Item.pickaxeStone.itemID, 1, 1},
		{Item.book.itemID, 1, 2},
		{Item.redstone.itemID, 8, 32},
		{Item.porkCooked.itemID, 3, 6},
		{Item.beefCooked.itemID, 3, 6},
		{Item.chickenCooked.itemID, 3, 6},
		{Item.bread.itemID, 1, 3},
		{Block.planks.blockID, 2, 16},
		{Block.wood.blockID, 2, 16},
		{Block.cobblestone.blockID, 2, 16},
		{Item.coal.itemID, 2, 8}
		};

	/** 2D array containing the item IDs of wedding gifts considered regular gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingRegularGiftIDs =
		{
		{Item.clay.itemID, 16, 32},
		{Item.axeIron.itemID, 1, 1},
		{Item.swordIron.itemID, 1, 1},
		{Item.shovelIron.itemID, 1, 1},
		{Item.arrow.itemID, 16, 32},
		{Item.pickaxeIron.itemID, 1, 1},
		{Item.redstone.itemID, 8, 32},
		{Item.porkCooked.itemID, 6, 8},
		{Item.beefCooked.itemID, 6, 8},
		{Item.chickenCooked.itemID, 6, 8},
		{Block.planks.blockID, 16, 32},
		{Block.wood.blockID, 16, 32},
		{Block.cobblestone.blockID, 16, 32},
		{Item.coal.itemID, 10, 16},
		{Item.legsIron.itemID, 1, 1},
		{Item.helmetIron.itemID, 1, 1},
		{Item.bootsIron.itemID, 1, 1},
		{Item.plateIron.itemID, 1, 1},
		{Item.melon.itemID, 4, 8},
		{Block.bookShelf.blockID, 2, 4},
		{Item.ingotIron.itemID, 8, 16}
		};

	/** 2D array containing the item IDs of wedding gifts considered great gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingGreatGiftIDs =
		{
		{Block.brick.blockID, 32, 32},
		{Item.axeDiamond.itemID, 1, 1},
		{Item.swordDiamond.itemID, 1, 1},
		{Item.shovelDiamond.itemID, 1, 1},
		{Item.arrow.itemID, 64, 64},
		{Item.pickaxeDiamond.itemID, 1, 1},
		{Block.planks.blockID, 32, 64},
		{Block.wood.blockID, 32, 64},
		{Block.cobblestone.blockID, 32, 64},
		{Item.coal.itemID, 32, 64},
		{Item.legsDiamond.itemID, 1, 1},
		{Item.helmetDiamond.itemID, 1, 1},
		{Item.bootsDiamond.itemID, 1, 1},
		{Item.plateDiamond.itemID, 1, 1},
		{Item.eyeOfEnder.itemID, 4, 8},
		{Block.enchantmentTable.blockID, 1, 1},
		{Block.cobblestoneMossy.blockID, 32, 64},
		{Item.diamond.itemID, 8, 16},
		{Block.jukebox.blockID, 1, 1},
		{Block.blockDiamond.blockID, 1, 2},
		{Block.blockGold.blockID, 1, 4},
		{Block.blockIron.blockID, 1, 8},
		{Block.obsidian.blockID, 4, 8},
		{Item.emerald.itemID, 4, 6}
		};

	/** 2D array containing the item IDs of items that a farmer may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] farmerAidIDs =
		{
		{Item.wheat.itemID, 1, 4},
		{Item.appleRed.itemID, 1, 3},
		{Item.seeds.itemID, 3, 12},
		{Item.reed.itemID, 3, 6},
		{Item.carrot.itemID, 3, 6},
		{Item.potato.itemID, 2, 4},
		};

	/** 2D array containing the item IDs of items that a butcher may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] butcherAidIDs =
		{
		{Item.beefRaw.itemID, 1, 3},
		{Item.porkRaw.itemID, 1, 3},
		{Item.chickenRaw.itemID, 1, 3},
		{Item.leather.itemID, 2, 6},
		{Item.feather.itemID, 6, 12},
		};

	/** 2D array containing the item IDs of items that a baker may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] bakerAidIDs =
		{
		{Item.bread.itemID, 1, 4},
		{Item.cake.itemID, 1, 1},
		};

	/**
	 * Map that contains string translations loaded from language files.
	 * Key will be the ID of the phrase.
	 * Value will be the translated representation of the phrase.
	 */
	public static Map<String, String> stringTranslations = new HashMap();

	/**
	 * Gets the appropriate skin list for the entity provided.
	 * 
	 * @param 	entity	The entity that needs a list of valid skins.
	 * 
	 * @return	A list of skins that are valid for the provided entity.
	 */
	public static List<String> getSkinList(AbstractEntity entity)
	{
		if (!(entity instanceof EntityPlayerChild))
		{
			if (entity.gender.equals("Male"))
			{
				switch (entity.profession)
				{
				case 0: return MCA.farmerSkinsMale;
				case 1: return MCA.librarianSkinsMale;
				case 2: return MCA.priestSkinsMale;
				case 3: return MCA.smithSkinsMale;
				case 4: return MCA.butcherSkinsMale;
				case 5: return MCA.guardSkinsMale;
				case 6: return MCA.bakerSkinsMale;
				case 7: return MCA.minerSkinsMale;
				}
			}

			else
			{
				switch (entity.profession)
				{
				case 0: return MCA.farmerSkinsFemale;
				case 1: return MCA.librarianSkinsFemale;
				case 2: return MCA.priestSkinsFemale;
				case 3: return MCA.smithSkinsFemale;
				case 4: return null;
				case 5: return MCA.guardSkinsFemale;
				case 6: return MCA.bakerSkinsFemale;
				case 7: return MCA.minerSkinsFemale;
				}
			}
		}

		else
		{
			if (entity.gender.equals("Male"))
			{
				return MCA.kidSkinsMale;
			}

			else if (entity.gender.equals("Female"))
			{
				return MCA.kidSkinsFemale;
			}
		}

		return null;
	}

	/**
	 * Deflates a byte array.
	 * 
	 * @param 	input	The byte array to be deflated.
	 * 
	 * @return	Deflated byte array.
	 */
	public static byte[] compressBytes(byte[] input)
	{
		try
		{
			Deflater deflater = new Deflater();
			deflater.setLevel(Deflater.BEST_COMPRESSION);
			deflater.setInput(input);

			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(input.length);
			deflater.finish();

			byte[] buffer = new byte[1024];

			while(!deflater.finished())
			{
				int count = deflater.deflate(buffer);
				byteOutput.write(buffer, 0, count);
			}

			byteOutput.close();
			return byteOutput.toByteArray();
		}

		catch (Throwable e)
		{
			MCA.instance.quitWithError("Error compressing byte array.", e);
			return null;
		}
	}

	/**
	 * Inflates a deflated byte array.
	 * 
	 * @param 	input	The byte array to be deflated.
	 * 
	 * @return	Decompressed byte array.
	 */
	public static byte[] decompressBytes(byte[] input)
	{
		try
		{
			Inflater inflater = new Inflater();
			inflater.setInput(input);

			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(input.length);

			byte[] buffer = new byte[1024];

			while(!inflater.finished())
			{
				int count = inflater.inflate(buffer);
				byteOutput.write(buffer, 0, count);
			}

			byteOutput.close();
			return byteOutput.toByteArray();
		}

		catch (Throwable e)
		{
			MCA.instance.quitWithError("Error decompressing byte array.", e);
			return null;
		}
	}

	/**
	 * Provides an MD5 hash based on input
	 * 
	 * @param 	input	String of data that MD5 hash will be generated for.
	 * 
	 * @return	MD5 hash of the provided input in string format.
	 */
	public static String getMD5Hash(String input)
	{
		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(input.getBytes());

			byte[] hash = md5.digest();
			StringBuffer buffer = new StringBuffer();

			for (byte b : hash) 
			{
				buffer.append(Integer.toHexString(b & 0xff));
			}

			return buffer.toString();
		}

		catch (Throwable e)
		{
			return "UNABLE TO PROCESS";
		}
	}

	/**
	 * Deletes a path and all files and folders within.
	 * 
	 * @param 	file	The path to delete.
	 */
	public static void deletePath(File file)
	{
		if (file.isDirectory())
		{
			if (file.list().length == 0)
			{
				file.delete();
			}

			else
			{
				String files[] = file.list();

				for (String temp : files)
				{
					File fileDelete = new File(file, temp);
					deletePath(fileDelete);
				}

				if (file.list().length == 0)
				{
					file.delete();
				}
			}
		}

		else
		{
			file.delete();
		}
	}

	/**
	 * Tests all valid lines within the source for errors when requested from the language system.
	 */
	@Deprecated
	public static void testLanguage()
	{
		AbstractEntity dummyEntity = new EntityVillagerAdult();
		String sourceDir = "D:/Programming/Minecraft Comes Alive/Development/src/minecraft/mca/";
		FileInputStream fileStream;
		DataInputStream in;
		BufferedReader br;
		int lineNumber = 0;
		System.out.println(sourceDir);

		for (File file : new File(sourceDir).listFiles())
		{
			lineNumber = 0;
			String readString = "";

			try
			{
				fileStream = new FileInputStream(file);
				in = new DataInputStream(fileStream);
				br = new BufferedReader(new InputStreamReader(in));

				while ((readString = br.readLine()) != null)  
				{
					lineNumber++;

					if (readString.contains("Localization.getString(") && readString.contains("static") == false)
					{
						readString = readString.trim();

						boolean useCharacterType = readString.contains("true");
						int firstQuoteIndex = readString.indexOf('"');
						int nextQuoteIndex = readString.indexOf('"', firstQuoteIndex + 1);

						String validString = readString.substring(firstQuoteIndex, nextQuoteIndex).replaceAll("\"", "");
						String result = LanguageHelper.getString(null, dummyEntity, validString, useCharacterType);

						if (result.contains("not found") || result.contains("(Parsing error)"))
						{
							System.out.println(result + " in " + file.getName() + " line " + lineNumber);
						}

						//System.out.println("Result of: " + validString + " in " + file.getName() + " = \t\t\t" + getString(null, validString));
					}
				}
				
				br.close();
			}

			catch (Throwable e)
			{
				System.out.println("Error on " + readString);
				continue;
			}
		}
	}

	/**
	 * Gets the appropriate farm creation map for the area and seed type provided.
	 * 
	 * @param 	areaX		The X size of the area to farm. Used to identify the correct sized area.
	 * @param 	seedType 	The type of seeds that should be planted. 0 = Wheat, 1 = Melon, 2 = Pumpkin, 3 = Carrot, 4 = Potato, 5 = sugarcane.

	 * @return	The appropriate farm creation map.
	 */
	public static String[] getFarmMap(int areaX, int seedType)
	{
		if (seedType == 0 || seedType == 3 || seedType == 4)
		{
			switch (areaX)
			{
			case 5: return normalFarmFiveByFive;
			case 10: return normalFarmTenByTen;
			case 15: return normalFarmFifteenByFifteen;
			}
		}

		else if (seedType == 1 || seedType == 2)
		{
			return blockFarmFiveByFive;
		}

		else if (seedType == 5)
		{
			switch (areaX)
			{
			case 5: return sugarcaneFarmFiveByFive;
			case 10: return sugarcaneFarmTenByTen;
			case 15: return sugarcaneFarmFifteenByFifteen;
			}
		}

		return null;
	}

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
		proxy.loadSkins();
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
		achievementNameHeir = new Achievement(1563, "MCA_NameHeir", 10, 10, itemHeirCrown, achievementCraftCrown).registerAchievement();
		achievementMonarchSecret = new Achievement(1564, "MCA_MonarchSecret", 7, 5, Item.writableBook, achievementCraftCrown).setSpecial().registerAchievement();

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
				achievementMonarchSecret);
		AchievementPage.registerAchievementPage(achievementPageMCA);

		LanguageHelper.loadLanguage();
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
	 * Logs information about the provided packet.
	 * 
	 * @param 	obj	The object to write to System.out.
	 */
	public void logPacketInformation(Object obj)
	{
		if (inDebugMode && debugDoLogPackets)
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

		logger.log(Level.FINER, "Minecraft Comes Alive: An exception occurred.\n>>>>>" + description + "<<<<<\n" + stackTrace.toString());
		System.out.println("Minecraft Comes Alive: An exception occurred.\n>>>>>" + description + "<<<<<\n" + stackTrace.toString());

		CrashReport crashReport = new CrashReport("MCA: " + description, e);
		
		Minecraft.getMinecraft().crashed(crashReport);
		Minecraft.getMinecraft().displayCrashReport(crashReport);
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
		event.registerServerCommand(new CommandCheckUpdates());
		event.registerServerCommand(new CommandDebugRule());
		
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

	static
	{
		acceptableGifts.put(Item.swordWood.itemID, 3);
		acceptableGifts.put(Item.axeWood.itemID, 3);
		acceptableGifts.put(Item.hoeWood.itemID, 3);
		acceptableGifts.put(Item.shovelWood.itemID, 3);
		acceptableGifts.put(Item.swordStone.itemID, 5);
		acceptableGifts.put(Item.axeStone.itemID, 5);
		acceptableGifts.put(Item.hoeStone.itemID, 5);
		acceptableGifts.put(Item.shovelStone.itemID, 5);
		acceptableGifts.put(Item.pickaxeWood.itemID, 3);
		acceptableGifts.put(Item.beefRaw.itemID, 2);
		acceptableGifts.put(Item.chickenRaw.itemID, 2);
		acceptableGifts.put(Item.porkRaw.itemID, 2);
		acceptableGifts.put(Item.leather.itemID, 2);
		acceptableGifts.put(Item.plateLeather.itemID, 5);
		acceptableGifts.put(Item.helmetLeather.itemID, 5);
		acceptableGifts.put(Item.legsLeather.itemID, 5);
		acceptableGifts.put(Item.bootsLeather.itemID, 5);
		acceptableGifts.put(Item.reed.itemID, 2);
		acceptableGifts.put(Item.seeds.itemID, 2);
		acceptableGifts.put(Item.wheat.itemID, 3);
		acceptableGifts.put(Item.bread.itemID, 6);
		acceptableGifts.put(Item.coal.itemID, 5);
		acceptableGifts.put(Item.sugar.itemID, 5);
		acceptableGifts.put(Item.clay.itemID, 2);
		acceptableGifts.put(Item.dyePowder.itemID, 1);

		acceptableGifts.put(Item.beefCooked.itemID, 7);
		acceptableGifts.put(Item.chickenCooked.itemID, 7);
		acceptableGifts.put(Item.porkCooked.itemID, 7);
		acceptableGifts.put(Item.cookie.itemID, 10);
		acceptableGifts.put(Item.melon.itemID, 10);
		acceptableGifts.put(Item.melonSeeds.itemID, 5);
		acceptableGifts.put(Item.helmetIron.itemID, 10);
		acceptableGifts.put(Item.plateIron.itemID, 10);
		acceptableGifts.put(Item.legsIron.itemID, 10);
		acceptableGifts.put(Item.bootsIron.itemID, 10);
		acceptableGifts.put(Item.cake.itemID, 12);
		acceptableGifts.put(Item.swordIron.itemID, 10);
		acceptableGifts.put(Item.axeIron.itemID, 10);
		acceptableGifts.put(Item.hoeIron.itemID, 10);
		acceptableGifts.put(Item.pickaxeIron.itemID, 10);
		acceptableGifts.put(Item.shovelIron.itemID, 10);
		acceptableGifts.put(Item.fishingRod.itemID, 3);
		acceptableGifts.put(Item.bow.itemID, 5);
		acceptableGifts.put(Item.book.itemID, 5);
		acceptableGifts.put(Item.bucketEmpty.itemID, 3);
		acceptableGifts.put(Item.bucketMilk.itemID, 5);
		acceptableGifts.put(Item.bucketWater.itemID, 2);
		acceptableGifts.put(Item.bucketLava.itemID, 2);
		acceptableGifts.put(Item.bowlSoup.itemID, 5);
		acceptableGifts.put(Item.pumpkinSeeds.itemID, 8);
		acceptableGifts.put(Item.flintAndSteel.itemID, 4);
		acceptableGifts.put(Item.redstone.itemID, 5);
		acceptableGifts.put(Item.boat.itemID, 4);
		acceptableGifts.put(Item.doorWood.itemID, 4);
		acceptableGifts.put(Item.doorIron.itemID, 6);
		acceptableGifts.put(Item.minecartEmpty.itemID, 3);
		acceptableGifts.put(Item.minecartCrate.itemID, 5);
		acceptableGifts.put(Item.minecartPowered.itemID, 7);
		acceptableGifts.put(Item.flint.itemID, 2);
		acceptableGifts.put(Item.goldNugget.itemID, 4);
		acceptableGifts.put(Item.ingotGold.itemID, 20);
		acceptableGifts.put(Item.ingotIron.itemID, 10);

		acceptableGifts.put(Item.diamond.itemID, 30);
		acceptableGifts.put(Item.map.itemID, 10);
		acceptableGifts.put(Item.pocketSundial.itemID, 5);
		acceptableGifts.put(Item.compass.itemID, 5);
		acceptableGifts.put(Item.blazeRod.itemID, 10);
		acceptableGifts.put(Item.blazePowder.itemID, 5);
		acceptableGifts.put(Item.swordDiamond.itemID, 15);
		acceptableGifts.put(Item.axeDiamond.itemID, 15);
		acceptableGifts.put(Item.shovelDiamond.itemID, 15);
		acceptableGifts.put(Item.hoeDiamond.itemID, 15);
		acceptableGifts.put(Item.pickaxeDiamond.itemID, 15);
		acceptableGifts.put(Item.helmetDiamond.itemID, 15);
		acceptableGifts.put(Item.plateDiamond.itemID, 15);
		acceptableGifts.put(Item.legsDiamond.itemID, 15);
		acceptableGifts.put(Item.bootsDiamond.itemID, 15);
		acceptableGifts.put(Item.painting.itemID, 6);
		acceptableGifts.put(Item.enderPearl.itemID, 5);
		acceptableGifts.put(Item.eyeOfEnder.itemID, 10);
		acceptableGifts.put(Item.potion.itemID, 3);
		acceptableGifts.put(Item.slimeBall.itemID, 3);
		acceptableGifts.put(Item.saddle.itemID, 5);
		acceptableGifts.put(Item.gunpowder.itemID, 7);
		acceptableGifts.put(Item.appleGold.itemID, 25);
		acceptableGifts.put(Item.record11.itemID, 15);
		acceptableGifts.put(Item.record13.itemID, 15);
		acceptableGifts.put(Item.recordBlocks.itemID, 15);
		acceptableGifts.put(Item.recordCat.itemID, 15);
		acceptableGifts.put(Item.recordChirp.itemID, 15);
		acceptableGifts.put(Item.recordFar.itemID, 15);
		acceptableGifts.put(Item.recordMall.itemID, 15);
		acceptableGifts.put(Item.recordMellohi.itemID, 15);
		acceptableGifts.put(Item.recordStal.itemID, 15);
		acceptableGifts.put(Item.recordStrad.itemID, 15);
		acceptableGifts.put(Item.recordWard.itemID, 15);
		acceptableGifts.put(Item.emerald.itemID, 25);

		acceptableGifts.put(Block.plantRed.blockID, 3);
		acceptableGifts.put(Block.plantYellow.blockID, 3);
		acceptableGifts.put(Block.planks.blockID, 5);
		acceptableGifts.put(Block.wood.blockID, 3);

		acceptableGifts.put(Block.pumpkin.blockID, 3);
		acceptableGifts.put(Block.chest.blockID, 5);
		acceptableGifts.put(Block.cloth.blockID, 2);
		acceptableGifts.put(Block.oreIron.blockID, 4);
		acceptableGifts.put(Block.oreGold.blockID, 7);
		acceptableGifts.put(Block.oreRedstone.blockID, 3);
		acceptableGifts.put(Block.rail.blockID, 3);
		acceptableGifts.put(Block.railDetector.blockID, 5);
		acceptableGifts.put(Block.railPowered.blockID, 5);
		acceptableGifts.put(Block.furnaceIdle.blockID, 5);
		acceptableGifts.put(Block.workbench.blockID, 5);
		acceptableGifts.put(Block.blockLapis.blockID, 15);

		acceptableGifts.put(Block.bookShelf.blockID, 7);
		acceptableGifts.put(Block.blockGold.blockID, 50);
		acceptableGifts.put(Block.blockIron.blockID, 25);
		acceptableGifts.put(Block.blockDiamond.blockID, 100);
		acceptableGifts.put(Block.brewingStand.blockID, 12);
		acceptableGifts.put(Block.enchantmentTable.blockID, 25);
		acceptableGifts.put(Block.brick.blockID, 15);
		acceptableGifts.put(Block.obsidian.blockID, 15);
		acceptableGifts.put(Block.pistonBase.blockID, 10);
		acceptableGifts.put(Block.glowStone.blockID, 10);

		acceptableGifts.put(Block.blockEmerald.blockID, 100);
	}
}
