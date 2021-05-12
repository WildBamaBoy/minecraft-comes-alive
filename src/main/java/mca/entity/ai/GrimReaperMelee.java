package mca.entity.ai;

import mca.entity.EntityGrimReaper;
import mca.enums.EnumReaperAttackState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;

public class GrimReaperMelee extends Goal {
    private final EntityGrimReaper reaper;
    private int attackDuration;

    public GrimReaperMelee(EntityGrimReaper reaper) {
        this.reaper = reaper;
    }

    @Override
    public boolean canUse() {
        return (reaper.getAttackState() == EnumReaperAttackState.PRE);
    }

    @Override
    public boolean canContinueToUse() {
        return attackDuration > 0;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        attackDuration = 100;
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void tick() {
        LivingEntity entityToAttack = reaper.getTarget();

        attackDuration--;

        if (entityToAttack != null && reaper.distanceTo(entityToAttack) <= 0.8D) {
            reaper.swing(Hand.MAIN_HAND);
            attackDuration = 0;

            float damage = reaper.level.getDifficulty().getId() * 5.75F;
            entityToAttack.hurt(DamageSource.mobAttack(reaper), damage);

            //apply wither effect
            entityToAttack.addEffect(new EffectInstance(Effects.WITHER, 200));

            reaper.setAttackState(EnumReaperAttackState.POST);
        }
    }
}
