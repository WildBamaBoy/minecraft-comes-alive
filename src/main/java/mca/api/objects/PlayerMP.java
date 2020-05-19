package mca.api.objects;

import lombok.Getter;
import mca.api.wrappers.WorldServerWrapper;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerMP extends Player {
    @Getter private EntityPlayerMP vanillaPlayerMP;
    @Getter private WorldServerWrapper vanillaWorldServer;

    public PlayerMP(EntityPlayerMP player) {
        super(player);
        this.vanillaPlayerMP = player;
        this.vanillaWorldServer = new WorldServerWrapper(player.getServerWorld());
    }
}
