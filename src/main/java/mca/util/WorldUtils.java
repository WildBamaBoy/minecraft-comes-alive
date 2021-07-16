package mca.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import java.util.List;
import java.util.function.Function;

public interface WorldUtils {
    static List<Entity> getCloseEntities(World world, Entity e) {
        return getCloseEntities(world, e, 256.0);
    }

    static List<Entity> getCloseEntities(World world, Entity e, double range) {
        Vec3d pos = e.getPos();
        return world.getOtherEntities(e, new Box(pos, pos).expand(range));
    }

    static <T extends Entity> List<T> getCloseEntities(World world, Entity e, Class<T> c) {
        return getCloseEntities(world, e, 256.0, c);
    }

    static <T extends Entity> List<T> getCloseEntities(World world, Entity e, double range, Class<T> c) {
        return getCloseEntities(world, e.getPos(), range, c);
    }

    static <T extends Entity> List<T> getCloseEntities(World world, Vec3d pos, double range, Class<T> c) {
        return world.getNonSpectatingEntities(c, new Box(pos, pos).expand(range));
    }

    static <T extends PersistentState> T loadData(ServerWorld world, Function<NbtCompound, T> loader, Function<ServerWorld, T> factory, String dataId) {
        return world.getPersistentStateManager().getOrCreate(loader, () -> factory.apply(world), dataId);
    }

    static void spawnEntity(World world, Entity entity, SpawnReason reason) {
        ((MobEntity) entity).initialize((ServerWorldAccess) world, world.getLocalDifficulty(entity.getBlockPos()), reason, null, null);
        world.spawnEntity(entity);
    }
}
