package cobalt.minecraft.pathfinding;

import cobalt.minecraft.util.math.CPos;
import net.minecraft.pathfinding.PathNavigator;
import lombok.Getter;

public class CPathNavigator {
    @Getter private final PathNavigator vanillaNavigator;

    private CPathNavigator(PathNavigator navigator) {
        this.vanillaNavigator = navigator;
    }

    public static CPathNavigator fromMC(PathNavigator navigator) {
        return new CPathNavigator(navigator);
    }

    public boolean tryGoTo(CPos pos) {
        return vanillaNavigator.tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 0.5F); //TODO verify
    }
}
