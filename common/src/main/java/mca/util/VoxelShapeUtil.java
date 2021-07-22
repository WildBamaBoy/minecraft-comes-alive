package mca.util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.function.Function;

public interface VoxelShapeUtil {
    Vec3d CENTER = new Vec3d(0.5, 0, 0.5);

    static Function<Direction, VoxelShape> rotator(VoxelShape base) {
        return d -> rotate(base, d);
    }

    static VoxelShape rotate(VoxelShape shape, Direction direction) {
        if (direction.asRotation() == 0) {
            return shape;
        }
        float angle = (float)(-direction.asRotation() * Math.PI / 180.0);
        return VoxelShapes.union(VoxelShapes.empty(), shape.getBoundingBoxes().stream()
            .map(box -> {
              //These first two are enough for orthogonal rotations
                Vec3d a = rotate(box.minX, box.minZ, angle);
                Vec3d b = rotate(box.maxX, box.maxZ, angle);
                //These cover odd angles
                Vec3d c = rotate(box.minX, box.maxZ, angle);
                Vec3d d = rotate(box.maxX, box.minZ, angle);

                return VoxelShapes.cuboid(new Box(
                        Math.min(Math.min(a.x, b.x), Math.min(c.x, d.x)),
                        box.minY,
                        Math.min(Math.min(a.z, b.z), Math.min(c.z, d.z)),
                        Math.max(Math.max(a.x, b.x), Math.max(c.x, d.x)),
                        box.maxY,
                        Math.max(Math.max(a.z, b.z), Math.max(c.z, d.z))
                ));
            })
            .toArray(VoxelShape[]::new));
    }

    static Vec3d rotate(double x, double z, float angle) {
        return new Vec3d(x, 0, z).subtract(CENTER).rotateY(angle).add(CENTER);
    }
}
