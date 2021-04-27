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
        vanillaNavigator.createPath(pos.getX(), pos.getY(), pos.getZ(), 32); //TODO verify
        return false;
    }
}
