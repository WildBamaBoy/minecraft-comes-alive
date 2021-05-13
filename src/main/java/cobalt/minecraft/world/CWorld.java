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
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class CWorld {
    @Getter
    private final World mcWorld;

    public final boolean isClientSide;
    public final Random rand;

    //TODO
    public Iterable<? extends Entity> loadedEntityList;

    private CWorld(World world) {
        this.mcWorld = world;
        this.isClientSide = world.isClientSide;
        this.rand = world.random;
    }

    public static CWorld fromMC(World world) {
        return new CWorld(world);
    }

    //returns all entities
    public List<Entity> getCloseEntities(Entity e) {
        return getCloseEntities(e, 256.0D);
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

    public CWorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataId) {
        DimensionSavedDataManager dm = ((ServerWorld) mcWorld).getDataStorage();
        return dm.computeIfAbsent(() -> {
            try {
                return (CWorldSavedData) clazz.getDeclaredConstructor(String.class).newInstance(dataId);
            } catch (Exception e) {
                Cobalt.getLog().info(e);
                return null;
            }
        }, dataId);
    }

    public void setData(String dataId, WorldSavedData data) {
        DimensionSavedDataManager dm = ((ServerWorld) mcWorld).getDataStorage();
        dm.set(data);
    }

    public Optional<PlayerEntity> getPlayerEntityByUUID(UUID uuid) {
        PlayerEntity player = mcWorld.getPlayerByUUID(uuid);
        if (player != null) {
            return Optional.of(player);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Entity> getEntityByUUID(UUID uuid) {
        return Optional.ofNullable(((ServerWorld) mcWorld).getEntity(uuid));
    }

    public Biome getBiome(BlockPos pos) {
        return mcWorld.getBiome(pos);
    }

    public void spawnEntity(Entity entity) {
        ((MobEntity) entity).finalizeSpawn((IServerWorld) mcWorld, mcWorld.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.NATURAL, null, null);
        mcWorld.addFreshEntity(entity);
    }
}
