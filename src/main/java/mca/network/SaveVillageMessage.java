package mca.network;

import mca.cobalt.network.Message;
import mca.entity.data.Village;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.player.PlayerEntity;

public class SaveVillageMessage implements Message {
    private static final long serialVersionUID = -4830365225086158551L;

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
    public void receive(PlayerEntity e) {
        VillageManagerData.get(e.world).getOrEmpty(id).ifPresent(village -> {
            village.setTaxes(taxes);
            village.setPopulationThreshold(populationThreshold);
            village.setMarriageThreshold(marriageThreshold);
        });
    }
}
