package mca.network;

import java.util.Optional;
import java.util.UUID;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.brain.VillagerBrain;
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
            selectAnswer(v, (ServerPlayerEntity)player, question, answer);
        }
    }

    private void selectAnswer(VillagerEntityMCA villager, ServerPlayerEntity player, String questionId, String answerId) {
        Question question = Dialogues.getInstance().getQuestion(questionId);
        Answer answer = question.getAnswer(answerId);

        float chance = answer.getChance(villager, player);
        Optional<AnswerAction> ac = answer.getNext().stream().filter(x -> x.getThreshold() <= chance).max((x, y) -> Float.compare(x.getThreshold(), y.getThreshold()));
        if (ac.isPresent()) {
            String id = ac.get().getId();

            int hearts = answer.getHearts(villager);

            // hearts
            if (ac.get().isSuccess()) {
                villager.getVillagerBrain().rewardHearts(player, hearts);
            } else if (ac.get().isFail()) {
                villager.getVillagerBrain().rewardHearts(player, -hearts);
            }

            Question newQuestion = Dialogues.getInstance().getRandomQuestion(id);
            if (newQuestion != null) {
                if (newQuestion.isAuto()) {
                    // this is basically a placeholder and fires an answer automatically
                    // use cases are n to 1 links or to split file size
                    selectAnswer(villager, player, newQuestion.getId(), newQuestion.getAnswers().get(0).getName());
                    return;
                } else {
                    NetworkHandler.sendToPlayer(new InteractionDialogueResponse(newQuestion, player, villager), (ServerPlayerEntity)player);
                }
            } else {
                // we send nevertheless and assume it's a final question
                villager.sendChatMessage(player, "dialogue." + id);
            }

            // close screen
            if (newQuestion == null || newQuestion.isCloseScreen()) {
                villager.getInteractions().stopInteracting();
            }
        } else {
            // should not happen
            villager.getInteractions().stopInteracting();
        }
    }
}
