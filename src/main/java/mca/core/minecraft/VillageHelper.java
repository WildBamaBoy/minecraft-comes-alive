package mca.core.minecraft;

import com.google.common.base.Optional;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.Village;
import net.minecraft.world.World;

import java.util.List;

public class VillageHelper {

    public static void tick(World world) {
        world.getVillageCollection().getVillageList().forEach(v -> spawnGuards(world, v));
    }

    public static void forceSpawnGuards(EntityPlayerMP player) {
        Village nearestVillage = player.world.getVillageCollection().getNearestVillage(player.getPosition(), 100);
        spawnGuards(player.world, nearestVillage);
    }

    public static void forceRaid(EntityPlayerMP player) {
        Village nearestVillage = player.world.getVillageCollection().getNearestVillage(player.getPosition(), 100);
        startRaid(player.world, nearestVillage);
    }

    private static void spawnGuards(World world, Village village) {
        int guardCapacity = village.getNumVillagers() / MCA.getConfig().guardSpawnRate;
        int guards = 0;

        // Grab all villagers in the area
        List<EntityVillagerMCA> list = world.getEntitiesWithinAABB(EntityVillagerMCA.class,
                new AxisAlignedBB((double) (village.getCenter().getX() - village.getVillageRadius()),
                        (double) (village.getCenter().getY() - 4),
                        (double) (village.getCenter().getZ() - village.getVillageRadius()),
                        (double) (village.getCenter().getX() + village.getVillageRadius()),
                        (double) (village.getCenter().getY() + 4),
                        (double) (village.getCenter().getZ() + village.getVillageRadius())));

        // Count up the guards
        for (EntityVillagerMCA villager : list) {
            if (villager.getProfessionForge().getRegistryName().equals(ProfessionsMCA.guard.getRegistryName())) guards++;
        }

        // Spawn a new guard if we don't have enough, up to 10
        if (guards < guardCapacity && guards < 10) {
            Vec3d spawnPos = findRandomSpawnPos(world, village, village.getCenter(), 2, 4, 2);

            if (spawnPos != null) {
                EntityVillagerMCA guard = new EntityVillagerMCA(world, Optional.of(ProfessionsMCA.guard), Optional.absent());
                guard.setPosition(spawnPos.x + 0.5D, spawnPos.y + 1.0D, spawnPos.z + 0.5D);
                guard.finalizeMobSpawn(world.getDifficultyForLocation(guard.getPos()), null, false);
                world.spawnEntity(guard);
            }
        }
    }

    private static void startRaid(World world, Village village) {
        int banditsToSpawn = world.rand.nextInt(5) + 1;

        while (banditsToSpawn > 0) {
            EntityVillagerMCA bandit = new EntityVillagerMCA(world, Optional.of(ProfessionsMCA.bandit), Optional.absent());
            BlockPos spawnLocation = village.getCenter();
            bandit.setPosition(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
            world.spawnEntity(bandit);
            banditsToSpawn--;
        }
    }

    private static Vec3d findRandomSpawnPos(World world, Village village, BlockPos pos, int x, int y, int z) {
        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos = pos.add(world.rand.nextInt(16) - 8, world.rand.nextInt(6) - 3, world.rand.nextInt(16) - 8);

            if (village.isBlockPosWithinSqVillageRadius(blockpos) && isAreaClearAround(world, new BlockPos(x, y, z), blockpos))
                return new Vec3d((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ());
        }

        return null;
    }

    private static boolean isAreaClearAround(World world, BlockPos blockSize, BlockPos blockLocation) {
        if (!world.getBlockState(blockLocation.down()).isTopSolid()) return false;
        int i = blockLocation.getX() - blockSize.getX() / 2;
        int j = blockLocation.getZ() - blockSize.getZ() / 2;

        for (int k = i; k < i + blockSize.getX(); ++k) {
            for (int l = blockLocation.getY(); l < blockLocation.getY() + blockSize.getY(); ++l) {
                for (int i1 = j; i1 < j + blockSize.getZ(); ++i1) {
                    if (world.getBlockState(new BlockPos(k, l, i1)).isNormalCube()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
