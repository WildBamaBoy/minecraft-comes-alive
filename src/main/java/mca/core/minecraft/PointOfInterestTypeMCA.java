package mca.core.minecraft;

import mca.core.MCA;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public interface PointOfInterestTypeMCA {
    PointOfInterestType JEWELER = PointOfInterestHelper.register(new Identifier(MCA.MOD_ID, "jeweler"), 1, 1, BlocksMCA.JEWELER_WORKBENCH);

    static void bootstrap() { }

}
