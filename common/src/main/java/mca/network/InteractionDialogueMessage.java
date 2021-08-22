package mca.network;

import java.util.Optional;
import java.util.UUID;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerLike;
import mca.network.client.GetVillagerResponse;
import mca.network.client.InteractionDialogueResponse;
import mca.resources.Dialogues;
import mca.resources.data.Answer;
import mca.resources.data.AnswerAction;
import mca.resources.data.Question;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class InteractionDialogueMessage implements Message {
    private static final long serialVersionUID = 1462101145658166706L;

    private final UUID villagerUUID;
    private final String question;
    private final String answer;

    public InteractionDialogueMessage(UUID uuid, String question, String answer) {
        villagerUUID = uuid;
        this.question = question;
        this.answer = answer;
    }

    @Override
    public void receive(PlayerEntity player) {
        Entity villager = ((ServerWorld)player.world).getEntity(villagerUUID);
        if (villager instanceof VillagerEntityMCA) {
            VillagerEntityMCA v = (VillagerEntityMCA)villager;
            Question question = Dialogues.getInstance().getQuestion(this.question);
            Answer a = question.getAnswer(answer);

            float chance = a.getChance(v);
            Optional<AnswerAction> ac = a.getNext().stream().filter(x -> x.getThreshold() <= chance).max((x, y) -> Float.compare(x.getThreshold(), y.getThreshold()));
            if (ac.isPresent()) {
                String id = ac.get().getId();

                Question newQuestion = Dialogues.getInstance().getRandomQuestion(id);
                if (newQuestion != null) {
                    v.sendChatMessage(player, newQuestion.getTranslationKey());
                }
                if (newQuestion == null || newQuestion.isCloseScreen()) {
                    v.getInteractions().stopInteracting();
                } else {
                    NetworkHandler.sendToPlayer(new InteractionDialogueResponse(newQuestion.getId()), (ServerPlayerEntity)player);
                }
            } else {
                v.getInteractions().stopInteracting();
            }
        }
    }
}
