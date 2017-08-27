package mca.blocks;

import java.util.List;
import java.util.Random;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;

public class BlockVillagerSpawner extends Block
{
	public BlockVillagerSpawner()
	{
		super(Material.IRON);

		setTickRandomly(true);
		setHardness(1.0F);
	}

	public int tickRate()
	{
		return Time.MINUTE * 3;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random) 
	{
		super.updateTick(world, pos, state, random);

		Point3D point = new Point3D(pos.getX(), pos.getY(), pos.getZ());
		List<EntityVillagerMCA> nearbyEntities = RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, world, point, 32);
		int nearbyHumans = nearbyEntities.size();

		if (nearbyHumans < MCA.getConfig().villagerSpawnerCap)
		{
			int spawnY = pos.getY();
			boolean continueSpawning = false;

			while (spawnY < 256)
			{
				Block block = RadixBlocks.getBlock(world, pos.getX(), spawnY, pos.getZ());
				Block blockAbove = RadixBlocks.getBlock(world, pos.getX(), spawnY + 1, pos.getZ());

				if (block == Blocks.AIR && blockAbove == Blocks.AIR)
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
				final EntityVillagerMCA human = new EntityVillagerMCA(world);
				human.attributes.assignRandomGender();
				human.attributes.assignRandomName();
				human.attributes.assignRandomPersonality();
				human.attributes.assignRandomProfession();
				human.attributes.assignRandomSkin();
				human.attributes.assignRandomScale();
				human.setPositionAndRotation((double) pos.getX() + 0.5F, (double) spawnY, (double) pos.getZ() + 0.5F, (float)random.nextInt(360) + 1, 0.0F);
				world.spawnEntity(human);
			}
		}

		world.scheduleUpdate(pos, this, tickRate());
	}
}
