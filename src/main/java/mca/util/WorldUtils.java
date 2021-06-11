package mca.util;

import mca.api.cobalt.minecraft.world.storage.CWorldSavedData;
import mca.core.MCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.util.List;

public class WorldUtils {
    public static List<Entity> getCloseEntities(World world, Entity e) {
        return getCloseEntities(world, e, 256.0);
    }

    public static List<Entity> getCloseEntities(World world, Entity e, double range) {
        BlockPos pos = e.blockPosition();
        return world.getEntities(e, new AxisAlignedBB(
                pos.getX() - range,
                pos.getY() - range,
                pos.getZ() - range,
                pos.getX() + range,
                pos.getY() + range,
                pos.getZ() + range
        ));
    }

    public static <T extends Entity> List<T> getCloseEntities(World world, Entity e, Class<? extends T> c) {
        return getCloseEntities(world, e, 256.0, c);
    }

    public static <T extends Entity> List<T> getCloseEntities(World world, Entity e, double range, Class<? extends T> c) {
        BlockPos pos = e.blockPosition();
        return getCloseEntities(world, pos, range, c);
    }

    public static <T extends Entity> List<T> getCloseEntities(World world, BlockPos pos, double range, Class<? extends T> c) {
        return world.getLoadedEntitiesOfClass(c, new AxisAlignedBB(
                pos.getX() - range,
                pos.getY() - range,
                pos.getZ() - range,
                pos.getX() + range,
                pos.getY() + range,
                pos.getZ() + range
        ));
    }

    public static <T extends WorldSavedData> T loadData(World world, Class<T> clazz, String dataId) {
        DimensionSavedDataManager dm = ((ServerWorld) world).getDataStorage();
        return (T) dm.computeIfAbsent(() -> {
            try {
                return (CWorldSavedData) clazz.getDeclaredConstructor(String.class).newInstance(dataId);
            } catch (Exception e) {
                MCA.getMod().logger.info(e);
                return null;
            }
        }, dataId);
    }

    public static void setData(World world, WorldSavedData data) {
        DimensionSavedDataManager dm = ((ServerWorld) world).getDataStorage();
        dm.set(data);
    }

    public static void spawnEntity(World world, Entity entity) {
        ((MobEntity) entity).finalizeSpawn((IServerWorld) world, world.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.NATURAL, null, null);
        world.addFreshEntity(entity);
    }
}
