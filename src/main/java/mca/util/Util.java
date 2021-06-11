package mca.util;

import com.google.gson.Gson;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class Util {
    private static final String RESOURCE_PREFIX = "assets/mca/";

    /**
     * Finds a y position given an x,y,z coordinate triple that is assumed to be the world's "ground".
     *
     * @param world The world in which blocks will be tested
     * @param x     X coordinate
     * @param y     Y coordinate, used as the starting height for finding ground.
     * @param z     Z coordinate
     * @return Integer representing the air block above the first non-air block given the provided ordered triples.
     */
    public static int getSpawnSafeTopLevel(World world, int x, int y, int z) {
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
        return gson.fromJson(Util.readResource(path), type);
    }

    public static List<BlockPos> getNearbyBlocks(BlockPos origin, World world, @Nullable Predicate<BlockState> filter, int xzDist, int yDist) {
        final List<BlockPos> pointsList = new ArrayList<>();
        for (int x = -xzDist; x <= xzDist; x++) {
            for (int y = -yDist; y <= yDist; y++) {
                for (int z = -xzDist; z <= xzDist; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        BlockPos pos = new BlockPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                        if (filter != null && filter.test(world.getBlockState(pos))) {
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
        double closest = 10000.0D;
        BlockPos returnPoint = null;
        for (BlockPos point : blocks) {
            double distance = origin.distSqr(point.getX(), point.getY(), point.getZ(), true);
            if (distance < closest) {
                closest = distance;
                returnPoint = point;
            }
        }

        return returnPoint;
    }

    public static String httpGet(String url) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (IOException ignored) {
            MCA.log("Failed to GET from: " + url);
        }
        return "";
    }

    public static float clamp(float val) {
        return Math.max(0.0f, Math.min(1.0f, val));
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
