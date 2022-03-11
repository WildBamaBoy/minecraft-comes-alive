package mca.entity.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import mca.MCA;
import mca.cobalt.registration.Registration;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;

public interface ProfessionsMCA {
    VillagerProfession OUTLAW = register("outlaw", PointOfInterestType.UNEMPLOYED, SoundEvents.ENTITY_VILLAGER_WORK_FARMER);
    VillagerProfession GUARD = register("guard", PointOfInterestType.UNEMPLOYED, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);
    VillagerProfession ARCHER = register("archer", PointOfInterestType.UNEMPLOYED, SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER);
    // VillagerProfession JEWELER = register("jeweler", PointOfInterestTypeMCA.JEWELER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);

    Set<VillagerProfession> canNotTrade = new HashSet<>();

    static void bootstrap() {
        PointOfInterestTypeMCA.bootstrap();

        canNotTrade.add(VillagerProfession.NONE);
        canNotTrade.add(VillagerProfession.NITWIT);
        canNotTrade.add(OUTLAW);
        canNotTrade.add(GUARD);
    }

    static VillagerProfession register(String name, PointOfInterestType workStation, @Nullable SoundEvent workSound) {
        return Registration.ObjectBuilders.Profession.creator().apply(new Identifier(MCA.MOD_ID, name), workStation, workSound,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    static String getFavoredBuilding(VillagerProfession profession) {
        if (VillagerProfession.CARTOGRAPHER == profession || VillagerProfession.LIBRARIAN == profession || VillagerProfession.CLERIC == profession) {
            return "library";
        } else if (GUARD == profession || ARCHER == profession) {
            return "inn";
        }
        return null;
    }
}
