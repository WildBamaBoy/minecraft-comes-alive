package mca.entity.ai;

import mca.entity.GrimReaperEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;
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

        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    public boolean canStart() {

        if (this.reaper.getRandom().nextInt(this.interval) != 0) {
            return false;
        }

        Vec3d vector3d = this.getPosition();
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
    protected Vec3d getPosition() {
        if (reaper.getTarget() != null) {
            return reaper.getTarget().getPos();
        } else {
            return RandomPositionGenerator.getAirPos(this.reaper, 8, 6, -2, null, 1.0F);
        }
    }

    public boolean shouldContinue() {
        return !this.reaper.getNavigation().isIdle();
    }

    public void start() {
        this.reaper.getNavigation().startMovingAlong(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    public void stop() {
        this.reaper.getNavigation().stop();
        super.stop();
    }
}