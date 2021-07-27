package mca.entity.interaction;

import mca.entity.ZombieVillagerEntityMCA;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class ZombieCommandHandler extends EntityCommandHandler<ZombieVillagerEntityMCA> {

    public ZombieCommandHandler(ZombieVillagerEntityMCA entity) {
        super(entity);
    }

    /**
     * Called on the server to respond to button events.
     */
    @Override
    public boolean handle(ServerPlayerEntity player, String command) {
        switch (command) {
            case "gift":
                // zombies only accept one type of gift, and for now it's not brains
                if (entity.interactMob(player, Hand.MAIN_HAND).isAccepted()) {
                    if (!player.abilities.creativeMode) {
                        player.getStackInHand(Hand.MAIN_HAND).decrement(1);
                    }
                }
                return true;
        }

        return super.handle(player, command);
    }
}
