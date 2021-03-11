package cobalt.minecraft.entity.merchant.villager;

import cobalt.core.Cobalt;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.SoundEvent;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Constructor;

public class CVillagerProfession {
    @Getter private final VillagerProfession mcProfession;
    private CVillagerProfession(VillagerProfession profession) {
        this.mcProfession = profession;
    }

    public static CVillagerProfession fromMC(VillagerProfession profession) {
        return new CVillagerProfession(profession);
    }

    public static CVillagerProfession createNew(String name, PointOfInterestType poiType, SoundEvent sound) {
        // Creating a new villager profession is private to VillagerProfession. Don't know whose bright idea that
        // was, either Mojang or Forge. Either way, doesn't matter to us, we'll crack it open by reflection.
        try {
            Constructor<VillagerProfession> constructor = VillagerProfession.class.getDeclaredConstructor(String.class, PointOfInterestType.class, ImmutableSet.class, ImmutableSet.class, SoundEvent.class);
            constructor.setAccessible(true);
            return fromMC(constructor.newInstance(name, poiType, ImmutableSet.of(), ImmutableSet.of(), sound));
        } catch (Exception e) {
            Cobalt.getLog().fatal("Unable to create new profession!", e);
            return null;
        }
    }
}
