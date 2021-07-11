package mca.core.minecraft;

import mca.core.MCA;
import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import java.util.Random;

public interface ProfessionsMCA {
    VillagerProfession CHILD = register("child", VillagerProfessionBuilder.create().workstation(PointOfInterestType.HOME).workSound(SoundEvents.ENTITY_VILLAGER_WORK_FARMER));
    VillagerProfession GUARD = register("guard", VillagerProfessionBuilder.create().workstation(PointOfInterestType.ARMORER).workSound(SoundEvents.ENTITY_VILLAGER_WORK_ARMORER));

    static void bootstrap() {
        PointOfInterestTypeMCA.bootstrap();
    }

    private static VillagerProfession register(String name, VillagerProfessionBuilder builder) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return Registry.register(Registry.VILLAGER_PROFESSION, id, VillagerProfessionBuilder.create().id(id).build());
    }

    static VillagerProfession randomProfession() {
        // TODO: use the world's random to avoid client/server desync.
        Random r = new Random();
        return Registry.VILLAGER_PROFESSION.getRandom(r);
    }
}