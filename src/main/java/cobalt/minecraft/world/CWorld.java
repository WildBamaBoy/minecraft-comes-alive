package cobalt.minecraft.world;

import cobalt.core.Cobalt;
import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.world.storage.CWorldSavedData;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.entity.CEntity;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class CWorld {
    @Getter private final World mcWorld;

    public final boolean isRemote;
    public final Random rand;

    private CWorld(World world) {
        this.mcWorld = world;
        this.isRemote = world.isRemote;
        this.rand = world.rand;
    }

    public static CWorld fromMC(World world) {
        return new CWorld(world);
    }

    public CWorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataId) {
        DimensionSavedDataManager dm = ((ServerWorld) mcWorld).getSavedData();
        return dm.getOrCreate(() -> {
            try {
                return (CWorldSavedData)clazz.getDeclaredConstructor(String.class).newInstance(dataId);
            } catch (Exception e) {
                Cobalt.getLog().info(e);
                return null;
            }}, dataId);
    }

    public void setData(String dataId, WorldSavedData data) {
        DimensionSavedDataManager dm = ((ServerWorld) mcWorld).getSavedData();
        dm.set(data);
    }

    public Optional<CPlayer> getPlayerEntityByUUID(UUID uuid) {
        PlayerEntity player = mcWorld.getPlayerByUuid(uuid);
        if (player != null) {
            return Optional.of(CPlayer.fromMC(player));
        } else {
            return Optional.empty();
        }
    }

    public Optional<CEntity> getEntityByUUID(UUID uuid) {
        return Optional.of(CEntity.fromMC(((ServerWorld)mcWorld).getEntityByUuid(uuid)));
    }

    public Biome getBiome(CPos pos) {
        return mcWorld.getBiome(pos.getMcPos());
    }

    public void spawnEntity(CEntity entity) {
        mcWorld.addEntity(entity.getMcEntity());
    }
}
