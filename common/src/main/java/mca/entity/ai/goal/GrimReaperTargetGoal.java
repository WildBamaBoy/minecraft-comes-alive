package mca.entity.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;
import java.util.List;

public class GrimReaperTargetGoal extends Goal {
    private final TargetPredicate attackTargeting = new TargetPredicate().setBaseMaxDistance(64.0D);
    private final PathAwareEntity mob;
    private int nextScanTick = 20;

    public GrimReaperTargetGoal(PathAwareEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if (this.nextScanTick > 0) {
            this.nextScanTick--;
        } else {
            this.nextScanTick = 20;
            List<PlayerEntity> list = mob.world.getPlayers(this.attackTargeting, mob, mob.getBoundingBox().expand(48.0D, 64.0D, 48.0D));
            if (!list.isEmpty()) {
                list.sort(Comparator.comparing(Entity::getY).reversed());

                for (PlayerEntity playerentity : list) {
                    if (mob.isTarget(playerentity, TargetPredicate.DEFAULT)) {
                        mob.setTarget(playerentity);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return mob.getTarget() != null;
    }
}
