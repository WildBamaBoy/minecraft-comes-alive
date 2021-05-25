package mca.core.minecraft;

import mca.core.forge.Registration;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.schedule.ScheduleBuilder;

public class SchedulesMCA {
    //This flips WORK and IDLE from the default schedule. this allows some of them to work after meeting. Given randomly to normal villagers
    public static final Schedule VILLAGER_DEFAULT_FLIPPED = new ScheduleBuilder(new Schedule())
            .changeActivityAt(10, Activity.IDLE)
            .changeActivityAt(9000, Activity.MEET)
            .changeActivityAt(11000, Activity.WORK)
            .changeActivityAt(12000, Activity.REST)
            .build();
    //DAY GUARD
    public static final Schedule GUARD_SCHEDULE = new ScheduleBuilder(new Schedule())
            .changeActivityAt(10, Activity.WORK)
            .changeActivityAt(9000, Activity.MEET)
            .changeActivityAt(11000, Activity.WORK)
            .changeActivityAt(12000, Activity.REST)
            .build();
    //NIGHT GUARD
    public static final Schedule NIGHT_GUARD_SCHEDULE = new ScheduleBuilder(new Schedule())
            .changeActivityAt(10, Activity.IDLE)
            .changeActivityAt(9000, Activity.MEET)
            .changeActivityAt(11000, Activity.WORK)
            .build();
    //IDLE Schedule for family members
    public static final Schedule FAMILY_IDLE = new ScheduleBuilder(new Schedule())
            .changeActivityAt(10, Activity.IDLE)
            .changeActivityAt(12000, Activity.REST)
            .build();

    public static void init() {
        Registration.SCHEDULES.register("villager_default_flipped", () -> VILLAGER_DEFAULT_FLIPPED);
    }
}
