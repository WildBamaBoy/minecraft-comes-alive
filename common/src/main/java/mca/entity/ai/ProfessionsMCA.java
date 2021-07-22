package mca.entity.ai;

import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import java.util.Random;

import mca.MCA;
import mca.cobalt.registration.Registration;

public interface ProfessionsMCA {
    VillagerProfession OUTLAW = register("outlaw", Registration.ObjectBuilders.Profession.create(PointOfInterestType.HOME, SoundEvents.ENTITY_VILLAGER_WORK_FARMER));
    VillagerProfession CHILD = register("child", Registration.ObjectBuilders.Profession.create(PointOfInterestType.HOME, SoundEvents.ENTITY_VILLAGER_WORK_FARMER));
    VillagerProfession GUARD = register("guard", Registration.ObjectBuilders.Profession.create(PointOfInterestType.ARMORER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER));

    VillagerProfession JEWELER = register("jeweler", Registration.ObjectBuilders.Profession.create(PointOfInterestTypeMCA.JEWELER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER));

    static void bootstrap() {
        PointOfInterestTypeMCA.bootstrap();
    }

    static VillagerProfession register(String name, VillagerProfession obj) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return Registration.register(Registry.VILLAGER_PROFESSION, id, obj);
    }

    static VillagerProfession randomProfession() {
        // TODO: use the world's random to avoid client/server desync.
        Random r = new Random();
        return Registry.VILLAGER_PROFESSION.getRandom(r);
    }
}