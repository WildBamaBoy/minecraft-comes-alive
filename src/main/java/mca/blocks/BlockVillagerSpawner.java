package mca.blocks;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import radixcore.constant.Time;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;

public class BlockVillagerSpawner extends Block
{
	public BlockVillagerSpawner()
	{
		super(Material.iron);

		setBlockName("VillagerSpawner");
		setBlockTextureName("mca:VillagerSpawner");
		setCreativeTab(MCA.getCreativeTabMain());
		setTickRandomly(true);
		setHardness(1.0F);

		GameRegistry.registerBlock(this, "VillagerSpawner");
	}

	public int tickRate()
	{
		return Time.MINUTE * 3;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) 
	{
		super.updateTick(world, x, y, z, random);

		List<Entity> nearbyEntities = RadixLogic.getAllEntitiesWithinDistanceOfCoordinates(world, x, y, z, 32);
		int nearbyHumans = 0;

		for (Entity entity : nearbyEntities)
		{
			if (entity instanceof EntityHuman)
			{
				nearbyHumans++;
			}
		}

		if (nearbyHumans < MCA.getConfig().villagerSpawnerCap)
		{
			int spawnY = y;
			boolean continueSpawning = false;

			while (spawnY < 256)
			{
				Block block = BlockHelper.getBlock(world, x, spawnY, z);
				Block blockAbove = BlockHelper.getBlock(world, x, spawnY + 1, z);

				if (block == Blocks.air && blockAbove == Blocks.air)
				{
					continueSpawning = true;
					break;
				}

				else
				{
					spawnY++;
				}
			}

			if (continueSpawning)
			{
				final EntityHuman human = new EntityHuman(world, world.rand.nextBoolean());
				human.setPositionAndRotation((double) x + 0.5F, (double) spawnY, (double) z + 0.5F, (float)random.nextInt(360) + 1, 0.0F);
				world.spawnEntityInWorld(human);
			}
		}

		world.scheduleBlockUpdate(x, y, z, this, tickRate());
	}
}
