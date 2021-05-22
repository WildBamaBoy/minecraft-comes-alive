package mca.core.minecraft;

import mca.core.MCA;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ActivityMCA {
    public static final Activity CHORE = new Activity("chore");


    public static void init() {
        CHORE.setRegistryName(new ResourceLocation(MCA.MOD_ID, "chore"));
        ForgeRegistries.ACTIVITIES.register(CHORE);
    }
}
