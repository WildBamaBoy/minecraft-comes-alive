package mca.entity.ai;

import mca.entity.GrimReaperEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class GrimReaperIdleGoal extends Goal {
    protected final GrimReaperEntity reaper;
    protected final double speedModifier;
    protected final int interval;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public GrimReaperIdleGoal(GrimReaperEntity reaper, double speed) {
        this(reaper, speed, 120);
    }

    public GrimReaperIdleGoal(GrimReaperEntity reaper, double speed, int interval) {
        this.reaper = reaper;
        this.speedModifier = speed;
        this.interval = interval;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public boolean canUse() {

        if (this.reaper.getRandom().nextInt(this.interval) != 0) {
            return false;
        }

        Vector3d vector3d = this.getPosition();
        if (vector3d == null) {
            return false;
        } else {
            this.wantedX = vector3d.x;
            this.wantedY = vector3d.y;
            this.wantedZ = vector3d.z;
            return true;
        }
    }

    @Nullable
    protected Vector3d getPosition() {
        if (reaper.getTarget() != null) {
            return reaper.getTarget().position();
        } else {
            return RandomPositionGenerator.getAirPos(this.reaper, 8, 6, -2, null, 1.0F);
        }
    }

    public boolean canContinueToUse() {
        return !this.reaper.getNavigation().isDone();
    }

    public void start() {
        this.reaper.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    public void stop() {
        this.reaper.getNavigation().stop();
        super.stop();
    }
}