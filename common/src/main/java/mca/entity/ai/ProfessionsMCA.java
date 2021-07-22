package mca.entity.ai;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.ArrayList;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import mca.MCA;
import mca.cobalt.registration.Registration;

public interface ProfessionsMCA {
    VillagerProfession OUTLAW = register("outlaw", PointOfInterestType.HOME, SoundEvents.ENTITY_VILLAGER_WORK_FARMER);
    VillagerProfession CHILD = register("child", PointOfInterestType.HOME, SoundEvents.ENTITY_VILLAGER_WORK_FARMER);
    VillagerProfession GUARD = register("guard", PointOfInterestType.ARMORER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);
    VillagerProfession JEWELER = register("jeweler", PointOfInterestTypeMCA.JEWELER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);

    static void bootstrap() {
        PointOfInterestTypeMCA.bootstrap();
    }

    static VillagerProfession register(String name, PointOfInterestType workStation, @Nullable SoundEvent workSound) {
        return Registration.ObjectBuilders.Profession.creator().apply(new Identifier(MCA.MOD_ID, name), workStation, workSound,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    static VillagerProfession randomProfession() {
        // TODO: use the world's random to avoid client/server desync.
        Random r = new Random();
        return Registry.VILLAGER_PROFESSION.getRandom(r);
    }
}