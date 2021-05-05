package cobalt.minecraft.entity.player;

import cobalt.minecraft.entity.CLivingEntity;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;

public class CPlayer extends CLivingEntity {
    @Getter
    private final PlayerEntity mcPlayer;

    protected CPlayer(PlayerEntity player) {
        super(player);
        this.mcPlayer = player;
    }

    public static CPlayer fromMC(PlayerEntity player) {
        return new CPlayer(player);
    }

    public boolean isCreativeMode() {
        return mcPlayer.isCreative();
    }
}
