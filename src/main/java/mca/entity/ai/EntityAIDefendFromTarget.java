package mca.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumHand;

public class EntityAIDefendFromTarget extends EntityAIBase {
    private EntityCreature attacker;

    private int attackTick;

    public EntityAIDefendFromTarget(EntityCreature creature) {
        this.attacker = creature;
        this.setMutexBits(0);
    }

    public boolean shouldExecute() {
        this.attackTick--;

        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null) {
            return false;
        } else if (!entitylivingbase.isEntityAlive()) {
            return false;
        } else if (this.attackTick > 0) {
            return false;
        } else {
            return 4.0D >= this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
        }
    }

    public boolean shouldContinueExecuting() {
        return false;
    }

    public void startExecuting() {
        this.attackTick = 10;

        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
        if (entitylivingbase != null) {
            this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);

            this.attacker.swingArm(EnumHand.MAIN_HAND);
            this.attacker.attackEntityAsMob(entitylivingbase);
        }
    }

    public void resetTask() {
    }

    public void updateTask() {

    }
}