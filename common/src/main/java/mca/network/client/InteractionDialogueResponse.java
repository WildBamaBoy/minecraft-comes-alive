package mca.network.client;

import java.util.List;
import mca.ClientProxy;
import mca.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import mca.resources.data.Question;
import net.minecraft.entity.player.PlayerEntity;

public class InteractionDialogueResponse implements Message {
    private static final long serialVersionUID = 1371939319244994642L;

    public final String question;
    public final List<String> answers;
    public final boolean silent;

    public InteractionDialogueResponse(Question question, PlayerEntity player, VillagerEntityMCA villager) {
        this.question = question.getId();
        this.answers = question.getValidAnswers(player, villager);
        this.silent = question.isSilent();
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleDialogueResponse(this);
    }
}
