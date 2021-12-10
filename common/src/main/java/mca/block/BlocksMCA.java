package mca.block;

import mca.MCA;
import mca.TagsMCA;
import mca.cobalt.registration.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public interface BlocksMCA {
    Block ROSE_GOLD_BLOCK = register("rose_gold_block", new Block(Block.Settings.copy(Blocks.GOLD_BLOCK)));
    Block ROSE_GOLD_ORE = register("rose_gold_ore", new OreBlock(Block.Settings.copy(Blocks.GOLD_ORE)));

    Block JEWELER_WORKBENCH = register("jeweler_workbench", new JewelerWorkbench(Block.Settings.copy(Blocks.OAK_WOOD).nonOpaque()));
    Block INFERNAL_FLAME = register("infernal_flame", new InfernalFlameBlock(Block.Settings.copy(Blocks.SOUL_FIRE)));

    Block GRAVELLING_HEADSTONE = register("gravelling_headstone", new TombstoneBlock(Block.Settings.copy(Blocks.STONE).nonOpaque(), 100, 50, new Vec3d(0, -25, 40), -90.0f,true, TombstoneBlock.GRAVELLING_SHAPE));
    Block UPRIGHT_HEADSTONE = register("upright_headstone", new TombstoneBlock(Block.Settings.copy(Blocks.STONE).nonOpaque(), 90, 50, new Vec3d(0, -55, 23),0.0f, true, TombstoneBlock.UPRIGHT_SHAPE));
    Block SLANTED_HEADSTONE = register("slanted_headstone", new TombstoneBlock(Block.Settings.copy(Blocks.STONE).nonOpaque(), 100, 15, new Vec3d(0, -25, 10), -45.0f,true, TombstoneBlock.SLANTED_SHAPE));
    Block CROSS_HEADSTONE = register("cross_headstone", new TombstoneBlock(Block.Settings.copy(Blocks.STONE).nonOpaque(), 80, 15, new Vec3d(0, -13, 15),-45.0f, true, TombstoneBlock.CROSS_SHAPE));
    Block WALL_HEADSTONE = register("wall_headstone", new TombstoneBlock(Block.Settings.copy(Blocks.STONE).nonOpaque(), 100, 15, new Vec3d(0, -25, 40),0.0f, false, TombstoneBlock.WALL_SHAPE));

    static void bootstrap() {
        TagsMCA.Blocks.bootstrap();
        BlockEntityTypesMCA.bootstrap();
    }

    static <T extends Block> T register(String name, T block) {
        return Registration.register(Registry.BLOCK, new Identifier(MCA.MOD_ID, name), block);
    }
}
