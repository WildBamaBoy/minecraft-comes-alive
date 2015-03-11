package radixcore.forge.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.registry.GameRegistry;

public final class SimpleOreGenerator implements IWorldGenerator
{
	private final Block spawnBlock;
	private final int maxVeinSize;
	private final int lowestSpawnLevel;
	private final int highestSpawnLevel;
	private final boolean canSpawnInNether;
	private final boolean canSpawnInOverworld;
	
	public SimpleOreGenerator(Block spawnBlock, int maxVeinSize, int lowestSpawnLevel, int highestSpawnLevel, boolean canSpawnInOverworld, boolean canSpawnInNether)
	{
		this.spawnBlock = spawnBlock;
		this.maxVeinSize = maxVeinSize;
		this.lowestSpawnLevel = lowestSpawnLevel;
		this.highestSpawnLevel = highestSpawnLevel;
		this.canSpawnInOverworld = canSpawnInOverworld;
		this.canSpawnInNether = canSpawnInNether;
	}
	
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		switch (world.provider.dimensionId)
		{
		case -1:
			generateNether(world, random, chunkX * 16, chunkZ * 16);;
		case 0:
			generateSurface(world, random, chunkX * 16, chunkZ * 16);
		default:
			break;
		}
	}

	private void generateSurface(World world, Random random, int x, int z)
	{
		if (canSpawnInOverworld)
		{
			this.addOreSpawn(spawnBlock, world, random, x, z, 16, 16, maxVeinSize, 5);
		}
	}

	private void generateNether(World world, Random random, int x, int z)
	{
		if (canSpawnInNether)
		{
			int xPos = x + random.nextInt(16);
			int yPos = 10 + random.nextInt(128);
			int zPos = z + random.nextInt(16);
			(new WorldGenMinable(spawnBlock, 1, 15, Blocks.netherrack)).generate(world, random, xPos, yPos, zPos);
		}
	}

	public void addOreSpawn(Block block, World world, Random random, int blockPosX, int blockPosZ, int maxX, int maxZ, int maxVeinSize, int chancesToSpawn)
	{
		int range = highestSpawnLevel - lowestSpawnLevel;

		for (int x = 0; x < chancesToSpawn; x++)
		{
			int posX = blockPosX + random.nextInt(maxX);
			int posY = lowestSpawnLevel + random.nextInt(range);
			int posZ = blockPosZ + random.nextInt(maxZ);
			(new WorldGenMinable(block, maxVeinSize)).generate(world, random, posX, posY, posZ);
		}
	}
	
	public static void register(SimpleOreGenerator generator, int weight)
	{
		GameRegistry.registerWorldGenerator(generator, weight);
	}
}