package mca.entity.ai;

import com.google.common.base.Optional;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentPair;
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
        return villager.get(EntityVillagerMCA.hasBaby);
    }

    public void updateTask() {
        if (villager.tickCount % 1200 != 0) return;
        villager.babyAge += 1;

        if (villager.babyAge < MCA.getConfig().babyGrowUpTime) return;

        EntityVillagerMCA child = new EntityVillagerMCA(villager.world, Optional.absent(), Optional.of(villager.get(EntityVillagerMCA.isBabyMale) ? EnumGender.MALE : EnumGender.FEMALE));
        child.set(EntityVillagerMCA.parents, ParentPair.fromVillager(villager).toNBT());
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
        villager.set(EntityVillagerMCA.hasBaby, false);
    }
}