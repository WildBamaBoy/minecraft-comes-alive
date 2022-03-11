package mca.item;

import mca.MCA;
import mca.cobalt.registration.Registration;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public interface ItemGroupMCA {
    ItemGroup MCA_GROUP = Registration.ObjectBuilders.ItemGroups.create(
            new Identifier(MCA.MOD_ID, "mca_tab"),
            () -> ItemsMCA.ENGAGEMENT_RING.getDefaultStack()
    );
}
