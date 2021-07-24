package mca.entity.ai;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.ScheduleBuilder;

public interface SchedulesMCA {
    //DAY GUARD
    Schedule GUARD_SCHEDULE = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.WORK)
            .withActivity(9000, Activity.MEET)
            .withActivity(11000, Activity.WORK)
            .withActivity(12000, Activity.REST)
            .build();
    //NIGHT GUARD
    Schedule NIGHT_GUARD_SCHEDULE = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.IDLE)
            .withActivity(9000, Activity.MEET)
            .withActivity(11000, Activity.WORK)
            .build();
    //IDLE Schedule for family members
    Schedule FAMILY_IDLE = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.IDLE)
            .withActivity(12000, Activity.REST)
            .build();

    static void bootstrap() { }
}
