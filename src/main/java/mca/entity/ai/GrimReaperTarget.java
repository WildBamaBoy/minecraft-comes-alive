package mca.entity.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;
import java.util.List;

public class GrimReaperTarget extends Goal {
    private final EntityPredicate attackTargeting = (new EntityPredicate()).range(64.0D);
    private int nextScanTick = 20;

    private final CreatureEntity mob;

    public GrimReaperTarget(CreatureEntity mob) {
        this.mob = mob;
    }

    public boolean canUse() {
        if (this.nextScanTick > 0) {
            --this.nextScanTick;
        } else {
            this.nextScanTick = 60;
            List<PlayerEntity> list = mob.level.getNearbyPlayers(this.attackTargeting, mob, mob.getBoundingBox().inflate(48.0D, 64.0D, 48.0D));
            if (!list.isEmpty()) {
                list.sort(Comparator.<Entity, Double>comparing(Entity::getY).reversed());

                for (PlayerEntity playerentity : list) {
                    if (mob.canAttack(playerentity, EntityPredicate.DEFAULT)) {
                        mob.setTarget(playerentity);
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public boolean canContinueToUse() {
        LivingEntity livingentity = mob.getTarget();
        return livingentity != null;
    }
}