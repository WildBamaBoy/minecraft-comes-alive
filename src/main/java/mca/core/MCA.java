package mca.core;

import java.util.HashMap;
import java.util.Map;

import mca.command.CommandMCA;
import mca.core.forge.EventHooksFML;
import mca.core.forge.EventHooksForge;
import mca.core.forge.GuiHandler;
import mca.core.forge.ServerProxy;
import mca.core.minecraft.Achievements;
import mca.core.minecraft.Blocks;
import mca.core.minecraft.Items;
import mca.core.radix.LanguageParser;
import mca.data.PlayerData;
import mca.entity.EntityHuman;
import mca.network.MCAPacketHandler;
import mca.tile.TileVillagerBed;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import radixcore.ModMetadataEx;
import radixcore.RadixCore;
import radixcore.data.AbstractPlayerData;
import radixcore.data.BlockObj;
import radixcore.data.DataContainer;
import radixcore.helpers.StartupHelper;
import radixcore.lang.LanguageManager;
import radixcore.math.Point3D;
import radixcore.update.RDXUpdateProtocol;
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
	private static Items items;
	private static Blocks blocks;
	private static Achievements achievements;
	private static CreativeTabs creativeTab;
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
    	creativeTab = StartupHelper.registerCreativeTab(Items.class, "engagementRing", metadata);
    	items = new Items();
    	blocks = new Blocks();
    	achievements = new Achievements();
    	
    	SkinLoader.loadSkins();

    	//Entity registry
    	EntityRegistry.registerModEntity(EntityHuman.class, EntityHuman.class.getSimpleName(), config.baseEntityId, this, 50, 2, true);

    	//Tile registry
    	GameRegistry.registerTileEntity(TileVillagerBed.class, TileVillagerBed.class.getSimpleName());
    	
    	//Recipes
    	GameRegistry.addRecipe(new ItemStack(Items.engagementRing), 
    			"GDG", "G G", "GGG", 'D', net.minecraft.init.Items.diamond, 'G', net.minecraft.init.Items.gold_ingot);
    	GameRegistry.addRecipe(new ItemStack(Items.roseGoldEngagementRing), 
    			"GDG", "G G", "GGG", 'D', net.minecraft.init.Items.diamond, 'G', Items.roseGoldIngot);
    	GameRegistry.addRecipe(new ItemStack(Items.weddingRing),
    			"GGG", "G G", "GGG", 'G', net.minecraft.init.Items.gold_ingot);
    	GameRegistry.addRecipe(new ItemStack(Items.roseGoldWeddingRing),
    			"GGG", "G G", "GGG", 'G', Items.roseGoldIngot);
    	GameRegistry.addRecipe(new ItemStack(Blocks.roseGoldBlock),
    			"GGG", "GGG", "GGG", 'G', Items.roseGoldIngot);
    	GameRegistry.addRecipe(new ItemStack(Items.matchmakersRing),
    			"III", "I I", "III", 'I', net.minecraft.init.Items.iron_ingot);
    	
    	//Smeltings
    	GameRegistry.addSmelting(Blocks.roseGoldOre, new ItemStack(Items.roseGoldIngot), 5);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
    	event.registerServerCommand(new CommandMCA());
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
	
	public static CreativeTabs getCreativeTab()
	{
		return creativeTab;
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
}
