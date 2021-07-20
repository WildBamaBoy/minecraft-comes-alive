package mca.entity.ai;

import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import java.util.Random;

import mca.MCA;

public interface ProfessionsMCA {
    VillagerProfession OUTLAW = register("outlaw", VillagerProfessionBuilder.create().workstation(PointOfInterestType.HOME).workSound(SoundEvents.ENTITY_VILLAGER_WORK_FARMER));
    VillagerProfession CHILD = register("child", VillagerProfessionBuilder.create().workstation(PointOfInterestType.HOME).workSound(SoundEvents.ENTITY_VILLAGER_WORK_FARMER));
    VillagerProfession GUARD = register("guard", VillagerProfessionBuilder.create().workstation(PointOfInterestType.ARMORER).workSound(SoundEvents.ENTITY_VILLAGER_WORK_ARMORER));

    VillagerProfession JEWELER = register("jeweler", VillagerProfessionBuilder.create().workstation(PointOfInterestTypeMCA.JEWELER).workSound(SoundEvents.ENTITY_VILLAGER_WORK_ARMORER));

    static void bootstrap() {
        PointOfInterestTypeMCA.bootstrap();
    }

    static VillagerProfession register(String name, VillagerProfessionBuilder builder) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return Registry.register(Registry.VILLAGER_PROFESSION, id, builder.id(id).build());
    }

    static VillagerProfession randomProfession() {
        // TODO: use the world's random to avoid client/server desync.
        Random r = new Random();
        return Registry.VILLAGER_PROFESSION.getRandom(r);
    }
}