package mca.entity.ai;

import com.google.common.base.Optional;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentData;
import mca.enums.EnumGender;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIAgeBaby extends EntityAIBase {
    private final EntityVillagerMCA villager;

    public EntityAIAgeBaby(EntityVillagerMCA entityIn) {
        this.villager = entityIn;
        this.setMutexBits(4);
    }

    public boolean shouldExecute() {
        return villager.get(EntityVillagerMCA.HAS_BABY);
    }

    public void updateTask() {
        if (villager.ticksExisted % 1200 != 0) return;
        villager.babyAge += 1;

        if (villager.babyAge < MCA.getConfig().babyGrowUpTime) return;

        EntityVillagerMCA child = new EntityVillagerMCA(villager.world, Optional.absent(), Optional.of(villager.get(EntityVillagerMCA.BABY_IS_MALE) ? EnumGender.MALE : EnumGender.FEMALE));
        child.set(EntityVillagerMCA.PARENTS, ParentData.fromVillager(villager).toNBT());
        child.setPosition(villager.posX, villager.posY, villager.posZ);

        villager.world.spawnEntity(child);
        villager.set(EntityVillagerMCA.HAS_BABY, false);
    }
}