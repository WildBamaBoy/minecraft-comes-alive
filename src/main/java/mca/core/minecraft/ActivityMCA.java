package mca.core.minecraft;

import mca.core.forge.Registration;
import net.minecraft.entity.ai.brain.schedule.Activity;

public class ActivityMCA {
    public static final Activity CHORE = new Activity("chore");


    public static void init() {
        Registration.ACTIVITIES.register("chore", () -> CHORE);
    }
}
