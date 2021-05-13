package mca.items;

import cobalt.minecraft.world.CWorld;
import lombok.Getter;
import mca.entity.VillagerFactory;
import mca.enums.EnumGender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

public class ItemSpawnEgg extends Item {
    @Getter
    private final EnumGender gender;

    public ItemSpawnEgg(EnumGender gender, Item.Properties properties) {
        super(properties);
        this.gender = gender;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP) {
            VillagerFactory.newVillager(CWorld.fromMC(context.getLevel()))
                    .withGender(gender)
                    .withPosition(context.getClickedPos())
                    .spawn();
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
