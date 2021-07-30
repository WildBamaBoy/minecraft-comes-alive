package mca.entity;

import mca.MCA;
import mca.cobalt.registration.Registration;
import mca.entity.ai.ActivityMCA;
import mca.entity.ai.MemoryModuleTypeMCA;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.SchedulesMCA;
import mca.entity.ai.relationship.Gender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface EntitiesMCA {
    EntityType<VillagerEntityMCA> MALE_VILLAGER = register("male_villager", EntityType.Builder
            .<VillagerEntityMCA>create((t, w) -> new VillagerEntityMCA(t, w, Gender.MALE), SpawnGroup.AMBIENT)
            .setDimensions(0.6F, 2.0F)
    );
    EntityType<VillagerEntityMCA> FEMALE_VILLAGER = register("female_villager", EntityType.Builder
            .<VillagerEntityMCA>create((t, w) -> new VillagerEntityMCA(t, w, Gender.FEMALE), SpawnGroup.AMBIENT)
            .setDimensions(0.6F, 2.0F)
    );
    EntityType<ZombieVillagerEntityMCA> MALE_ZOMBIE_VILLAGER = register("male_zombie_villager", EntityType.Builder
            .<ZombieVillagerEntityMCA>create((t, w) -> new ZombieVillagerEntityMCA(t, w, Gender.MALE), SpawnGroup.MONSTER)
            .setDimensions(0.6F, 2.0F)
    );
    EntityType<ZombieVillagerEntityMCA> FEMALE_ZOMBIE_VILLAGER = register("female_zombie_villager", EntityType.Builder
            .<ZombieVillagerEntityMCA>create((t, w) -> new ZombieVillagerEntityMCA(t, w, Gender.FEMALE), SpawnGroup.MONSTER)
            .setDimensions(0.6F, 2.0F)
    );
    EntityType<GrimReaperEntity> GRIM_REAPER = register("grim_reaper", EntityType.Builder
            .<GrimReaperEntity>create(GrimReaperEntity::new, SpawnGroup.MONSTER)
            .setDimensions(1, 2.6F)
            .makeFireImmune()
    );

    static void bootstrap() {
        MemoryModuleTypeMCA.bootstrap();
        ActivityMCA.bootstrap();
        SchedulesMCA.bootstrap();
        ProfessionsMCA.bootstrap();
        bootstrapAttributes();
    }

    static void bootstrapAttributes() {
        Registration.ObjectBuilders.DefaultEntityAttributes.add(MALE_VILLAGER, VillagerEntityMCA::createVillagerAttributes);
        Registration.ObjectBuilders.DefaultEntityAttributes.add(FEMALE_VILLAGER, VillagerEntityMCA::createVillagerAttributes);
        Registration.ObjectBuilders.DefaultEntityAttributes.add(MALE_ZOMBIE_VILLAGER, ZombieVillagerEntityMCA::createZombieAttributes);
        Registration.ObjectBuilders.DefaultEntityAttributes.add(FEMALE_ZOMBIE_VILLAGER, ZombieVillagerEntityMCA::createZombieAttributes);
        Registration.ObjectBuilders.DefaultEntityAttributes.add(GRIM_REAPER, GrimReaperEntity::createAttributes);
    }

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return Registration.register(Registry.ENTITY_TYPE, id, builder.build(id.toString()));
    }
}
