package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAISwing extends EntityAIBase {
    private final EntityVillagerMCA villager;
    private int swingProgressTicks;
    private float swingProgress;

    public EntityAISwing(EntityVillagerMCA entityIn) {
        this.villager = entityIn;
        this.setMutexBits(4);
    }

    public boolean shouldExecute() {
        return villager.get(EntityVillagerMCA.IS_SWINGING);
    }

    public void updateTask() {
        swingProgressTicks++;

        if (swingProgressTicks >= 8) {
            swingProgressTicks = 0;
            villager.set(EntityVillagerMCA.IS_SWINGING, false);
        }

        villager.swingProgress = (float) swingProgressTicks / (float) 8;
    }
}