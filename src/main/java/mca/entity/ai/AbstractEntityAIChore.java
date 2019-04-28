package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public abstract class AbstractEntityAIChore extends EntityAIBase {
    protected final EntityVillagerMCA villager;
    protected EntityPlayer assigningPlayer;

    public AbstractEntityAIChore(EntityVillagerMCA entityIn) {
        this.villager = entityIn;
        this.setMutexBits(4);
    }

    public void setAssigningPlayer(EntityPlayer player) {
        assigningPlayer = player;
    }
}