package cobalt.minecraft.world;

import cobalt.core.Cobalt;
import cobalt.minecraft.world.storage.CWorldSavedData;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CWorld {
    public final boolean isClientSide;
    public final Random rand;
    @Getter
    private final World mcWorld;

    private CWorld(World world) {
        this.mcWorld = world;
        this.isClientSide = world.isClientSide;
        this.rand = world.random;
    }

    public static CWorld fromMC(World world) {
        return new CWorld(world);
    }

    //returns all entities within a given range around a given position
    public List<Entity> getCloseEntities(Entity e) {
        return getCloseEntities(e, 256.0);
    }

    public List<Entity> getCloseEntities(Entity e, double range) {
        BlockPos pos = e.blockPosition();
        return mcWorld.getEntities(e, new AxisAlignedBB(
                pos.getX() - range,
                pos.getY() - range,
                pos.getZ() - range,
                pos.getX() + range,
                pos.getY() + range,
                pos.getZ() + range
        ));
    }

    public <T extends Entity> List<T> getCloseEntities(Entity e, Class<? extends T> c) {
        return getCloseEntities(e, 256.0, c);
    }

    public <T extends Entity> List<T> getCloseEntities(Entity e, double range, Class<? extends T> c) {
        BlockPos pos = e.blockPosition();
        return getCloseEntities(pos, range, c);
    }

    public <T extends Entity> List<T> getCloseEntities(BlockPos pos, double range, Class<? extends T> c) {
        return mcWorld.getLoadedEntitiesOfClass(c, new AxisAlignedBB(
                pos.getX() - range,
                pos.getY() - range,
                pos.getZ() - range,
                pos.getX() + range,
                pos.getY() + range,
                pos.getZ() + range
        ));
    }

    public <T extends WorldSavedData> T loadData(Class<T> clazz, String dataId) {
        DimensionSavedDataManager dm = ((ServerWorld) mcWorld).getDataStorage();
        return (T) dm.computeIfAbsent(() -> {
            try {
                return (CWorldSavedData) clazz.getDeclaredConstructor(String.class).newInstance(dataId);
            } catch (Exception e) {
                Cobalt.getLog().info(e);
                return null;
            }
        }, dataId);
    }

    public void setData(WorldSavedData data) {
        DimensionSavedDataManager dm = ((ServerWorld) mcWorld).getDataStorage();
        dm.set(data);
    }

    public PlayerEntity getPlayerEntityByUUID(UUID uuid) {
        return mcWorld.getPlayerByUUID(uuid);
    }

    public Entity getEntityByUUID(UUID uuid) {
        return (((ServerWorld) mcWorld).getEntity(uuid));
    }

    public Biome getBiome(BlockPos pos) {
        return mcWorld.getBiome(pos);
    }

    public void spawnEntity(Entity entity) {
        ((MobEntity) entity).finalizeSpawn((IServerWorld) mcWorld, mcWorld.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.NATURAL, null, null);
        mcWorld.addFreshEntity(entity);
    }
}
