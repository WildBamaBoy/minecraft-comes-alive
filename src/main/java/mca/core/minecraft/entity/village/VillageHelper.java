package mca.core.minecraft.entity.village;

import mca.entity.data.Village;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import java.util.*;

public class VillageHelper {
    private static final int MOVE_IN_COOLDOWN = 6000;

    public static Optional<Village> getNearestVillage(Entity entity) {
        return VillageManagerData.get(entity.world).findVillages(v -> v.isWithinBorder(entity)).findFirst();
    }

    public static void tick(World world) {
        if (!world.isClient) {
            return;
        }

        //keep track on where player are currently in
        Residency.tick(world);

        //taxes time
        long time = world.getTime();
        if (time % 24000 == 0) {
            Taxation.updateTaxes(world);
        }

        //update village overall mechanics
        if (time % 6000 == 0) {
            for (Village v : VillageManagerData.get(world)) {
                if (v.lastMoveIn + MOVE_IN_COOLDOWN < time) {
                    Fortification.spawnGuards(world, v);
                    Procreation.procreate(world, v);
                    Procreation.marry(world, v);
                }
            }
        }
    }
}
