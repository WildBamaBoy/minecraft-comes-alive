package mca.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import mca.api.ChoreRegistry;
import mca.api.CookableFood;
import mca.api.CropEntry;
import mca.api.WoodcuttingEntry;
import mca.api.enums.EnumCropCategory;
import mca.command.CommandMCA;
import mca.core.forge.EventHooksFML;
import mca.core.forge.EventHooksForge;
import mca.core.forge.GuiHandler;
import mca.core.forge.ServerProxy;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModBlocks;
import mca.core.minecraft.ModItems;
import mca.core.radix.LanguageParser;
import mca.data.PlayerData;
import mca.entity.EntityHuman;
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
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

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

@Mod(modid = MCA.ID, name = MCA.NAME, version = MCA.VERSION, dependencies = "required-after:RadixCore@[2.0.0,)", acceptedMinecraftVersions = "[1.7.10]",
		guiFactory = "mca.core.forge.client.MCAGuiFactory")
public class MCA
{
	public static final String ID = "MCA";
	public static final String NAME = "Minecraft Comes Alive";
	public static final String VERSION = "5.0.0";

	@Instance(ID)
	private static MCA instance;
	private static ModMetadata metadata;
	private static ModItems items;
	private static ModBlocks blocks;
	private static ModAchievements achievements;
	private static CreativeTabs creativeTabMain;
	private static CreativeTabs creativeTabGemCutting;
	private static Config config;
	private static LanguageManager languageManager;
	private static MCAPacketHandler packetHandler;
	
	private static Logger logger;
	
	@SidedProxy(clientSide = "mca.core.forge.ClientProxy", serverSide = "mca.core.forge.ServerProxy")
	public static ServerProxy proxy;
	
	public static Map<String, AbstractPlayerData> playerDataMap;
	
	@SideOnly(Side.CLIENT)
	public static DataContainer playerDataContainer;
	@SideOnly(Side.CLIENT)
	public static Point3D destinyCenterPoint;
	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {	
    	instance = this;
		metadata = event.getModMetadata();
    	logger = event.getModLog();
    	config = new Config(event);
    	languageManager = new LanguageManager(ID, new LanguageParser());
    	packetHandler = new MCAPacketHandler(ID);
    	proxy.registerRenderers();
    	proxy.registerEventHandlers();
    	playerDataMap = new HashMap<String, AbstractPlayerData>();
    	
    	ModMetadataEx exData = ModMetadataEx.getFromModMetadata(metadata);
    	exData.updateProtocolClass = RDXUpdateProtocol.class;
    	exData.classContainingClientDataContainer = MCA.class;
    	exData.playerDataMap = playerDataMap;
    	
    	RadixCore.registerMod(exData);
    	
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
    	GameRegistry.addRecipe(new ItemStack(ModItems.engagementRing), 
    			"GDG", "G G", "GGG", 'D', Items.diamond, 'G', Items.gold_ingot);
    	GameRegistry.addRecipe(new ItemStack(ModItems.engagementRingRG), 
    			"GDG", "G G", "GGG", 'D', Items.diamond, 'G', ModItems.roseGoldIngot);
    	GameRegistry.addRecipe(new ItemStack(ModItems.weddingRing),
    			"GGG", "G G", "GGG", 'G', Items.gold_ingot);
    	GameRegistry.addRecipe(new ItemStack(ModItems.weddingRingRG),
    			"GGG", "G G", "GGG", 'G', ModItems.roseGoldIngot);
    	GameRegistry.addRecipe(new ItemStack(ModBlocks.roseGoldBlock),
    			"GGG", "GGG", "GGG", 'G', ModItems.roseGoldIngot);
    	GameRegistry.addRecipe(new ItemStack(ModItems.matchmakersRing),
    			"III", "I I", "III", 'I', Items.iron_ingot);
    	GameRegistry.addRecipe(new ItemStack(ModItems.tombstone),
    			" S ", "SIS", "SSS", 'S', Blocks.stone, 'I', Items.sign);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.diamondDust), Items.diamond);
		
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
		}
		
    	//Smeltings
    	GameRegistry.addSmelting(ModBlocks.roseGoldOre, new ItemStack(ModItems.roseGoldIngot), 5.0F);
    	GameRegistry.addSmelting(ModItems.diamondDust, new ItemStack(Items.diamond), 5.0F);

    	SimpleOreGenerator.register(new SimpleOreGenerator(ModBlocks.roseGoldOre, 6, 12, 40, true, false), MCA.config.roseGoldSpawnWeight);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	ChoreRegistry.addObjectAsGift(Items.wooden_sword, 3);
		ChoreRegistry.addObjectAsGift(Items.wooden_axe, 3);
		ChoreRegistry.addObjectAsGift(Items.wooden_hoe, 3);
		ChoreRegistry.addObjectAsGift(Items.wooden_shovel, 3);
		ChoreRegistry.addObjectAsGift(Items.stone_sword, 5);
		ChoreRegistry.addObjectAsGift(Items.stone_axe, 5);
		ChoreRegistry.addObjectAsGift(Items.stone_hoe, 5);
		ChoreRegistry.addObjectAsGift(Items.stone_shovel, 5);
		ChoreRegistry.addObjectAsGift(Items.wooden_pickaxe, 3);
		ChoreRegistry.addObjectAsGift(Items.beef, 2);
		ChoreRegistry.addObjectAsGift(Items.chicken, 2);
		ChoreRegistry.addObjectAsGift(Items.porkchop, 2);
		ChoreRegistry.addObjectAsGift(Items.leather, 2);
		ChoreRegistry.addObjectAsGift(Items.leather_chestplate, 5);
		ChoreRegistry.addObjectAsGift(Items.leather_helmet, 5);
		ChoreRegistry.addObjectAsGift(Items.leather_leggings, 5);
		ChoreRegistry.addObjectAsGift(Items.leather_boots, 5);
		ChoreRegistry.addObjectAsGift(Items.reeds, 2);
		ChoreRegistry.addObjectAsGift(Items.wheat_seeds, 2);
		ChoreRegistry.addObjectAsGift(Items.wheat, 3);
		ChoreRegistry.addObjectAsGift(Items.bread, 6);
		ChoreRegistry.addObjectAsGift(Items.coal, 5);
		ChoreRegistry.addObjectAsGift(Items.sugar, 5);
		ChoreRegistry.addObjectAsGift(Items.clay_ball, 2);
		ChoreRegistry.addObjectAsGift(Items.dye, 1);
		ChoreRegistry.addObjectAsGift(Items.cooked_beef, 7);
		ChoreRegistry.addObjectAsGift(Items.cooked_chicken, 7);
		ChoreRegistry.addObjectAsGift(Items.cooked_porkchop, 7);
		ChoreRegistry.addObjectAsGift(Items.cookie, 10);
		ChoreRegistry.addObjectAsGift(Items.melon, 10);
		ChoreRegistry.addObjectAsGift(Items.melon_seeds, 5);
		ChoreRegistry.addObjectAsGift(Items.iron_helmet, 10);
		ChoreRegistry.addObjectAsGift(Items.iron_chestplate, 10);
		ChoreRegistry.addObjectAsGift(Items.iron_leggings, 10);
		ChoreRegistry.addObjectAsGift(Items.iron_boots, 10);
		ChoreRegistry.addObjectAsGift(Items.cake, 12);
		ChoreRegistry.addObjectAsGift(Items.iron_sword, 10);
		ChoreRegistry.addObjectAsGift(Items.iron_axe, 10);
		ChoreRegistry.addObjectAsGift(Items.iron_hoe, 10);
		ChoreRegistry.addObjectAsGift(Items.iron_pickaxe, 10);
		ChoreRegistry.addObjectAsGift(Items.iron_shovel, 10);
		ChoreRegistry.addObjectAsGift(Items.fishing_rod, 3);
		ChoreRegistry.addObjectAsGift(Items.bow, 5);
		ChoreRegistry.addObjectAsGift(Items.book, 5);
		ChoreRegistry.addObjectAsGift(Items.bucket, 3);
		ChoreRegistry.addObjectAsGift(Items.milk_bucket, 5);
		ChoreRegistry.addObjectAsGift(Items.water_bucket, 2);
		ChoreRegistry.addObjectAsGift(Items.lava_bucket, 2);
		ChoreRegistry.addObjectAsGift(Items.mushroom_stew, 5);
		ChoreRegistry.addObjectAsGift(Items.pumpkin_seeds, 8);
		ChoreRegistry.addObjectAsGift(Items.flint_and_steel, 4);
		ChoreRegistry.addObjectAsGift(Items.redstone, 5);
		ChoreRegistry.addObjectAsGift(Items.boat, 4);
		ChoreRegistry.addObjectAsGift(Items.wooden_door, 4);
		ChoreRegistry.addObjectAsGift(Items.iron_door, 6);
		ChoreRegistry.addObjectAsGift(Items.minecart, 7);
		ChoreRegistry.addObjectAsGift(Items.flint, 2);
		ChoreRegistry.addObjectAsGift(Items.gold_nugget, 4);
		ChoreRegistry.addObjectAsGift(Items.gold_ingot, 20);
		ChoreRegistry.addObjectAsGift(Items.iron_ingot, 10);
		ChoreRegistry.addObjectAsGift(Items.diamond, 30);
		ChoreRegistry.addObjectAsGift(Items.map, 10);
		ChoreRegistry.addObjectAsGift(Items.clock, 5);
		ChoreRegistry.addObjectAsGift(Items.compass, 5);
		ChoreRegistry.addObjectAsGift(Items.blaze_rod, 10);
		ChoreRegistry.addObjectAsGift(Items.blaze_powder, 5);
		ChoreRegistry.addObjectAsGift(Items.diamond_sword, 15);
		ChoreRegistry.addObjectAsGift(Items.diamond_axe, 15);
		ChoreRegistry.addObjectAsGift(Items.diamond_shovel, 15);
		ChoreRegistry.addObjectAsGift(Items.diamond_hoe, 15);
		ChoreRegistry.addObjectAsGift(Items.diamond_leggings, 15);
		ChoreRegistry.addObjectAsGift(Items.diamond_helmet, 15);
		ChoreRegistry.addObjectAsGift(Items.diamond_chestplate, 15);
		ChoreRegistry.addObjectAsGift(Items.diamond_leggings, 15);
		ChoreRegistry.addObjectAsGift(Items.diamond_boots, 15);
		ChoreRegistry.addObjectAsGift(Items.painting, 6);
		ChoreRegistry.addObjectAsGift(Items.ender_pearl, 5);
		ChoreRegistry.addObjectAsGift(Items.ender_eye, 10);
		ChoreRegistry.addObjectAsGift(Items.potionitem, 3);
		ChoreRegistry.addObjectAsGift(Items.slime_ball, 3);
		ChoreRegistry.addObjectAsGift(Items.saddle, 5);
		ChoreRegistry.addObjectAsGift(Items.gunpowder, 7);
		ChoreRegistry.addObjectAsGift(Items.golden_apple, 25);
		ChoreRegistry.addObjectAsGift(Items.record_11, 15);
		ChoreRegistry.addObjectAsGift(Items.record_13, 15);
		ChoreRegistry.addObjectAsGift(Items.record_wait, 15);
		ChoreRegistry.addObjectAsGift(Items.record_cat, 15);
		ChoreRegistry.addObjectAsGift(Items.record_chirp, 15);
		ChoreRegistry.addObjectAsGift(Items.record_far, 15);
		ChoreRegistry.addObjectAsGift(Items.record_mall, 15);
		ChoreRegistry.addObjectAsGift(Items.record_mellohi, 15);
		ChoreRegistry.addObjectAsGift(Items.record_stal, 15);
		ChoreRegistry.addObjectAsGift(Items.record_strad, 15);
		ChoreRegistry.addObjectAsGift(Items.record_ward, 15);
		ChoreRegistry.addObjectAsGift(Items.emerald, 25);
		ChoreRegistry.addObjectAsGift(Blocks.red_flower, 5);
		ChoreRegistry.addObjectAsGift(Blocks.yellow_flower, 5);
		ChoreRegistry.addObjectAsGift(Blocks.planks, 5);
		ChoreRegistry.addObjectAsGift(Blocks.log, 3);
		ChoreRegistry.addObjectAsGift(Blocks.pumpkin, 3);
		ChoreRegistry.addObjectAsGift(Blocks.chest, 5);
		ChoreRegistry.addObjectAsGift(Blocks.wool, 2);
		ChoreRegistry.addObjectAsGift(Blocks.iron_ore, 4);
		ChoreRegistry.addObjectAsGift(Blocks.gold_ore, 7);
		ChoreRegistry.addObjectAsGift(Blocks.redstone_ore, 3);
		ChoreRegistry.addObjectAsGift(Blocks.rail, 3);
		ChoreRegistry.addObjectAsGift(Blocks.detector_rail, 5);
		ChoreRegistry.addObjectAsGift(Blocks.activator_rail, 5);
		ChoreRegistry.addObjectAsGift(Blocks.furnace, 5);
		ChoreRegistry.addObjectAsGift(Blocks.crafting_table, 5);
		ChoreRegistry.addObjectAsGift(Blocks.lapis_block, 15);
		ChoreRegistry.addObjectAsGift(Blocks.bookshelf, 7);
		ChoreRegistry.addObjectAsGift(Blocks.gold_block, 50);
		ChoreRegistry.addObjectAsGift(Blocks.iron_block, 25);
		ChoreRegistry.addObjectAsGift(Blocks.diamond_block, 100);
		ChoreRegistry.addObjectAsGift(Blocks.brewing_stand, 12);
		ChoreRegistry.addObjectAsGift(Blocks.enchanting_table, 25);
		ChoreRegistry.addObjectAsGift(Blocks.brick_block, 15);
		ChoreRegistry.addObjectAsGift(Blocks.obsidian, 15);
		ChoreRegistry.addObjectAsGift(Blocks.piston, 10);
		ChoreRegistry.addObjectAsGift(Blocks.glowstone, 10);
		ChoreRegistry.addObjectAsGift(Blocks.emerald_block, 100);
		
		ChoreRegistry.addBlockToMiningAI(1, Blocks.coal_ore);
		ChoreRegistry.addBlockToMiningAI(2, Blocks.iron_ore);
		ChoreRegistry.addBlockToMiningAI(3, Blocks.lapis_ore);
		ChoreRegistry.addBlockToMiningAI(4, Blocks.gold_ore);
		ChoreRegistry.addBlockToMiningAI(5, Blocks.diamond_ore);
		ChoreRegistry.addBlockToMiningAI(6, Blocks.emerald_ore);
		ChoreRegistry.addBlockToMiningAI(7, Blocks.quartz_ore);
		ChoreRegistry.addBlockToMiningAI(8, ModBlocks.roseGoldOre);
		
		ChoreRegistry.addBlockToWoodcuttingAI(1, new WoodcuttingEntry(Blocks.log, 0, Blocks.sapling, 0));
		ChoreRegistry.addBlockToWoodcuttingAI(2, new WoodcuttingEntry(Blocks.log, 1, Blocks.sapling, 1));
		ChoreRegistry.addBlockToWoodcuttingAI(3, new WoodcuttingEntry(Blocks.log, 2, Blocks.sapling, 2));
		ChoreRegistry.addBlockToWoodcuttingAI(4, new WoodcuttingEntry(Blocks.log, 3, Blocks.sapling, 3));
		ChoreRegistry.addBlockToWoodcuttingAI(5, new WoodcuttingEntry(Blocks.log2, 0, Blocks.sapling, 4));
		ChoreRegistry.addBlockToWoodcuttingAI(6, new WoodcuttingEntry(Blocks.log2, 1, Blocks.sapling, 5));
		
		ChoreRegistry.addEntityToHuntingAI(EntitySheep.class);
		ChoreRegistry.addEntityToHuntingAI(EntityCow.class);
		ChoreRegistry.addEntityToHuntingAI(EntityPig.class);
		ChoreRegistry.addEntityToHuntingAI(EntityChicken.class);
		ChoreRegistry.addEntityToHuntingAI(EntityOcelot.class, false);
		ChoreRegistry.addEntityToHuntingAI(EntityWolf.class, false);
		
		ChoreRegistry.addFoodToCookingAI(new CookableFood(Items.porkchop, Items.cooked_porkchop));
		ChoreRegistry.addFoodToCookingAI(new CookableFood(Items.beef, Items.cooked_beef));
		ChoreRegistry.addFoodToCookingAI(new CookableFood(Items.chicken, Items.cooked_chicken));
		ChoreRegistry.addFoodToCookingAI(new CookableFood(Items.fish, Items.cooked_fished));
		ChoreRegistry.addFoodToCookingAI(new CookableFood(Items.potato, Items.baked_potato));
		
		ChoreRegistry.addCropToFarmingAI(1, new CropEntry(EnumCropCategory.WHEAT, Blocks.wheat, Items.wheat_seeds, Blocks.wheat, 7, Items.wheat, 1, 4));
		ChoreRegistry.addCropToFarmingAI(2, new CropEntry(EnumCropCategory.WHEAT, Blocks.potatoes, Items.potato, Blocks.potatoes, 7, Items.potato, 1, 4));
		ChoreRegistry.addCropToFarmingAI(3, new CropEntry(EnumCropCategory.WHEAT, Blocks.carrots, Items.carrot, Blocks.carrots, 7, Items.carrot, 1, 4));
		ChoreRegistry.addCropToFarmingAI(4, new CropEntry(EnumCropCategory.MELON, Blocks.melon_stem, Items.melon_seeds, Blocks.melon_block, 0, Items.melon, 2, 6));
		ChoreRegistry.addCropToFarmingAI(5, new CropEntry(EnumCropCategory.MELON, Blocks.pumpkin_stem, Items.pumpkin_seeds, Blocks.pumpkin, 0, null, 1, 1));
		ChoreRegistry.addCropToFarmingAI(6, new CropEntry(EnumCropCategory.SUGARCANE, Blocks.reeds, Items.reeds, Blocks.reeds, 0, Items.reeds, 1, 1));
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
   			data.saveDataToFile();
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
		human.setPosition(pointOfSpawn.iPosX, pointOfSpawn.iPosY, pointOfSpawn.iPosZ);

		if (hasFamily)
		{
			final EntityHuman spouse = new EntityHuman(world, !isMale, EnumProfession.getAtRandom().getId(), false);
			spouse.setPosition(human.posX, human.posY, human.posZ);
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
				child.setPosition(pointOfSpawn.iPosX, pointOfSpawn.iPosY, pointOfSpawn.iPosZ);
				world.spawnEntityInWorld(child);
			}
		}

		world.spawnEntityInWorld(human);
	}
}
