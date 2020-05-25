package mca.entity.ai;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import mca.api.objects.Player;
import mca.api.objects.Pos;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;

@RequiredArgsConstructor
public class EntityAIProcreate extends EntityAIBase {
    private final EntityVillagerMCA villager;
    public int procreateTimer;

    @Override
    public boolean shouldExecute() {
        return villager.get(EntityVillagerMCA.IS_PROCREATING);
    }

    @Override
    public void updateTask() {
        if (procreateTimer % 5 == 0) villager.spawnParticles(EnumParticleTypes.HEART);

        if (--procreateTimer <= 0) {
            villager.set(EntityVillagerMCA.IS_PROCREATING, false);

            Optional<Player> spousePlayer = villager.world.getPlayerEntityByUUID(villager.get(EntityVillagerMCA.SPOUSE_UUID).or(Constants.ZERO_UUID));
            spousePlayer.ifPresent(p -> {
            	villager.world.playSound(p, new Pos(villager.posX, villager.posY, villager.posZ), SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                p.inventory.addItemStackToInventory(new ItemStack(villager.getRNG().nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL));
                PlayerSaveData.get(p).setBabyPresent(true);

                if (villager.getRNG().nextFloat() < MCA.getConfig().chanceToHaveTwins / 100) {
                	p.inventory.addItemStackToInventory(new ItemStack(villager.getRNG().nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL));
                }
            });
        }
    }
}
