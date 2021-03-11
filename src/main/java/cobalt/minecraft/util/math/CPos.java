package cobalt.minecraft.util.math;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;

/**
 * Wrapper for Minecraft's BlockPos class.
 */
public class CPos {
    public static final CPos ORIGIN = new CPos(0,0,0);
    @Getter private BlockPos mcPos;

    public CPos(int x, int y, int z) {
        mcPos = new BlockPos(x, y, z);
    }

    public CPos(double x, double y, double z) {
        mcPos = new BlockPos(x, y, z);
    }

    private CPos(BlockPos pos) {
        this.mcPos = pos;
    }

    public static CPos fromMC(BlockPos pos) {
        return new CPos(pos);
    }

    public int getX() {
        return mcPos.getX();
    }

    public int getY() {
        return mcPos.getY();
    }

    public int getZ() {
        return mcPos.getZ();
    }

    public CPos add(int x, int y, int z) {
        mcPos.add(x, y, z);
        return this;
    }

    public double getDistance(int x, int y, int z) {
        return Math.sqrt(mcPos.distanceSq(x, y, z, false));
    }

    public CPos down() {
        mcPos = mcPos.down();
        return this;
    }
}
