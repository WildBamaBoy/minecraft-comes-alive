package mca.core.minecraft;

import mca.core.MCA;
import mca.core.forge.Registration;
import mca.entity.GrimReaperEntity;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;

public class EntitiesMCA {
    public static final EntityType<VillagerEntityMCA> VILLAGER = EntityType.Builder.<VillagerEntityMCA>of((entityType, world) -> new VillagerEntityMCA(world), EntityClassification.AMBIENT).sized(0.6F, 1.8F).build(new ResourceLocation(MCA.MOD_ID, "villager").toString());
    public static final EntityType<GrimReaperEntity> GRIM_REAPER = EntityType.Builder.of(GrimReaperEntity::new, EntityClassification.MONSTER).sized(1.0F, 2.6F).build(new ResourceLocation(MCA.MOD_ID, "grim_reaper").toString());

    public static void init() {
        Registration.ENTITY_TYPES.register("villager", () -> VILLAGER);
        Registration.ENTITY_TYPES.register("grim_repear", () -> GRIM_REAPER);
    }


}
