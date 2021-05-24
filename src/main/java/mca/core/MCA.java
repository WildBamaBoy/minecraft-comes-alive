package mca.core;

import cobalt.mod.forge.CobaltForgeMod;
import cobalt.network.NetworkHandler;
import lombok.Getter;
import mca.api.API;
import mca.client.render.RenderGrimReaper;
import mca.client.render.RenderVillagerMCA;
import mca.core.forge.EventHooks;
import mca.core.minecraft.ItemGroupMCA;
import mca.core.minecraft.Registration;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.items.*;
import mca.network.*;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
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
    @Getter
    public static MCA mod;
    private static Config config;

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

    public static final String MOD_ID = "mca";

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
/*        ITEM_MALE_EGG = registerItem("egg_male", new ItemSpawnEgg(EnumGender.MALE, new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_FEMALE_EGG = registerItem("egg_female", new ItemSpawnEgg(EnumGender.FEMALE, new Item.Properties().tab(ItemGroupMCA.MCA)));
        //ITEM_WEDDING_RING = registerItem("wedding_ring", new ItemWeddingRing(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(1)));
        //ITEM_WEDDING_RING_RG = registerItem("wedding_ring_rg", new ItemWeddingRingRG(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(1)));
        //ITEM_ENGAGEMENT_RING = registerItem("engagement_ring", new ItemEngagementRing(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(1)));
        //ITEM_ENGAGEMENT_RING_RG = registerItem("engagement_ring_rg", new ItemEngagementRingRG(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(1)));
        //ITEM_MATCHMAKERS_RING = registerItem("matchmakers_ring", new ItemMatchmakersRing(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(2)));
        ITEM_BABY_BOY = registerItem("baby_boy", new ItemBaby(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_BABY_GIRL = registerItem("baby_girl", new ItemBaby(new Item.Properties().tab(ItemGroupMCA.MCA)));
        //ITEM_ROSE_GOLD_INGOT = registerItem("rose_gold_ingot", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_ROSE_GOLD_DUST = registerItem("rose_gold_dust", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_GOLD_DUST = registerItem("gold_dust", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_VILLAGER_EDITOR = registerItem("villager_editor", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_STAFF_OF_LIFE = registerItem("staff_of_life", new ItemStaffOfLife(new Item.Properties().tab(ItemGroupMCA.MCA).durability(5)));
        ITEM_WHISTLE = registerItem("whistle", new ItemWhistle(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_BOOK_DEATH = registerItem("book_death", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_BOOK_ROMANCE = registerItem("book_romance", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_BOOK_FAMILY = registerItem("book_family", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        //ITEM_BOOK_ROSE_GOLD = registerItem("book_rose_gold", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_BOOK_INFECTION = registerItem("book_infection", new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
        ITEM_BLUEPRINT = registerItem("blueprint", new ItemBlueprint(new Item.Properties().tab(ItemGroupMCA.MCA)));
*/
        ENTITYTYPE_VILLAGER = registerEntity(EntityVillagerMCA::new, EntityClassification.AMBIENT, "villager",
                0.6F, 1.8F);

        ENTITYTYPE_GRIM_REAPER = registerEntity(EntityGrimReaper::new, EntityClassification.MONSTER, "grim_reaper",
                1.0F, 2.6F);

        PROFESSION_GUARD = registerProfession("guard", PointOfInterestType.ARMORER, SoundEvents.VILLAGER_WORK_ARMORER);
        PROFESSION_CHILD = registerProfession("child", PointOfInterestType.HOME, SoundEvents.VILLAGER_WORK_FARMER);
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

    public static RegistryObject<EntityType<EntityVillagerMCA>> ENTITYTYPE_VILLAGER;
    public static RegistryObject<EntityType<EntityGrimReaper>> ENTITYTYPE_GRIM_REAPER;

    public static RegistryObject<VillagerProfession> PROFESSION_CHILD;
    public static RegistryObject<VillagerProfession> PROFESSION_GUARD;
}
