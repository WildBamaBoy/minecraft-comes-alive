package mca.entity.ai;

import mca.core.MCA;
import mca.entity.VillagerFactory;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentData;
import mca.enums.EnumAgeState;
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
        villager.set(EntityVillagerMCA.BABY_AGE, villager.get(EntityVillagerMCA.BABY_AGE) + 1);

        if (villager.get(EntityVillagerMCA.BABY_AGE) < MCA.getConfig().babyGrowUpTime) return;

        EntityVillagerMCA child = VillagerFactory.newVillager(villager.world)
        		.withGender(villager.get(EntityVillagerMCA.BABY_IS_MALE) ? EnumGender.MALE : EnumGender.FEMALE)
        		.withParents(ParentData.fromVillager(villager))
        		.withPosition(villager.posX, villager.posY, villager.posZ)
        		.build();

        child.setStartingAge(MCA.getConfig().childGrowUpTime * 60 * 20 * -1);
        child.setScaleForAge(true);
        child.set(EntityVillagerMCA.AGE_STATE, EnumAgeState.BABY.getId());
        
        villager.world.spawnEntity(child);
        villager.set(EntityVillagerMCA.HAS_BABY, false);
    }
}