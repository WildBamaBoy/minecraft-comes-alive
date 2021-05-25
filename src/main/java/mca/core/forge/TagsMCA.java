package mca.core.forge;

import mca.core.MCA;
import mca.items.ItemBaby;
import mca.items.ItemBlueprint;
import mca.items.ItemSpawnEgg;
import mca.items.ItemWhistle;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

public class TagsMCA {
    public static final class Blocks {
        public static final ITag.INamedTag<Block> ORES_ROSE_GOLD= forge("ores/rose_gold");
        public static final ITag.INamedTag<Block> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        public static final ITag.INamedTag<Block> STORAGE_BLOCKS_VILLAGER_SPAWNER = forge("storage_blocks/villager_spawner");
        public static final ITag.INamedTag<Block> STORAGE_BLOCKS_TOMBSTONE = forge("storage_blocks/tombstone");
        public static final ITag.INamedTag<Block> STORAGE_BLOCKS_JEWELER_WORKBENCH = forge("storage_blocks/jeweler_workbench");

        private Blocks() {}

        private static ITag.INamedTag<Block> forge(String path) {
            return BlockTags.bind(new ResourceLocation("forge", path).toString());
        }

        private static ITag.INamedTag<Block> mca(String path) {
            return BlockTags.bind(new ResourceLocation(MCA.MOD_ID, path).toString());
        }
    }

    public static final class Items {
        public static final ITag.INamedTag<Item> ORES_ROSE_GOLD = forge("ores/rose_gold");
        public static final ITag.INamedTag<Item> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        public static final ITag.INamedTag<Item> STORAGE_BLOCKS_VILLAGER_SPAWNER = forge("storage_blocks/villager_spawner");
        public static final ITag.INamedTag<Item> STORAGE_BLOCKS_TOMBSTONE = forge("storage_blocks/tombstone");
        public static final ITag.INamedTag<Item> STORAGE_BLOCKS_JEWELER_WORKBENCH = forge("storage_blocks/jeweler_workbench");

        public static final ITag.INamedTag<Item> INGOTS_ROSE_GOLD = forge("ingots/rose_gold");
        public static final ITag.INamedTag<Item> ROSE_GOLD_DUST = forge("rose_gold_dust");
        public static final ITag.INamedTag<Item> GOLD_DUST = forge("gold_dust");
        public static final ITag.INamedTag<Item> MALE_EGG = forge("egg_male");
        public static final ITag.INamedTag<Item> FEMALE_EGG = forge("egg_female");
        public static final ITag.INamedTag<Item> BABY_BOY = forge("baby_boy");
        public static final ITag.INamedTag<Item> BABY_GIRL = forge("baby_girl");
        public static final ITag.INamedTag<Item> WEDDING_RING = forge("wedding_ring");
        public static final ITag.INamedTag<Item> WEDDING_RING_RG = forge("wedding_ring_rg");
        public static final ITag.INamedTag<Item> ENGAGEMENT_RING = forge("engagement_ring");
        public static final ITag.INamedTag<Item> ENGAGEMENT_RING_RG = forge("engagement_ring_rg");
        public static final ITag.INamedTag<Item> MATCHMAKERS_RING = forge("matchmakers_ring");
        public static final ITag.INamedTag<Item> VILLAGER_EDITOR = forge("villager_editor");
        public static final ITag.INamedTag<Item> STAFF_OF_LIFE = forge("staff_of_life");
        public static final ITag.INamedTag<Item> WHISTLE = forge("whistle");
        public static final ITag.INamedTag<Item> BLUEPRINT = forge("blueprint");
        public static final ITag.INamedTag<Item> BOOK_ROSE_GOLD = forge("book_rose_gold");
        public static final ITag.INamedTag<Item> BOOK_DEATH = forge("book_death");
        public static final ITag.INamedTag<Item> BOOK_ROMANCE = forge("book_romance");
        public static final ITag.INamedTag<Item> BOOK_FAMILY = forge("book_family");
        public static final ITag.INamedTag<Item> BOOK_INFECTION = forge("book_infection");

        private Items() {}

        private static ITag.INamedTag<Item> forge(String path) {
            return ItemTags.bind(new ResourceLocation("forge", path).toString());
        }

        private static ITag.INamedTag<Item> mca(String path) {
            return ItemTags.bind(new ResourceLocation(MCA.MOD_ID, path).toString());
        }
    }
}
