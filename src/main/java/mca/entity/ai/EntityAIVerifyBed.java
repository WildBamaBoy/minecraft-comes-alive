package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class EntityAIVerifyBed extends EntityAIBase {
    private final EntityVillagerMCA villager;
    private int ticks = 0;

    public EntityAIVerifyBed(EntityVillagerMCA entityIn) {
        this.villager = entityIn;
    }

    public boolean shouldExecute() {
        return villager.get(EntityVillagerMCA.BED_POS) != BlockPos.ORIGIN && !villager.getWorld().isDaytime();
    }

    public void updateTask() {
        ticks++;
        if (ticks % 100 == 0) {
            System.out.println("verify");
            BlockPos bed = villager.get(EntityVillagerMCA.BED_POS);
            if (villager.world.getBlockState(bed).getBlock() != Blocks.BED) {
                villager.set(EntityVillagerMCA.BED_POS, BlockPos.ORIGIN);
            }
            ticks = 0;
        }
    }
}