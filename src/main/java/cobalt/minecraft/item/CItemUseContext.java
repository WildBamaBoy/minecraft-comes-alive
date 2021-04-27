package cobalt.minecraft.item;

import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.world.CWorld;
import lombok.Getter;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;

public class CItemUseContext {
    @Getter ItemUseContext mcContext;

    private CItemUseContext(ItemUseContext context) {
        this.mcContext = context;
    }

    public static CItemUseContext fromMC(ItemUseContext context) {
        return new CItemUseContext(context);
    }

    public CPos getPos() {
        return CPos.fromMC(mcContext.getClickedPos());
    }

    public Direction getDirection() {
        return mcContext.getClickedFace();
    }

    public CWorld getWorld() {
        return CWorld.fromMC(mcContext.getLevel());
    }
}
