package cobalt.minecraft.util.math;

import lombok.Getter;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Wrapper for Minecraft's Vector3f class.
 */
public class CVec3f {
    public static final CVec3f ORIGIN = new CVec3f(0,0,0);
    @Getter private Vector3f mcVec;

    public CVec3f(float x, float y, float z) {
        mcVec = new Vector3f(x, y, z);
    }

    public float getX() {
        return mcVec.x();
    }

    public float getY() {
        return mcVec.y();
    }

    public float getZ() {
        return mcVec.z();
    }
}
