package mca.entity;

import mca.MCA;
import mca.entity.ai.ActivityMCA;
import mca.entity.ai.MemoryModuleTypeMCA;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.SchedulesMCA;
import mca.entity.ai.relationship.Gender;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface EntitiesMCA {
    EntityType<VillagerEntityMCA> MALE_VILLAGER = register("villager", FabricEntityTypeBuilder
            .<VillagerEntityMCA>create(SpawnGroup.AMBIENT, (t, w) -> new VillagerEntityMCA(t, w, Gender.MALE))
            .dimensions(EntityDimensions.fixed(0.6F, 2.0F))
            .build()
    );
    EntityType<VillagerEntityMCA> FEMALE_VILLAGER = register("female_villager", FabricEntityTypeBuilder
            .<VillagerEntityMCA>create(SpawnGroup.AMBIENT, (t, w) -> new VillagerEntityMCA(t, w, Gender.FEMALE))
            .dimensions(EntityDimensions.fixed(0.6F, 2.0F))
            .build()
    );
    EntityType<GrimReaperEntity> GRIM_REAPER = register("grim_reaper", FabricEntityTypeBuilder
            .<GrimReaperEntity>create(SpawnGroup.MONSTER, GrimReaperEntity::new)
            .dimensions(EntityDimensions.fixed(1, 2.6F))
            .fireImmune()
            .build()
    );

    static void bootstrap() {
        MemoryModuleTypeMCA.bootstrap();
        ActivityMCA.bootstrap();
        SchedulesMCA.bootstrap();
        ProfessionsMCA.bootstrap();
        FabricDefaultAttributeRegistry.register(MALE_VILLAGER, VillagerEntityMCA.createVillagerAttributes());
        FabricDefaultAttributeRegistry.register(FEMALE_VILLAGER, VillagerEntityMCA.createVillagerAttributes());
        FabricDefaultAttributeRegistry.register(GRIM_REAPER, GrimReaperEntity.createAttributes());
    }

    static <T extends Entity> EntityType<T> register(String name, EntityType<T> builder) {
        return Registry.register(Registry.ENTITY_TYPE, new Identifier(MCA.MOD_ID, name), builder);
    }
}
