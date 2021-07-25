package mca.item;

import mca.MCA;
import mca.TagsMCA;
import mca.block.BlocksMCA;
import mca.cobalt.registration.Registration;
import mca.crafting.recipe.RecipesMCA;
import mca.entity.EntitiesMCA;
import mca.entity.ai.relationship.Gender;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface ItemsMCA {
    Item EGG_MALE = register("egg_male", new SpawnEggItem(EntitiesMCA.MALE_VILLAGER, 0x5e9aff, 0x3366bc, baseProps()));
    Item EGG_FEMALE = register("egg_female", new SpawnEggItem(EntitiesMCA.FEMALE_VILLAGER, 0xe85ca1, 0xe3368c, baseProps()));
    Item BABY_BOY = register("baby_boy", new BabyItem(Gender.MALE, baseProps().maxCount(1)));
    Item BABY_GIRL = register("baby_girl", new BabyItem(Gender.FEMALE, baseProps().maxCount(1)));

    Item WEDDING_RING = register("wedding_ring", new WeddingRingItem(unstackableProps()));
    Item WEDDING_RING_RG = register("wedding_ring_rg", new WeddingRingItem(unstackableProps()));
    Item ENGAGEMENT_RING = register("engagement_ring", new WeddingRingItem(unstackableProps(), 0.5F));
    Item ENGAGEMENT_RING_RG = register("engagement_ring_rg", new WeddingRingItem(unstackableProps(), 0.5F));
    Item MATCHMAKERS_RING = register("matchmakers_ring", new MatchmakersRingItem(baseProps().maxCount(2)));

    Item VILLAGER_EDITOR = register("villager_editor", new Item(baseProps()));
    Item STAFF_OF_LIFE = register("staff_of_life", new StaffOfLifeItem(baseProps().maxDamage(5)));
    Item WHISTLE = register("whistle", new WhistleItem(baseProps()));
    Item BLUEPRINT = register("blueprint", new BlueprintItem(baseProps()));

    Item BOOK_ROSE_GOLD = register("book_rose_gold", new Item(baseProps()));
    Item BOOK_DEATH = register("book_death", new Item(baseProps()));
    Item BOOK_ROMANCE = register("book_romance", new Item(baseProps()));
    Item BOOK_FAMILY = register("book_family", new Item(baseProps()));
    Item BOOK_INFECTION = register("book_infection", new Item(baseProps()));

    Item GOLD_DUST = register("gold_dust", new Item(baseProps()));
    Item ROSE_GOLD_DUST = register("rose_gold_dust", new Item(baseProps()));
    Item ROSE_GOLD_INGOT = register("rose_gold_ingot", new Item(baseProps()));

    Item DIVORCE_PAPERS = register("divorce_papers", new TooltippedItem(baseProps()));

    Item ROSE_GOLD_BLOCK = register("rose_gold_block", new BlockItem(BlocksMCA.ROSE_GOLD_BLOCK, baseProps()));
    Item ROSE_GOLD_ORE = register("rose_gold_ore", new BlockItem(BlocksMCA.ROSE_GOLD_ORE, baseProps()));

    Item VILLAGER_SPAWNER = register("villager_spawner", new BlockItem(BlocksMCA.VILLAGER_SPAWNER, baseProps()));
    Item JEWELER_WORKBENCH = register("jeweler_workbench", new BlockItem(BlocksMCA.JEWELER_WORKBENCH, baseProps()));

    Item UPRIGHT_HEADSTONE = register("upright_headstone", new BlockItem(BlocksMCA.UPRIGHT_HEADSTONE, baseProps()));
    Item SLANTED_HEADSTONE = register("slanted_headstone", new BlockItem(BlocksMCA.SLANTED_HEADSTONE, baseProps()));
    Item CROSS_HEADSTONE = register("cross_headstone", new BlockItem(BlocksMCA.CROSS_HEADSTONE, baseProps()));

    static void bootstrap() {
        TagsMCA.Blocks.bootstrap();
        RecipesMCA.bootstrap();
    }

    static Item register(String name, Item item) {
        return Registration.register(Registry.ITEM, new Identifier(MCA.MOD_ID, name), item);
    }

    static Item.Settings baseProps() {
        return new Item.Settings().group(ItemGroupMCA.MCA_GROUP);
    }

    static Item.Settings unstackableProps() {
        return baseProps().maxCount(1);
    }
}