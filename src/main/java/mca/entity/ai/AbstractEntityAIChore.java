package mca.entity.ai;

import com.google.common.base.Optional;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.ai.EntityAIBase;
import cobalt.minecraft.entity.player.CPlayer;

public abstract class AbstractEntityAIChore extends EntityAIBase {
    protected final EntityVillagerMCA villager;

    public AbstractEntityAIChore(EntityVillagerMCA entityIn) {
        this.villager = entityIn;
        this.setMutexBits(4);
    }

    @Override
    public void updateTask() {
        super.updateTask();

        if (!getAssigningPlayer().isPresent()) {
            MCA.getLog().warn("Force-stopped chore because assigning player was not present.");
            villager.stopChore();
        }
    }

    Optional<CPlayer> getAssigningPlayer() {
        CPlayer player = villager.world.getPlayerEntityByUUID(villager.get(EntityVillagerMCA.choreAssigningPlayer).or(Constants.ZERO_UUID));
        return Optional.fromNullable(player);
    }
}