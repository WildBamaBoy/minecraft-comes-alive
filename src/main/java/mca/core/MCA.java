package mca.core;

import cobalt.items.CItemBasic;
import cobalt.mod.forge.CobaltForgeMod;
import lombok.Getter;
import mca.api.API;
import mca.client.render.RenderVillagerMCA;
import mca.command.CommandMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.items.ItemBaby;
import mca.items.ItemSpawnEgg;
import mca.items.ItemStaffOfLife;
import mca.items.ItemWhistle;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod("mca")
public class MCA extends CobaltForgeMod {
    @Getter
    public static MCA mod;
    private static Config config;

    public MCA() {
        super();
        mod = this;
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

    public static String localize(String key, String... vars) {
        return mod.localizer.localize(key, vars);
    }

    @Override
    public void registerContent() {
        ITEM_MALE_EGG = registerItem("egg_male", new ItemSpawnEgg(EnumGender.MALE, new Item.Properties().tab(TAB)));
        ITEM_FEMALE_EGG = registerItem("egg_female", new ItemSpawnEgg(EnumGender.FEMALE, new Item.Properties().tab(TAB)));
        ITEM_WEDDING_RING = registerItem("wedding_ring", new CItemBasic(new Item.Properties().tab(TAB).stacksTo(1)));
        ITEM_WEDDING_RING_RG = registerItem("wedding_ring_rg", new CItemBasic(new Item.Properties().tab(TAB).stacksTo(1)));
        ITEM_ENGAGEMENT_RING = registerItem("engagement_ring", new CItemBasic(new Item.Properties().tab(TAB).stacksTo(1)));
        ITEM_ENGAGEMENT_RING_RG = registerItem("engagement_ring_rg", new CItemBasic(new Item.Properties().tab(TAB).stacksTo(1)));
        ITEM_MATCHMAKERS_RING = registerItem("matchmakers_ring", new CItemBasic(new Item.Properties().tab(TAB).stacksTo(2)));
        ITEM_BABY_BOY = registerItem("baby_boy", new ItemBaby(new Item.Properties().tab(TAB)));
        ITEM_BABY_GIRL = registerItem("baby_girl", new ItemBaby(new Item.Properties().tab(TAB)));
        ITEM_ROSE_GOLD_INGOT = registerItem("rose_gold_ingot", new CItemBasic(new Item.Properties().tab(TAB)));
        ITEM_ROSE_GOLD_DUST = registerItem("rose_gold_dust", new CItemBasic(new Item.Properties().tab(TAB)));
        ITEM_GOLD_DUST = registerItem("gold_dust", new CItemBasic(new Item.Properties().tab(TAB)));
        ITEM_VILLAGER_EDITOR = registerItem("villager_editor", new CItemBasic(new Item.Properties().tab(TAB)));
        ITEM_STAFF_OF_LIFE = registerItem("staff_of_life", new ItemStaffOfLife(new Item.Properties().tab(TAB)));
        ITEM_WHISTLE = registerItem("whistle", new ItemWhistle(new Item.Properties().tab(TAB)));
        ITEM_BOOK_DEATH = registerItem("book_death", new CItemBasic(new Item.Properties().tab(TAB)));
        ITEM_BOOK_ROMANCE = registerItem("book_romance", new CItemBasic(new Item.Properties().tab(TAB)));
        ITEM_BOOK_FAMILY = registerItem("book_family", new CItemBasic(new Item.Properties().tab(TAB)));
        ITEM_BOOK_ROSE_GOLD = registerItem("book_rose_gold", new CItemBasic(new Item.Properties().tab(TAB)));
        ITEM_BOOK_INFECTION = registerItem("book_infection", new CItemBasic(new Item.Properties().tab(TAB)));

        ENTITYTYPE_VILLAGER = registerEntity(EntityVillagerMCA::new, RenderVillagerMCA::new, EntityClassification.AMBIENT, "villager_mca",
                1.0F, 1.85F);

        PROFESSION_GUARD = registerProfession("guard", PointOfInterestType.ARMORER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);
        PROFESSION_CHILD = registerProfession("child", PointOfInterestType.HOME, SoundEvents.ENTITY_VILLAGER_WORK_FARMER);
    }

    @Override
    public void onSetup() {
        API.init();
        config = new Config(event);
        this.localizer.registerVarParser(str -> str.replaceAll("%Supporter%", getRandomSupporter()));
    }

    @Override
    public void onClientSetup() {
        RenderingRegistry.registerEntityRenderingHandler(MCA.ENTITYTYPE_VILLAGER.get(), RenderVillagerMCA::new);
    }

    @Override
    public void registerCommands(FMLServerStartingEvent event) {
        CommandMCA.register(event.getCommandDispatcher());
    }

    public String getModId() {
        return "mca";
    }

    private String getRandomSupporter() {
        return "";
    }

    public static final ItemGroup TAB = new ItemGroup("mcaTab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ITEM_ENGAGEMENT_RING.get());
        }
    };

    public static RegistryObject<Item> ITEM_MALE_EGG;
    public static RegistryObject<Item> ITEM_FEMALE_EGG;
    public static RegistryObject<Item> ITEM_WEDDING_RING;
    public static RegistryObject<Item> ITEM_WEDDING_RING_RG;
    public static RegistryObject<Item> ITEM_ENGAGEMENT_RING;
    public static RegistryObject<Item> ITEM_ENGAGEMENT_RING_RG;
    public static RegistryObject<Item> ITEM_MATCHMAKERS_RING;
    public static RegistryObject<Item> ITEM_BABY_BOY;
    public static RegistryObject<Item> ITEM_BABY_GIRL;
    public static RegistryObject<Item> ITEM_ROSE_GOLD_INGOT;
    public static RegistryObject<Item> ITEM_ROSE_GOLD_DUST;
    public static RegistryObject<Item> ITEM_GOLD_DUST;
    public static RegistryObject<Item> ITEM_VILLAGER_EDITOR;
    public static RegistryObject<Item> ITEM_STAFF_OF_LIFE;
    public static RegistryObject<Item> ITEM_WHISTLE;
    public static RegistryObject<Item> ITEM_BOOK_DEATH;
    public static RegistryObject<Item> ITEM_BOOK_ROMANCE;
    public static RegistryObject<Item> ITEM_BOOK_FAMILY;
    public static RegistryObject<Item> ITEM_BOOK_ROSE_GOLD;
    public static RegistryObject<Item> ITEM_BOOK_INFECTION;

    public static RegistryObject<EntityType<EntityVillagerMCA>> ENTITYTYPE_VILLAGER;

    public static RegistryObject<VillagerProfession> PROFESSION_CHILD;
    public static RegistryObject<VillagerProfession> PROFESSION_GUARD;
}
