package mca.core.minecraft;

import com.google.common.collect.ImmutableSet;
import mca.core.forge.Registration;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ProfessionsMCA {
    public static final VillagerProfession CHILD = new VillagerProfession("child", PointOfInterestType.HOME, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_FARMER);
    public static final VillagerProfession GUARD = new VillagerProfession("guard", PointOfInterestType.ARMORER, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_ARMORER);
    // as set of invalid professions
    private static final List<VillagerProfession> PROFESSIONS = new ArrayList<>(12);

    public static void register() {
        Registration.PROFESSIONS.register("child", () -> CHILD);
        Registration.PROFESSIONS.register("guard", () -> GUARD);

        //TODO get other modded professions
        PROFESSIONS.addAll(Arrays.asList(
                VillagerProfession.ARMORER,
                VillagerProfession.BUTCHER,
                VillagerProfession.CARTOGRAPHER,
                VillagerProfession.CLERIC,
                VillagerProfession.FARMER,
                VillagerProfession.FISHERMAN,
                VillagerProfession.FLETCHER,
                VillagerProfession.LEATHERWORKER,
                VillagerProfession.LIBRARIAN,
                VillagerProfession.MASON,
                VillagerProfession.NITWIT,
                GUARD
        ));
    }

    public static VillagerProfession randomProfession() {
        Random r = new Random();
        return PROFESSIONS.get(r.nextInt(PROFESSIONS.size()));
    }


}