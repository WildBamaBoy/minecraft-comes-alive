package mca.network.client;

import java.util.List;
import mca.ClientProxy;
import mca.cobalt.network.Message;
import mca.util.SerializablePair;
import net.minecraft.entity.player.PlayerEntity;

public class AnalysisResults implements Message {
    public final List<SerializablePair<String, Float>> analysis;

    public AnalysisResults(List<SerializablePair<String, Float>> analysis) {
        this.analysis = analysis;
    }

    @Override
    public void receive(PlayerEntity e) {
        ClientProxy.getNetworkHandler().handleAnalysisResults(this);
    }
}
