package mca.entity.ai;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.ScheduleBuilder;

public interface SchedulesMCA {
    //DEFAULT (500 ticks longer awaken)
    Schedule DEFAULT = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.IDLE)
            .withActivity(2000, Activity.WORK)
            .withActivity(9000, Activity.MEET)
            .withActivity(11000, Activity.IDLE)
            .withActivity(12500, Activity.REST)
            .build();

    //DAY GUARD
    Schedule GUARD = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.WORK)
            .withActivity(9000, Activity.MEET)
            .withActivity(11000, Activity.WORK)
            .withActivity(14000, Activity.REST)
            .build();

    //NIGHT GUARD
    Schedule GUARD_NIGHT = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.REST)
            .withActivity(8000, Activity.IDLE)
            .withActivity(9000, Activity.MEET)
            .withActivity(14000, Activity.WORK)
            .build();

    //IDLE Schedule for family members
    Schedule FAMILY_IDLE = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.IDLE)
            .withActivity(12500, Activity.REST)
            .build();

    static void bootstrap() {
    }
}
