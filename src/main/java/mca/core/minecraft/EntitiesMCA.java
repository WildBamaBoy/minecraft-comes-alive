package mca.core.minecraft;

import mca.core.MCA;
import mca.core.forge.Registration;
import mca.entity.GrimReaperEntity;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;

public class EntitiesMCA {
    public static final EntityType<VillagerEntityMCA> VILLAGER = EntityType.Builder.<VillagerEntityMCA>create((entityType, world) -> new VillagerEntityMCA(world), SpawnGroup.AMBIENT).setDimensions(0.6F, 2.0F).build(new Identifier(MCA.MOD_ID, "villager").toString());
    public static final EntityType<GrimReaperEntity> GRIM_REAPER = EntityType.Builder.create(GrimReaperEntity::new, SpawnGroup.MONSTER).setDimensions(1.0F, 2.6F).build(new Identifier(MCA.MOD_ID, "grim_reaper").toString());

    public static void init() {
        Registration.ENTITY_TYPES.register("villager", () -> VILLAGER);
        Registration.ENTITY_TYPES.register("grim_reaper", () -> GRIM_REAPER);
    }


}
