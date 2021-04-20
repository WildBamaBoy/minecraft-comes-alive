package mca.entity.ai;

import com.google.common.base.Optional;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentData;
import mca.enums.EnumGender;
import mca.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;

import static mca.entity.EntityVillagerMCA.SPOUSE_UUID;

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

        //inherit genes
        //if the father does not exist or is a player, it just uses the mother genes, ignoring any interpolations
        Optional<Entity> spouse = Util.getEntityByUUID(villager.world, villager.get(SPOUSE_UUID).or(Constants.ZERO_UUID));
        EntityVillagerMCA father = villager;
        if (spouse.isPresent() && spouse.get() instanceof EntityVillagerMCA) {
            father = (EntityVillagerMCA) spouse.get();
        }
        child.inheritGenes(villager, father);

        villager.world.spawnEntity(child);
        villager.set(EntityVillagerMCA.HAS_BABY, false);
    }
}