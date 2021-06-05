package mca.core.minecraft;

import mca.core.forge.Registration;
import mca.enums.EnumGender;
import mca.items.*;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

public final class ItemsMCA {
    public static final RegistryObject<Item> EGG_MALE = Registration.ITEMS.register("egg_male", () -> new ItemSpawnEgg(EnumGender.MALE, new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> EGG_FEMALE = Registration.ITEMS.register("egg_female", () -> new ItemSpawnEgg(EnumGender.FEMALE, new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> BABY_BOY = Registration.ITEMS.register("baby_boy", () -> new ItemBaby(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> BABY_GIRL = Registration.ITEMS.register("baby_girl", () -> new ItemBaby(new Item.Properties().tab(ItemGroupMCA.MCA)));

    public static final RegistryObject<Item> WEDDING_RING = Registration.ITEMS.register("wedding_ring", () -> new ItemWeddingRing(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(1)));
    public static final RegistryObject<Item> WEDDING_RING_RG = Registration.ITEMS.register("wedding_ring_rg", () -> new ItemWeddingRingRG(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(1)));
    public static final RegistryObject<Item> ENGAGEMENT_RING = Registration.ITEMS.register("engagement_ring", () -> new ItemEngagementRing(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(1)));
    public static final RegistryObject<Item> ENGAGEMENT_RING_RG = Registration.ITEMS.register("engagement_ring_rg", () -> new ItemEngagementRingRG(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(1)));
    public static final RegistryObject<Item> MATCHMAKERS_RING = Registration.ITEMS.register("matchmakers_ring", () -> new ItemMatchmakersRing(new Item.Properties().tab(ItemGroupMCA.MCA).stacksTo(2)));

    public static final RegistryObject<Item> VILLAGER_EDITOR = Registration.ITEMS.register("villager_editor", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> STAFF_OF_LIFE = Registration.ITEMS.register("staff_of_life", () -> new ItemStaffOfLife(new Item.Properties().tab(ItemGroupMCA.MCA).durability(5)));
    public static final RegistryObject<Item> WHISTLE = Registration.ITEMS.register("whistle", () -> new ItemWhistle(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> BLUEPRINT = Registration.ITEMS.register("blueprint", () -> new ItemBlueprint(new Item.Properties().tab(ItemGroupMCA.MCA)));

    public static final RegistryObject<Item> BOOK_ROSE_GOLD = Registration.ITEMS.register("book_rose_gold", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> BOOK_DEATH = Registration.ITEMS.register("book_death", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> BOOK_ROMANCE = Registration.ITEMS.register("book_romance", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> BOOK_FAMILY = Registration.ITEMS.register("book_family", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> BOOK_INFECTION = Registration.ITEMS.register("book_infection", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));

    public static final RegistryObject<Item> GOLD_DUST = Registration.ITEMS.register("gold_dust", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> ROSE_GOLD_DUST = Registration.ITEMS.register("rose_gold_dust", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));
    public static final RegistryObject<Item> ROSE_GOLD_INGOT = Registration.ITEMS.register("rose_gold_ingot", () -> new Item(new Item.Properties().tab(ItemGroupMCA.MCA)));


    public static void register() {}

    private static Item.Properties baseProps() {
        return new Item.Properties().tab(ItemGroupMCA.MCA);
    }

    private static Item.Properties unstackableProps() {
        return baseProps().stacksTo(1);
    }


}