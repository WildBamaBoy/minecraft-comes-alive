package mca.util;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class Util {
    private static final String RESOURCE_PREFIX = "assets/mca/";

    private Util() {
    }

    /**
     * Finds a y position given an x,y,z coordinate triple that is assumed to be the world's "ground".
     *
     * @param world	The world in which blocks will be tested
     * @param x			X coordinate
     * @param y			Y coordinate, used as the starting height for finding ground.
     * @param z			Z coordinate
     * @return Integer representing the air block above the first non-air block given the provided ordered triples.
     */
    public static int getSpawnSafeTopLevel(World world, int x, int y, int z)
    {
        Block block = Blocks.AIR;
        while (block == Blocks.AIR && y > 0) {
            y--;
            block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        }

        return y + 1;
    }

    public static String readResource(String path) {
        String data;
        String location = RESOURCE_PREFIX + path;

        try {
            data = IOUtils.toString(new InputStreamReader(MCA.class.getClassLoader().getResourceAsStream(location)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource from JAR: " + location);
        }

        return data;
    }

    public static <T> T readResourceAsJSON(String path, Class<T> type) {
        Gson gson = new Gson();
        T data = gson.fromJson(Util.readResource(path), type);
        return data;
    }

    public static Optional<Entity> getEntityByUUID(World world, UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return Optional.of(entity);
            }
        }
        return Optional.absent();
    }

    public static <T extends Entity> Optional<T> getEntityByUUID(World world, UUID uuid, Class<? extends T> clazz) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getClass().isAssignableFrom(clazz) && entity.getUniqueID().equals(uuid)) {
                return Optional.of((T) entity);
            }
        }
        return Optional.absent();
    }

    public static List<BlockPos> getNearbyBlocks(BlockPos origin, World world, @Nullable Class filter, int xzDist, int yDist) {
        final List<BlockPos> pointsList = new ArrayList<>();
        for (int x = -xzDist; x <= xzDist; x++) {
            for (int y = -yDist; y <= yDist; y++) {
                for (int z = -xzDist; z <= xzDist; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        BlockPos pos = new BlockPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                        if (filter != null && filter.isAssignableFrom(world.getBlockState(pos).getBlock().getClass())) {
                            pointsList.add(pos);
                        } else if (filter == null) {
                            pointsList.add(pos);
                        }
                    }
                }
            }
        }
        return pointsList;
    }

    public static BlockPos getNearestPoint(BlockPos origin, List<BlockPos> blocks) {
        double closest = 100.0D;
        BlockPos returnPoint = null;
        for (BlockPos point : blocks) {
            double distance = origin.getDistance(point.getX(), point.getY(), point.getZ());
            if (distance < closest) {
                closest = distance;
                returnPoint = point;
            }
        }

        return returnPoint;
    }

    public static String httpGet(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        try {
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            MCA.getLog().error("Failed to check for updates.", e);
        }
        return "";
    }
}
