package mca.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import mca.api.CookableFood;
import mca.api.CropEntry;
import mca.api.FishingEntry;
import mca.api.MiningEntry;
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
import mca.core.forge.SoundsMCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModBlocks;
import mca.core.minecraft.ModItems;
import mca.core.radix.CrashWatcher;
import mca.core.radix.LanguageParser;
import mca.data.NBTPlayerData;
import mca.data.PlayerData;
import mca.data.PlayerDataCollection;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityHuman;
import mca.enums.EnumCut;
import mca.enums.EnumProfession;
import mca.network.MCAPacketHandler;
import mca.tile.TileMemorial;
import mca.tile.TileTombstone;
import mca.tile.TileVillagerBed;
import mca.util.SkinLoader;
import net.minecraft.block.Block;
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
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;
import radixcore.data.AbstractPlayerData;
import radixcore.forge.gen.SimpleOreGenerator;
import radixcore.lang.LanguageManager;
import radixcore.math.Point3D;
import radixcore.update.RDXUpdateProtocol;
import radixcore.util.RadixExcept;
import radixcore.util.RadixLogic;
import radixcore.util.RadixStartup;

@Mod(modid = MCA.ID, name = MCA.NAME, version = MCA.VERSION, dependencies = "required-after:RadixCore@[1.10.2-2.1.3,)", acceptedMinecraftVersions = "[1.10.2]",
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
	public static NBTPlayerData myPlayerData;
	@SideOnly(Side.CLIENT)
	public static Point3D destinyCenterPoint;
	@SideOnly(Side.CLIENT)
	public static boolean destinySpawnFlag;
	@SideOnly(Side.CLIENT)
	public static boolean reloadLanguage;

	//Fields used for unit testing only.
	public static boolean isTesting;
	public static PlayerData stevePlayerData;
	public static PlayerData alexPlayerData;

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

		SoundsMCA.registerSounds();
		
		MinecraftForge.EVENT_BUS.register(new EventHooksForge());
		MinecraftForge.EVENT_BUS.register(new EventHooksFML());
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
		proxy.registerRenderers();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		SkinLoader.loadSkins();

		//Entity registry
		EntityRegistry.registerModEntity(EntityHuman.class, EntityHuman.class.getSimpleName(), config.baseEntityId, this, 50, 2, true);
		EntityRegistry.registerModEntity(EntityChoreFishHook.class, EntityChoreFishHook.class.getSimpleName(), config.baseEntityId + 1, this, 50, 2, true);
		EntityRegistry.registerModEntity(EntityGrimReaper.class, EntityGrimReaper.class.getSimpleName(), config.baseEntityId + 2, this, 50, 2, true);
		
		//Tile registry
		GameRegistry.registerTileEntity(TileVillagerBed.class, TileVillagerBed.class.getSimpleName());
		GameRegistry.registerTileEntity(TileTombstone.class, TileTombstone.class.getSimpleName());
		GameRegistry.registerTileEntity(TileMemorial.class, TileMemorial.class.getSimpleName());

		//Recipes
		GameRegistry.addRecipe(new ItemStack(ModItems.divorcePapers, 1), 
				new Object[] { " IF", " P ", 'I', new ItemStack(Items.DYE, 1, 0), 'F', Items.FEATHER, 'P', Items.PAPER });

		GameRegistry.addRecipe(new ItemStack(ModItems.whistle), 
				" W#", "###", '#', Items.IRON_INGOT, 'W', Blocks.PLANKS);
		GameRegistry.addRecipe(new ItemStack(Items.GOLD_INGOT), 
				"GGG", "GGG", "GGG", 'G', ModItems.goldDust);
		GameRegistry.addRecipe(new ItemStack(ModItems.roseGoldIngot, 9), 
				"GGG", "GGG", "GGG", 'G', ModItems.roseGoldDust);
		GameRegistry.addRecipe(new ItemStack(ModItems.engagementRing), 
				"GDG", "G G", "GGG", 'D', Items.DIAMOND, 'G', Items.GOLD_INGOT);
		GameRegistry.addRecipe(new ItemStack(ModItems.engagementRingRG), 
				"GDG", "G G", "GGG", 'D', Items.DIAMOND, 'G', ModItems.roseGoldIngot);
		GameRegistry.addRecipe(new ItemStack(ModItems.weddingRingRG),
				"GGG", "G G", "GGG", 'G', ModItems.roseGoldIngot);
		GameRegistry.addRecipe(new ItemStack(ModBlocks.roseGoldBlock),
				"GGG", "GGG", "GGG", 'G', ModItems.roseGoldIngot);
		GameRegistry.addRecipe(new ItemStack(ModItems.matchmakersRing),
				"III", "I I", "III", 'I', Items.IRON_INGOT);
		GameRegistry.addRecipe(new ItemStack(ModItems.tombstone),
				" S ", "SIS", "SSS", 'S', Blocks.STONE, 'I', Items.SIGN);
		GameRegistry.addRecipe(new ItemStack(ModItems.gemCutter),
				"  G", "IG ", "DI ", 'G', Items.GOLD_INGOT, 'I', Items.IRON_INGOT, 'D', Items.DIAMOND);
		GameRegistry.addRecipe(new ItemStack(ModItems.heartMold),
				"CCC", "C C", " C ", 'C', Items.CLAY_BALL);
		GameRegistry.addRecipe(new ItemStack(ModItems.tinyMold),
				" C ", "C C", " C ", 'C', Items.CLAY_BALL);
		GameRegistry.addRecipe(new ItemStack(ModItems.ovalMold),
				"CCC", "   ", "CCC", 'C', Items.CLAY_BALL);    	
		GameRegistry.addRecipe(new ItemStack(ModItems.squareMold),
				"CCC", "C C", "CCC", 'C', Items.CLAY_BALL);    	
		GameRegistry.addRecipe(new ItemStack(ModItems.triangleMold),
				" C ", "C C", "CCC", 'C', Items.CLAY_BALL);    	
		GameRegistry.addRecipe(new ItemStack(ModItems.starMold),
				" C ", "CCC", " C ", 'C', Items.CLAY_BALL);
		GameRegistry.addRecipe(new ItemStack(ModItems.needle),
				"I  ", " I ", "  I", 'I', new ItemStack(Items.IRON_INGOT));
		GameRegistry.addRecipe(new ItemStack(ModItems.newOutfit),
				"C C", "CCC", "CCC", 'C', ModItems.cloth);

		//Variable recipes
		if (!config.disableWeddingRingRecipe)
		{
			GameRegistry.addRecipe(new ItemStack(ModItems.weddingRing),
					"GGG", "G G", "GGG", 'G', Items.GOLD_INGOT);
		}

		else
		{
			logger.fatal("Config: MCA's default wedding ring recipe is currently disabled. You can change this in the config. You must use Rose Gold to craft wedding rings!");
		}

		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.roseGoldDust), ModItems.roseGoldIngot);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.goldDust, 6), Items.WATER_BUCKET, new ItemStack(ModItems.roseGoldDust));

		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedRed), new ItemStack(Items.BED), new ItemStack(Blocks.CARPET, 1, 14));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedBlue), new ItemStack(Items.BED), new ItemStack(Blocks.CARPET, 1, 11));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedGreen), new ItemStack(Items.BED), new ItemStack(Blocks.CARPET, 1, 13));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedPurple), new ItemStack(Items.BED), new ItemStack(Blocks.CARPET, 1, 10));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.bedPink), new ItemStack(Items.BED), new ItemStack(Blocks.CARPET, 1, 6));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.BED), new ItemStack(ModItems.bedRed));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.BED), new ItemStack(ModItems.bedBlue));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.BED), new ItemStack(ModItems.bedGreen));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.BED), new ItemStack(ModItems.bedPurple));
		GameRegistry.addShapelessRecipe(new ItemStack(Items.BED), new ItemStack(ModItems.bedPink));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.needleAndString), new ItemStack(ModItems.needle), new ItemStack(Items.STRING));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.roseGoldIngot, 9), new ItemStack(ModBlocks.roseGoldBlock));

		for(int i = 0; i < 16; i++)
		{
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.cloth), new ItemStack(Blocks.WOOL), new ItemStack(ModItems.needleAndString, 1, i));
		}		

		//Cut diamond recipes
		for (EnumCut cut : EnumCut.values())
		{
			Item cutItem = null;
			Item ringItem = null;
			Item ringRGItem = null;

			switch (cut)
			{
			case HEART:
				cutItem = ModItems.diamondHeart;
				ringItem = ModItems.engagementRingHeart;
				ringRGItem = ModItems.engagementRingHeartRG;
				break;
			case OVAL: 
				cutItem = ModItems.diamondOval;
				ringItem = ModItems.engagementRingOval;
				ringRGItem = ModItems.engagementRingOvalRG;
				break;
			case SQUARE: 
				cutItem = ModItems.diamondSquare;
				ringItem = ModItems.engagementRingSquare;
				ringRGItem = ModItems.engagementRingSquareRG;
				break;
			case STAR: 
				cutItem = ModItems.diamondStar;
				ringItem = ModItems.engagementRingStar;
				ringRGItem = ModItems.engagementRingStarRG;
				break;
			case TINY: 
				cutItem = ModItems.diamondTiny;
				ringItem = ModItems.engagementRingTiny;
				ringRGItem = ModItems.engagementRingTinyRG;
				break;
			case TRIANGLE: 
				cutItem = ModItems.diamondTriangle;
				ringItem = ModItems.engagementRingTriangle;
				ringRGItem = ModItems.engagementRingTriangleRG;
				break;
			default:
				continue;
			}

			//Base recipes
			ItemStack baseStack = new ItemStack(cutItem, 1);

			GameRegistry.addRecipe(new ItemStack(ringItem, 1), 
					"GDG", "G G", "GGG", 'D', baseStack, 'G', Items.GOLD_INGOT);
			GameRegistry.addRecipe(new ItemStack(ringRGItem, 1), 
					"GDG", "G G", "GGG", 'D', baseStack, 'G', ModItems.roseGoldIngot);

			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.diamondHeart, 1, 0), new ItemStack(ModItems.heartMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), Items.DIAMOND);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.diamondTiny, 1, 0), new ItemStack(ModItems.tinyMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), Items.DIAMOND);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.diamondOval, 1, 0), new ItemStack(ModItems.ovalMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), Items.DIAMOND);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.diamondSquare, 1, 0), new ItemStack(ModItems.squareMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), Items.DIAMOND);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.diamondTriangle, 1, 0), new ItemStack(ModItems.triangleMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), Items.DIAMOND);
			GameRegistry.addShapelessRecipe(new ItemStack(ModItems.diamondStar, 1, 0), new ItemStack(ModItems.starMold), new ItemStack(ModItems.gemCutter, 1, OreDictionary.WILDCARD_VALUE), Items.DIAMOND);
		}

		//Smeltings
		GameRegistry.addSmelting(ModBlocks.roseGoldOre, new ItemStack(ModItems.roseGoldIngot), 5.0F);

		if (MCA.config.roseGoldSpawnWeight > 0)
		{
			SimpleOreGenerator.register(new SimpleOreGenerator(ModBlocks.roseGoldOre, 6, 12, 40, true, false), MCA.config.roseGoldSpawnWeight);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		RegistryMCA.addObjectAsGift(Items.WOODEN_SWORD, 3);
		RegistryMCA.addObjectAsGift(Items.WOODEN_AXE, 3);
		RegistryMCA.addObjectAsGift(Items.WOODEN_HOE, 3);
		RegistryMCA.addObjectAsGift(Items.WOODEN_SHOVEL, 3);
		RegistryMCA.addObjectAsGift(Items.STONE_SWORD, 5);
		RegistryMCA.addObjectAsGift(Items.STONE_AXE, 5);
		RegistryMCA.addObjectAsGift(Items.STONE_HOE, 5);
		RegistryMCA.addObjectAsGift(Items.STONE_SHOVEL, 5);
		RegistryMCA.addObjectAsGift(Items.WOODEN_PICKAXE, 3);
		RegistryMCA.addObjectAsGift(Items.BEEF, 2);
		RegistryMCA.addObjectAsGift(Items.CHICKEN, 2);
		RegistryMCA.addObjectAsGift(Items.PORKCHOP, 2);
		RegistryMCA.addObjectAsGift(Items.LEATHER, 2);
		RegistryMCA.addObjectAsGift(Items.LEATHER_CHESTPLATE, 5);
		RegistryMCA.addObjectAsGift(Items.LEATHER_HELMET, 5);
		RegistryMCA.addObjectAsGift(Items.LEATHER_LEGGINGS, 5);
		RegistryMCA.addObjectAsGift(Items.LEATHER_BOOTS, 5);
		RegistryMCA.addObjectAsGift(Items.REEDS, 2);
		RegistryMCA.addObjectAsGift(Items.WHEAT_SEEDS, 2);
		RegistryMCA.addObjectAsGift(Items.WHEAT, 3);
		RegistryMCA.addObjectAsGift(Items.BREAD, 6);
		RegistryMCA.addObjectAsGift(Items.COAL, 5);
		RegistryMCA.addObjectAsGift(Items.SUGAR, 5);
		RegistryMCA.addObjectAsGift(Items.CLAY_BALL, 2);
		RegistryMCA.addObjectAsGift(Items.DYE, 1);
		RegistryMCA.addObjectAsGift(Items.COOKED_BEEF, 7);
		RegistryMCA.addObjectAsGift(Items.COOKED_CHICKEN, 7);
		RegistryMCA.addObjectAsGift(Items.COOKED_PORKCHOP, 7);
		RegistryMCA.addObjectAsGift(Items.COOKIE, 10);
		RegistryMCA.addObjectAsGift(Items.MELON, 10);
		RegistryMCA.addObjectAsGift(Items.MELON_SEEDS, 5);
		RegistryMCA.addObjectAsGift(Items.IRON_HELMET, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_CHESTPLATE, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_LEGGINGS, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_BOOTS, 10);
		RegistryMCA.addObjectAsGift(Items.CAKE, 12);
		RegistryMCA.addObjectAsGift(Items.IRON_SWORD, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_AXE, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_HOE, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_PICKAXE, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_SHOVEL, 10);
		RegistryMCA.addObjectAsGift(Items.FISHING_ROD, 3);
		RegistryMCA.addObjectAsGift(Items.BOW, 5);
		RegistryMCA.addObjectAsGift(Items.BOOK, 5);
		RegistryMCA.addObjectAsGift(Items.BUCKET, 3);
		RegistryMCA.addObjectAsGift(Items.MILK_BUCKET, 5);
		RegistryMCA.addObjectAsGift(Items.WATER_BUCKET, 2);
		RegistryMCA.addObjectAsGift(Items.LAVA_BUCKET, 2);
		RegistryMCA.addObjectAsGift(Items.MUSHROOM_STEW, 5);
		RegistryMCA.addObjectAsGift(Items.PUMPKIN_SEEDS, 8);
		RegistryMCA.addObjectAsGift(Items.FLINT_AND_STEEL, 4);
		RegistryMCA.addObjectAsGift(Items.REDSTONE, 5);
		RegistryMCA.addObjectAsGift(Items.BOAT, 4);
		RegistryMCA.addObjectAsGift(Items.OAK_DOOR, 4);
		RegistryMCA.addObjectAsGift(Items.IRON_DOOR, 6);
		RegistryMCA.addObjectAsGift(Items.MINECART, 7);
		RegistryMCA.addObjectAsGift(Items.FLINT, 2);
		RegistryMCA.addObjectAsGift(Items.GOLD_NUGGET, 4);
		RegistryMCA.addObjectAsGift(Items.GOLD_INGOT, 20);
		RegistryMCA.addObjectAsGift(Items.IRON_INGOT, 10);
		RegistryMCA.addObjectAsGift(Items.DIAMOND, 30);
		RegistryMCA.addObjectAsGift(Items.MAP, 10);
		RegistryMCA.addObjectAsGift(Items.CLOCK, 5);
		RegistryMCA.addObjectAsGift(Items.COMPASS, 5);
		RegistryMCA.addObjectAsGift(Items.BLAZE_ROD, 10);
		RegistryMCA.addObjectAsGift(Items.BLAZE_POWDER, 5);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_SWORD, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_AXE, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_SHOVEL, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_HOE, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_HELMET, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_CHESTPLATE, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_LEGGINGS, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_BOOTS, 15);
		RegistryMCA.addObjectAsGift(Items.PAINTING, 6);
		RegistryMCA.addObjectAsGift(Items.ENDER_PEARL, 5);
		RegistryMCA.addObjectAsGift(Items.ENDER_EYE, 10);
		RegistryMCA.addObjectAsGift(Items.POTIONITEM, 3);
		RegistryMCA.addObjectAsGift(Items.SLIME_BALL, 3);
		RegistryMCA.addObjectAsGift(Items.SADDLE, 5);
		RegistryMCA.addObjectAsGift(Items.GUNPOWDER, 7);
		RegistryMCA.addObjectAsGift(Items.GOLDEN_APPLE, 25);
		RegistryMCA.addObjectAsGift(Items.RECORD_11, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_13, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_WAIT, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_CAT, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_CHIRP, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_FAR, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_MALL, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_MELLOHI, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_STAL, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_STRAD, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_WARD, 15);
		RegistryMCA.addObjectAsGift(Items.EMERALD, 25);
		RegistryMCA.addObjectAsGift(Blocks.RED_FLOWER, 5);
		RegistryMCA.addObjectAsGift(Blocks.YELLOW_FLOWER, 5);
		RegistryMCA.addObjectAsGift(Blocks.PLANKS, 5);
		RegistryMCA.addObjectAsGift(Blocks.LOG, 3);
		RegistryMCA.addObjectAsGift(Blocks.PUMPKIN, 3);
		RegistryMCA.addObjectAsGift(Blocks.CHEST, 5);
		RegistryMCA.addObjectAsGift(Blocks.WOOL, 2);
		RegistryMCA.addObjectAsGift(Blocks.IRON_ORE, 4);
		RegistryMCA.addObjectAsGift(Blocks.GOLD_ORE, 7);
		RegistryMCA.addObjectAsGift(Blocks.REDSTONE_ORE, 3);
		RegistryMCA.addObjectAsGift(Blocks.RAIL, 3);
		RegistryMCA.addObjectAsGift(Blocks.DETECTOR_RAIL, 5);
		RegistryMCA.addObjectAsGift(Blocks.ACTIVATOR_RAIL, 5);
		RegistryMCA.addObjectAsGift(Blocks.FURNACE, 5);
		RegistryMCA.addObjectAsGift(Blocks.CRAFTING_TABLE, 5);
		RegistryMCA.addObjectAsGift(Blocks.LAPIS_BLOCK, 15);
		RegistryMCA.addObjectAsGift(Blocks.BOOKSHELF, 7);
		RegistryMCA.addObjectAsGift(Blocks.GOLD_BLOCK, 50);
		RegistryMCA.addObjectAsGift(Blocks.IRON_BLOCK, 25);
		RegistryMCA.addObjectAsGift(Blocks.DIAMOND_BLOCK, 100);
		RegistryMCA.addObjectAsGift(Blocks.BREWING_STAND, 12);
		RegistryMCA.addObjectAsGift(Blocks.ENCHANTING_TABLE, 25);
		RegistryMCA.addObjectAsGift(Blocks.BRICK_BLOCK, 15);
		RegistryMCA.addObjectAsGift(Blocks.OBSIDIAN, 15);
		RegistryMCA.addObjectAsGift(Blocks.PISTON, 10);
		RegistryMCA.addObjectAsGift(Blocks.GLOWSTONE, 10);
		RegistryMCA.addObjectAsGift(Blocks.EMERALD_BLOCK, 100);
		RegistryMCA.addObjectAsGift(ModBlocks.roseGoldBlock, 35);
		RegistryMCA.addObjectAsGift(ModBlocks.roseGoldOre, 7);
		RegistryMCA.addObjectAsGift(Blocks.REDSTONE_BLOCK, 20);
		RegistryMCA.addObjectAsGift(ModItems.diamondHeart, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondOval, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondSquare, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondStar, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondTriangle, 50);
		RegistryMCA.addObjectAsGift(ModItems.diamondTiny, 50);

		RegistryMCA.addFishingEntryToFishingAI(0, new FishingEntry(Items.FISH));
		RegistryMCA.addFishingEntryToFishingAI(1, new FishingEntry(Items.FISH, ItemFishFood.FishType.CLOWNFISH.getMetadata()));
		RegistryMCA.addFishingEntryToFishingAI(2, new FishingEntry(Items.FISH, ItemFishFood.FishType.COD.getMetadata()));
		RegistryMCA.addFishingEntryToFishingAI(3, new FishingEntry(Items.FISH, ItemFishFood.FishType.PUFFERFISH.getMetadata()));
		RegistryMCA.addFishingEntryToFishingAI(4, new FishingEntry(Items.FISH, ItemFishFood.FishType.SALMON.getMetadata()));
		
		if (getConfig().additionalGiftItems.length > 0)
		{
			for (String entry : getConfig().additionalGiftItems)
			{
				try
				{
					String[] split = entry.split("\\|");
					int heartsValue = Integer.parseInt(split[1]);
					String itemName = split[0];

					if (!itemName.startsWith("#"))
					{
						Object item = Item.REGISTRY.getObject(new ResourceLocation(itemName));
						Object block = Block.REGISTRY.getObject(new ResourceLocation(itemName));
						Object addObject = item != null ? item : block != null ? block : null;

						if (addObject != null)
						{
							RegistryMCA.addObjectAsGift(addObject, heartsValue);
							logger.info("Successfully added " + itemName + " with hearts value of " + heartsValue + " to gift registry.");
						}

						else
						{
							logger.error("Failed to find item by name provided. Gift entry not created: " + entry);
						}
					}
				}

				catch (Exception e)
				{
					logger.error("Failed to add additional gift due to error. Use <item name>|<hearts value>: " + entry);
				}
			}
		}

		RegistryMCA.addBlockToMiningAI(1, new MiningEntry(Blocks.COAL_ORE, Items.COAL, 0.45F));
		RegistryMCA.addBlockToMiningAI(2, new MiningEntry(Blocks.IRON_ORE, 0.4F));
		RegistryMCA.addBlockToMiningAI(3, new MiningEntry(Blocks.LAPIS_ORE, new ItemStack(Items.DYE, 1, 4), 0.3F));
		RegistryMCA.addBlockToMiningAI(4, new MiningEntry(Blocks.GOLD_ORE, 0.05F));
		RegistryMCA.addBlockToMiningAI(5, new MiningEntry(Blocks.DIAMOND_ORE, Items.DIAMOND, 0.04F));
		RegistryMCA.addBlockToMiningAI(6, new MiningEntry(Blocks.EMERALD_ORE, Items.EMERALD, 0.03F));
		RegistryMCA.addBlockToMiningAI(7, new MiningEntry(Blocks.QUARTZ_ORE, Items.QUARTZ, 0.02F));
		RegistryMCA.addBlockToMiningAI(8, new MiningEntry(ModBlocks.roseGoldOre, 0.07F));

		RegistryMCA.addBlockToWoodcuttingAI(1, new WoodcuttingEntry(Blocks.LOG, 0, Blocks.SAPLING, 0));
		RegistryMCA.addBlockToWoodcuttingAI(2, new WoodcuttingEntry(Blocks.LOG, 1, Blocks.SAPLING, 1));
		RegistryMCA.addBlockToWoodcuttingAI(3, new WoodcuttingEntry(Blocks.LOG, 2, Blocks.SAPLING, 2));
		RegistryMCA.addBlockToWoodcuttingAI(4, new WoodcuttingEntry(Blocks.LOG, 3, Blocks.SAPLING, 3));
		RegistryMCA.addBlockToWoodcuttingAI(5, new WoodcuttingEntry(Blocks.LOG2, 0, Blocks.SAPLING, 4));
		RegistryMCA.addBlockToWoodcuttingAI(6, new WoodcuttingEntry(Blocks.LOG2, 1, Blocks.SAPLING, 5));

		RegistryMCA.addEntityToHuntingAI(EntitySheep.class);
		RegistryMCA.addEntityToHuntingAI(EntityCow.class);
		RegistryMCA.addEntityToHuntingAI(EntityPig.class);
		RegistryMCA.addEntityToHuntingAI(EntityChicken.class);
		RegistryMCA.addEntityToHuntingAI(EntityOcelot.class, false);
		RegistryMCA.addEntityToHuntingAI(EntityWolf.class, false);

		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.PORKCHOP, Items.COOKED_PORKCHOP));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.BEEF, Items.COOKED_BEEF));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.CHICKEN, Items.COOKED_CHICKEN));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.FISH, Items.COOKED_FISH));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.POTATO, Items.BAKED_POTATO));

		RegistryMCA.addCropToFarmingAI(1, new CropEntry(EnumCropCategory.WHEAT, Blocks.WHEAT, Items.WHEAT_SEEDS, Blocks.WHEAT, 7, Items.WHEAT, 1, 4));
		RegistryMCA.addCropToFarmingAI(2, new CropEntry(EnumCropCategory.WHEAT, Blocks.POTATOES, Items.POTATO, Blocks.POTATOES, 7, Items.POTATO, 1, 4));
		RegistryMCA.addCropToFarmingAI(3, new CropEntry(EnumCropCategory.WHEAT, Blocks.CARROTS, Items.CARROT, Blocks.CARROTS, 7, Items.CARROT, 1, 4));
		RegistryMCA.addCropToFarmingAI(4, new CropEntry(EnumCropCategory.MELON, Blocks.MELON_STEM, Items.MELON_SEEDS, Blocks.MELON_BLOCK, 0, Items.MELON, 2, 6));
		RegistryMCA.addCropToFarmingAI(5, new CropEntry(EnumCropCategory.MELON, Blocks.PUMPKIN_STEM, Items.PUMPKIN_SEEDS, Blocks.PUMPKIN, 0, null, 1, 1));
		RegistryMCA.addCropToFarmingAI(6, new CropEntry(EnumCropCategory.SUGARCANE, Blocks.REEDS, Items.REEDS, Blocks.REEDS, 0, Items.REEDS, 1, 1));

		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.DIRT, 1, 6), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.DEADBUSH, 1, 1), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.CACTUS, 1, 3), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STICK, 1, 4), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ROTTEN_FLESH, 1, 4), EnumGiftCategory.BAD);

		RegistryMCA.addWeddingGift(new WeddingGift(Items.CLAY_BALL, 4, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STONE_AXE, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STONE_SWORD, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STONE_SHOVEL, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.APPLE, 1, 4), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ARROW, 8, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STONE_PICKAXE, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.BOOK, 1, 2), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.REDSTONE, 8, 32), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_PORKCHOP, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_BEEF, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_CHICKEN, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.BREAD, 1, 3), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.PLANKS, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.LOG, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.COBBLESTONE, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COAL, 2, 8), EnumGiftCategory.GOOD);

		RegistryMCA.addWeddingGift(new WeddingGift(Items.CLAY_BALL, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_AXE, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_SWORD, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_SHOVEL, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ARROW, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_PICKAXE, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.REDSTONE, 8, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_PORKCHOP, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_BEEF, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_CHICKEN, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.PLANKS, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.LOG, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.COBBLESTONE, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COAL, 10, 16), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_HELMET, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_CHESTPLATE, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_BOOTS, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_LEGGINGS, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.MELON, 4, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.BOOKSHELF, 2, 4), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_INGOT, 8, 16), EnumGiftCategory.BETTER);

		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.BRICK_BLOCK, 32, 32), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_AXE, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_SWORD, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_SHOVEL, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ARROW, 64, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_PICKAXE, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.PLANKS, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.LOG, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.COBBLESTONE, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COAL, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_LEGGINGS, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_HELMET, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_BOOTS, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_CHESTPLATE, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ENDER_EYE, 4, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.ENCHANTING_TABLE, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.MOSSY_COBBLESTONE, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND, 8, 16), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.JUKEBOX, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.DIAMOND_BLOCK, 1, 2), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.GOLD_BLOCK, 1, 4), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.IRON_BLOCK, 1, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.OBSIDIAN, 4, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.EMERALD, 4, 6), EnumGiftCategory.BEST);
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandMCA());
	}
	
	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{
		for (AbstractPlayerData data : playerDataMap.values())
		{
			try
			{
				data.saveDataToFile();
			}

			catch (NullPointerException e)
			{
				RadixExcept.logErrorCatch(e, "Catching error saving player data due to NPE.");
			}

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

	public static NBTPlayerData getPlayerData(EntityPlayer player)
	{
		if (!player.worldObj.isRemote)
		{
			return PlayerDataCollection.get().getPlayerData(player.getUniqueID());
		}
		
		else
		{
			return myPlayerData;
		}
	}
	
	public static NBTPlayerData getPlayerData(World worldObj, UUID uuid)
	{
		return PlayerDataCollection.get().getPlayerData(uuid);
	}

	public static EntityHuman getHumanByPermanentId(int id) 
	{
		for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worldServers)
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

			human.setMarriedTo(spouse);
			spouse.setMarriedTo(human);

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

	public static void initializeForTesting() 
	{
		MCA.isTesting = true;
		MCA.metadata = new ModMetadata();
		MCA.metadata.modId = MCA.ID;
	}
}
