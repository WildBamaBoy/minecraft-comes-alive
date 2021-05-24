package mca.core;

import cobalt.mod.forge.CobaltForgeMod;
import cobalt.network.NetworkHandler;
import lombok.Getter;
import mca.api.API;
import mca.client.render.RenderGrimReaper;
import mca.client.render.RenderVillagerMCA;
import mca.core.forge.EventHooks;
import mca.core.minecraft.ActivityMCA;
import mca.core.minecraft.MemoryModuleTypeMCA;
import mca.core.minecraft.Registration;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.network.*;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod(MCA.MOD_ID)
public class MCA extends CobaltForgeMod {
    public static final String MOD_ID = "mca";
    @Getter
    public static MCA mod;
    public static RegistryObject<EntityType<EntityVillagerMCA>> ENTITYTYPE_VILLAGER;
    public static RegistryObject<EntityType<EntityGrimReaper>> ENTITYTYPE_GRIM_REAPER;
    public static RegistryObject<VillagerProfession> PROFESSION_CHILD;
    public static RegistryObject<VillagerProfession> PROFESSION_GUARD;
    private static Config config;

    public static int tick = 0;

    static {
        NetworkHandler.registerMessage(InteractionVillagerMessage.class);
        NetworkHandler.registerMessage(InteractionServerMessage.class);
        NetworkHandler.registerMessage(BabyNamingVillagerMessage.class);
        NetworkHandler.registerMessage(ReviveVillagerMessage.class);
        NetworkHandler.registerMessage(SavedVillagersRequest.class);
        NetworkHandler.registerMessage(SavedVillagersResponse.class);
        NetworkHandler.registerMessage(GetVillagerRequest.class);
        NetworkHandler.registerMessage(GetVillagerResponse.class);
        NetworkHandler.registerMessage(CallToPlayerMessage.class);
        NetworkHandler.registerMessage(GetVillageRequest.class);
        NetworkHandler.registerMessage(GetVillageResponse.class);
    }

    public MCA() {
        super();
        mod = this;

        //Register class. Registering mod components in the Forge registry (such as items, blocks, sounds, etc.)
        Registration.register();
    }

    public static Config getConfig() {
        return config;
    }

    public static void log(String message) {
        mod.logger.info(message);
    }

    public static void log(String message, Exception e) {
        mod.logger.fatal(e);
    }

    public static void logAndThrow(String message, Exception e) {
        mod.logger.fatal(message, e);
        throw new RuntimeException(e);
    }

    public static StringTextComponent localizeText(String key, String... vars) {
        return new StringTextComponent(localize(key, vars));
    }

    public static String localize(String key, String... vars) {
        return mod.localizer.localize(key, vars);
    }

    @Override
    public void registerContent() {

        ENTITYTYPE_VILLAGER = registerEntity
                (EntityVillagerMCA::new,
                        EntityClassification.AMBIENT,
                        "villager",
                        0.6F, 1.8F);

        ENTITYTYPE_GRIM_REAPER = registerEntity(EntityGrimReaper::new, EntityClassification.MONSTER, "grim_reaper",
                1.0F, 2.6F);

        PROFESSION_GUARD = registerProfession("guard", PointOfInterestType.ARMORER, SoundEvents.VILLAGER_WORK_ARMORER);
        PROFESSION_CHILD = registerProfession("child", PointOfInterestType.HOME, SoundEvents.VILLAGER_WORK_FARMER);

        MemoryModuleTypeMCA.init();
        ActivityMCA.init();
    }

    @Override
    public void onSetup() {
        API.init();
        config = new Config();

        // depricated, will change in 1.17
        GlobalEntityTypeAttributes.put(ENTITYTYPE_VILLAGER.get(), EntityVillagerMCA.createAttributes().build());
        GlobalEntityTypeAttributes.put(ENTITYTYPE_GRIM_REAPER.get(), EntityGrimReaper.createAttributes().build());

        MinecraftForge.EVENT_BUS.register(new EventHooks());
    }

    /*public static final ItemGroup TAB = new ItemGroup("mcaTab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ITEM_ENGAGEMENT_RING.get());
        }
    };*/

    /*public static RegistryObject<Item> ITEM_MALE_EGG;
    public static RegistryObject<Item> ITEM_FEMALE_EGG;
    //public static RegistryObject<Item> ITEM_WEDDING_RING;
    //public static RegistryObject<Item> ITEM_WEDDING_RING_RG;
    //public static RegistryObject<Item> ITEM_ENGAGEMENT_RING;
    //public static RegistryObject<Item> ITEM_ENGAGEMENT_RING_RG;
    //public static RegistryObject<Item> ITEM_MATCHMAKERS_RING;
    public static RegistryObject<Item> ITEM_BABY_BOY;
    public static RegistryObject<Item> ITEM_BABY_GIRL;
    //public static RegistryObject<Item> ITEM_ROSE_GOLD_INGOT;
    public static RegistryObject<Item> ITEM_ROSE_GOLD_DUST;
    public static RegistryObject<Item> ITEM_GOLD_DUST;
    public static RegistryObject<Item> ITEM_VILLAGER_EDITOR;
    public static RegistryObject<Item> ITEM_STAFF_OF_LIFE;
    public static RegistryObject<Item> ITEM_WHISTLE;
    public static RegistryObject<Item> ITEM_BOOK_DEATH;
    public static RegistryObject<Item> ITEM_BOOK_ROMANCE;
    public static RegistryObject<Item> ITEM_BOOK_FAMILY;
    //public static RegistryObject<Item> ITEM_BOOK_ROSE_GOLD;
    public static RegistryObject<Item> ITEM_BOOK_INFECTION;
    public static RegistryObject<Item> ITEM_BLUEPRINT;*/

    @Override
    public void onClientSetup() {
        RenderingRegistry.registerEntityRenderingHandler(MCA.ENTITYTYPE_VILLAGER.get(), RenderVillagerMCA::new);
        RenderingRegistry.registerEntityRenderingHandler(MCA.ENTITYTYPE_GRIM_REAPER.get(), RenderGrimReaper::new);
    }

    @Override
    public void registerCommands(FMLServerStartingEvent event) {

    }

    public String getModId() {
        return "mca";
    }

    public String getRandomSupporter() {
        return "";
    }
}
