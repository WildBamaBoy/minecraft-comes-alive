package mca.util;

import net.minecraft.util.math.BlockBox;

public class BlockBoxExtended extends BlockBox {
    public BlockBoxExtended(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BlockBox expand(int margin) {
        return expand(margin, margin, margin);
    }

    public BlockBox expand(int x, int y, int z) {
        return new BlockBox(
                minX - x,
                minY - y,
                minZ - z,
                maxX + x,
                maxY + y,
                maxZ + z
        );
    }

    public int getMaxBlockCount() {
        return Math.max(Math.max(getBlockCountX(), getBlockCountY()), getBlockCountZ());
    }
}
