package mca.items;

import cobalt.items.CItemBasic;
import cobalt.minecraft.item.CItemUseContext;
import lombok.Getter;
import mca.entity.VillagerFactory;
import mca.enums.EnumGender;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

public class ItemSpawnEgg extends CItemBasic {
    @Getter private final EnumGender gender;

    public ItemSpawnEgg(EnumGender gender, Properties properties) {
        super(properties);
        this.gender = gender;
    }

    @Override
    public ActionResultType handleUseOnBlock(CItemUseContext context) {
        if (!context.getWorld().isRemote && context.getDirection() == Direction.UP) {
            VillagerFactory.newVillager(context.getWorld())
                    .withGender(gender)
                    .withPosition(context.getPos())
                    .spawn();
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
