package mca.core.minecraft;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

//TODO
@net.minecraftforge.registries.ObjectHolder("minecraft")
public final class BlocksMCA {
    public static final Block ROSE_GOLD_BLOCK = register("rose_gold_block", new Block(AbstractBlock.Properties.of(Material.METAL, MaterialColor.GOLD).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.METAL)));
    public static final Block ROSE_GOLD_ORE = register("rose_gold_ore", new OreBlock(AbstractBlock.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));

    private static Block register(String p_222382_0_, Block p_222382_1_) {
        return Registry.register(Registry.BLOCK, p_222382_0_, p_222382_1_);
    }

    private static final ArrayList<Block> BLOCKS = new ArrayList<>();
}