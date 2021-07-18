package mca;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface TagsMCA {
    public interface Blocks {
        Tag<Block> ORES_ROSE_GOLD = forge("ores/rose_gold");

        Tag<Block> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        Tag<Block> TOMBSTONES = mca("tombstones");
        Tag<Block> STORAGE_BLOCKS_VILLAGER_SPAWNER = mca("storage_blocks/villager_spawner");
        Tag<Block> STORAGE_BLOCKS_TOMBSTONE = mca("storage_blocks/tombstone");
        Tag<Block> STORAGE_BLOCKS_JEWELER_WORKBENCH = mca("storage_blocks/jeweler_workbench");

        static void bootstrap() {}

        private static Tag<Block> forge(String path) {
            return TagRegistry.block(new Identifier("forge", path));
        }

        private static Tag<Block> mca(String path) {
            return TagRegistry.block(new Identifier(MCA.MOD_ID, path));
        }
    }

    public interface Items {
        Tag<Item> ORES_ROSE_GOLD = forge("ores/rose_gold");

        Tag<Item> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        Tag<Item> STORAGE_BLOCKS_VILLAGER_SPAWNER = forge("storage_blocks/villager_spawner");
        Tag<Item> STORAGE_BLOCKS_TOMBSTONE = mca("storage_blocks/tombstone");
        Tag<Item> STORAGE_BLOCKS_JEWELER_WORKBENCH = mca("storage_blocks/jeweler_workbench");

        Tag<Item> INGOTS_ROSE_GOLD = mca("ingots/rose_gold");
        Tag<Item> ROSE_GOLD_DUST = mca("dusts/rose_gold_dust");
        Tag<Item> GOLD_DUST = mca("dusts/gold_dust");
        Tag<Item> EGG_MALE = mca("egg_male");
        Tag<Item> EGG_FEMALE = mca("egg_female");
        Tag<Item> BABY_BOY = mca("baby_boy");
        Tag<Item> BABY_GIRL = mca("baby_girl");
        Tag<Item> WEDDING_RING = mca("wedding_ring");
        Tag<Item> WEDDING_RING_RG = mca("wedding_ring_rg");
        Tag<Item> ENGAGEMENT_RING = mca("engagement_ring");
        Tag<Item> ENGAGEMENT_RING_RG = mca("engagement_ring_rg");
        Tag<Item> MATCHMAKERS_RING = mca("matchmakers_ring");
        Tag<Item> VILLAGER_EDITOR = mca("villager_editor");
        Tag<Item> STAFF_OF_LIFE = mca("staff_of_life");
        Tag<Item> WHISTLE = mca("whistle");
        Tag<Item> BLUEPRINT = mca("blueprint");
        Tag<Item> LECTERN_BOOKS = forge("blueprint");
        Tag<Item> BOOK_ROSE_GOLD = mca("book_rose_gold");
        Tag<Item> BOOK_DEATH = mca("book_death");
        Tag<Item> BOOK_ROMANCE = forge("book_romance");
        Tag<Item> BOOK_FAMILY = forge("book_family");
        Tag<Item> BOOK_INFECTION = forge("book_infection");

        //public static final Tags.IOptionalNamedTag<Item> DYES_ROSE_GOLD = DyeColor.ROSE_GOLD.getTag();

        static void bootstrap() {}

        private static Tag<Item> forge(String path) {
            return TagRegistry.item(new Identifier("forge", path));
        }

        private static Tag<Item> mca(String path) {
            return TagRegistry.item(new Identifier(MCA.MOD_ID, path));
        }
    }
}
