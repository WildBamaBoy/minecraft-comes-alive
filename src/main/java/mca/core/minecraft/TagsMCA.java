package mca.core.minecraft;

import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class TagsMCA {
    public static final class Blocks {
        public static final ITag.INamedTag<Block> ORES_ROSE_GOLD= forge("ores/rose_gold");
        public static final ITag.INamedTag<Block> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        public static final ITag.INamedTag<Block> STORAGE_BLOCKS_VILLAGER_SPAWNER = forge("storage_blocks/villager_spawner");
        public static final ITag.INamedTag<Block> TOMBSTONE = forge("storage_blocks/tombstone");

        private static ITag.INamedTag<Block> forge(String path) {
            return BlockTags.bind(new ResourceLocation("forge", path).toString());
        }

        private static ITag.INamedTag<Block> mod(String path) {
            return BlockTags.bind(new ResourceLocation(MCA.MOD_ID, path).toString());
        }
    }

    public static final class Items {
        public static final ITag.INamedTag<Item> ORES_ROSE_GOLD = forge("ores/rose_gold");
        public static final ITag.INamedTag<Item> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        public static final ITag.INamedTag<Item> STORAGE_BLOCKS_VILLAGER_SPAWNER = forge("storage_blocks/villager_spawner");
        public static final ITag.INamedTag<Item> TOMBSTONE = forge("storage_blocks/tombstone");

        public static final ITag.INamedTag<Item> INGOTS_ROSE_GOLD = forge("ingots/rose_gold");


        private static ITag.INamedTag<Item> forge(String path) {
            return ItemTags.bind(new ResourceLocation("forge", path).toString());
        }

        private static ITag.INamedTag<Item> mod(String path) {
            return ItemTags.bind(new ResourceLocation(MCA.MOD_ID, path).toString());
        }
    }
}
