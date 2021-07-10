package mca.core.minecraft;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.ScheduleBuilder;

public class SchedulesMCA {
    //DAY GUARD
    public static final Schedule GUARD_SCHEDULE = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.WORK)
            .withActivity(9000, Activity.MEET)
            .withActivity(11000, Activity.WORK)
            .withActivity(12000, Activity.REST)
            .build();
    //NIGHT GUARD
    public static final Schedule NIGHT_GUARD_SCHEDULE = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.IDLE)
            .withActivity(9000, Activity.MEET)
            .withActivity(11000, Activity.WORK)
            .build();
    //IDLE Schedule for family members
    public static final Schedule FAMILY_IDLE = new ScheduleBuilder(new Schedule())
            .withActivity(10, Activity.IDLE)
            .withActivity(12000, Activity.REST)
            .build();

    public static void init() {
    }
}
