package mca.core.minecraft;

import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public final class RoseGoldOreGenerator implements IWorldGenerator {
    public RoseGoldOreGenerator() {
    }

    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() == 0) generateSurface(world, random, chunkX * 16, chunkZ * 16);
    }

    private void generateSurface(World world, Random random, int x, int z) {
        this.addOreSpawn(BlocksMCA.ROSE_GOLD_ORE, world, random, x, z, 16, 16, 6, 5);
    }

    public void addOreSpawn(Block block, World world, Random random, int blockPosX, int blockPosZ, int maxX, int maxZ, int maxVeinSize, int chancesToSpawn) {
        if (block == BlocksMCA.ROSE_GOLD_ORE && !MCA.getConfig().allowRoseGoldGeneration) block = Blocks.STONE;

        int range = 25;
        for (int x = 0; x < chancesToSpawn; x++) {
            int posX = blockPosX + random.nextInt(maxX);
            int posY = 12 + random.nextInt(range);
            int posZ = blockPosZ + random.nextInt(maxZ);
            (new WorldGenMinable(block.getDefaultState(), maxVeinSize)).generate(world, random, new BlockPos(posX, posY, posZ));
        }
    }
}