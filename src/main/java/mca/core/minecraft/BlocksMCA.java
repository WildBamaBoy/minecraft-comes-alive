package mca.core.minecraft;

import mca.core.forge.Registration;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;


public final class BlocksMCA {
    public static final RegistryObject<Block> ROSE_GOLD_BLOCK = register("rose_gold_block", () ->
            new Block(AbstractBlock.Properties.of(Material.METAL, MaterialColor.GOLD).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.METAL)));

    public static final RegistryObject<Block> ROSE_GOLD_ORE = register("rose_gold_ore", () ->
            new OreBlock(AbstractBlock.Properties.of(Material.STONE).requiresCorrectToolForDrops().harvestLevel(2).harvestTool(ToolType.PICKAXE).strength(3.0F, 3.0F).sound(SoundType.STONE)));

//    public static final RegistryObject<Block> VILLAGER_SPAWNER = register("villager_spawner",() ->
//            new SpawnerBlock(AbstractBlock.Properties.of(Material.METAL).speedFactor(VillagerData.getMinXpPerLevel(7)).requiresCorrectToolForDrops().sound(SoundType.METAL)));

//    public static final RegistryObject<Block> TOMBSTONE = register("tombstone",() ->
//            new Block(AbstractBlock.Properties.of(Material.STONE).sound(SoundType.STONE)));

//TODO <Block> JEWELER_WORKBENCH -> profession of a jeweler


    public static void register() {
    }


    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> block) {
        return Registration.BLOCKS.register(name, block);
    }

    protected static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> ret = registerNoItem(name, block);
        Registration.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().tab(ItemGroupMCA.MCA)));
        return ret;
    }
}