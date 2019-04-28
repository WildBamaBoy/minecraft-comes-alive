package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

import java.util.List;

public class EntityAIHunting extends AbstractEntityAIChore {
    private int ticks = 0;
    private int nextAction = 0;
    private EntityAnimal target = null;

    public EntityAIHunting(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(4);
    }

    public boolean shouldExecute() {
        return EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) == EnumChore.HUNT;
    }

    public void updateTask() {
        if (!villager.inventory.contains(ItemSword.class)) {
            villager.stopChore();
            //TODO tell player they need a sword
        }

        if (target == null) {
            ticks++;

            if (ticks >= nextAction) {
                ticks = 0;

                if (villager.world.rand.nextFloat() >= 0.0D) {
                    List<EntityAnimal> animals = villager.world.getEntitiesWithinAABB(EntityAnimal.class, villager.getEntityBoundingBox().grow(30.0D, 3.0D, 30.0D));
                    double closest = Double.MAX_VALUE;
                    EntityAnimal closestAnimal = null;

                    for (EntityAnimal animal : animals) {
                        if (animal instanceof EntityTameable) {
                            continue;
                        }

                        if (villager.getDistance(animal) < closest) {
                            closestAnimal = animal;
                            closest = villager.getDistance(animal);
                        }
                    }

                    if (closestAnimal != null && villager.getDistance(closestAnimal) < 15.0D) {
                        closestAnimal.getNavigator().setPath(closestAnimal.getNavigator().getPathToEntityLiving(villager), 1.0F);
                        target = closestAnimal;
                    }
                }

                nextAction = 1200;
            }
        } else {
            boolean pathSuccess = villager.getNavigator().setPath(villager.getNavigator().getPathToEntityLiving(target), 0.4F);

            if (!pathSuccess || target.isDead) {
                target = null;
            } else if (villager.getDistance(target) <= 3.5F) {
                villager.getNavigator().setPath(villager.getNavigator().getPathToEntityLiving(target), 1.0F);
                villager.swingArm(EnumHand.MAIN_HAND);
                target.attackEntityFrom(DamageSource.causeMobDamage(villager), 5.0F); //TODO damage from sword type
                //TODO collect items
            }
        }
    }
}