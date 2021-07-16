package mca.entity.ai.goal;

import mca.entity.GrimReaperEntity;
import net.minecraft.entity.ai.NoWaterTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

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

    @Override
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
            return NoWaterTargeting.find(this.reaper, 8, 6, -2, Vec3d.ofBottomCenter(reaper.getBlockPos()), 1.0F);
        }
    }

    @Override
    public boolean shouldContinue() {
        return !this.reaper.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.reaper.getNavigation().startMovingTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    @Override
    public void stop() {
        this.reaper.getNavigation().stop();
        super.stop();
    }
}