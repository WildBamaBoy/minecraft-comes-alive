package mca;

import mca.cobalt.registration.Registration;
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

        static Tag<Block> forge(String path) {
            return Registration.ObjectBuilders.Tags.block(new Identifier("forge", path));
        }

        static Tag<Block> mca(String path) {
            return Registration.ObjectBuilders.Tags.block(new Identifier(MCA.MOD_ID, path));
        }
    }

    public interface Items {
        Tag<Item> ORES_ROSE_GOLD = forge("ores/rose_gold");

        Tag<Item> STORAGE_BLOCKS_ROSE_GOLD = forge("storage_blocks/rose_gold");
        Tag<Item> STORAGE_BLOCKS_VILLAGER_SPAWNER = forge("storage_blocks/villager_spawner");
        Tag<Item> STORAGE_BLOCKS_TOMBSTONE = mca("storage_blocks/tombstone");
        Tag<Item> STORAGE_BLOCKS_JEWELER_WORKBENCH = mca("storage_blocks/jeweler_workbench");

        Tag<Item> VILLAGER_EGGS = mca("villager_eggs");
        Tag<Item> ZOMBIE_EGGS = mca("zombie_eggs");

        Tag<Item> BABIES = mca("babies");

        //public static final Tags.IOptionalNamedTag<Item> DYES_ROSE_GOLD = DyeColor.ROSE_GOLD.getTag();

        static void bootstrap() {}

        static Tag<Item> forge(String path) {
            return Registration.ObjectBuilders.Tags.item(new Identifier("forge", path));
        }

        static Tag<Item> mca(String path) {
            return Registration.ObjectBuilders.Tags.item(new Identifier(MCA.MOD_ID, path));
        }
    }
}
