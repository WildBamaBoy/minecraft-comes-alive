package mca.core;

import mca.api.API;
import mca.command.CommandAdminMCA;
import mca.command.CommandMCA;
import mca.core.forge.EventHooks;
import mca.core.forge.GuiHandler;
import mca.core.forge.NetMCA;
import mca.core.forge.ServerProxy;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.core.minecraft.RoseGoldOreGenerator;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.util.Util;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Random;

@Mod(modid = MCA.MODID, name = MCA.NAME, version = MCA.VERSION, guiFactory = "mca.client.MCAGuiFactory")
public class MCA {
    public static final String MODID = "mca";
    public static final String NAME = "Minecraft Comes Alive";
    public static final String VERSION = "6.0.2";
    @SidedProxy(clientSide = "mca.core.forge.ClientProxy", serverSide = "mca.core.forge.ServerProxy")
    public static ServerProxy proxy;
    public static CreativeTabs creativeTab;
    @Mod.Instance
    private static MCA instance;
    private static Logger logger;
    private static Localizer localizer;
    private static Config config;
    private static long startupTimestamp;
    public static String latestVersion = "";
    public static boolean updateAvailable = false;
    public String[] supporters = new String[0];

    public static Logger getLog() {
        return logger;
    }

    public static MCA getInstance() {
        return instance;
    }

    public static Localizer getLocalizer() {
        return localizer;
    }

    public static Config getConfig() {
        return config;
    }

    public static long getStartupTimestamp() {
        return startupTimestamp;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        startupTimestamp = new Date().getTime();
        instance = this;
        logger = event.getModLog();
        proxy.registerEntityRenderers();
        localizer = new Localizer();
        config = new Config(event);
        creativeTab = new CreativeTabs("MCA") {
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(ItemsMCA.ENGAGEMENT_RING);
            }
        };
        MinecraftForge.EVENT_BUS.register(new EventHooks());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        NetMCA.registerMessages();

        supporters = Util.httpGet("https://minecraftcomesalive.com/api/supporters").split(",");
        MCA.getLog().info("Loaded " + supporters.length + " supporters.");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new RoseGoldOreGenerator(), MCA.getConfig().roseGoldSpawnWeight);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "EntityVillagerMCA"), EntityVillagerMCA.class, EntityVillagerMCA.class.getSimpleName(), 1120, this, 50, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "GrimReaperMCA"), EntityGrimReaper.class, EntityGrimReaper.class.getSimpleName(), 1121, this, 50, 2, true);
        ProfessionsMCA.registerCareers();

        proxy.registerModelMeshers();
        ItemsMCA.assignCreativeTabs();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        API.init();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMCA());
        event.registerServerCommand(new CommandAdminMCA());
    }

    public String getRandomSupporter() {
        if (supporters.length > 0) {
            return supporters[new Random().nextInt(supporters.length)];
        } else {
            return API.getRandomName(EnumGender.getRandom());
        }
    }
}
