package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.CEnumHand;

import java.util.Comparator;
import java.util.Optional;

public class EntityAIHunting extends AbstractEntityAIChore {
    private int ticks = 0;
    private int nextAction = 0;
    private EntityAnimal target = null;

    public EntityAIHunting(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getHealth() < villager.getMaxHealth()) {
            villager.stopChore();
        }
        return EnumChore.byId(villager.get(EntityVillagerMCA.activeChore)) == EnumChore.HUNT;
    }

    public void updateTask() {
        super.updateTask();

        if (!villager.inventory.contains(ItemSword.class)) {
            villager.say(getAssigningPlayer(), "chore.hunting.nosword");
            villager.stopChore();
        }

        if (target == null) {
            ticks++;

            if (ticks >= nextAction) {
                ticks = 0;
                if (villager.world.rand.nextFloat() >= 0.0D) {
                    Optional<EntityAnimal> animal = villager.world.getEntitiesWithinAABB(EntityAnimal.class, villager.getEntityBoundingBox().grow(15.0D, 3.0D, 15.0D)).stream()
                            .filter((a) -> !(a instanceof EntityTameable))
                            .min(Comparator.comparingDouble(villager::getDistance));

                    if (animal.isPresent()) {
                        target = animal.get();
                        target.getNavigation().setPath(target.getNavigation().getPathToLivingEntity(villager), 1.0F);
                    }
                }

                nextAction = 300;
            }
        } else {
            boolean pathSuccess = villager.getNavigation().setPath(villager.getNavigation().getPathToLivingEntity(target), 0.6F);

            if (!pathSuccess || target.isDead) {
                // search for EntityItems around the target and grab them
                villager.world.loadedEntityList.stream()
                        .filter((e) -> e instanceof EntityItem && e.getDistance(target) <= 5.0D)
                        .forEach((item) -> {
                            villager.inventory.addItem(((EntityItem) item).getItem());
                            item.setDead();
                        });
                target = null;
            } else if (villager.getDistance(target) <= 3.5F) {
                villager.getNavigation().setPath(villager.getNavigation().getPathToLivingEntity(target), 1.0F);
                villager.swingArm(CEnumHand.MAIN_HAND);
                target.attackEntityFrom(DamageSource.causeMobDamage(villager), 6.0F);
                villager.getHeldItem(CEnumHand.MAIN_HAND).damageItem(2, villager);
            }
        }
    }
}