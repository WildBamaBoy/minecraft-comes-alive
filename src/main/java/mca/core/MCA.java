package mca.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mca.api.CookableFood;
import mca.api.CropEntry;
import mca.api.RegistryMCA;
import mca.api.WeddingGift;
import mca.api.WoodcuttingEntry;
import mca.api.enums.EnumCropCategory;
import mca.api.enums.EnumGiftCategory;
import mca.command.CommandMCA;
import mca.core.forge.EventHooksFML;
import mca.core.forge.EventHooksForge;
import mca.core.forge.GuiHandler;
import mca.core.forge.ServerProxy;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModBlocks;
import mca.core.minecraft.ModItems;
import mca.core.radix.CrashWatcher;
import mca.core.radix.LanguageParser;
import mca.data.PlayerData;
import mca.entity.EntityHuman;
import mca.enums.EnumCut;
import mca.enums.EnumProfession;
import mca.network.MCAPacketHandler;
import mca.tile.TileTombstone;
import mca.tile.TileVillagerBed;
import mca.util.SkinLoader;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;
import radixcore.data.AbstractPlayerData;
import radixcore.data.DataContainer;
import radixcore.forge.gen.SimpleOreGenerator;
import radixcore.lang.LanguageManager;
import radixcore.math.Point3D;
import radixcore.update.RDXUpdateProtocol;
import radixcore.util.RadixLogic;
import radixcore.util.RadixStartup;

@Mod(modid = MCA.ID, name = MCA.NAME, version = MCA.VERSION, dependencies = "required-after:RadixCore@[2.0.2,)", acceptedMinecraftVersions = "[1.7.10]",
guiFactory = "mca.core.forge.client.MCAGuiFactory")
public class MCA
{
	public static final String ID = "MCA";
	public static final String NAME = "Minecraft Comes Alive";
	public static final String VERSION = "@VERSION@";

	@Instance(ID)
	private static MCA instance;
	private static ModMetadata metadata;
	private static ModItems items;
	private static ModBlocks blocks;
	private static ModAchievements achievements;
	private static CreativeTabs creativeTabMain;
	private static CreativeTabs creativeTabGemCutting;
	private static Config clientConfig;
	private static Config config;
	private static LanguageManager languageManager;
	private static MCAPacketHandler packetHandler;
	private static CrashWatcher crashWatcher;

	private static Logger logger;

	@SidedProxy(clientSide = "mca.core.forge.ClientProxy", serverSide = "mca.core.forge.ServerProxy")
	public static ServerProxy proxy;

	public static Map<String, AbstractPlayerData> playerDataMap;

	@SideOnly(Side.CLIENT)
	public static DataContainer playerDataContainer;
	@SideOnly(Side.CLIENT)
	public static Point3D destinyCenterPoint;
	@SideOnly(Side.CLIENT)
	public static boolean destinySpawnFlag;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{	
		instance = this;
		metadata = event.getModMetadata();
		logger = event.getModLog();
		config = new Config(event);
		clientConfig = config;
		languageManager = new LanguageManager(ID, new LanguageParser());
		crashWatcher = new CrashWatcher();
		packetHandler = new MCAPacketHandler(ID);
		proxy.registerRenderers();
		proxy.registerEventHandlers();
		playerDataMap = new HashMap<String, AbstractPlayerData>();

		ModMetadataEx exData = ModMetadataEx.getFromModMetadata(metadata);
		exData.updateProtocolClass = config.allowUpdateChecking ? RDXUpdateProtocol.class : null;
		exData.classContainingClientDataContainer = MCA.class;
		exData.classContainingGetPlayerDataMethod = MCA.class;
		exData.playerDataMap = playerDataMap;

		RadixCore.registerMod(exData);

		if (exData.updateProtocolClass == null)
		{
			logger.fatal("Config: Update checking is turned off. You will not be notified of any available updates for MCA.");
		}

		FMLCommonHandler.instance().bus().register(new EventHooksFML());
		MinecraftForge.EVENT_BUS.register(new EventHooksForge());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		creativeTabMain = RadixStartup.registerCreativeTab(ModItems.class, "engagementRing", metadata, null);
		creativeTabGemCutting = RadixStartup.registerCreativeTab(ModItems.class, "diamondHeart", metadata, "gemCutting");
		items = new ModItems();
		blocks = new ModBlocks();
		achievements = new ModAchievements();

		SkinLoader.loadSkins();

		//Entity registry
		EntityRegistry.registerModEntity(EntityHuman.class, EntityHuman.class.getSimpleName(), config.baseEntityId, this, 50, 2, true);

		//Tile registry
		GameRegistry.registerTileEntity(TileVillagerBed.class, TileVillagerBed.class.getSimpleName());
		GameRegistry.registerTileEntity(TileTombstone.class, TileTombstone.class.getSimpleName());

		//Recipes
		GameRegistry.addRecipe(new ItemStack(ModItems.divorcePapers, 1), 
				new Object[] { " IF", " P ", 'I', new ItemStack(Items.dye, 1, 0), 'F', Items.feather, 'P', Items.paper });

		GameRegistry.addRecipe(new ItemStack(ModItems.whistle), 
				" W#", "###", '#', Items.iron_ingot, 'W', Blocks.planks);
		GameRegistry.addRecipe(new ItemStack(Items.gold_ingot), 
				"GGG", "GGG", "GGG", 'G', ModItems.goldDust);
		GameRegistry.addRecipe(new ItemStack(ModItems.roseGoldIngot, 9), 
				"GGG", "GGG", "GGG", 'G', ModItems.roseGoldDust);
		GameRegistry.addRecipe(new ItemStack(ModItems.engagementRing), 
				"GDG", "G G", "GGG", 'D', Items.diamond, 'G', Items.gold_ingot);
		GameRegistry.addRecipe(new ItemStack(ModItems.engagementRingRG), 
				"GDG", "G G", "GGG", 'D', Items.diamond, 'G', ModItems.roseGoldIngot);
		GameRegistry.addRecipe(new ItemStack(ModItems.weddingRingRG),
				"GGG", "G G", "GGG", 'G', ModItems.roseGoldIngot);
		GameRegistry.addRecipe(new ItemStack(ModBlocks.roseGoldBlock),
				"GGG", "GGG", "GGG", 'G', ModItems.roseGoldIngot);
		GameRegistry.addRecipe(new ItemStack(ModItems.matchmakersRing),
				"III", "I I", "III", 'I', Items.iron_ingot);
		GameRegistry.addRecipe(new ItemStack(ModItems.tombstone),
				" S ", "SIS", "SSS", 'S', Blocks.stone, 'I', Items.sign);
		GameRegistry.addRecipe(new ItemStack(ModItems.gemCutter),
				"  G", "IG ", "DI ", 'G', Items.gold_ingot, 'I', Items.iron_ingot, 'D', Items.diamond);
		GameRegistry.addRecipe(new ItemStack(ModItems.heartMold),
				"CCC", "C C", " C ", 'C', Items.clay_ball);
		GameRegistry.addRecipe(new ItemStack(ModItems.tinyMold),
				" C ", "C C", " C ", 'C', Items.clay_ball);
		GameRegistry.addRecipe(new ItemStack(ModItems.ovalMold),
				"CCC", "   ", "CCC", 'C', Items.clay_ball);    	
		GameRegistry.addRecipe(new ItemStack(ModItems.squareMold),
				"CCC", "C C", "CCC", 'C', Items.clay_ball);    	
		GameRegistry.addRecipe(new ItemStack(ModItems.triangleMold),
				" C ", "C C", "CCC", 'C', Items.clay_ball);    	
		GameRegistry.addRecipe(new ItemStack(ModItems.starMold),
				" C ", "CCC", " C ", 'C', Items.clay_ball);
		GameRegistry.addRecipe(new ItemStack(ModItems.needle),
				"I  ", " I ", "  I", 'I', new ItemStack(Items.iron_ingot));
		GameRegistry.addRecipe(new ItemStack(ModItems.newOutfit),
				"C C", "CCC", "CCC", 'C', ModItems.cloth);
		
		//Variable recipes
		if (!config.disableWeddingRingRecipe)
		{
			GameRegistry.addRecipe(new ItemStack(ModItems.weddingRing),
					"GGG", "G G", "GGG", 'G', Items.gold_ingot);
		}

		else
		{
			logger.fatal("Config: MCA's default wedding ring recipe is currently disabled. You can change this in the config. You must use Rose Gold to craft wedding rings!");
		}

		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.diamondDust), Items.diamond);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.roseGoldDust), ModItems.roseGoldIngot);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.goldDust, 6), Items.water_bucket, new ItemStack(ModItems.roseGoldDust));

		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedRed), new ItemStack(Items.bed), new ItemStack(Blocks.carpet, 1, 14));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedBlue), new ItemStack(Items.bed), new ItemStack(Blocks.carpet, 1, 11));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedGreen), new ItemStack(Items.bed), new ItemStack(Blocks.carpet, 1, 13));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedPurple), new ItemStack(Items.bed), new ItemStack(Blocks.carpet, 1, 10));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedPink), new ItemStack(Items.bed), new ItemStack(Blocks.carpet, 1, 6));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.bed), new ItemStack(ModItems.bedRed));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.bed), new ItemStack(ModItems.bedBlue));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.bed), new ItemStack(ModItems.bedGreen));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.bed), new ItemStack(ModItems.bedPurple));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.bed), new ItemStack(ModItems.bedPink));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.needleAndString), new ItemStack(ModItems.needle), new ItemStack(Items.string));

		for(int i = 0; i < 16; i++)
		{
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.cloth), new ItemStack(Blocks.wool), new ItemStack(ModItems.needleAndString, 1, i));
		}		

		//Colored diamond recipes.
		for (int i = 0; i < 16; ++i)
		{
			ItemStack coloredDiamond =  new ItemStack(ModItems.coloredDiamond, 1, i);
			ItemStack coloredDiamondDust = new ItemStack(ModItems.coloredDiamondDust, 1, i);
			ItemStack engagementRing = new ItemStack(ModItems.coloredEngagementRing, 1, i);
			ItemStack engagementRingRG = new ItemStack(ModItems.coloredEngagementRingRG, 1, i);
			ItemStack dye = new ItemStack(Items.dye, 1, i);

			GameRegistry.addShapelessRecipe(coloredDiamondDust, dye, new ItemStack(ModItems.diamondDust));

			GameRegistry.addRecipe(engagementRing, 
					"GDG", "G G", "GGG", 'D', coloredDiamond, 'G', Items.gold_ingot);
			GameRegistry.addRecipe(engagementRingRG, 
					"GDG", "G G", "GGG", 'D', coloredDiamond, 'G', ModItems.roseGoldIngot);

			GameRegistry.addSmelting(coloredDiamondDust, coloredDiamond, 5.0F);

			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.coloredDiamondHeart, 1, i), new ItemStack(ModItems.heartMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), coloredDiamond);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.coloredDiamondOval, 1, i), new ItemStack(ModItems.ovalMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), coloredDiamond);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.coloredDiamondSquare, 1, i), new ItemStack(ModItems.squareMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), coloredDiamond);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.coloredDiamondTriangle, 1, i), new ItemStack(ModItems.triangleMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), coloredDiamond);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.coloredDiamondStar, 1, i), new ItemStack(ModItems.starMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), coloredDiamond);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.coloredDiamondTiny, 1, i), new ItemStack(ModItems.tinyMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), coloredDiamond);
		}

		//Cut diamond recipes
		for (EnumCut cut : EnumCut.values())
		{
			Item cutItem = null;
			Item ringItem = null;
			Item ringRGItem = null;

			Item cutItemC = null;
			Item ringItemC = null;
			Item ringRGItemC = null;

			switch (cut)
			{
			case HEART:
				cutItem = ModItems.diamondHeart;
				ringItem = ModItems.engagementRingHeart;
				ringRGItem = ModItems.engagementRingHeartRG;
				cutItemC = ModItems.coloredDiamondHeart; 
				ringItemC = ModItems.ringHeartColored;
				ringRGItemC = ModItems.ringHeartColoredRG;
				break;
			case OVAL: 
				cutItem = ModItems.diamondOval;
				ringItem = ModItems.engagementRingOval;
				ringRGItem = ModItems.engagementRingOvalRG;
				cutItemC = ModItems.coloredDiamondOval; 
				ringItemC = ModItems.ringOvalColored;
				ringRGItemC = ModItems.ringOvalColoredRG;
				break;
			case SQUARE: 
				cutItem = ModItems.diamondSquare;
				ringItem = ModItems.engagementRingSquare;
				ringRGItem = ModItems.engagementRingSquareRG;
				cutItemC = ModItems.coloredDiamondSquare; 
				ringItemC = ModItems.ringSquareColored;
				ringRGItemC = ModItems.ringSquareColoredRG;
				break;
			case STAR: 
				cutItem = ModItems.diamondStar;
				ringItem = ModItems.engagementRingStar;
				ringRGItem = ModItems.engagementRingStarRG;
				cutItemC = ModItems.coloredDiamondStar; 
				ringItemC = ModItems.ringStarColored;
				ringRGItemC = ModItems.ringStarColoredRG;
				break;
			case TINY: 
				cutItem = ModItems.diamondTiny;
				ringItem = ModItems.engagementRingTiny;
				ringRGItem = ModItems.engagementRingTinyRG;
				cutItemC = ModItems.coloredDiamondTiny; 
				ringItemC = ModItems.ringTinyColored;
				ringRGItemC = ModItems.ringTinyColoredRG;
				break;
			case TRIANGLE: 
				cutItem = ModItems.diamondTriangle;
				ringItem = ModItems.engagementRingTriangle;
				ringRGItem = ModItems.engagementRingTriangleRG;
				cutItemC = ModItems.coloredDiamondTriangle; 
				ringItemC = ModItems.ringTriangleColored;
				ringRGItemC = ModItems.ringTriangleColoredRG;
				break;
			default:
				continue;
			}

			//Base recipes
			ItemStack baseStack = new ItemStack(cutItem, 1);

			GameRegistry.addRecipe(new ItemStack(ringItem, 1), 
					"GDG", "G G", "GGG", 'D', baseStack, 'G', Items.gold_ingot);
			GameRegistry.addRecipe(new ItemStack(ringRGItem, 1), 
					"GDG", "G G", "GGG", 'D', baseStack, 'G', ModItems.roseGoldIngot);

			//All colors
			for (int i = 0; i < 16; ++i)
			{
				ItemStack coloredStack = new ItemStack(cutItemC, 1, i);

				GameRegistry.addRecipe(new ItemStack(ringItemC, 1, i), 
						"GDG", "G G", "GGG", 'D', coloredStack, 'G', Items.gold_ingot);
				GameRegistry.addRecipe(new ItemStack(ringRGItemC, 1, i), 
						"GDG", "G G", "GGG", 'D', coloredStack, 'G', ModItems.roseGoldIngot);
			}
		}

		//Smeltings
		GameRegistry.addSmelting(ModBlocks.roseGoldOre, new ItemStack(ModItems.roseGoldIngot), 5.0F);
		GameRegistry.addSmelting(ModItems.diamondDust, new ItemStack(Items.diamond), 5.0F);

		if (MCA.config.roseGoldSpawnWeight > 0)
		{
			SimpleOreGenerator.register(new SimpleOreGenerator(ModBlocks.roseGoldOre, 6, 12, 40, true, false), MCA.config.roseGoldSpawnWeight);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		RegistryMCA.addObjectAsGift(Items.wooden_sword, 3);
		RegistryMCA.addObjectAsGift(Items.wooden_axe, 3);
		RegistryMCA.addObjectAsGift(Items.wooden_hoe, 3);
		RegistryMCA.addObjectAsGift(Items.wooden_shovel, 3);
		RegistryMCA.addObjectAsGift(Items.stone_sword, 5);
		RegistryMCA.addObjectAsGift(Items.stone_axe, 5);
		RegistryMCA.addObjectAsGift(Items.stone_hoe, 5);
		RegistryMCA.addObjectAsGift(Items.stone_shovel, 5);
		RegistryMCA.addObjectAsGift(Items.wooden_pickaxe, 3);
		RegistryMCA.addObjectAsGift(Items.beef, 2);
		RegistryMCA.addObjectAsGift(Items.chicken, 2);
		RegistryMCA.addObjectAsGift(Items.porkchop, 2);
		RegistryMCA.addObjectAsGift(Items.leather, 2);
		RegistryMCA.addObjectAsGift(Items.leather_chestplate, 5);
		RegistryMCA.addObjectAsGift(Items.leather_helmet, 5);
		RegistryMCA.addObjectAsGift(Items.leather_leggings, 5);
		RegistryMCA.addObjectAsGift(Items.leather_boots, 5);
		RegistryMCA.addObjectAsGift(Items.reeds, 2);
		RegistryMCA.addObjectAsGift(Items.wheat_seeds, 2);
		RegistryMCA.addObjectAsGift(Items.wheat, 3);
		RegistryMCA.addObjectAsGift(Items.bread, 6);
		RegistryMCA.addObjectAsGift(Items.coal, 5);
		RegistryMCA.addObjectAsGift(Items.sugar, 5);
		RegistryMCA.addObjectAsGift(Items.clay_ball, 2);
		RegistryMCA.addObjectAsGift(Items.dye, 1);
		RegistryMCA.addObjectAsGift(Items.cooked_beef, 7);
		RegistryMCA.addObjectAsGift(Items.cooked_chicken, 7);
		RegistryMCA.addObjectAsGift(Items.cooked_porkchop, 7);
		RegistryMCA.addObjectAsGift(Items.cookie, 10);
		RegistryMCA.addObjectAsGift(Items.melon, 10);
		RegistryMCA.addObjectAsGift(Items.melon_seeds, 5);
		RegistryMCA.addObjectAsGift(Items.iron_helmet, 10);
		RegistryMCA.addObjectAsGift(Items.iron_chestplate, 10);
		RegistryMCA.addObjectAsGift(Items.iron_leggings, 10);
		RegistryMCA.addObjectAsGift(Items.iron_boots, 10);
		RegistryMCA.addObjectAsGift(Items.cake, 12);
		RegistryMCA.addObjectAsGift(Items.iron_sword, 10);
		RegistryMCA.addObjectAsGift(Items.iron_axe, 10);
		RegistryMCA.addObjectAsGift(Items.iron_hoe, 10);
		RegistryMCA.addObjectAsGift(Items.iron_pickaxe, 10);
		RegistryMCA.addObjectAsGift(Items.iron_shovel, 10);
		RegistryMCA.addObjectAsGift(Items.fishing_rod, 3);
		RegistryMCA.addObjectAsGift(Items.bow, 5);
		RegistryMCA.addObjectAsGift(Items.book, 5);
		RegistryMCA.addObjectAsGift(Items.bucket, 3);
		RegistryMCA.addObjectAsGift(Items.milk_bucket, 5);
		RegistryMCA.addObjectAsGift(Items.water_bucket, 2);
		RegistryMCA.addObjectAsGift(Items.lava_bucket, 2);
		RegistryMCA.addObjectAsGift(Items.mushroom_stew, 5);
		RegistryMCA.addObjectAsGift(Items.pumpkin_seeds, 8);
		RegistryMCA.addObjectAsGift(Items.flint_and_steel, 4);
		RegistryMCA.addObjectAsGift(Items.redstone, 5);
		RegistryMCA.addObjectAsGift(Items.boat, 4);
		RegistryMCA.addObjectAsGift(Items.wooden_door, 4);
		RegistryMCA.addObjectAsGift(Items.iron_door, 6);
		RegistryMCA.addObjectAsGift(Items.minecart, 7);
		RegistryMCA.addObjectAsGift(Items.flint, 2);
		RegistryMCA.addObjectAsGift(Items.gold_nugget, 4);
		RegistryMCA.addObjectAsGift(Items.gold_ingot, 20);
		RegistryMCA.addObjectAsGift(Items.iron_ingot, 10);
		RegistryMCA.addObjectAsGift(Items.diamond, 30);
		RegistryMCA.addObjectAsGift(Items.map, 10);
		RegistryMCA.addObjectAsGift(Items.clock, 5);
		RegistryMCA.addObjectAsGift(Items.compass, 5);
		RegistryMCA.addObjectAsGift(Items.blaze_rod, 10);
		RegistryMCA.addObjectAsGift(Items.blaze_powder, 5);
		RegistryMCA.addObjectAsGift(Items.diamond_sword, 15);
		RegistryMCA.addObjectAsGift(Items.diamond_axe, 15);
		RegistryMCA.addObjectAsGift(Items.diamond_shovel, 15);
		RegistryMCA.addObjectAsGift(Items.diamond_hoe, 15);
		RegistryMCA.addObjectAsGift(Items.diamond_leggings, 15);
		RegistryMCA.addObjectAsGift(Items.diamond_helmet, 15);
		RegistryMCA.addObjectAsGift(Items.diamond_chestplate, 15);
		RegistryMCA.addObjectAsGift(Items.diamond_leggings, 15);
		RegistryMCA.addObjectAsGift(Items.diamond_boots, 15);
		RegistryMCA.addObjectAsGift(Items.painting, 6);
		RegistryMCA.addObjectAsGift(Items.ender_pearl, 5);
		RegistryMCA.addObjectAsGift(Items.ender_eye, 10);
		RegistryMCA.addObjectAsGift(Items.potionitem, 3);
		RegistryMCA.addObjectAsGift(Items.slime_ball, 3);
		RegistryMCA.addObjectAsGift(Items.saddle, 5);
		RegistryMCA.addObjectAsGift(Items.gunpowder, 7);
		RegistryMCA.addObjectAsGift(Items.golden_apple, 25);
		RegistryMCA.addObjectAsGift(Items.record_11, 15);
		RegistryMCA.addObjectAsGift(Items.record_13, 15);
		RegistryMCA.addObjectAsGift(Items.record_wait, 15);
		RegistryMCA.addObjectAsGift(Items.record_cat, 15);
		RegistryMCA.addObjectAsGift(Items.record_chirp, 15);
		RegistryMCA.addObjectAsGift(Items.record_far, 15);
		RegistryMCA.addObjectAsGift(Items.record_mall, 15);
		RegistryMCA.addObjectAsGift(Items.record_mellohi, 15);
		RegistryMCA.addObjectAsGift(Items.record_stal, 15);
		RegistryMCA.addObjectAsGift(Items.record_strad, 15);
		RegistryMCA.addObjectAsGift(Items.record_ward, 15);
		RegistryMCA.addObjectAsGift(Items.emerald, 25);
		RegistryMCA.addObjectAsGift(Blocks.red_flower, 5);
		RegistryMCA.addObjectAsGift(Blocks.yellow_flower, 5);
		RegistryMCA.addObjectAsGift(Blocks.planks, 5);
		RegistryMCA.addObjectAsGift(Blocks.log, 3);
		RegistryMCA.addObjectAsGift(Blocks.pumpkin, 3);
		RegistryMCA.addObjectAsGift(Blocks.chest, 5);
		RegistryMCA.addObjectAsGift(Blocks.wool, 2);
		RegistryMCA.addObjectAsGift(Blocks.iron_ore, 4);
		RegistryMCA.addObjectAsGift(Blocks.gold_ore, 7);
		RegistryMCA.addObjectAsGift(Blocks.redstone_ore, 3);
		RegistryMCA.addObjectAsGift(Blocks.rail, 3);
		RegistryMCA.addObjectAsGift(Blocks.detector_rail, 5);
		RegistryMCA.addObjectAsGift(Blocks.activator_rail, 5);
		RegistryMCA.addObjectAsGift(Blocks.furnace, 5);
		RegistryMCA.addObjectAsGift(Blocks.crafting_table, 5);
		RegistryMCA.addObjectAsGift(Blocks.lapis_block, 15);
		RegistryMCA.addObjectAsGift(Blocks.bookshelf, 7);
		RegistryMCA.addObjectAsGift(Blocks.gold_block, 50);
		RegistryMCA.addObjectAsGift(Blocks.iron_block, 25);
		RegistryMCA.addObjectAsGift(Blocks.diamond_block, 100);
		RegistryMCA.addObjectAsGift(Blocks.brewing_stand, 12);
		RegistryMCA.addObjectAsGift(Blocks.enchanting_table, 25);
		RegistryMCA.addObjectAsGift(Blocks.brick_block, 15);
		RegistryMCA.addObjectAsGift(Blocks.obsidian, 15);
		RegistryMCA.addObjectAsGift(Blocks.piston, 10);
		RegistryMCA.addObjectAsGift(Blocks.glowstone, 10);
		RegistryMCA.addObjectAsGift(Blocks.emerald_block, 100);
		RegistryMCA.addObjectAsGift(ModBlocks.roseGoldBlock, 35);
		RegistryMCA.addObjectAsGift(ModBlocks.roseGoldOre, 7);
		RegistryMCA.addObjectAsGift(Blocks.redstone_block, 20);

		RegistryMCA.addObjectAsGift(ModItems.diamondHeart, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondOval, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondSquare, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondStar, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondTriangle, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondTiny, 50);

		RegistryMCA.addBlockToMiningAI(1, Blocks.coal_ore);
		RegistryMCA.addBlockToMiningAI(2, Blocks.iron_ore);
		RegistryMCA.addBlockToMiningAI(3, Blocks.lapis_ore);
		RegistryMCA.addBlockToMiningAI(4, Blocks.gold_ore);
		RegistryMCA.addBlockToMiningAI(5, Blocks.diamond_ore);
		RegistryMCA.addBlockToMiningAI(6, Blocks.emerald_ore);
		RegistryMCA.addBlockToMiningAI(7, Blocks.quartz_ore);
		RegistryMCA.addBlockToMiningAI(8, ModBlocks.roseGoldOre);

		RegistryMCA.addBlockToWoodcuttingAI(1, new WoodcuttingEntry(Blocks.log, 0, Blocks.sapling, 0));
		RegistryMCA.addBlockToWoodcuttingAI(2, new WoodcuttingEntry(Blocks.log, 1, Blocks.sapling, 1));
		RegistryMCA.addBlockToWoodcuttingAI(3, new WoodcuttingEntry(Blocks.log, 2, Blocks.sapling, 2));
		RegistryMCA.addBlockToWoodcuttingAI(4, new WoodcuttingEntry(Blocks.log, 3, Blocks.sapling, 3));
		RegistryMCA.addBlockToWoodcuttingAI(5, new WoodcuttingEntry(Blocks.log2, 0, Blocks.sapling, 4));
		RegistryMCA.addBlockToWoodcuttingAI(6, new WoodcuttingEntry(Blocks.log2, 1, Blocks.sapling, 5));

		RegistryMCA.addEntityToHuntingAI(EntitySheep.class);
		RegistryMCA.addEntityToHuntingAI(EntityCow.class);
		RegistryMCA.addEntityToHuntingAI(EntityPig.class);
		RegistryMCA.addEntityToHuntingAI(EntityChicken.class);
		RegistryMCA.addEntityToHuntingAI(EntityOcelot.class, false);
		RegistryMCA.addEntityToHuntingAI(EntityWolf.class, false);

		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.porkchop, Items.cooked_porkchop));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.beef, Items.cooked_beef));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.chicken, Items.cooked_chicken));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.fish, Items.cooked_fished));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.potato, Items.baked_potato));

		RegistryMCA.addCropToFarmingAI(1, new CropEntry(EnumCropCategory.WHEAT, Blocks.wheat, Items.wheat_seeds, Blocks.wheat, 7, Items.wheat, 1, 4));
		RegistryMCA.addCropToFarmingAI(2, new CropEntry(EnumCropCategory.WHEAT, Blocks.potatoes, Items.potato, Blocks.potatoes, 7, Items.potato, 1, 4));
		RegistryMCA.addCropToFarmingAI(3, new CropEntry(EnumCropCategory.WHEAT, Blocks.carrots, Items.carrot, Blocks.carrots, 7, Items.carrot, 1, 4));
		RegistryMCA.addCropToFarmingAI(4, new CropEntry(EnumCropCategory.MELON, Blocks.melon_stem, Items.melon_seeds, Blocks.melon_block, 0, Items.melon, 2, 6));
		RegistryMCA.addCropToFarmingAI(5, new CropEntry(EnumCropCategory.MELON, Blocks.pumpkin_stem, Items.pumpkin_seeds, Blocks.pumpkin, 0, null, 1, 1));
		RegistryMCA.addCropToFarmingAI(6, new CropEntry(EnumCropCategory.SUGARCANE, Blocks.reeds, Items.reeds, Blocks.reeds, 0, Items.reeds, 1, 1));

		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.dirt, 1, 6), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.deadbush, 1, 1), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.cactus, 1, 3), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.stick, 1, 4), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.rotten_flesh, 1, 4), EnumGiftCategory.BAD);

		RegistryMCA.addWeddingGift(new WeddingGift(Items.clay_ball, 4, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.stone_axe, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.stone_sword, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.stone_shovel, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.apple, 1, 4), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.arrow, 8, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.stone_pickaxe, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.book, 1, 2), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.redstone, 8, 32), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.cooked_porkchop, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.cooked_beef, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.cooked_chicken, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.bread, 1, 3), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.planks, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.log, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.cobblestone, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.coal, 2, 8), EnumGiftCategory.GOOD);

		RegistryMCA.addWeddingGift(new WeddingGift(Items.clay_ball, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_axe, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_sword, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_shovel, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.arrow, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_pickaxe, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.redstone, 8, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.cooked_porkchop, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.cooked_beef, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.cooked_chicken, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.planks, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.log, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.cobblestone, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.coal, 10, 16), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_helmet, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_chestplate, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_boots, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_leggings, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.melon, 4, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.bookshelf, 2, 4), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.iron_ingot, 8, 16), EnumGiftCategory.BETTER);

		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.brick_block, 32, 32), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond_axe, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond_sword, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond_shovel, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.arrow, 64, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond_pickaxe, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.planks, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.log, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.cobblestone, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.coal, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond_leggings, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond_helmet, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond_boots, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond_chestplate, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ender_eye, 4, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.enchanting_table, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.mossy_cobblestone, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.diamond, 8, 16), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.jukebox, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.diamond_block, 1, 2), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.gold_block, 1, 4), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.iron_block, 1, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.obsidian, 4, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.emerald, 4, 6), EnumGiftCategory.BEST);
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandMCA());

		File playerDataPath = new File(AbstractPlayerData.getPlayerDataPath(event.getServer().getEntityWorld(), MCA.ID));
		playerDataPath.mkdirs();

		for (File f : playerDataPath.listFiles())
		{
			String uuid = f.getName().replace(".dat", "");
			PlayerData data = new PlayerData(uuid, event.getServer().getEntityWorld());
			data = data.readDataFromFile(null, PlayerData.class, f);

			MCA.playerDataMap.put(uuid, data);
		}
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{
		for (AbstractPlayerData data : playerDataMap.values())
		{
			if (data != null) //Bad data seems to be generated with other mods.
			{
				data.saveDataToFile();
			}
		}

		MCA.playerDataMap.clear();
	}

	public static MCA getInstance()
	{
		return instance;
	}

	public static Logger getLog()
	{
		return logger;
	}

	public static Config getConfig()
	{
		return config;
	}

	public static void setConfig(Config configObj)
	{
		config = configObj;
	}

	public static void resetConfig()
	{
		if (config != clientConfig)
		{
			logger.info("Resetting config to client-side values...");
			config = clientConfig;
		}
	}

	public static ModMetadata getMetadata()
	{
		return metadata;
	}

	public static CreativeTabs getCreativeTabMain()
	{
		return creativeTabMain;
	}

	public static CreativeTabs getCreativeTabGemCutting()
	{
		return creativeTabGemCutting;
	}

	public static LanguageManager getLanguageManager()
	{
		return languageManager;
	}

	public static MCAPacketHandler getPacketHandler()
	{
		return packetHandler;
	}

	public static PlayerData getPlayerData(EntityPlayer player)
	{
		if (!player.worldObj.isRemote)
		{
			return (PlayerData) playerDataMap.get(player.getUniqueID().toString());
		}

		else
		{
			return playerDataContainer.getPlayerData(PlayerData.class);
		}
	}

	public static PlayerData getPlayerData(String uuid)
	{
		return (PlayerData) playerDataMap.get(uuid);
	}

	public static EntityHuman getHumanByPermanentId(int id) 
	{
		for (WorldServer world : MinecraftServer.getServer().worldServers)
		{
			for (Object obj : world.loadedEntityList)
			{
				if (obj instanceof EntityHuman)
				{
					EntityHuman human = (EntityHuman)obj;

					if (human.getPermanentId() == id)
					{
						return human;
					}
				}
			}
		}

		return null;
	}

	public static void naturallySpawnVillagers(Point3D pointOfSpawn, World world, int originalProfession)
	{
		boolean hasFamily = RadixLogic.getBooleanWithProbability(20);
		boolean isMale = RadixLogic.getBooleanWithProbability(50);

		final EntityHuman human = new EntityHuman(world, isMale, originalProfession != -1 ? originalProfession : EnumProfession.getAtRandom().getId(), true);
		human.setPosition(pointOfSpawn.dPosX, pointOfSpawn.dPosY, pointOfSpawn.dPosZ);

		if (hasFamily)
		{
			final EntityHuman spouse = new EntityHuman(world, !isMale, EnumProfession.getAtRandom().getId(), false);
			spouse.setPosition(human.posX, human.posY, human.posZ - 1);
			world.spawnEntityInWorld(spouse);

			human.setIsMarried(true, spouse);
			spouse.setIsMarried(true, human);

			String motherName = !isMale ? human.getName() : spouse.getName();
			String fatherName = isMale ? human.getName() : spouse.getName();
			int motherID = !isMale ? human.getPermanentId() : spouse.getPermanentId();
			int fatherID = isMale ? human.getPermanentId() : spouse.getPermanentId();

			//Children
			for (int i = 0; i < 2; i++)
			{
				if (RadixLogic.getBooleanWithProbability(66))
				{
					continue;
				}

				final EntityHuman child = new EntityHuman(world, RadixLogic.getBooleanWithProbability(50), true, motherName, fatherName, motherID, fatherID, false);
				child.setPosition(pointOfSpawn.dPosX, pointOfSpawn.dPosY, pointOfSpawn.dPosZ + 1);
				world.spawnEntityInWorld(child);
			}
		}

		world.spawnEntityInWorld(human);
	}

	public static CrashWatcher getCrashWatcher() 
	{
		return crashWatcher;
	}
}
