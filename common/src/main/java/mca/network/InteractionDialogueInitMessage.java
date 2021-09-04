package mca.network;

import java.util.UUID;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.ProfessionsMCA;
import mca.network.client.InteractionDialogueResponse;
import mca.resources.Dialogues;
import mca.resources.data.Question;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class InteractionDialogueInitMessage implements Message {
    private static final long serialVersionUID = -8007274573058750406L;

    private final UUID villagerUUID;

    public InteractionDialogueInitMessage(UUID uuid) {
        villagerUUID = uuid;
    }

    @Override
    public void receive(PlayerEntity player) {
        Entity v = ((ServerWorld)player.world).getEntity(villagerUUID);
        if (v instanceof VillagerEntityMCA) {
            VillagerEntityMCA villager = (VillagerEntityMCA)v;
            villager.setProfession(ProfessionsMCA.GUARD);
            Question question = Dialogues.getInstance().getQuestion("main");
            NetworkHandler.sendToPlayer(new InteractionDialogueResponse(question, player, villager), (ServerPlayerEntity)player);
        }
    }
}
