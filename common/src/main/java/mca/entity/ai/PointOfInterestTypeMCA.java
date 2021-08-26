package mca.entity.ai;

import mca.MCA;
import mca.block.BlocksMCA;
import mca.cobalt.registration.Registration;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public interface PointOfInterestTypeMCA {
    // TODO
    // PointOfInterestType JEWELER = Registration.ObjectBuilders.Poi.create(new Identifier(MCA.MOD_ID, "jeweler"), 1, 1, BlocksMCA.JEWELER_WORKBENCH);

    static void bootstrap() { }
}
