package mca.network;

import mca.api.cobalt.network.Message;
import mca.entity.data.Village;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.player.ServerPlayerEntity;

public class SaveVillageMessage extends Message {
    private final int id;
    private final int taxes;
    private final int populationThreshold;
    private final int marriageThreshold;

    public SaveVillageMessage(Village village) {
        this.id = village.getId();
        this.taxes = village.getTaxes();
        this.populationThreshold = village.getPopulationThreshold();
        this.marriageThreshold = village.getMarriageThreshold();
    }

    @Override
    public void receive(ServerPlayerEntity e) {
        Village village = VillageManagerData.get(e.level).villages.get(id);
        if (village != null) {
            village.setTaxes(taxes);
            village.setPopulationThreshold(populationThreshold);
            village.setMarriageThreshold(marriageThreshold);
        }
    }
}
