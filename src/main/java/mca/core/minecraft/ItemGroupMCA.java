package mca.core.minecraft;

import mca.core.MCA;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public interface ItemGroupMCA {
    ItemGroup MCA_GROUP = FabricItemGroupBuilder
            .create(new Identifier(MCA.MOD_ID, "mcaTab"))
            .icon(() -> ItemsMCA.ENGAGEMENT_RING.getDefaultStack())
            .build();
}
