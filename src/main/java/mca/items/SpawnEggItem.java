package mca.items;

import lombok.Getter;
import mca.entity.VillagerFactory;
import mca.enums.Gender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

public class SpawnEggItem extends Item {
    @Getter
    private final Gender gender;

    public SpawnEggItem(Gender gender, Item.Properties properties) {
        super(properties);
        this.gender = gender;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP) {
            VillagerFactory.newVillager(context.getLevel())
                    .withGender(gender)
                    .withPosition(context.getClickedPos())
                    .spawn();
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
