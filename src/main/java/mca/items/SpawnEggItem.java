package mca.items;

import mca.entity.VillagerFactory;
import mca.enums.Gender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;

public class SpawnEggItem extends Item {
    private final Gender gender;

    public SpawnEggItem(Gender gender, Item.Settings properties) {
        super(properties);
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient && context.getSide() == Direction.UP) {
            VillagerFactory.newVillager(context.getWorld())
                    .withGender(gender)
                    .withPosition(context.getBlockPos())
                    .spawn();
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
