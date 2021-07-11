package mca.util;

import mca.cobalt.minecraft.world.storage.CWorldSavedData;
import mca.core.MCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import java.util.List;
import java.util.function.Supplier;

public class WorldUtils {
    public static List<Entity> getCloseEntities(World world, Entity e) {
        return getCloseEntities(world, e, 256.0);
    }

    public static List<Entity> getCloseEntities(World world, Entity e, double range) {
        BlockPos pos = e.getBlockPos();
        return world.getOtherEntities(e, new Box(
                pos.getX() - range,
                pos.getY() - range,
                pos.getZ() - range,
                pos.getX() + range,
                pos.getY() + range,
                pos.getZ() + range
        ));
    }

    public static <T extends Entity> List<T> getCloseEntities(World world, Entity e, Class<T> c) {
        return getCloseEntities(world, e, 256.0, c);
    }

    public static <T extends Entity> List<T> getCloseEntities(World world, Entity e, double range, Class<T> c) {
        return getCloseEntities(world, e.getPos(), range, c);
    }

    public static <T extends Entity> List<T> getCloseEntities(World world, Vec3d pos, double range, Class<T> c) {
        return world.getEntitiesByClass(c, new Box(pos, pos).expand(range), null);
    }

    public static <T extends CWorldSavedData> T loadData(World world, Class<T> clazz, String dataId) {
        PersistentStateManager dm = ((ServerWorld) world).getPersistentStateManager();

        Supplier<T> factory = () -> {
            try {
                return clazz.getDeclaredConstructor(String.class).newInstance(dataId);
            } catch (Exception e) {
                MCA.logger.info(e);
                return null;
            }
        };

        return dm.getOrCreate(compoundTag -> {
            T value = factory.get();
            if (value != null) {
                value.fromNbt(compoundTag);
            }
            return value;
        }, factory, dataId);
    }

    @Deprecated
    public static void setData(World world, PersistentState data) {
        PersistentStateManager dm = ((ServerWorld) world).getPersistentStateManager();
        dm.set("", data);
    }

    public static void setData(World world, String id, PersistentState data) {
        ((ServerWorld) world).getPersistentStateManager().set(id, data);
    }

    public static void spawnEntity(World world, Entity entity) {
        ((MobEntity) entity).initialize((ServerWorldAccess) world, world.getLocalDifficulty(entity.getBlockPos()), SpawnReason.NATURAL, null, null);
        world.spawnEntity(entity);
    }
}
