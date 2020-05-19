package mca.core.minecraft;

import java.util.List;

import mca.api.objects.PlayerMP;
import mca.api.objects.Pos;
import mca.api.wrappers.WorldWrapper;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;

public class VillageHelper {
    public static void tick(WorldWrapper world) {
        world.getVillageCollection().getVillageList().forEach(v -> spawnGuards(world, v));
    }

    public static void forceSpawnGuards(PlayerMP player) {
        Village nearestVillage = player.world.getVillageCollection().getNearestVillage(player.getPosition().getBlockPos(), 100);
        spawnGuards(player.world, nearestVillage);
    }

    public static void forceRaid(PlayerMP player) {
        Village nearestVillage = player.world.getVillageCollection().getNearestVillage(player.getPosition().getBlockPos(), 100);
        startRaid(player.world, nearestVillage);
    }

    private static void spawnGuards(WorldWrapper world, Village village) {
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
            Vec3d spawnPos = findRandomSpawnPos(world, village, new Pos(village.getCenter()), 2, 4, 2);

            if (spawnPos != null) {
                EntityVillagerMCA guard = new EntityVillagerMCA(world.getVanillaWorld());
                guard.setProfession(ProfessionsMCA.guard);
                guard.setPosition(spawnPos.x + 0.5D, spawnPos.y + 1.0D, spawnPos.z + 0.5D);
                guard.finalizeMobSpawn(world.getDifficultyForLocation(guard.getPos()), null, false);
                world.spawnEntity(guard);
            }
        }
    }

    private static void startRaid(WorldWrapper world, Village village) {
        int banditsToSpawn = world.rand.nextInt(5) + 1;

        while (banditsToSpawn > 0) {
            EntityVillagerMCA bandit = new EntityVillagerMCA(world.getVanillaWorld());
            bandit.setProfession(ProfessionsMCA.bandit);
            BlockPos spawnLocation = village.getCenter();
            bandit.setPosition(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
            world.spawnEntity(bandit);
            banditsToSpawn--;
        }
    }

    private static Vec3d findRandomSpawnPos(WorldWrapper world, Village village, Pos pos, int x, int y, int z) {
        for (int i = 0; i < 10; ++i) {
            Pos wrappedPos = pos.add(world.rand.nextInt(16) - 8, world.rand.nextInt(6) - 3, world.rand.nextInt(16) - 8);

            if (village.isBlockPosWithinSqVillageRadius(wrappedPos.getBlockPos()) && isAreaClearAround(world, new Pos(x, y, z), wrappedPos))
                return new Vec3d((double) wrappedPos.getX(), (double) wrappedPos.getY(), (double) wrappedPos.getZ());
        }

        return null;
    }

    private static boolean isAreaClearAround(WorldWrapper world, Pos blockSize, Pos blockLocation) {
        if (!world.getBlockState(blockLocation.down()).isSideSolid(world.getVanillaWorld(), blockLocation.down().getBlockPos(), EnumFacing.UP)) return false;
        int i = blockLocation.getX() - blockSize.getX() / 2;
        int j = blockLocation.getZ() - blockSize.getZ() / 2;

        for (int k = i; k < i + blockSize.getX(); ++k) {
            for (int l = blockLocation.getY(); l < blockLocation.getY() + blockSize.getY(); ++l) {
                for (int i1 = j; i1 < j + blockSize.getZ(); ++i1) {
                    if (world.getBlockState(new Pos(k, l, i1)).isNormalCube()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
