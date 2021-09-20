package mca.mixin;

import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ZombieVillagerEntity.class)
abstract class MixinZombieVillagerEntity {
    @ModifyVariable(method = "setVillagerData", at = @At("HEAD"), ordinal = 0)
    private VillagerData setVillagerData(VillagerData villagerData) {
        VillagerProfession profession = villagerData.getProfession();
        if (profession.toString().startsWith("mca.")) {
            villagerData = villagerData.withProfession(VillagerProfession.NONE);
        }
        return villagerData;
    }
}
