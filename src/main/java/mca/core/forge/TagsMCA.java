package mca.core.forge;

import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class TagsMCA {
    public static final class Blocks {
        public static final Tag.Identified<Block> ORES_ROSE_GOLD = forge("ores/rose_gold");

        public static final Tag.Identified<Block> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        public static final Tag.Identified<Block> STORAGE_BLOCKS_VILLAGER_SPAWNER = mca("storage_blocks/villager_spawner");
        public static final Tag.Identified<Block> STORAGE_BLOCKS_TOMBSTONE = mca("storage_blocks/tombstone");
        public static final Tag.Identified<Block> STORAGE_BLOCKS_JEWELER_WORKBENCH = mca("storage_blocks/jeweler_workbench");

        private Blocks() {
        }

        private static Tag.Identified<Block> forge(String path) {
            return BlockTags.register(new Identifier("forge", path).toString());
        }

        private static Tag.Identified<Block> mca(String path) {
            return BlockTags.register(new Identifier(MCA.MOD_ID, path).toString());
        }
    }

    public static final class Items {
        public static final Tag.Identified<Item> ORES_ROSE_GOLD = forge("ores/rose_gold");

        public static final Tag.Identified<Item> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        public static final Tag.Identified<Item> STORAGE_BLOCKS_VILLAGER_SPAWNER = forge("storage_blocks/villager_spawner");
        public static final Tag.Identified<Item> STORAGE_BLOCKS_TOMBSTONE = mca("storage_blocks/tombstone");
        public static final Tag.Identified<Item> STORAGE_BLOCKS_JEWELER_WORKBENCH = mca("storage_blocks/jeweler_workbench");

        public static final Tag.Identified<Item> INGOTS_ROSE_GOLD = mca("ingots/rose_gold");
        public static final Tag.Identified<Item> ROSE_GOLD_DUST = mca("dusts/rose_gold_dust");
        public static final Tag.Identified<Item> GOLD_DUST = mca("dusts/gold_dust");
        public static final Tag.Identified<Item> EGG_MALE = mca("egg_male");
        public static final Tag.Identified<Item> EGG_FEMALE = mca("egg_female");
        public static final Tag.Identified<Item> BABY_BOY = mca("baby_boy");
        public static final Tag.Identified<Item> BABY_GIRL = mca("baby_girl");
        public static final Tag.Identified<Item> WEDDING_RING = mca("wedding_ring");
        public static final Tag.Identified<Item> WEDDING_RING_RG = mca("wedding_ring_rg");
        public static final Tag.Identified<Item> ENGAGEMENT_RING = mca("engagement_ring");
        public static final Tag.Identified<Item> ENGAGEMENT_RING_RG = mca("engagement_ring_rg");
        public static final Tag.Identified<Item> MATCHMAKERS_RING = mca("matchmakers_ring");
        public static final Tag.Identified<Item> VILLAGER_EDITOR = mca("villager_editor");
        public static final Tag.Identified<Item> STAFF_OF_LIFE = mca("staff_of_life");
        public static final Tag.Identified<Item> WHISTLE = mca("whistle");
        public static final Tag.Identified<Item> BLUEPRINT = mca("blueprint");
        public static final Tag.Identified<Item> LECTERN_BOOKS = forge("blueprint");
        public static final Tag.Identified<Item> BOOK_ROSE_GOLD = mca("book_rose_gold");
        public static final Tag.Identified<Item> BOOK_DEATH = mca("book_death");
        public static final Tag.Identified<Item> BOOK_ROMANCE = forge("book_romance");
        public static final Tag.Identified<Item> BOOK_FAMILY = forge("book_family");
        public static final Tag.Identified<Item> BOOK_INFECTION = forge("book_infection");

        //public static final Tags.IOptionalNamedTag<Item> DYES_ROSE_GOLD = DyeColor.ROSE_GOLD.getTag();

        private Items() {
        }

        private static Tag.Identified<Item> forge(String path) {
            return ItemTags.register(new Identifier("forge", path).toString());
        }

        private static Tag.Identified<Item> mca(String path) {
            return ItemTags.register(new Identifier(MCA.MOD_ID, path).toString());
        }
    }
}
